package no.application.sofia.busmapapp.markers;

import com.google.android.gms.maps.model.Marker;

import org.json.JSONObject;


/**
 * Created by oknak_000 on 08.05.2015.
 */
public abstract class VehicleMarker {
    protected Marker vehicleMarker;
    protected JSONObject vehicleInfoJSON;
    protected String vehicleRef;

    public VehicleMarker(Marker marker, JSONObject vehicleInfoJSON){
        this.vehicleMarker = marker;
        this.vehicleInfoJSON = vehicleInfoJSON;
    }

    protected abstract String generateTitle();

    protected abstract String generateSnippet();


}
