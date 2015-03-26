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
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.SearchAutoComplete;

import no.application.sofia.busmapapp.activities.MainActivity;

/**
 * Created by Sofia on 17.03.15.
 * This class handles a custom keyboard. The keyboard is used when the user wants to search for stops
 * It is a reduced keyboard including only necessary buttons to search for every line provided by Ruter.
 * If another provider is implemented this keyboard might be too big.
 */
public class CustomKeyboard {

    private KeyboardView mKeyboardView;
    private MainActivity mHostActivity;
    private View mCurrentView;

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

            if( focusCurrent.getClass() == EditText.class) {
                EditText editText = (EditText) focusCurrent;
                Editable editable = editText.getText();
                int start = editText.getSelectionStart();
                if (primaryCode == Keyboard.KEYCODE_DONE) {
                    mHostActivity.sendQuery(editable);
                    hideCustomKeyboard();
                }
                else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                    if (editable != null && start > 0)
                        editable.delete(start - 1, start);
                }
                else {
                    editable.insert(start, Character.toString((char) primaryCode));
                }
            }
            else if (focusCurrent.getClass() == TintEditText.class){
                TintEditText editText = (TintEditText) focusCurrent;
                Editable editable = editText.getText();
                int start = editText.getSelectionStart();
                if (primaryCode == Keyboard.KEYCODE_DONE) {
                    mHostActivity.sendQuery(editable);
                    hideCustomKeyboard();
                }
                else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                    if (editable != null && start > 0)
                        editable.delete(start - 1, start);
                }
                else {
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

    public CustomKeyboard(Activity host, View currentView, int viewId, int layoutId){
        mHostActivity = (MainActivity)host; //Saving the host activity for later
        mCurrentView = currentView;
        mKeyboardView = (KeyboardView)mCurrentView.findViewById(viewId); //Getting the reference for the KeyboardView
        mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutId)); //Setting the Keyboard for the KeyboardView
        mKeyboardView.setPreviewEnabled(false); //making the keyboard not show the preview balloons every time a key is tapped
        mKeyboardView.setOnKeyboardActionListener(mOnKeyBoardActionListener); //Setting the listener. Defined above this constructor
        mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); //Hide the standard keyboard initially
    }

    public boolean isCustomKeyboard(){
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    public void showCustomKeyboard(View view){
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mHostActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void hideCustomKeyboard(){
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }

    public void registerEditText(int resid) {
        // Find the EditText 'resid'
        final EditText editText= (EditText)mCurrentView.findViewById(resid);
        // Make the custom keyboard appear
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                editText.setText("");
                if (hasFocus) showCustomKeyboard(v);
                else hideCustomKeyboard();
            }
        });
        editText.setOnClickListener(new View.OnClickListener() {
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            @Override
            public void onClick(View v) {
                showCustomKeyboard(v);
            }
        });
        // Disable standard keyboard hard way
        // NOTE There is also an easy way: 'editText.setInputType(InputType.TYPE_NULL)' (but you will not have a cursor, and no 'editText.setCursorVisible(true)' doesn't work )
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                EditText editText = (EditText) v;
                editText.setText("");
                int inType = editText.getInputType();       // Backup the input type
                editText.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                editText.onTouchEvent(event);               // Call native handler
                editText.setInputType(inType);              // Restore input type
                return true; // Consume touch event
            }
        });
        // Disable spell check (hex strings look like words to Android)
        editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

}
