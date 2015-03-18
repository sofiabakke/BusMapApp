package no.application.sofia.busmapapp;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.IBinder;
import android.support.v7.internal.widget.TintEditText;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.SearchAutoComplete;

import no.application.sofia.busmapapp.activities.MainActivity;

/**
 * Created by Sofia on 17.03.15.
 */
public class CustomKeyboard {

    private KeyboardView mKeyboardView;
    private MainActivity mHostActivity;
    private MenuItem searchMenuItem;


    private OnKeyboardActionListener mOnKeyBoardActionListener = new OnKeyboardActionListener() {

        @Override
        public void onPress(int primaryCode) {

        }

        @Override
        public void onRelease(int primaryCode) {

        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            View focusCurrent = mHostActivity.getWindow().getCurrentFocus();
            Class currentClass = focusCurrent.getClass();
            Class searchClass = mHostActivity.findViewById(R.id.action_line_search).getClass();
            SearchView.SearchAutoComplete sac = new SearchView.SearchAutoComplete(mHostActivity);
            Class searchAutoCompleteClass = sac.getClass();

            if( focusCurrent.getClass() == SearchView.SearchAutoComplete.class) {

                SearchAutoComplete searchView = (SearchAutoComplete) focusCurrent;
                Editable editable = searchView.getText();
                int start = searchView.getSelectionStart();
                if (primaryCode == Keyboard.KEYCODE_DONE) {
                    Log.d("onKey", "Done Clicked");
                    mHostActivity.sendQuery(editable);
                    hideCustomKeyboard();
                } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                    Log.d("onKey", "Deleting input");
                    if (editable != null && start > 0)
                        editable.delete(start - 1, start);
                } else {
                    Log.d("onKey", "Letter or number clicked");
                    editable.insert(start, Character.toString((char) primaryCode));
                }
            }
        }

        @Override
        public void onText(CharSequence text) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    };

    public CustomKeyboard(Activity host, int viewId, int layoutId){
        mHostActivity = (MainActivity)host;
        mKeyboardView = (KeyboardView)mHostActivity.findViewById(viewId);
        mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutId));
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setOnKeyboardActionListener(mOnKeyBoardActionListener);
    }

    public boolean isCustomKeyboard(){
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    public void showCustomKeyboard(View view){
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);

    }

    public void hideCustomKeyboard(){
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
        if (searchMenuItem != null)
            searchMenuItem.collapseActionView();
    }

    public void registerSearchView(SearchView searchView, final MenuItem searchMenuItem){
        SearchManager searchManager = (SearchManager) mHostActivity.getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(mHostActivity.getComponentName()));
        this.searchMenuItem = searchMenuItem;
        final IBinder searchToken = searchView.getWindowToken();
        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("Focus change", "hasFocus: " + hasFocus);

                if (hasFocus) {
                    showCustomKeyboard(v);
                    InputMethodManager imm = (InputMethodManager) mHostActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchToken, 0);
                }
                else
                    hideCustomKeyboard();

            }
        });

        //The searchview clicked after the it is already in focus
        searchView.setOnClickListener(onClickListener);

        //When the searc view is clicked before it is focused
        searchView.setOnSearchClickListener(onClickListener);

        searchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("Touch", event.getAction() + "");
                SearchView searchViewTemp = (SearchView) v;
                int inType = searchViewTemp.getInputType();         //backup the input type
                searchViewTemp.setInputType(InputType.TYPE_NULL);   //Disable standard keyboard
                searchViewTemp.onTouchEvent(event);
                searchViewTemp.setInputType(inType);
                InputMethodManager imm = (InputMethodManager) mHostActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchToken, 0);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchMenuItem.collapseActionView(); //collapsing the searchview after search
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        //Disable spell check
        searchView.setInputType(searchView.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("onClick", v.getLayerType()+"");
            showCustomKeyboard(v);
            InputMethodManager imm = (InputMethodManager) mHostActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    };

}
