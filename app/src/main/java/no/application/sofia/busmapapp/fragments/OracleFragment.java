package no.application.sofia.busmapapp.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import no.application.sofia.busmapapp.R;
import no.application.sofia.busmapapp.activities.MainActivity;
import no.application.sofia.busmapapp.interfaces.OnMenuItemClickedListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link //OracleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OracleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OracleFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private OnMenuItemClickedListener mListener;

//    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param sectionNumber the number in the navigation drawer
     * @return A new instance of fragment OracleFragment.
     */
    public static OracleFragment newInstance(int sectionNumber) {
        OracleFragment fragment = new OracleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public OracleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_oracle, container, false);

        final Button submitButton = (Button) view.findViewById(R.id.button_submit);
        final EditText textQuestion = (EditText) view.findViewById(R.id.texfield_question);
        final TextView answer = (TextView) view.findViewById(R.id.textView_answer);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer.setText(textQuestion.getText());
            }
        });

        setHasOptionsMenu(true);
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_oracle, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_map){
            if (mListener != null)
                mListener.menuItemSelected(id);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMenuItemClickedListener) activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement OnMenuItemClickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

}