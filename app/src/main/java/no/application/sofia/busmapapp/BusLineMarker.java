package no.application.sofia.busmapapp;

import com.google.android.gms.analytics.ExceptionParser;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
				Date arrivalTime = new Date(currentArrival.getJSONObject("Arrival").getString("ExpectedArrivalTime"));
				if(currentTime.getTime() > arrivalTime.getTime()){
					previousStop = currentArrival;
				}else{
					nextStop = currentArrival;
					break;
				}
			}



			LatLng position = calculatePosition(previousStop, nextStop, currentTime);
			vehicleMarker.setPosition(position);
			String title = generateTitle(nextStop);
			vehicleMarker.setTitle(title);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private String generateTitle(JSONObject nextStop){
		return "Hello";
	}

	private LatLng calculatePosition(JSONObject prev, JSONObject next, Date currentTime){
		if(next == null){
			return null;
		}
		if(prev == null){
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


			Date arrivalNext = new Date(next.getJSONObject("Arrival").getString("ExpectedArrivalTime"));
			Date arrivalPrev = new Date(prev.getJSONObject("Arrival").getString("ExpectedArrivalTime"));
			float multiplicator = (arrivalNext.getTime() - currentTime.getTime()) / (arrivalNext.getTime() - arrivalPrev.getTime());
			if(multiplicator > 1 ){
				multiplicator = 1;
			}else if(multiplicator < 0){
				multiplicator = 0;
			}

			double latX = multiplicator * (lat1 - lat0) + lat0;
			double lngX = multiplicator * (lng1 - lng0) + lng0;

			return new LatLng(latX, lngX);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
