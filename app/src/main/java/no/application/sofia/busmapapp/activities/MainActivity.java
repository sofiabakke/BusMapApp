package no.application.sofia.busmapapp.activities;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.fragments.MapFragment;
import no.application.sofia.busmapapp.fragments.NavigationDrawerFragment;
import no.application.sofia.busmapapp.fragments.OracleFragment;
import no.application.sofia.busmapapp.interfaces.OnStopItemClickedListener;
import no.application.sofia.busmapapp.subfragments.StopFragment;
import no.application.sofia.busmapapp.fragments.StopsFragment;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, OnStopItemClickedListener{

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        CharSequence tempMTitle = mTitle;
        onSectionAttached(currentFragment.getArguments().getInt("section_number"));
        if (!tempMTitle.equals(mTitle))
            restoreActionBar(); //Need to change action bar when mTitle is changed
    }

    /*
        Used for Lists in FavoritesFragment and NearByFragment for stops
         */
    @Override
    public void onStopItemClicked(int id) {
        Log.d("Item Clicked", "Stop item clicked with ID: " + id);
        onStopItemAttached(id);
        restoreActionBar(); //Changeing the title in the action bar


    }

    //Used when an element is selected in a list.
    public void onStopItemAttached(int number){
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (number) {
            case 1:
                mTitle = "Item 1"; //These should be the name of the stop attached in the future
                fragmentManager.beginTransaction()
                        .replace(R.id.container, StopFragment.newInstance(number))
                        .addToBackStack(null)
                        .commit();
                break;
            case 2:
                mTitle = "Item 2";
                fragmentManager.beginTransaction()
                        .replace(R.id.container, StopFragment.newInstance(number))
                        .addToBackStack(null)
                        .commit();
                break;
            case 3:
                mTitle = "Item 3";
                fragmentManager.beginTransaction()
                        .replace(R.id.container, StopFragment.newInstance(number))
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }
}
