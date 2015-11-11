package org.lennartb.gtlaptimer.Helpers;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import org.lennartb.gtlaptimer.R;

/**
 *
 */
public class DialogCreator extends DialogFragment implements View.OnClickListener {

    public DialogCreator() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getArguments().getSerializable("DialogType") == DIALOG_TYPE.REVERSE_DIALOG) {
            View view = inflater.inflate(R.layout.reverse_dialog_fragment, container);
            Button normButton = (Button) view.findViewById(R.id.normalDirectionButton);
            Button revButton = (Button) view.findViewById(R.id.reverseDirectionButton);
            normButton.setOnClickListener(this);
            revButton.setOnClickListener(this);
            getDialog().setTitle("Reverse?");
            return view;
        }

        if (getArguments().getSerializable("DialogType") == DIALOG_TYPE.ABOUT_DIALOG) {
            View view = inflater.inflate(R.layout.about_dialog_fragment, container);
            Button okButton = (Button) view.findViewById(R.id.aboutOkButton);
            okButton.setOnClickListener(this);
            getDialog().setTitle("About GTLapTimer");
            return view;
        }

        return null;
    }

    @Override
    public void onClick(View v) {

        DialogCreatorListener activity = (DialogCreatorListener) getActivity();
        switch (v.getId()) {
            case R.id.normalDirectionButton:
                activity.onFinishDialog(false);
                break;
            case R.id.reverseDirectionButton:
                activity.onFinishDialog(true);
                break;
            case R.id.aboutOkButton:
                // No return values required, the dialog is just closed.
                break;
        }
        dismiss();
    }

    public enum DIALOG_TYPE {
        REVERSE_DIALOG,
        ABOUT_DIALOG
    }

    public interface DialogCreatorListener {
        void onFinishDialog(boolean result);
    }
}