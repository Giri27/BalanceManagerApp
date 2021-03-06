package com.bluetigers.balancemanagerapp.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.bluetigers.balancemanagerapp.R;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class EarningsDialog extends AppCompatDialogFragment {

    private static final String TAG = "Earnings Dialog";

    private EditText earningsTxt;
    private EditText earningsDescription;

    private DatePicker datePicker;

    private EarningsDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (EarningsDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement Earnings Dialog Listner");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_earnings_dialog, null);

        builder.setView(view)
                .setNegativeButton("Cancel", (dialog, which) -> Log.v(TAG, "dialog: operation canceled!"))
                .setPositiveButton("Add", (dialog, which) -> {
                    float amout = Float.parseFloat(earningsTxt.getText().toString());

                    Date date = null;

                    int month = datePicker.getMonth() + 1;

                    String dateTxt = datePicker.getDayOfMonth() + "/" + month + "/" + datePicker.getYear();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        date = new Date(Objects.requireNonNull(dateFormat.parse(dateTxt)).getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    String description = earningsDescription.getText().toString();

                    if (description.isEmpty())
                        description = "Entrate";

                    listener.applyEarnings(amout, date, description);
                });

        earningsTxt = view.findViewById(R.id.dialog_earnings_add);
        datePicker = view.findViewById(R.id.dialog_earnings_date);
        earningsDescription = view.findViewById(R.id.dialog_earnings_description);

        return builder.create();
    }

    public interface EarningsDialogListener {
        void applyEarnings(float amount, Date date, String description);
    }
}
