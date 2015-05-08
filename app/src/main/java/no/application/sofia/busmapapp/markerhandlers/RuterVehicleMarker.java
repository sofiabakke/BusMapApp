package no.application.sofia.busmapapp.markerhandlers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by oknak_000 on 18.03.2015.
 */
public class RuterVehicleMarker {
	private Marker vehicleMarker;
	private JSONObject vehicleInfoJSON;
	private ArrayList<VehicleArrival> arrivals;
	public int vehicleID = 0;
	private int transportation = 0;
	private VehicleArrival storedNextStop;
	private RouteMarkerHandler markerHandler;

	public RuterVehicleMarker(Marker marker, JSONObject vehicleInfoJSON, RouteMarkerHandler markerHandler) {
		vehicleMarker = marker;
		arrivals = new ArrayList<VehicleArrival>();
		this.vehicleInfoJSON = vehicleInfoJSON;
		this.markerHandler = markerHandler;
		try {

			vehicleID = vehicleInfoJSON.getInt("VehicleID");
			transportation = vehicleInfoJSON.getInt("Transportation");

			JSONArray arrivalsJSON = vehicleInfoJSON.getJSONArray("Arrivals");
			for (int i = 0; i < arrivalsJSON.length(); i++) {
				arrivals.add(new VehicleArrival(arrivalsJSON.getJSONObject(i), transportation, markerHandler));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void update(JSONObject stopVisit, int busStopID){
		try{
			for(int i = 0; i < arrivals.size(); i++){
				arrivals.get(i).updateArrivalIfSame(stopVisit, busStopID);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void updateVehicle(JSONObject vehicleInfoJSON, int busStopID, int transportation){
		try {

			for (int i = 0; i < vehicleInfoJSON.length(); i++) {
				boolean handled = false;
				JSONObject arrivalJSON = vehicleInfoJSON.getJSONArray("Arrivals").getJSONObject(i);
				for (int j = 0; j < arrivals.size() && !handled; j++) {
					handled = arrivals.get(j).updateArrivalIfSame(arrivalJSON, busStopID);
				}

				if (!handled) {
					arrivals.add(new VehicleArrival(arrivalJSON, transportation, markerHandler));
				}
			}
		}catch(Exception e){
			Log.d("JSON ERROR", vehicleInfoJSON.toString());
			e.printStackTrace();
		}
	}


	public void updatePosition() {
		Date currentTime = new Date();

		VehicleArrival prevStop = null;
		VehicleArrival nextStop = null;
		VehicleArrival afterNextStop = null;
		for (int i = 0; i < arrivals.size(); i++) {
			VehicleArrival currentArrival = arrivals.get(i);
			if (currentTime.getTime() > currentArrival.getTime()) {
				if (prevStop == null || prevStop.getTime() < currentArrival.getTime())
					prevStop = currentArrival;
			} else {
				if (nextStop == null || nextStop.getTime() > currentArrival.getTime()) {
					afterNextStop = nextStop;
					nextStop = currentArrival;
				}else if (afterNextStop == null || afterNextStop.getTime() > currentArrival.getTime()){
					afterNextStop = currentArrival;
				}
			}
		}

		if(storedNextStop != nextStop){
			if(storedNextStop != null) {
				//Date passed = new Date(storedNextStop.getTime());
				//Log.d("Stop passed", "New next stop = " + passed.toString());
				storedNextStop.forceUpdate(2000);
				nextStop.forceUpdate(2000);
				prevStop.forceUpdate(2000);
			}
			storedNextStop = nextStop;
		}


		final LatLng position = calculatePosition(prevStop, nextStop, currentTime);

		final float bearing = caluclateBearing(prevStop, nextStop);

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

	private LatLng calculatePosition(VehicleArrival prev, VehicleArrival next, Date currentTime) {
		if (next == null) {
			return null;
		}
		if (prev == null) {
			return next.getPosition();
		}

		long m1 = next.getTime() - currentTime.getTime();
		long m2 = next.getTime() - prev.getTime();
		double multiplicator = (double) m1 / (double) m2;

		multiplicator = multiplicator < 0 ? 0 : multiplicator > 1 ? 1 : multiplicator;

		double lat = multiplicator * (prev.getPosition().latitude - next.getPosition().latitude) + next.getPosition().latitude;
		double lng = multiplicator * (prev.getPosition().longitude - next.getPosition().longitude) + next.getPosition().longitude;

		return new LatLng(lat, lng);
	}

	private float caluclateBearing(VehicleArrival prev, VehicleArrival next) {
		if (next == null || prev == null)
			return 0;

		LatLng pos1 = prev.getPosition();
		LatLng pos2 = next.getPosition();

		double φ1 = toRadians(pos1.latitude);
		double φ2 = toRadians(pos2.latitude);
		double λ1 = toRadians(pos1.longitude);
		double λ2 = toRadians(pos2.longitude);

		double y = Math.sin(λ2 - λ1) * Math.cos(φ2);
		double x = Math.cos(φ1) * Math.sin(φ2) -
			Math.sin(φ1) * Math.cos(φ2) * Math.cos(λ2 - λ1);

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
