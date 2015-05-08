package no.application.sofia.busmapapp.markerhandlers;

import org.json.JSONArray;


/**
 * Created by oknak_000 on 18.03.2015.
 */
public interface RouteMarkerHandler {

	void stopUpdateThread();
    void restartUpdateThread();
    void addRouteMarkers(String route);
    void updateOneStop(int lineID, int busStopID);
    void updateAllStops(int lineID);
    JSONArray getBusLinesByOperator(String operator);

}