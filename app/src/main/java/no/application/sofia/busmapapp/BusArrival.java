package no.application.sofia.busmapapp;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by oknak_000 on 20.03.2015.
 */
public class BusArrival {
	private Date arrivalTime;
	private LatLng position;
	private String vehicleID;
	private String type = "";
	private String busStopName = "";
	private String destName = "";
	private String lineName = "";
	private String[] transportationTypes= {
		"Walk ",
		"Airport Bus ",
		"Bus ",
		"Dummy ",
		"Airport Train ",
		"Boat ",
		"Train ",
		"Tram ",
		"Metro "};

	public BusArrival(JSONObject arrival, int typeID){
		try {
			lineName = arrival.getString("LineName");
			destName = arrival.getString("DestinationName");
			busStopName = arrival.getString("BusStopName");
			arrivalTime = new Date(arrival.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS"));

			vehicleID = arrival.getString("VehicleID");
			double lat = arrival.getJSONObject("BusStopPosition").getDouble("Latitude");
			double lng = arrival.getJSONObject("BusStopPosition").getDouble("Longitude");
			position = new LatLng(lat, lng);

			Log.d("BusArrival", arrival.toString());

			this.type = transportationTypes[typeID];



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
		String type = "Bus ";

		return type + lineName + " towards " + destName;
	}

	public String generateSnippet(){
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		format.setTimeZone(TimeZone.getDefault());
		String time = format.format(arrivalTime);
		return ("Arrives at " + busStopName + " at " + time);
	}

	public void updateFromAPIIfNeeded(){
		// Update from api!
	}


}
