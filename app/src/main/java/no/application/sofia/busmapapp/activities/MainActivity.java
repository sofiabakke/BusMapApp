package no.application.sofia.busmapapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.WindowManager;
import android.widget.Toast;

import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.databasehelpers.Stop;
import no.application.sofia.busmapapp.fragments.MapFragment;
import no.application.sofia.busmapapp.fragments.NavigationDrawerFragment;
import no.application.sofia.busmapapp.fragments.OracleFragment;
import no.application.sofia.busmapapp.interfaces.OnLatLngClickedListener;
import no.application.sofia.busmapapp.interfaces.OnStopItemClickedListener;
import no.application.sofia.busmapapp.subfragments.FavoritesFragment;
import no.application.sofia.busmapapp.subfragments.StopFragment;
import no.application.sofia.busmapapp.fragments.StopsFragment;
import no.application.sofia.busmapapp.temporaryClasses.AddStopDialogFragment;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, OnStopItemClickedListener, AddStopDialogFragment.NoticeDialogListener, OnLatLngClickedListener{

    //used to save the fragments when they are created
    private MapFragment mapFragment;
    private StopsFragment stopsFragment;
    private OracleFragment oracleFragment;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    public void sendQuery(Editable editable){
        String query = editable.toString();
        mapFragment.searchRouteByName(query);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mapFragment != null)
            mapFragment.decideIfAddLinesToLocalDb();
    }

    /*
        Code for the navigation drawer
         */
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch(position){
            case 0:
                if(mapFragment == null)
                    mapFragment = MapFragment.newInstance(position+1);
                mapFragment.setFromNavDrawer(true);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mapFragment)
                        .commit();
                break;
            case 1:
                if(stopsFragment == null)
                    stopsFragment = StopsFragment.newInstance(position+1);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, stopsFragment)
                        .commit();
                break;
            case 2:
                if(oracleFragment == null)
                    oracleFragment = OracleFragment.newInstance(position+1);
                fragmentManager.beginTransaction()
                        .replace(R.id.container, oracleFragment)
                        .commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_map);
                break;
            case 2:
                mTitle = getString(R.string.title_stops);
                break;
            case 3:
                mTitle = getString(R.string.title_oracle);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
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

        if(id == R.id.action_add_stop)
            showDialog();

        if(id == R.id.action_add_lines)
            mapFragment.decideIfAddLinesToLocalDb();
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        CharSequence tempMTitle = mTitle;
        onSectionAttached(currentFragment.getArguments().getInt("section_number"));
        if (!tempMTitle.equals(mTitle))
            restoreActionBar(); //Need to change action bar when mTitle is changed
        if(mapFragment.mKeyboard.isCustomKeyboard())
            mapFragment.mKeyboard.hideCustomKeyboard();
        else
            super.onBackPressed();
    }

    /*
        Used for Lists in FavoritesFragment and NearByFragment for stops
         */
    @Override
    public void onStopItemClicked(Stop stop) {
        onStopItemAttached(stop);
        restoreActionBar(); //Changing the title in the action bar
    }

    //Used when an element is selected in a list.
    public void onStopItemAttached(Stop stop){
        FragmentManager fragmentManager = getSupportFragmentManager();
        mTitle = stop.getName();
        fragmentManager.beginTransaction()
                .replace(R.id.container, StopFragment.newInstance(stop.getId()))
                .addToBackStack(null)
                .commit();
    }

    public void showDialog(){
        DialogFragment dialogFragment = new AddStopDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "addStop");
    }

    @Override
    public void onDialogPositiveClick(int entryId, String name, double lat, double lng) {
        if (FavoritesFragment.db != null && FavoritesFragment.adapter != null) {
            Stop stop = new Stop(entryId, name, lat, lng);
            FavoritesFragment.db.addStop(stop);
            FavoritesFragment.adapter.add(stop);
            FavoritesFragment.adapter.notifyDataSetChanged();

        }
        else{
            Toast.makeText(this, "Need To open Stops view first", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLatLngClicked(Stop stop) {
        FragmentManager fm = getSupportFragmentManager();

        if (mapFragment == null)
            mapFragment = MapFragment.newInstance(stop.getId());
        mapFragment.setFromNavDrawer(false);
        mapFragment.setMyLocation(stop.getLat(), stop.getLng());
        fm.beginTransaction().replace(R.id.container, mapFragment).addToBackStack(null).commit();

    }

}
