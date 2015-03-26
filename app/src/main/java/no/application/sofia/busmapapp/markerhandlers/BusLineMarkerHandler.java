package no.application.sofia.busmapapp.markerhandlers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import no.application.sofia.busmapapp.R;

/**
 * Created by oknak_000 on 18.03.2015.
 */
public class BusLineMarkerHandler {
	private static GoogleMap busMap;
	private ArrayList<BusLineMarker> vehicleMarkers;
	private ArrayList<StopMarker> stopMarkers;
	private Thread updateThread;
	private boolean running = true;
	private int lastLineID = 0;

	public BusLineMarkerHandler(GoogleMap busMap){
		this.busMap = busMap;
		vehicleMarkers = new ArrayList<>();
		stopMarkers = new ArrayList<>();
		updateThread = new Thread(new Runnable() {
			@Override
			public void run()
			{
				int loops = 0;
				while (!updateThread.isInterrupted()) {
					try {
						Thread.sleep(500);
						if(running) {
							for (int i = 0; i < vehicleMarkers.size(); i++) {
								vehicleMarkers.get(i).updatePosition();
							}
						}
						loops++;
						if(loops == 120000) {
							if (lastLineID != 0)
								updateAllStops(lastLineID);
							loops = 0;
						}

					} catch (InterruptedException e) {

					}
				}
			}
		});
		updateThread.start();
	}


	public void stopUpdateThread(){
		running = false;
	}
	public void restartUpdateThread(){
		running = true;
	}

	public void addRouteMarkers(String route){
		busMap.clear();
		vehicleMarkers.clear();
		stopMarkers.clear();


		final int lineID = Integer.parseInt(route);
		lastLineID = lineID;

		new Thread(new Runnable() {
			public void run() {
				addRouteLineToMap("Ruter", lineID);
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				addVehicleMarkersToMap("Ruter", lineID);
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				addStopMarkersToMap("Ruter", lineID);
			}
		}).start();
	}

	private void addRouteLineToMap(String operator, int lineID){
		JSONArray busStops = getBusStopsOnLine(operator, lineID);
		ArrayList<LatLng> stopPositions = new ArrayList<LatLng>();
		for(int i = 0; i < busStops.length(); i++){
			try {
				JSONObject positionJSON = busStops.getJSONObject(i).getJSONObject("Position");
				stopPositions.add(new LatLng(positionJSON.getDouble("Latitude"),
					positionJSON.getDouble("Longitude")));
			}catch(Exception e){
				e.printStackTrace();
			}
		}



		// Instantiates a new Polyline object and adds points to define a rectangle
		final PolylineOptions rectOptions = new PolylineOptions()
			.addAll(stopPositions);

		// Forces main thread to add polyline to the map, as this can not be done in a separate thread
		final Handler mainHandler = new Handler(Looper.getMainLooper());
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				busMap.addPolyline(rectOptions);
			}
		});

	}

	private void addVehicleMarkersToMap(String operator, int lineID){
		JSONArray buses = getBusArrivalsOnLine(operator, lineID);
		updateStopMarkerSnippets(buses);
		for(int i = 0; i < buses.length(); i++){
			try {
				final JSONObject busJSON = buses.getJSONObject(i);
				addMarker(busJSON);

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private void addMarker(final JSONObject busJSON){
		final Handler mainHandler = new Handler(Looper.getMainLooper());
		final BusLineMarkerHandler markerHandler = this;
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				try {

					BusLineMarker marker = new BusLineMarker(
						busMap.addMarker(new MarkerOptions()
							.position(new LatLng(0, 0))
							.title("asdasdasd")
							.anchor(0.5f, 0.5f)
							.flat(true)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow_blue_half_and_half))),
						busJSON,
						markerHandler
					);
					marker.updatePosition();
					vehicleMarkers.add(marker);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}



	private JSONArray getBusArrivalsOnLine(String operator, int lineID){
		String url = "http://api.bausk.no/Bus/getBusArrivalsOnLine/" + operator + "/" + lineID;
		String busArrivals = sendJSONRequest(url);
		JSONArray json = new JSONArray();
		try{
			json = new JSONArray(busArrivals);
		}catch(Exception e){
			e.printStackTrace();
		}
		return json;
	}

	private void addStopMarkersToMap(String operator, int lineID){
		JSONArray stops = getBusStopsOnLine(operator, lineID);
		for (int i = 0; i < stops.length(); i++){
			try{
				final JSONObject json = stops.getJSONObject(i);
				final JSONObject positionJSON = json.getJSONObject("Position");
				final LatLng pos = new LatLng(positionJSON.getDouble("Latitude"), positionJSON.getDouble("Longitude"));

				final Handler mainHandler = new Handler(Looper.getMainLooper());
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
						try{

							stopMarkers.add(new StopMarker(
								busMap.addMarker(new MarkerOptions()
									.title(json.getString("Name"))
									.position(pos)
									.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_stop_red))
									.snippet("Updating...")
									.anchor(0.5f, 0.5f)
									.flat(true)),
								json.getInt("ID")));
							busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				});
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private JSONArray getBusStopsOnLine(String operator, int lineID){
		String URL = "http://api.bausk.no/Stops/getBusStopsOnLine/" + operator + "/" + lineID;

		String busStopJSONs = sendJSONRequest(URL);
		JSONArray json = new JSONArray();
		try {
			json = new JSONArray(busStopJSONs);
			Log.i(BusLineMarkerHandler.class.getName(), json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

	public void updateOneStop(int lineID, int busStopID){
		String url = "http://api.bausk.no/Bus/getStopVisitsOnStop/Ruter/" + busStopID + "/" + lineID;

		try {
			JSONObject json = new JSONObject(sendJSONRequest(url));
			updateOneStopSnippet(json);
			JSONArray stopVisits = json.getJSONArray("StopVisits");
			LatLng position = new LatLng(json.getJSONObject("Position").getDouble("Latitude"),
				json.getJSONObject("Position").getDouble("Longitude"));

			int transportation = json.getInt("Transportation");
			for(int i = 0; i < stopVisits.length(); i++){
				JSONObject stopVisit = stopVisits.getJSONObject(i);

				boolean handled = false;
				for(int j = 0; j < vehicleMarkers.size() && !handled; j++){
					if(vehicleMarkers.get(j).vehicleID == stopVisit.getInt("VehicleID")){
						handled = true;
						vehicleMarkers.get(j).update(stopVisit, busStopID);
					}
				}

				if(!handled){
					// ADD NEW MAKERER!
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void updateAllStops(int lineID){
		JSONArray json = getBusArrivalsOnLine("Ruter", lineID);
		updateStopMarkerSnippets(json);

		for(int i = 0; i < json.length(); i++){
			boolean handled = false;
			for(int j = 0; j < vehicleMarkers.size() && !handled; j++){
				try {
					if (json.getJSONObject(i).getInt("VehicleID") == vehicleMarkers.get(j).vehicleID) {
						int transportation = json.getJSONObject(i).getInt("Transportation");
						vehicleMarkers.get(j).updateVehicle(json.getJSONObject(i), 0, transportation);
						handled = true;
					}

				}catch(Exception e){
					e.printStackTrace();
				}
			}
			if(!handled){
				try {
					addMarker(json.getJSONObject(i));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}

	}


	// Downloads JSON from a given URL
	private String sendJSONRequest(String URL){
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
				Log.e(BusLineMarkerHandler.class.getName(), "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	public JSONArray getBusLinesByOperator(String operator){
		String url = "http://api.bausk.no/Bus/getBusLinesByOperator/" + operator;
		String busInfoJsons = sendJSONRequest(url);
		JSONArray json = new JSONArray();
		try {
			json = new JSONArray(busInfoJsons);
		}catch (Exception e){
			e.printStackTrace();
		}
		return json;
	}
	private void updateStopMarkerSnippets(JSONArray buses){
		for(int i = 0; i < stopMarkers.size(); i++){
			stopMarkers.get(i).clearSnippet();
		}

		for(int i = 0; i < buses.length(); i++){
			try {
				JSONArray arrivals = buses.getJSONObject(i).getJSONArray("Arrivals");
				for (int j = 0; j < arrivals.length(); j++){
					for(int x = 0; x < stopMarkers.size(); x++){
						JSONObject arrival = arrivals.getJSONObject(j);
						if(stopMarkers.get(x).id == arrival.getInt("BusStopID")){
							stopMarkers.get(x).addArrival(arrival);
							break;
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		for(int i = 0; i < stopMarkers.size(); i++){
			stopMarkers.get(i).finishSnippet();
		}
	}
	private void updateOneStopSnippet(JSONObject json){

		for(int i = 0; i < stopMarkers.size(); i++){
			try {
				if(json.getInt("BusStopID") == stopMarkers.get(i).id) {
					stopMarkers.get(i).updateSnippet(json.getJSONArray("StopVisits"));
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
