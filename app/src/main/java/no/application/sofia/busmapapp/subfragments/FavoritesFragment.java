package no.application.sofia.busmapapp.subfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.databasehelpers.Stop;
import no.application.sofia.busmapapp.databasehelpers.StopsDbHelper;
import no.application.sofia.busmapapp.interfaces.OnLatLngClickedListener;
import no.application.sofia.busmapapp.interfaces.OnStopItemClickedListener;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link //OnFragmentInteractionListener}
 * interface.
 *
 * Needed to comment out the OnFragmentInteractionListener in order to run the application for now.
 * Think this listener should be used when actual content are implemented
 */
public class FavoritesFragment extends Fragment implements AbsListView.OnItemClickListener {
    private static final String ARG_TAB_NUMBER = "tab_number";

    private OnStopItemClickedListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView stopListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    public static StopAdapter adapter;
    public static StopsDbHelper db;
    public static List<Stop> list;
    private OnLatLngClickedListener latLngListener;


    public static FavoritesFragment newInstance(int tabNumber) {
        FavoritesFragment fragment = new FavoritesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_NUMBER, tabNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavoritesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new StopsDbHelper(this.getActivity());
        list = db.getAllStops();
        adapter = new StopAdapter(this.getActivity(), R.layout.favorite_inner_view, list);

//        // TODO: Change Adapter to display your content
//        mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
//                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites_item_list, container, false);
        view.setTag("favoritesFragment");

        // Set the adapter
        stopListView = (AbsListView) view.findViewById(R.id.list_favorite);
        stopListView.setAdapter(adapter);

        // Set OnItemClickedListener so we can be notified on item clicks
        stopListView.setOnItemClickListener(this);

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnStopItemClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnStopItemClickedListener");
        }
        try {
            latLngListener = (OnLatLngClickedListener) activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement OnLatLngClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        latLngListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            mListener.onStopItemClicked(list.get(position));
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = stopListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }



    //The adapter class making it possible to display stops from the database
    public class StopAdapter extends ArrayAdapter<Stop>{

        Context context;
        List<Stop> stopList = new ArrayList<>();
        int layoutResourceId;
        View[] viewElements;

        public StopAdapter(Context context, int layoutResourceId, List<Stop> objects){
            super(context, layoutResourceId, objects);
            this.context = context;
            this.layoutResourceId = layoutResourceId;
            this.stopList = objects;
            viewElements = new View[3];
        }

        //Used to create a custom view in the list
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckBox checkBox;
            TextView stopName;
            final TextView latLng;
            if (convertView == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.favorite_inner_view, parent, false);

                stopName = (TextView) convertView.findViewById(R.id.text_stop_name);
                viewElements[0] = stopName;

                final Activity onClickActivity = getActivity();
                checkBox = (CheckBox)convertView.findViewById(R.id.checkbox_favorite);
                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        Stop changeStop = (Stop) cb.getTag();
                        changeStop.setFavorite(cb.isChecked()?1:0);
                        FavoritesFragment.db.updateStop(changeStop);

                        if(cb.isChecked())
                            Toast.makeText(onClickActivity, "Made " + changeStop.getName() + " a favorite", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(onClickActivity, "Removed " + changeStop.getName() + " from favorite", Toast.LENGTH_LONG).show();
                    }
                });
                viewElements[1] = checkBox;

                latLng = (TextView)convertView.findViewById(R.id.textview_latlng);
                latLng.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView tv = (TextView) v;
                        latLngListener.onLatLngClicked((Stop)(tv.getTag()));
                    }
                });
                viewElements[2] = latLng;
            }
            else {
                stopName = (TextView)viewElements[0];
                checkBox = (CheckBox)viewElements[1];
                latLng = (TextView)viewElements[2];
            }

            Stop current = stopList.get(position);
            stopName.setText(current.getName());
            latLng.setText("Lat: " + current.getLat() + "\nLng: " + current.getLng());
            checkBox.setChecked(current.getFavorite() == 1);
            checkBox.setTag(current);
            latLng.setTag(current);
            return convertView;
        }


        public List<Stop> getStopList(){
            return stopList;
        }
    }
}
