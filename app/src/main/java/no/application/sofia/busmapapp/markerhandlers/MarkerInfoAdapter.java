package no.application.sofia.busmapapp.markerhandlers;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import no.application.sofia.busmapapp.R;

/**
 * Created by Sofia on 20.03.15.
 */
public class MarkerInfoAdapter implements InfoWindowAdapter {
    private View mSnippet;
    private LayoutInflater inflater;

    public MarkerInfoAdapter(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getInfoContents(Marker marker) {
        if (mSnippet == null)
            mSnippet = inflater.inflate(R.layout.snippet, null);
        TextView tv = (TextView) mSnippet.findViewById(R.id.textview_title);
        tv.setText(marker.getTitle());
        tv = (TextView)mSnippet.findViewById(R.id.textview_snippet);
        tv.setText(marker.getSnippet());
        return mSnippet;
    }
}
