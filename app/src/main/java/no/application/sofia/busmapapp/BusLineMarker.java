package no.application.sofia.busmapapp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.analytics.ExceptionParser;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by oknak_000 on 18.03.2015.
 */
public class BusLineMarker {
	private Marker vehicleMarker;
	private JSONObject vehicleInfoJSON;
	private ArrayList<BusArrival> arrivals;
	private int vehicleID = 0;
	private int transportation = 0;

	public BusLineMarker(Marker marker, JSONObject vehicleInfoJSON){
		vehicleMarker = marker;
		arrivals = new ArrayList<BusArrival>();
		this.vehicleInfoJSON = vehicleInfoJSON;
		try {

			vehicleID = vehicleInfoJSON.getInt("VehicleID");
			transportation = vehicleInfoJSON.getInt("Transportation");

			JSONArray arrivalsJSON = vehicleInfoJSON.getJSONArray("Arrivals");
			for (int i = 0; i < arrivalsJSON.length(); i++) {
				arrivals.add(new BusArrival(arrivalsJSON.getJSONObject(i), transportation));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void APIupdate(){
		for(int i=0; i < arrivals.size(); i++){
			arrivals.get(i).updateFromAPIIfNeeded();
		}
	}

	public void update(){
		Date currentTime = new Date();

		BusArrival prevStop = null;
		BusArrival nextStop = null;
		for(int i = 0; i < arrivals.size(); i++){
			BusArrival currentArrival = arrivals.get(i);
			if(currentTime.getTime() > currentArrival.getTime()){
				prevStop = currentArrival;
			}else{
				nextStop = currentArrival;
				break;
			}
		}

		final LatLng position = calculatePosition(prevStop, nextStop, currentTime);

		final String title = nextStop.generateTitle();
		final String snippet = nextStop.generateSnippet();

		final Handler mainHandler = new Handler(Looper.getMainLooper());
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				if(position == null) {
					vehicleMarker.setVisible(false);
				}else {
					vehicleMarker.setPosition(position);
					vehicleMarker.setTitle(title);
					vehicleMarker.setSnippet(snippet);
					vehicleMarker.setVisible(true);
				}
			}
		});
	}

	private LatLng calculatePosition(BusArrival prev, BusArrival next, Date currentTime){
		if(next == null){
			return null;
		}
		if(prev == null){
			return next.getPosition();
		}

		long m1 = next.getTime() - currentTime.getTime();
		long m2 = next.getTime() - prev.getTime();
		double multiplicator = (double)m1 / (double)m2;

		multiplicator = multiplicator < 0 ? 0 : multiplicator > 1 ? 1 : multiplicator;

		double lat = multiplicator * (prev.getPosition().latitude - next.getPosition().latitude) + next.getPosition().latitude;
		double lng = multiplicator * (prev.getPosition().longitude - next.getPosition().longitude) + next.getPosition().longitude;

		return new LatLng(lat, lng);

	}
}
