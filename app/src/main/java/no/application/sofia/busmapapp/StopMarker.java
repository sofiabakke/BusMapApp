package no.application.sofia.busmapapp;


import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by oknak_000 on 24.03.2015.
 */
public class StopMarker {
	private Marker marker;
	public int id = 0;
	private String unfinishedSnippet = "";
	private int entries = 0;
	private final int maxEntries = 5;

	public StopMarker(Marker marker, int id){
		this.id = id;
		this.marker = marker;
	}

	public void clearSnippet(){
		unfinishedSnippet = "";
		entries = 0;
	}

	public void addArrival(JSONObject arrival){
		if(entries < maxEntries) {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			format.setTimeZone(TimeZone.getDefault());
			try {
				Date arrivalTime = new Date(arrival.getJSONObject("Arrival").getLong("ExpectedArrivalTimeMS"));

				unfinishedSnippet += arrival.getString("LineName") + " " + arrival.getString("DestinationName") + " " + format.format(arrivalTime) + "\n";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		entries++;
	}

	public void finishSnippet(){
		if(unfinishedSnippet.length() > 12){
			unfinishedSnippet = unfinishedSnippet.substring(0, unfinishedSnippet.length()-1);
		}

		final String snippet = unfinishedSnippet;

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

	public void updateSnippet(JSONArray arrivals) {
		clearSnippet();
		for (int i = 0; i < arrivals.length(); i++) {
			try {
				JSONObject json = arrivals.getJSONObject(i);
				addArrival(json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		finishSnippet();
		/*
		final String snip = snippet;

		final Handler mainHandler = new Handler(Looper.getMainLooper());
		mainHandler.post(new Runnable() {
			@Override
			public void run() {
				try {
					marker.setSnippet(snip);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});*/
	}
}
