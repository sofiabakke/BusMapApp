package no.application.sofia.busmapapp.markers;

import com.google.android.gms.maps.GoogleMap;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by oknak_000 on 08.05.2015.
 */
public class MarkerHandlerSM extends MarkerHandler {

    private ArrayList<StopVisit> stopVisits;
    private ArrayList<StopMarkerSM> stopMarkers;
    private int lastLineID = 0;

    public MarkerHandlerSM(GoogleMap busMap){
        super(busMap);
        stopVisits = new ArrayList<StopVisit>();
        initThread();
    }


    public void addLineToMap(){
        // Look up all stops on line
        //
    }

    @Override
    protected void initThread() {
        updateThread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                int loops = 0;
                while (!updateThread.isInterrupted()) {
                    try {
                        if(running) {
                            for(int i = 0; i < vehicleMarkers.size(); i++){
                                vehicleMarkers.get(i).updatePosition();
                            }
                            updateAll();
                        }
                        Thread.sleep(500);
                        loops++;
                        if(loops == 120000){
                            if(lastLineID != 0);
                                updateAllStopsOnLine(lastLineID);
                            loops = 0;
                        }
                    } catch (InterruptedException e) {

                    }
                }
            }
        });
        updateThread.start();
    }

    @Override
    public void addRouteMarkers(String route){
        busMap.clear();
        vehicleMarkers.clear();
        stopMarkers.clear();

        final int lineID = Integer.parseInt(route);
        lastLineID = lineID;

        new Thread(new Runnable() {
            public void run() {
                addRouteLineToMap(lineID);
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                addVehicleMarkersToMap(lineID);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                addStopMarkersToMap(lineID);
            }
        }).start();
    }

    private void addRouteLineToMap(int lineID){

    }

    private void addVehicleMarkersToMap(int lineID){

    }

    private void addStopMarkersToMap(int lineID){

    }





    @Override
    protected void updateAll() {

    }

    private void updateAllStopsOnLine(int lineID){

    }

    public void updateOneLineArrivalsonStop(StopMarkerSM stopMarker, int lineID){
        String url = "http://reisapi.ruter.no/StopVisit/GetDepartures/" + stopMarker.stopID + "?linenames=" + lineID + "&json=true";
        updateArrivals(url, stopMarker);
    }

    public void updateAllArrivalsOnStop(StopMarkerSM stopMarker){
        String url = "http://reisapi.ruter.no/StopVisit/GetDepartures/" + stopMarker.stopID + "?json=true";
        updateArrivals(url, stopMarker);
    }

    private void updateArrivals(String url, StopMarkerSM stopMarker){
        String arrivalsString = sendJSONRequest(url);
        JSONArray arrivalsJSON = new JSONArray();
        try{
            arrivalsJSON = new JSONArray(arrivalsString);
        }catch (Exception e) {
            e.printStackTrace();
        }

        stopVisits.clear();

        for(int i = 0; i < arrivalsJSON.length(); i++){
            try {
                stopVisits.add(new StopVisit(this, stopMarker, arrivalsJSON.getJSONObject(i)));

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}