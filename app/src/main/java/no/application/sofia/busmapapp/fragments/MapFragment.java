package no.application.sofia.busmapapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.SearchView;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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
    private ArrayList<String> characters;
    public CustomKeyboard mKeyboard;
    private InputMethodManager imm;



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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_map, menu);
        mKeyboard = new CustomKeyboard(getActivity(), R.id.keyboardview, R.xml.line_search_keyboard);
        final MenuItem searchMenuItem = menu.findItem(R.id.action_line_search);
        //Setting the input type on the searchview
        try {
            final SearchView searchView = (SearchView) searchMenuItem.getActionView();
            Class searchClass = searchView.getClass();
            searchView.setInputType(InputType.TYPE_NULL);
            mKeyboard.registerSearchView(searchView, searchMenuItem);


        }catch(Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_line_search){
            Log.d("Item Selected", item.toString());
        }
        return super.onOptionsItemSelected(item);
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
        try{
        Line line = db.getLineByName(name);
        String lineId = line.getLineId()+"";
        searchForRoute(lineId);
        }catch (Exception e){
            Toast.makeText(getActivity(), "Line " + name + " does not exist", Toast.LENGTH_LONG).show();
        }
    }

    private void searchForRoute(String route){
        busMap.clear();

        final int lineID = Integer.parseInt(route);

        new Thread(new Runnable() {
            public void run() {
                addRouteLineToMap("Ruter", lineID);
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                addRouteMarkersToMap("Ruter", lineID);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                addStopMarkersToMap("Ruter", lineID);
            }
        }).start();
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
    private void addRouteLineToMap(String operator, int lineID){
        JSONArray busStops = getBusStopsOnLine(operator, lineID);
        ArrayList<LatLng> stopPositions = new ArrayList<LatLng>();
        for(int i = 0; i < busStops.length(); i++){
            try {
                JSONObject positionJSON = busStops.getJSONObject(i).getJSONObject("Position");
                stopPositions.add(new LatLng(positionJSON.getDouble("Latitude"),
                        positionJSON.getDouble("Longitude")));
            }catch(Exception e){
                e.printStackTrace();
            }
        }



        // Instantiates a new Polyline object and adds points to define a rectangle
        final PolylineOptions rectOptions = new PolylineOptions()
                .addAll(stopPositions);

        // Forces main thread to add polyline to the map, as this can not be done in a separate thread
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                busMap.addPolyline(rectOptions);
            }
        });

    }

    private void addRouteMarkersToMap(String operator, int lineID){
        JSONArray buses = getBusPositionsOnLine(operator, lineID);
        for(int i = 0; i < buses.length(); i++){
            try {
                final JSONObject json = buses.getJSONObject(i);
                final JSONObject positionJSON = json.getJSONObject("Position");
                final LatLng pos = new LatLng(positionJSON.getDouble("Latitude"), positionJSON.getDouble("Longitude"));

                // Forces main thread to add markers to the map, as this can not be done in a separate thread
                final Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
	                        int transportation = json.getInt("Transportation");
	                        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker_bus);
	                        String transName = "Bus";

                            if (transportation == 0){ // Walking

		                    }else if (transportation == 1){
	                            transName = "Airport Bus";
	                            // icon =
		                    }else if (transportation == 2){
	                            transName = "Bus";
	                            // icon =
		                    }else if (transportation == 3){
	                            transName = "Dummy";
	                            // icon =
		                    }else if (transportation == 4){
	                            transName = "Airport Train";
	                            // icon =
                            }else if (transportation == 5){
	                            transName = "Boat";
	                            // icon =
                            }else if (transportation == 6){
	                            transName = "Train";
	                            // icon =
                            }else if (transportation == 7){
	                            transName = "Tram";
	                            icon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker_tram);
                            }else if(transportation == 8){
	                            transName = "Metro";
	                            icon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker_sub);
                            }

	                        busMap.addMarker(new MarkerOptions()
		                        .title(transName + " " + json.getString("LineID") + " towards " + json.getString("DestinationName"))
		                        .position(pos)
		                        .icon(icon)
		                        .snippet("Arrives at " + json.getString("NextBusStopName") + " at " + json.getString("NextBusStopArrival").substring(11,19)));
	                        busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });

            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void addStopMarkersToMap(String operator, int lineID){
        JSONArray stops = getBusStopsOnLine(operator, lineID);
        for (int i = 0; i < stops.length(); i++){
            try{
                final JSONObject json = stops.getJSONObject(i);
                final JSONObject positionJSON = json.getJSONObject("Position");
                final LatLng pos = new LatLng(positionJSON.getDouble("Latitude"), positionJSON.getDouble("Longitude"));

                final Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            busMap.addMarker(new MarkerOptions()
                                    .title("Name: " + json.getString("Name"))
                                    .position(pos)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_stop))
                                    .snippet("STOOOOP"));
                            busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private JSONArray getBusStopsOnLine(String operator, int lineID){
        String URL = "http://api.bausk.no/Stops/getBusStopsOnLine/" + operator + "/" + lineID;

        String busStopJSONs = sendJSONRequest(URL);
        JSONArray json = new JSONArray();
        try {
            json = new JSONArray(busStopJSONs);
            Log.i(MapFragment.class.getName(), json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;

    }

    private JSONArray getBusPositionsOnLine(String operator, int lineID){
        String URL = "http://api.bausk.no/Bus/getBusPositionsOnLine/" + operator + "/" + lineID;

        String busJSONs = sendJSONRequest(URL);
        JSONArray json = new JSONArray();
        try {
            json = new JSONArray(busJSONs);
            Log.i(MapFragment.class.getName(), json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    // Downloads JSON from a given URL
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

    private JSONArray getBusLinesByOperator(String operator){
        String url = "http://api.bausk.no/Bus/getBusLinesByOperator/" + operator;
        String busInfoJsons = sendJSONRequest(url);
        JSONArray json = new JSONArray();
        try {
            json = new JSONArray(busInfoJsons);
        }catch (Exception e){
            e.printStackTrace();
        }
        return json;
    }

    //Called when the button to add all lines in the action menu is clicked
    private void addAllLinesToDb(){
        JSONArray busLines = getBusLinesByOperator("Ruter");
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
