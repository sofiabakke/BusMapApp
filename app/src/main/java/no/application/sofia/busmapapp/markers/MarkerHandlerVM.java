package no.application.sofia.busmapapp.markers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import no.application.sofia.busmapapp.R;

/**
 * Created by oknak_000 on 08.05.2015.
 */
public class MarkerHandlerVM extends VehicleMarkerHandler {

    public MarkerHandlerVM(GoogleMap busMap){
        super(busMap);
        initThread();

        //updateAll();
    }

    @Override
    protected void initThread() {
        updateThread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                //while (!updateThread.isInterrupted()) {
                    try {
                        if(running) {
                            updateAll();
                        }
                        //Thread.sleep(5000);
                    } catch (Exception e) {

                    }
                //}
            }
        });
        updateThread.start();
    }

    @Override
    protected void updateAll(){
        JSONObject json = getVehiclePositions();
        try {
            JSONArray vehicles = json.getJSONObject("Siri").getJSONObject("ServiceDelivery").getJSONArray("VehicleMonitoringDelivery").getJSONObject(0).getJSONArray("VehicleActivity");

            addMarkers(vehicles);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private JSONObject getVehiclePositions(){
        String url = "http://data.itsfactory.fi/siriaccess/vm/json";
        String busPositions = sendJSONRequest(url);
        JSONObject json = new JSONObject();
        try{
            json = new JSONObject(busPositions);
        }catch (Exception e){
            e.printStackTrace();
        }
        return json;
    }

    private void addMarkers(final JSONArray vehicles){
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        Log.d("MarkerHandlerVM", "Vehciles: " + vehicles.length());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < vehicles.length(); i++) {
                        VehicleMarkerVM marker = new VehicleMarkerVM(
                                busMap.addMarker(new MarkerOptions()
                                .position(new LatLng(0, 0))
                                .title("asdasdasd")
                                .anchor(0.5f, 0.5f)
                                .flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow_blue_half_and_half))
                                ),vehicles.getJSONObject(i)
                        );
                        // marker.updatePosition();
                        vehicleMarkers.add(marker);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
