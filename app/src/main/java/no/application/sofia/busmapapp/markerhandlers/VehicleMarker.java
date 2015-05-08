package no.application.sofia.busmapapp.markerhandlers;

import com.google.android.gms.maps.model.Marker;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by oknak_000 on 07.05.2015.
 */
public class VehicleMarker {
    protected Marker vehicleMarker;
    protected JSONObject vehicleInfoJSON;
    protected RouteMarkerHandler markerHandler;


    public VehicleMarker(Marker marker, JSONObject vehicleInfoJSON, RouteMarkerHandler markerHandler) {
        this.vehicleMarker = marker;
        this.vehicleInfoJSON = vehicleInfoJSON;
        this.markerHandler = markerHandler;
    }


}
