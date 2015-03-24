package no.application.sofia.busmapapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Collections;

import no.application.sofia.busmapapp.CustomKeyboard;
import no.application.sofia.busmapapp.MarkerInfoAdapter;
import no.application.sofia.busmapapp.BusLineMarkerHandler;
import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.activities.MainActivity;
import no.application.sofia.busmapapp.databasehelpers.Line;
import no.application.sofia.busmapapp.databasehelpers.LineDbHelper;


public class MapFragment extends Fragment {
    private static GoogleMap busMap;
    private LatLng myLocation;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private boolean fromNavDrawer = false; //To check where the map was selected
    private LineDbHelper db;
    private ArrayList<String> characters; //Used to find which letters are used in line names
    public CustomKeyboard mKeyboard; //The custom keyboard for doing search
    private Bundle savedInstanceState; //Need it when using a custom snippet for the map

	private BusLineMarkerHandler busLineHandler;


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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new LineDbHelper(getActivity());
        characters = new ArrayList<>();
        this.savedInstanceState = savedInstanceState;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mKeyboard = new CustomKeyboard(getActivity(), view, R.id.keyboardview, R.xml.line_search_keyboard);

        mKeyboard.registerEditText(R.id.edittext_search_lines);
        return view;
    }


    //Used to decide whether or not all lines should be added to the database
    //If the database already contains any number of records, no stops are added and a toast is shown to the user.
    public void decideIfAddLinesToLocalDb(){
        long databaseLength = db.dbLength();
        if (databaseLength <= 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    addAllLinesToDb();
                }
            }).start();
        }
    }

    /**
     * Makes all line names have uppercase letter
     * @param name
     * @return
     */
    private String myToUpperCase(String name){
        char[] chars = name.toCharArray();
        String newName = "";
        for (char c : chars){
            if (Character.isLetter(c)) {
                newName += (c + "").toUpperCase();
                if (!characters.contains(c+""))
                    characters.add(c + "");
            }
            else
                newName += c;
        }
        return newName;
    }

    public void searchRouteByName(String name){
        Line line = db.getLineByName(name);
        if (line.getName() == null)
            Toast.makeText(getActivity(), "Line " + name + " does not exist. Try another line.", Toast.LENGTH_LONG).show();
        else {
            String lineId = line.getLineId() + "";
            Toast.makeText(getActivity(), "Line " + line.getName() + " exists", Toast.LENGTH_LONG).show();
            searchForRoute(lineId);
        }
    }

    private void searchForRoute(String route){
	    busLineHandler.addRouteMarkers(route);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
		busLineHandler.restartUpdateThread();
        decideIfAddLinesToLocalDb();
    }

	@Override
	public void onPause(){
		super.onPause();
		busLineHandler.stopUpdateThread();
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER)); //Setting the Action Bar text
    }

    private void setUpMapIfNeeded(){
        // Do a null check to confirm that we have not already instantiated the map.
        if (busMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            busMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.busmap)).getMap();
            busMap.setInfoWindowAdapter(new MarkerInfoAdapter(getLayoutInflater(savedInstanceState)));
            // Check if we were successful in obtaining the map.
            if (busMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap(){
        busMap.setMyLocationEnabled(true);
	    busLineHandler = new BusLineMarkerHandler(busMap);


        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (fromNavDrawer) {
            if (lastKnownLocation != null) {
                //Try to use the last known location to set lat long
                myLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            } else {
                //If the app is not able to find the last known location, the map is centered to Oslo
                myLocation = new LatLng(59.9138688, 10.7522454);
            }
            fromNavDrawer = false;
            busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));
        }
        else{
            //Setting the camera to pin on the location of a stop clicked from the stops fragment
            busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));
            busMap.addMarker(new MarkerOptions().position(myLocation).title("Chosen Sop Location"));
        }
        busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));
        //addRouteMarkersToMap("Ruter", 21);

    }

    @Override
    public void onDetach() {
        super.onDetach();
	    busLineHandler.stopUpdateThread();
        //When the some other fragment in the navigation drawer is selected, the busMap is set to
        // null again to be able to setup the map when the fragment is reattached.
        busMap = null;
    }

    public void setFromNavDrawer(boolean fromNavDrawer){
        this.fromNavDrawer = fromNavDrawer;
    }

    //This needs to be called before replacing a stops fragment to this one in order to zoom to the stops location
    public void setMyLocation(double lat, double lng){
        myLocation = new LatLng(lat, lng);
    }

    //Called when the button to add all lines in the action menu is clicked
    private void addAllLinesToDb(){
        JSONArray busLines = busLineHandler.getBusLinesByOperator("Ruter");
        int counter = 1;
        for (int i = 0; i < busLines.length(); i++){
            try {
                JSONObject currentJson = busLines.getJSONObject(i);
                int lineID = currentJson.getInt("LineID");
                String name = myToUpperCase(currentJson.getString("Name"));
                int transportation = currentJson.getInt("Transportation");
                Log.d("Line " + counter, "LineID: " + lineID + ", Name: " + name + ", Transportation: " + transportation);
                counter++;
                Line line = new Line(lineID, name, transportation);
                db.addLine(line);
            }catch (Exception e){
                e.printStackTrace();
                break;
            }
        }
        Log.i("DataBase Length", db.dbLength() + "");
        Collections.sort(characters);
        Log.d("Characters discovered: ", characters.toString());
    }


}
