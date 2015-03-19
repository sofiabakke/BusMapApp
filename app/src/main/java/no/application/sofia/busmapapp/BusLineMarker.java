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
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by oknak_000 on 18.03.2015.
 */
public class BusLineMarker {
	private Marker vehicleMarker;
	private JSONObject vehicleInfoJSON;

	public BusLineMarker(Marker marker, JSONObject vehicleInfoJSON){
		vehicleMarker = marker;
		this.vehicleInfoJSON = vehicleInfoJSON;

	}

	public void APIupdate(){

	}

	public void update(){
		Date currentTime = new Date();
		try {
			JSONArray arrivals = vehicleInfoJSON.getJSONArray("Arrivals");
			JSONObject previousStop = null;
			JSONObject nextStop = null;
			for (int i = 0; i < arrivals.length(); i++){
				JSONObject currentArrival = arrivals.getJSONObject(i);
				long arrivalTime = currentArrival.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS");
				if(currentTime.getTime() > arrivalTime){
					previousStop = currentArrival;
				}else{
					nextStop = currentArrival;
					break;
				}
			}



			final LatLng position = calculatePosition(previousStop, nextStop, currentTime);
			final String title = generateTitle(vehicleInfoJSON);
			final String snippet = generateSnippet(nextStop);

			final Handler mainHandler = new Handler(Looper.getMainLooper());
			mainHandler.post(new Runnable() {
				@Override
				public void run() {
					vehicleMarker.setPosition(position);
					vehicleMarker.setTitle(title);
					vehicleMarker.setSnippet(snippet);
				}
			});

		}catch (Exception e){
			e.printStackTrace();
		}
	}


	private LatLng calculatePosition(JSONObject prev, JSONObject next, Date currentTime){
		if(next == null){
			Log.d("BusLineMarker", "Next=null");
			return null;
		}
		if(prev == null){
			Log.d("BusLineMarker", "Prev=null");
			try {
				double lat = next.getJSONObject("BusStopPosition").getDouble("Latitude");
				double lng = next.getJSONObject("BusStopPosition").getDouble("Longitude");
				return new LatLng(lat, lng);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		try {
			double lat0 = prev.getJSONObject("BusStopPosition").getDouble("Latitude");
			double lng0 = prev.getJSONObject("BusStopPosition").getDouble("Longitude");

			double lat1 = next.getJSONObject("BusStopPosition").getDouble("Latitude");
			double lng1 = next.getJSONObject("BusStopPosition").getDouble("Longitude");


			//Date arrivalNext = new Date(next.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS"));
			long arrivalNext = next.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS");
			long arrivalPrev = prev.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS");
			//Date arrivalPrev = new Date(prev.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS"));

			long t0 = arrivalNext - currentTime.getTime();
			long t1 = arrivalNext - arrivalPrev;
			float multiplicator = (float)t0 / (float)t1;

			if(multiplicator > 1 ){
				multiplicator = 1;
			}else if(multiplicator < 0){
				multiplicator = 0;
			}


			double latX = multiplicator * (lat0 - lat1) + lat1;
			double lngX = multiplicator * (lng0 - lng1) + lng1;

			return new LatLng(latX, lngX);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	private String generateTitle(JSONObject busJSON){
		// Add transportation types
		String type = "Bus ";
		try {
			String lineName = busJSON.getJSONArray("Arrivals").getJSONObject(0).getString("LineName");
			String destination = busJSON.getJSONArray("Arrivals").getJSONObject(0).getString("DestinationName");
			return type + lineName + " towards " + destination;
		}catch(Exception e){
			e.printStackTrace();
			return "Bus";
		}
	}

	private String generateSnippet(JSONObject nextStop){
		try{
			String nextStopName = nextStop.getString("BusStopName");
			long arrivalTimeLong = nextStop.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS");
			Date arrivalTime = new Date(arrivalTimeLong);
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			format.setTimeZone(TimeZone.getDefault());
			String time = format.format(arrivalTime);
			return ("Arrives at " + nextStopName + " at " + time);
		}catch (Exception e){
			e.printStackTrace();
			return "";
		}
	}

}
