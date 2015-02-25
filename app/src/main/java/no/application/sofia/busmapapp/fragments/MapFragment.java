package no.application.sofia.busmapapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.activities.MainActivity;


public class MapFragment extends Fragment {
    private static GoogleMap busMap;
    private LatLng myLocation;
    private static final String ARG_SECTION_NUMBER = "section_number";
	private static EditText searchField;


    public static MapFragment newInstance(int sectionNumber) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }



    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

	   searchField = (EditText) view.findViewById(R.id.search_box);
       return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER)); //Setting the Action Bar text
//        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded(){
        // Do a null check to confirm that we have not already instantiated the map.
        if (busMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            busMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.busmap)).getMap();
            // Check if we were successful in obtaining the map.
            if (busMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap(){
        busMap.setMyLocationEnabled(true);
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(lastKnownLocation != null){
            //Try to use the last known location to set lat long
            myLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        }
        else{
            //If the app is not able to find the last known location, the map is centered to Oslo
            myLocation = new LatLng(59.9138688, 10.7522454);
        }
        busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));
	    addRouteMarkersToMap("Ruter", 21);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        //When the some other fragment in the navigation drawer is selected, the busMap is set to
        // null again to be able to setup the map when the fragment is reattached.
        busMap = null;
        Log.d("onDetach", "In MapFragment");
    }


	private void addRouteMarkersToMap(String operator, int lineID){
		JSONArray buses = getBusPositionsOnLine(operator, lineID);
		for(int i = 0; i < buses.length(); i++){
			try {
				JSONObject json = buses.getJSONObject(i);
				JSONObject positionJSON = json.getJSONObject("Position");
				LatLng pos = new LatLng(positionJSON.getDouble("Latitude"), positionJSON.getDouble("Longitude"));
				busMap.addMarker(new MarkerOptions()
					.title("Line: " + json.getString("LineID") + " VehicleID: " + json.getString("VehicleID"))
					.position(pos)
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
					.snippet("HELLO THERE!"));
			}catch(JSONException e){
				e.printStackTrace();
			}
		}
	}

	private JSONArray getBusPositionsOnLine(String operator, int lineID){
		StrictMode.ThreadPolicy policy = new StrictMode.
		ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		String URL = "http://api.bausk.no/Bus/getBusPositionsOnLine/" + operator + "/" + lineID;

		String input = sendJSONRequest(URL);
		JSONArray json = new JSONArray();
		try {
			json = new JSONArray(input);
			Log.i(MapFragment.class.getName(), json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

	private String sendJSONRequest(String URL){
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(URL);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(MapFragment.class.getName(), "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

}
