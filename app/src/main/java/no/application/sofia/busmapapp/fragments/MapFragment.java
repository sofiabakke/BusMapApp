package no.application.sofia.busmapapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.activities.MainActivity;


public class MapFragment extends Fragment {
    private static GoogleMap busMap;
    private LatLng myLocation;
    private static final String ARG_SECTION_NUMBER = "section_number";
    private boolean fromNavDrawer = false; //To check where the map was selected


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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //When the some other fragment in the navigation drawer is selected, the busMap is set to
        // null again to be able to setup the map when the fragment is reattached.
        busMap = null;
        Log.d("onDetach", "In MapFragment");
    }

    public void setFromNavDrawer(boolean fromNavDrawer){
        this.fromNavDrawer = fromNavDrawer;
    }

    //This needs to be called before replacing a stops fragment to this one in order to zoom to the stops location
    public void setMyLocation(double lat, double lng){
        myLocation = new LatLng(lat, lng);
    }
}
