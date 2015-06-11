package no.application.sofia.busmapapp.markers;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by oknak_000 on 08.05.2015.
 */
public class StopMarkerSM extends StopMarker {
    public int stopID = 0;

    public StopMarkerSM(Marker marker, int stopID){
        super(marker);
        this.stopID = stopID;
    }
}
