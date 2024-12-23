package com.example.groupprojectapp2;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link profileTestPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class profileTestPage extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Model model = Model.getInstance();
    private String mParam1;
    private String mParam2;
    stockHandler StockHandler = stockHandler.getInstance();
    stockInfoPageHandler StockInfoPageHandler = stockInfoPageHandler.getInstance();

    public profileTestPage() {
        // Required empty public constructor
    }

    public static profileTestPage newInstance(String param1, String param2) {
        profileTestPage fragment = new profileTestPage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_test_page, container, false);


        TextView textProfile = view.findViewById(R.id.text_profile);


        textProfile.setText("Fund admin : " + model.getCurrentUser());

        // Scrollable add items like jetpack compose
        LinearLayout dynamicContainer = view.findViewById(R.id.dynamic_container);


        StockHandler.fetchCryptosAndStocksOwned().addOnSuccessListener(cryptosOwned -> {
            Log.i("firebase", "Fetched cryptos: " + cryptosOwned.toString());
            // Add items based on owned stocks/assets
            for (String item : cryptosOwned) {
                //todo Make quanitiy/output look nice
                String[] parts = item.split(",");
                String displayText = parts[0] + " - € : " + parts[1] + " - Quantity: " + parts[2];


                LinearLayout row = new LinearLayout(getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                row.setPadding(8, 8, 8, 8);


                TextView textView = new TextView(getContext());
                textView.setText(displayText);
                textView.setLayoutParams(new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f)); // Weight 1 to take remaining space
                textView.setTextSize(16);
                textView.setTextColor(Color.WHITE);


                row.addView(textView);


                dynamicContainer.addView(row);
            }
        }).addOnFailureListener(e -> {
            Log.e("firebase", "Error fetching cryptos: " + e.getMessage());
        });

        navButtons(view);
        return view;
    }


    private void navButtons(View view) {
        view.findViewById(R.id.btn_alert).setOnClickListener(v -> {
            Log.i("firebase", "Navigating to alert page");
            Navigation.findNavController(view).navigate(R.id.action_profileTestPage_to_alertPage);
        });
        view.findViewById(R.id.btn_ratings).setOnClickListener(v -> {
            Log.i("firebase", "Navigating to ratings page");
            Navigation.findNavController(view).navigate(R.id.action_profileTestPage_to_ratePage);
        });
        view.findViewById(R.id.btn_help).setOnClickListener(v -> {
            Log.i("firebase", "Navigating to help page");
            Navigation.findNavController(view).navigate(R.id.action_profileTestPage_to_helpPage);
        });
        view.findViewById(R.id.btn_ai).setOnClickListener(v -> {
            Log.i("firebase", "Navigating to AI page");
            Navigation.findNavController(view).navigate(R.id.action_profileTestPage_to_aiPage);
        });
        view.findViewById(R.id.btn_invest).setOnClickListener(v -> {
            Log.i("firebase", "Navigating to profile page");
            Navigation.findNavController(view).navigate(R.id.action_profileTestPage_to_cryptoPage);
        });
    }

}
