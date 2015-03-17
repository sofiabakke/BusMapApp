package no.application.sofia.busmapapp;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.support.v7.widget.SearchView;

import no.application.sofia.busmapapp.activities.MainActivity;

/**
 * Created by Sofia on 17.03.15.
 */
public class CustomKeyboard {

    private KeyboardView mKeyboardView;
    private Activity mHostActivity;

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
            if( focusCurrent==null || focusCurrent.getClass()!=EditText.class ) return;
            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();
            if (primaryCode == Keyboard.KEYCODE_DONE){

            }
            else if (primaryCode == Keyboard.KEYCODE_DELETE){
                if( editable!=null && start>0 )
                    editable.delete(start - 1, start);
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
        mHostActivity = host;
        mKeyboardView = (KeyboardView)mHostActivity.findViewById(viewId);
        mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutId));
        mKeyboardView.setPreviewEnabled(false);
        mKeyboardView.setOnKeyboardActionListener(mOnKeyBoardActionListener);
        mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public boolean isCustomKeyboard(){
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    public void showCustomKeyboard(View view){
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        if (view != null)
            ((InputMethodManager) mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void hideCustomKeyboard(){
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }

    public void registerSearchView(SearchView searchView){
        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    showCustomKeyboard(v);
                else
                    hideCustomKeyboard();

            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomKeyboard(v);
            }
        });

        searchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                SearchView searchViewTemp = (SearchView)v;
                int inType = searchViewTemp.getInputType();         //backup the input type
                searchViewTemp.setInputType(InputType.TYPE_NULL);   //Disable standard keyboard
                searchViewTemp.onTouchEvent(event);
                searchViewTemp.setInputType(inType);
                return true;
            }
        });
        //Disable spell check
        searchView.setInputType(searchView.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

    }

}
