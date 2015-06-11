package no.application.sofia.busmapapp.markers;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker
        ;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import no.application.sofia.busmapapp.markerhandlers.VehicleArrival;

/**
 * Created by oknak_000 on 08.05.2015.
 */
public class VehicleMarkerSM extends VehicleMarker {
    private int transportation = 0;
    private ArrayList<VehicleArrival> arrivals;
    private ArrayList<StopVisit> stopVisits;

    private StopVisit storedNextStop = null;


    public VehicleMarkerSM(Marker marker, JSONObject vehicleInfoJSON) {
        super(marker, vehicleInfoJSON);
        arrivals = new ArrayList<VehicleArrival>();
        stopVisits = new ArrayList<StopVisit>();

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
    @Override
    public void updatePosition(){
        Date currentTime = new Date();

        StopVisit prevStop = null;
        StopVisit nextStop = null;
        StopVisit afterNextStop = null;
        for(int i = 0; i < stopVisits.size(); i++){
            StopVisit currentStopVisit = stopVisits.get(i);
            if(currentTime.getTime() > currentStopVisit.getTime()){
                if(prevStop == null || prevStop.getTime() < currentStopVisit.getTime())
                    prevStop = currentStopVisit;
            } else {
                if(nextStop == null || nextStop.getTime() > currentStopVisit.getTime()){
                    afterNextStop = nextStop;
                    nextStop = currentStopVisit;
                } else if (afterNextStop == null || afterNextStop.getTime() > currentStopVisit.getTime()){
                    afterNextStop = currentStopVisit;
                }
            }
        }

        if(storedNextStop != nextStop){
            if(storedNextStop != null){
                storedNextStop.forceUpdate(2000);
                nextStop.forceUpdate(2000);
                prevStop.forceUpdate(2000);
            }
            storedNextStop = nextStop;
        }

        final LatLng position = calculatePosition(prevStop, nextStop, currentTime);
        final float bearing = calculateBearing(prevStop, nextStop);

        final String title = nextStop.generateTitle();
        final String snippet = nextStop.generateSnippet();

        final Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (position == null) {
                    vehicleMarker.setVisible(false);
                } else {
                    vehicleMarker.setPosition(position);
                    vehicleMarker.setTitle(title);
                    vehicleMarker.setSnippet(snippet);
                    vehicleMarker.setVisible(true);
                    vehicleMarker.setRotation(bearing);
                }
            }
        });

        nextStop.updateFromAPIIfNeeded();
        afterNextStop.updateFromAPIIfNeeded();
    }

    private LatLng calculatePosition(StopVisit prevStop, StopVisit nextStop, Date currentTime){
        if (nextStop == null) {
            return null;
        }
        if (prevStop == null) {
            return nextStop.getStopPosition();
        }

        long m1 = nextStop.getTime() - currentTime.getTime();
        long m2 = nextStop.getTime() - prevStop.getTime();
        double multiplicator = (double) m1 / (double) m2;

        multiplicator = multiplicator < 0 ? 0 : multiplicator > 1 ? 1 : multiplicator;

        double lat = multiplicator * (prevStop.getStopPosition().latitude - nextStop.getStopPosition().latitude) + nextStop.getStopPosition().latitude;
        double lng = multiplicator * (prevStop.getStopPosition().longitude - nextStop.getStopPosition().longitude) + nextStop.getStopPosition().longitude;

        return new LatLng(lat, lng);
    }

    private float calculateBearing(StopVisit prevStop, StopVisit nextStop){
        if (nextStop == null || prevStop == null)
            return 0;

        LatLng pos1 = prevStop.getStopPosition();
        LatLng pos2 = nextStop.getStopPosition();

        double ?1 = toRadians(pos1.latitude);
        double ?2 = toRadians(pos2.latitude);
        double ?1 = toRadians(pos1.longitude);
        double ?2 = toRadians(pos2.longitude);

        double y = Math.sin(?2 - ?1) * Math.cos(?2);
        double x = Math.cos(?1) * Math.sin(?2) -
                Math.sin(?1) * Math.cos(?2) * Math.cos(?2 - ?1);

        double deg = toDegrees(Math.atan2(y, x));
        return (float)(deg) % 360f;
    }

    private double toRadians(double deg) {
        return (deg * (Math.PI / 180.0));
    }

    private double toDegrees(double rad) {
        return rad * (180.0 / Math.PI);
    }
}
