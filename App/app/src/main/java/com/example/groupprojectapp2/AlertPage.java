package com.example.groupprojectapp2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlertPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlertPage extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    stockHandler StockHandler = stockHandler.getInstance();
    public AlertPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AlertPage.
     */
    // TODO: Rename and change types and number of parameters
    public static AlertPage newInstance(String param1, String param2) {
        AlertPage fragment = new AlertPage();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alert_page, container, false);
        StockHandler.compareStockPrices(new stockHandler.OnPriceChangeListener() {
            @Override
            public void onPriceChange(String priceChanges) {
                // Replace commas with newlines
                String formattedPriceChanges = priceChanges.replace(",", "\n");

                // Find the TextView and display the formatted string
                TextView priceTextView = getActivity().findViewById(R.id.alert_text); // Replace with your TextView ID
                priceTextView.setText(formattedPriceChanges);
            }
        });


        view.findViewById(R.id.btn_invest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_alertPage_to_cryptoPage);
            }
        });

        view.findViewById(R.id.btn_ratings).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_alertPage_to_ratePage);
            }
        });



        view.findViewById(R.id.btn_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_alertPage_to_helpPage);
            }
        });

        view.findViewById(R.id.btn_ai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_alertPage_to_aiPage);
            }
        });

        view.findViewById(R.id.btn_ratings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_alertPage_to_ratePage);
            }
        });
        view.findViewById(R.id.btn_profile).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_alertPage_to_profileTestPage2));


        return view;
    }
}