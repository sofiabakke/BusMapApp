package no.application.sofia.busmapapp.activities;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.fragments.MapFragment;
import no.application.sofia.busmapapp.fragments.OracleFragment;
import no.application.sofia.busmapapp.interfaces.OnMenuItemClickedListener;


public class MainActivity extends ActionBarActivity implements OnMenuItemClickedListener{

    //used to save the fragments when they are created
    private MapFragment mapFragment;
    private OracleFragment oracleFragment;


    /**
     * sendQuery is used to send the text in the search field
     * above the map to the mapFragment. This method is needed
     * because the custom keyboard is not connected directly to the
     * mapFragment. It communicates with the activity.
     * @param editable The text input from the search field
     */
    public void sendQuery(Editable editable){
        String query = editable.toString();
        if (isNetworkAvailable())
            mapFragment.searchRouteByLineNumber(query);
        else
            Toast.makeText(this, "There is no internet connection", Toast.LENGTH_LONG).show();
    }

    /**
     * Checking if some sort of internet is available
     * @return true if there is internet available, false otherwise
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Standard method which starts the creates the activity and the initial fragment: mapFragment
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initial fragment transaction without adding it to the back stack
        //This is to support exiting the application without removing the fragment first.
        if(mapFragment == null)
            mapFragment = MapFragment.newInstance(1);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mapFragment)
                .commit();
    }


    /**
     * Creating the menu, or the actionbar with its items
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Deciding what to do when a menu item is selected by the user
     * @param item the item being tapped, or selected, by the user
     * @return
     */
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


    /**
     * Customizing what happens when the back button is pressed.
     * If the custom keyboard is showing, it should be hidden.
     * If not, the application should close.
     */
    @Override
    public void onBackPressed() {
        if(mapFragment.mKeyboard.isCustomKeyboard())
            mapFragment.mKeyboard.hideCustomKeyboard();
        else
            super.onBackPressed();
    }

    /**
     * Inflating the Oracle when the item in the menu is clicked
     */
    private void inflateOracle(){
        if (oracleFragment == null)
            oracleFragment = OracleFragment.newInstance(2);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.container, oracleFragment)
                .addToBackStack("oracle")
                .commit();
    }

    /**
     * The same method as inflateOracle, except for the Map instead
     */
    private void inflateMap(){
        if(mapFragment == null)
            mapFragment = MapFragment.newInstance(1);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mapFragment)
                .addToBackStack("map")
                .commit();
    }

    /**
     * The method called then some menu item is tapped.
     * The fragments communicate with it through the interface OnMenuItemClickedListener.
     * @param id the id of the item clicked
     */
    @Override
    public void menuItemSelected(int id) {
        if (id == R.id.action_oracle)
            inflateOracle();
        if (id == R.id.action_map)
            inflateMap();
    }
}
