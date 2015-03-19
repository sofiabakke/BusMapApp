package no.application.sofia.busmapapp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by oknak_000 on 18.03.2015.
 */
public class BusLineMarkerHandler {
	private static GoogleMap busMap;
	private ArrayList<BusLineMarker> vehicleMarkers;
	private Thread updateThread;
	private boolean running = true;

	public BusLineMarkerHandler(GoogleMap busMap){
		this.busMap = busMap;
		vehicleMarkers = new ArrayList<BusLineMarker>();
		updateThread = new Thread(new Runnable() {
			@Override
			public void run()
			{
				while (!updateThread.isInterrupted()) {
					try {
						Thread.sleep(500);
						if(running) {
							Log.d("BusLineMarker", "Update");
							for (int i = 0; i < vehicleMarkers.size(); i++) {
								vehicleMarkers.get(i).update();
							}
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

		final int lineID = Integer.parseInt(route);

		new Thread(new Runnable() {
			public void run() {
				addRouteLineToMap("Ruter", lineID);
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				//addRouteMarkersToMap("Ruter", lineID);
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
		for(int i = 0; i < buses.length(); i++){
			try {
				final JSONObject busJSON = buses.getJSONObject(i);

				final Handler mainHandler = new Handler(Looper.getMainLooper());
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
						try {

							BusLineMarker marker = new BusLineMarker(
								busMap.addMarker(new MarkerOptions()
									.position(new LatLng(0,0))
									.title("asdasdasd")
									.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_bus))),
								busJSON
							);
							marker.update();
							vehicleMarkers.add(marker);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				});
			}catch(Exception e){
				e.printStackTrace();
			}
		}
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

	private void addRouteMarkersToMap(String operator, int lineID){
		JSONArray buses = getBusPositionsOnLine(operator, lineID);

		for(int i = 0; i < buses.length(); i++){
			try {
				final JSONObject json = buses.getJSONObject(i);
				final JSONObject positionJSON = json.getJSONObject("Position");
				final LatLng pos = new LatLng(positionJSON.getDouble("Latitude"), positionJSON.getDouble("Longitude"));

				// Forces main thread to add markers to the map, as this can not be done in a separate thread
				final Handler mainHandler = new Handler(Looper.getMainLooper());
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
						try {
							int transportation = json.getInt("Transportation");
							BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker_bus);
							String transName = "Bus";

							if (transportation == 0){ // Walking

							}else if (transportation == 1){
								transName = "Airport Bus";
								// icon =
							}else if (transportation == 2){
								transName = "Bus";
								// icon =
							}else if (transportation == 3){
								transName = "Dummy";
								// icon =
							}else if (transportation == 4){
								transName = "Airport Train";
								// icon =
							}else if (transportation == 5){
								transName = "Boat";
								// icon =
							}else if (transportation == 6){
								transName = "Train";
								// icon =
							}else if (transportation == 7){
								transName = "Tram";
								icon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker_tram);
							}else if(transportation == 8){
								transName = "Metro";
								icon = BitmapDescriptorFactory.fromResource(R.drawable.map_marker_sub);
							}

							busMap.addMarker(new MarkerOptions()
								.title(transName + " " + json.getString("LineID") + " towards " + json.getString("DestinationName"))
								.position(pos)
								.icon(icon)
								.snippet("Arrives at " + json.getString("NextBusStopName") + " at " + json.getString("NextBusStopArrival").substring(11,19)));
							busMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				});

			}catch(JSONException e){
				e.printStackTrace();
			}
		}
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
							busMap.addMarker(new MarkerOptions()
								.title("Name: " + json.getString("Name"))
								.position(pos)
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_stop))
								.snippet("STOOOOP"));
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

	private JSONArray getBusPositionsOnLine(String operator, int lineID){
		String URL = "http://api.bausk.no/Bus/getBusPositionsOnLine/" + operator + "/" + lineID;

		String busJSONs = sendJSONRequest(URL);
		JSONArray json = new JSONArray();
		try {
			json = new JSONArray(busJSONs);
			Log.i(BusLineMarkerHandler.class.getName(), json.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
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

}
