package no.application.sofia.busmapapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import no.application.sofia.busmapapp.R;
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
    private String submittedString = "";
    private TextView answerTextView;

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

        final EditText textQuestion = (EditText) view.findViewById(R.id.texfield_question);
        answerTextView = (TextView) view.findViewById(R.id.textView_answer);

        textQuestion.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH){
                    Log.d("Search", "Registered!");
                    submittedString = textQuestion.getText().toString();
                    startThread();
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });

        //Used to clear the answer TextView when the user wants to ask another question
        textQuestion.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                answerTextView.setText("");
                return false;
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

    private void startThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Submitted String", submittedString);
                if (!submittedString.equals("") && submittedString != null)
                    sendQuery(submittedString);
            }
        }).start();
    }


    //need to have this outside of method to work inside new runable
    String busstuc = "nothing";

    private void sendQuery(final String query){
        Log.d("Sending query", "sending query");
        final String url = "http://vm-6114.idi.ntnu.no:9001/search";
        String answer = sendPostRequest(url, query);
        try {
            JSONObject json = new JSONObject(answer);
            busstuc = json.getString("busstuc");
            Log.d("BUSSTUC", busstuc);
        }catch (Exception e){
            e.printStackTrace();
        }

        //can only change views on ui thread
        final String cleanString = html2text(busstuc);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                answerTextView.setText(cleanString);
            }
        });

    }

    // Downloads JSON from a given URL with a query as entity
    private String sendPostRequest(String URL, String query){
        StringBuilder builder = new StringBuilder();
        int TIMEOUT = 10000;
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);
        HttpClient client = new DefaultHttpClient(httpParams);
        HttpPost httpPost = new HttpPost(URL);

        try {
            httpPost.setEntity(new ByteArrayEntity(("{\"query\":\""+query+"\"}").getBytes("UTF8")));
            HttpResponse response = client.execute(httpPost);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(OracleFragment.class.getName(), "Failed to download file. Status code: " + statusCode);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("Builder", builder.toString());
        return builder.toString();
    }

    /**
     * Used to remove html tags in text strings using Jsoup, an external library
     * @param html the text string containing html tags
     * @return the same text string without html tags
     */
    public String html2text(String html){
        return Jsoup.parse(html).text();
    }

}