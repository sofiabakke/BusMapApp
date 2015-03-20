package no.application.sofia.busmapapp;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by oknak_000 on 20.03.2015.
 */
public class BusArrival {
	private Date arrivalTime;
	private LatLng position;
	private int transportation = 5;
	private String busStopName = "";
	private String destName = "";
	private String lineName = "";

	public BusArrival(JSONObject arrival){
		try {
			lineName = arrival.getString("LineName");
			destName = arrival.getString("DestinationName");
			busStopName = arrival.getString("BusStopName");
			arrivalTime = new Date(arrival.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS"));

			double lat = arrival.getJSONObject("BusStopPosition").getDouble("Latitude");
			double lng = arrival.getJSONObject("BusStopPosition").getDouble("Longitude");
			position = new LatLng(lat, lng);

			Log.d("BusArrival", arrival.toString());

			transportation = 1; // DO SOMETHING ELSE


		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public long getTime(){
		return arrivalTime.getTime();
	}

	public LatLng getPosition(){
		return position;
	}

	public String generateTitle(){
		return "";
	}

	public String generateSnippet(){
		return "";
	}

	public void updateIfNeeded(){
		// Update from api!
	}


}
