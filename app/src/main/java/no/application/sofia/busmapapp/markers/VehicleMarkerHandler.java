package no.application.sofia.busmapapp.markers;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractList;
import java.util.ArrayList;

import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.markerhandlers.RouteMarkerHandler;

/**
 * Created by oknak_000 on 08.05.2015.
 */
public abstract class VehicleMarkerHandler {
    protected static GoogleMap busMap;
    AbstractList<VehicleMarker> vehicleMarkers;
    protected Thread updateThread;
    protected boolean running = true;


    public VehicleMarkerHandler(GoogleMap busMap){
        this.busMap = busMap;
        vehicleMarkers = new ArrayList<VehicleMarker>();
    }

    protected abstract void initThread();
    protected abstract void updateAll();

    public void stopUpdateThread(){ running = false; }
    public void restartUpdateThread(){ running = true; }

    // Downloads JSON from a given URL
    protected String sendJSONRequest(String URL){
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
                Log.e(RouteMarkerHandler.class.getName(), "Failed to download file");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

}
