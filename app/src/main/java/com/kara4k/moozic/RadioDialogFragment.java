package com.kara4k.moozic;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class RadioDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {


    public static final String NAME = "name";
    public static final String DESC = "description";
    public static final String PATH = "path";
    public static final String POSITION = "position";


    private EditText mNameEditText;
    private EditText mDescEditText;
    private EditText mPathEditText;
    private int mPosition = -1;


    public static RadioDialogFragment newInstance() {
        return new RadioDialogFragment();
    }

    public static RadioDialogFragment newInstance(String radioName, String radioDesc, String radioPath, int position) {
        Bundle args = new Bundle();
        args.putString(NAME, radioName);
        args.putString(DESC, radioDesc);
        args.putString(PATH, radioPath);
        args.putInt(POSITION, position);
        RadioDialogFragment fragment = new RadioDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_fragment_radio, null);
        mNameEditText = (EditText) view.findViewById(R.id.radio_name_edit_text);
        mDescEditText = (EditText) view.findViewById(R.id.radio_desc_edit_text);
        mPathEditText = (EditText) view.findViewById(R.id.radio_path_edit_text);

        if (getArguments() != null) {
            Bundle arguments = getArguments();
            mNameEditText.setText(arguments.getString(NAME));
            mDescEditText.setText(arguments.getString(DESC));
            mPathEditText.setText(arguments.getString(PATH));
            mPosition = arguments.getInt(POSITION);
        }

        return new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle(R.string.dialog_add_radio_title)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        Intent intent = new Intent();
        intent.putExtra(NAME, mNameEditText.getText().toString());
        intent.putExtra(DESC, mDescEditText.getText().toString());
        intent.putExtra(PATH, mPathEditText.getText().toString());
        intent.putExtra(POSITION, mPosition);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }
}
