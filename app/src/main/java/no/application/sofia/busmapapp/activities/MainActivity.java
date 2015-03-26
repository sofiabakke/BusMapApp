package no.application.sofia.busmapapp.activities;


import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;

import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.fragments.MapFragment;


public class MainActivity extends ActionBarActivity {

    //used to save the fragments when they are created
    private MapFragment mapFragment;


    public void sendQuery(Editable editable){
        String query = editable.toString();
        mapFragment.searchRouteByName(query);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(mapFragment == null)
            mapFragment = MapFragment.newInstance(1);
        mapFragment.setFromNavDrawer(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mapFragment)
                .commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            return true;

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if(mapFragment.mKeyboard.isCustomKeyboard())
            mapFragment.mKeyboard.hideCustomKeyboard();
        else
            super.onBackPressed();
    }



}
