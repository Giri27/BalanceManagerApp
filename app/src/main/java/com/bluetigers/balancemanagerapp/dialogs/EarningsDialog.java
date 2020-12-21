package com.bluetigers.balancemanagerapp.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
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
                .setTitle("Add earnings")
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.v(TAG, "dialog: operation canceled!");
                })
                .setPositiveButton("Add", (dialog, which) -> {
                    float amout = Float.parseFloat(earningsTxt.getText().toString());

                    Date date = null;

                    String dateTxt = datePicker.getDayOfMonth() + "/" + datePicker.getMonth() + "/" + datePicker.getYear();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        date = new Date(Objects.requireNonNull(dateFormat.parse(dateTxt)).getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    listener.applyTexts(amout, date);
                });

        earningsTxt = view.findViewById(R.id.dialog_earnings_add);
        datePicker = view.findViewById(R.id.dialog_earnings_date);

        return builder.create();
    }

    public interface EarningsDialogListener {
        void applyTexts(float amount, Date date);
    }
}
