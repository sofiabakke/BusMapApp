package no.application.sofia.busmapapp;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
	public int busStopID = 0;
	private String destName = "";
	private String lineName = "";
	private String lineID = "";
	private BusLineMarkerHandler markerHandler;


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

	private Date lastUpdate;

	public BusArrival(JSONObject arrival, int typeID, BusLineMarkerHandler markerHandler){
		try {
			lineName = arrival.getString("LineName");
			lineID = arrival.getString("LineID");
			destName = arrival.getString("DestinationName");
			busStopName = arrival.getString("BusStopName");
			busStopID = arrival.getInt("BusStopID");
			arrivalTime = new Date(arrival.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS"));

			vehicleID = arrival.getString("VehicleID");
			double lat = arrival.getJSONObject("BusStopPosition").getDouble("Latitude");
			double lng = arrival.getJSONObject("BusStopPosition").getDouble("Longitude");
			position = new LatLng(lat, lng);

			Log.d("BusArrival", arrival.toString());

			this.type = transportationTypes[typeID];
			lastUpdate = new Date();
			this.markerHandler = markerHandler;

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public boolean updateArrivalIfSame(JSONObject arrival, int stopID){
		try {
			if(lineName == arrival.getString("LineName") &&
			lineID == arrival.getString("LineID") &&
			destName == arrival.getString("DestinationName") &&
			(busStopID == stopID || busStopID == arrival.getInt("BusStopID"))) {

				arrivalTime = new Date(arrival.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS"));
				lastUpdate = new Date();
				return true;
			}
		}catch (Exception e){
			e.printStackTrace();
		}

		return false;
	}


	public long getTime(){
		return arrivalTime.getTime();
	}

	public LatLng getPosition(){
		return position;
	}

	public String generateTitle(){
		return type + lineName + " " + destName;
	}

	public String generateSnippet(){
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		format.setTimeZone(TimeZone.getDefault());
		String time = format.format(arrivalTime);
		String last = format.format(lastUpdate);
		return ("Arrives at " + busStopName + " at " + time + "\nLast updated: " + last);
	}

	public void updateFromAPIIfNeeded(){
		Date currentTime = new Date();

		if(currentTime.getTime() > lastUpdate.getTime() + 25000){
			update();
		}
	}
	public void forceUpdate(int time){
		Date currentTime = new Date();
		if(lastUpdate.getTime() + time > currentTime.getTime())
			update();
	}

	private void update(){
		Date currentTime = new Date();
		lastUpdate = currentTime;




		new Thread(new Runnable() {
			public void run() {
				markerHandler.updateOneStop(Integer.parseInt(lineID), busStopID);
				/*String url = "http://api.bausk.no/Bus/getStopVisitsOnStop/Ruter/" + busStopID + "/" + lineID;
				try {
					JSONObject json = new JSONObject(sendJSONRequest(url));
					JSONArray stopVisits = json.getJSONArray("StopVisits");

					for(int i = 0; i < stopVisits.length(); i++){
						JSONObject stopVisit = stopVisits.getJSONObject(i);

						if(stopVisit.getString("VehicleID").equals(vehicleID)){
							arrivalTime = new Date(stopVisit.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS"));
						}
					}

				}catch(Exception e){
					e.printStackTrace();
				}*/
			}
		}).start();
	}
}
