package no.application.sofia.busmapapp.subfragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.dummy.DummyContent;
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
    private int tabNumber;

    private OnStopItemClickedListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;


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

        if (getArguments() != null) {
            tabNumber = getArguments().getInt(ARG_TAB_NUMBER);
        }

        // TODO: Change Adapter to display your content
        mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);

        Log.d("onCreate", "In FavoritesFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        view.setTag("favoritesFragment");

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        // Set OnItemClickedListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        Log.d("onCreateView", "In FavoritesFragment");
        Log.d("mAdapter", mAdapter + "");

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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("OnDestroyView", "FavoritesFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("onDetach", "In FavoritesFragment");
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            mListener.onStopItemClicked(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public int getTabNumber(){
        return tabNumber;
    }
}
