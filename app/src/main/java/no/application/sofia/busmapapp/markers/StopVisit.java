package no.application.sofia.busmapapp.markers;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by oknak_000 on 08.05.2015.
 */
public class StopVisit {
    private StopMarkerSM stopMarker;
    private MarkerHandlerSM markerHandlerSM;

    private Date arrivalTime;
    private Date lastUpdate;
    private int vehicleRef = 0;
    private int lineID = 0;

    public StopVisit(MarkerHandlerSM markerHandlerSM, StopMarkerSM stopMarker, JSONObject arrivalJSON){
        this.markerHandlerSM = markerHandlerSM;
        this.stopMarker = stopMarker;

        lastUpdate = new Date();

        try {
            String arrival = arrivalJSON.getJSONObject("MonitoredVehicleJourney").getJSONObject("MonitoredCall").getString("ExpectedArrivalTime");
            DateFormat format = new SimpleDateFormat("yyyy-MM-ddTkk:mm:ss");
            arrivalTime = format.parse(arrival);
            // Handle timezones!

            vehicleRef = Integer.parseInt(arrivalJSON.getJSONObject("MonitoredVehicleJourney").getString("VehicleRef"));
            lineID = Integer.parseInt(arrivalJSON.getJSONObject("MonitoredVehicleJourney").getString("LineRef"));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public long getTime(){
        return arrivalTime.getTime();
    }

    public void updateFromAPIIfNeeded(){

    }

    public String generateTitle(){
        return "";
    }

    public String generateSnippet(){
        return "";
    }

    public LatLng getStopPosition(){
        return stopMarker.marker.getPosition();
    }

    public void forceUpdate(int time){
        Date currentTime = new Date();
        if(lastUpdate.getTime() + time > currentTime.getTime())
            update();
    }

    public void update(){
        Date currentTime = new Date();
        lastUpdate = currentTime;




        new Thread(new Runnable() {
            public void run() {
                markerHandlerSM.updateOneLineArrivalsonStop(stopMarker, lineID);
            }
        }).start();
    }


}
