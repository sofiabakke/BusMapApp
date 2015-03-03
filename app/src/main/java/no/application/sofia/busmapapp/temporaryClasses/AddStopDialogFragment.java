package no.application.sofia.busmapapp.temporaryClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import no.application.sofia.busmapapp.R;

public class AddStopDialogFragment extends DialogFragment {
    NoticeDialogListener dialogListener;

    public AddStopDialogFragment(){

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.fragment_add_stop_dialog, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);

        final Activity activity = getActivity();

        // Add action buttons
        builder.setPositiveButton(R.string.button_confirm_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String sEntryId = ((EditText)view.findViewById(R.id.editText_entryId)).getText().toString();
                String sName = ((EditText)view.findViewById(R.id.editText_name)).getText().toString();
                String sLat = ((EditText)view.findViewById(R.id.editText_lat)).getText().toString();
                String sLng = ((EditText)view.findViewById(R.id.editText_lng)).getText().toString();

                if (sEntryId.equalsIgnoreCase("") || sName.equalsIgnoreCase("") || sLat.equalsIgnoreCase("") || sLng.equalsIgnoreCase(""))
                    Toast.makeText(activity, "Some of the fields are empty", Toast.LENGTH_LONG).show();
                else{
                    int entryId = Integer.parseInt(sEntryId);
                    double lat = Double.parseDouble(sLat);
                    double lng = Double.parseDouble(sLng);
                    dialogListener.onDialogPositiveClick(entryId, sName, lat, lng);

                }
            }
        });
        builder.setNegativeButton(R.string.button_cancel_dialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        return builder.create();
    }


    /**
     * Needed to instantiate the listener
     * @param activity The current activity this fragment runs in
     */
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            dialogListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    public interface NoticeDialogListener {
        public void onDialogPositiveClick(int entryId, String name, double lat, double lng);
    }
}