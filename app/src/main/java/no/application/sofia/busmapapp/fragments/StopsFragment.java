package no.application.sofia.busmapapp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.util.Locale;

import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.activities.MainActivity;
import no.application.sofia.busmapapp.subfragments.FavoritesFragment;
import no.application.sofia.busmapapp.subfragments.NearByFragment;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StopsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StopsFragment extends Fragment implements ActionBar.TabListener{
    private static final String ARG_SECTION_NUMBER = "section_number";
    private int sectionNumber;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    private FavoritesFragment favFrag;
    private NearByFragment nbFrag;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param sectionNumber sets the section number
     * @return A new instance of fragment StopsFragment.
     */
    public static StopsFragment newInstance(int sectionNumber) {
        StopsFragment fragment = new StopsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public StopsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        sectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stops, container, false);
        Log.d("onCreateView", "In StopsFragment");

        viewPager = (ViewPager)view.findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        Button favoriteButton = (Button)view.findViewById(R.id.button_favorite);
        Button nearbyButton = (Button)view.findViewById(R.id.button_nearby);

        setupButton(favoriteButton);
        setupButton(nearbyButton);
        return view;
    }

    public void setupButton(final Button button){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Click", button.getText() + "");
                if (button.getTag().equals("favorite"))
                    viewPager.setCurrentItem(0);
                else if (button.getTag().equals("nearby"))
                    viewPager.setCurrentItem(1);
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER)); //Setting the Action Bar text
        Log.d("onAttach", "In StopsFragment");
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("onDetach", "In StopFragment");
        if (favFrag != null)
            favFrag.onDetach();
        if(nbFrag != null){
            nbFrag.onDetach();
        }


    }

    public void setFavFrag(FavoritesFragment favFrag){
        this.favFrag = favFrag;
    }

    public void setNbFrag(NearByFragment nbFrag){
        this.nbFrag = nbFrag;
    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 0:
                    FavoritesFragment favoriteFragment = FavoritesFragment.newInstance(position + 1);
                    setFavFrag(favoriteFragment);
                    return favoriteFragment;
                case 1:
                    NearByFragment nearByFragment = NearByFragment.newInstance(position + 1);
                    setNbFrag(nearByFragment);
                    return nearByFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section_favorites).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section_nearby).toUpperCase(l);
            }
            return null;
        }
    }

}
