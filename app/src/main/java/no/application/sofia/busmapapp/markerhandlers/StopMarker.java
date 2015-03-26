package no.application.sofia.busmapapp.markerhandlers;


import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by oknak_000 on 24.03.2015.
 */
public class StopMarker {
	private Marker marker;
	public int id = 0;
	private int entries = 0;
	private final int maxEntries = 5;
	private ArrayList<Visit> visits;

	public StopMarker(Marker marker, int id){
		this.id = id;
		this.marker = marker;
		this.visits = new ArrayList<>();
	}

	public void updateSnippet(JSONArray arrivals) {
		clearSnippet();
		for (int i = 0; i < arrivals.length() && i < 5; i++) {
			try {
				JSONObject json = arrivals.getJSONObject(i);
				addArrival(json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		finishSnippet();
	}

	public void clearSnippet(){
		visits.clear();
		entries = 0;
	}

	public void addArrival(JSONObject arrival){
		try {
			long arrivalTime = arrival.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS");
			visits.add(new Visit(arrival.getString("LineName") + " " + arrival.getString("DestinationName"), arrivalTime));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void finishSnippet(){
		sortVisits();
		String snip = "";
		for(int i = 0; i < 5 && i < visits.size(); i++){
			if(i > 0)
				snip += "\n";
			snip += visits.get(i).toString();
		}

		final String snippet = snip;

		final Handler mainHandler = new Handler(Looper.getMainLooper());
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					marker.setSnippet(snippet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void sortVisits(){
		// Sorts using insertion sort
		for(int i = 0; i < visits.size() - 1; i++){
			Visit candidate = visits.get(i);
			int index = i;
			for(int j = i + 1; j < visits.size(); j++){
				if(candidate.arrivalTime > visits.get(j).arrivalTime){
					candidate = visits.get(j);
					index = j;
				}
			}

			// Swap the two visits if a better candidate was found
			if(index != i){
				visits.set(index, visits.get(i));
				visits.set(i, candidate);
			}
		}
	}


	private class Visit{
		String name = "";
		long arrivalTime = 0;

		public Visit(String name, long arrivalTime){
			this.arrivalTime = arrivalTime;
			this.name = name;
		}

		public String toString(){
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			format.setTimeZone(TimeZone.getDefault());
			return name + " " + format.format(new Date(arrivalTime));
		}
	}
}


