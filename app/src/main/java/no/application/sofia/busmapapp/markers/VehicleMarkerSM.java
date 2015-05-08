package no.application.sofia.busmapapp.markers;
import com.google.android.gms.maps.model.Marker
        ;

import org.json.JSONObject;

import java.util.ArrayList;

import no.application.sofia.busmapapp.markerhandlers.VehicleArrival;

/**
 * Created by oknak_000 on 08.05.2015.
 */
public class VehicleMarkerSM extends VehicleMarker {
    private int transportation = 0;
    private ArrayList<VehicleArrival> arrivals;

    public VehicleMarkerSM(Marker marker, JSONObject vehicleInfoJSON) {
        super(marker, vehicleInfoJSON);
        arrivals = new ArrayList<VehicleArrival>();
        try{
            vehicleRef = vehicleInfoJSON.getString("VehicleID");


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected String generateTitle(){
        return "";
    }

    protected String generateSnippet(){
        return "";
    }

    @Override
    public void update(JSONObject newJSON){

    }
}
