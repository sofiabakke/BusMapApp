package no.application.sofia.busmapapp.markers;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONObject;

/**
 * Created by oknak_000 on 08.05.2015.
 */
public class VehicleMarkerVM extends VehicleMarker{

    public VehicleMarkerVM(Marker marker, JSONObject vehicleInfoJSON){
        super(marker, vehicleInfoJSON);


        try {
            JSONObject locationJSON = vehicleInfoJSON.getJSONObject("MonitoredVehicleJourney").getJSONObject("VehicleLocation");
            LatLng position = new LatLng(locationJSON.getDouble("Latitude"), locationJSON.getDouble("Longitude"));

            float bearing = (float)vehicleInfoJSON.getJSONObject("MonitoredVehicleJourney").getDouble("Bearing");

            marker.setPosition(position);
            marker.setRotation(bearing);
            marker.setTitle(generateTitle());
            marker.setSnippet(generateSnippet());

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected String generateTitle(){
        String title = "Title";
        try {
            String lineNumber = vehicleInfoJSON.getJSONObject("MonitoredVehicleJourney").getJSONObject("LineRef").getString("value");
            String destination = vehicleInfoJSON.getJSONObject("MonitoredVehicleJourney").getJSONObject("DestinationName").getString("value");
            title = "Line " + lineNumber + " towards " + destination + ".";
        }catch(Exception e){
            e.printStackTrace();
        }
        return title;
    }

    protected String generateSnippet(){
        return "";
    }
}
