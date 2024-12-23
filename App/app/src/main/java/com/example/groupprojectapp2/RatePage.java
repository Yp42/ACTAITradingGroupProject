package com.example.groupprojectapp2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RatePage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RatePage extends Fragment {



    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;
    stockHandler StockHandler = stockHandler.getInstance();
    public RatePage() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RatePage.
     */

    public static RatePage newInstance(String param1, String param2) {
        RatePage fragment = new RatePage();
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

        View view = inflater.inflate(R.layout.fragment_rate_page, container, false);
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    // Submit text to firestore
                    EditText textInput = view.findViewById(R.id.edit_rating);
                    EditText subjectInput = view.findViewById(R.id.edit_review);


                    String text = textInput.getText().toString();
                    String subject = subjectInput.getText().toString();
                    StockHandler.leaveReview(subject,text);


                    Log.i("Firestore", text);
                }
                catch(Exception e){Log.e("Firestore",e.toString());}
            }
        });


        view.findViewById(R.id.btn_invest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_ratePage_to_cryptoPage);
            }
        });

        view.findViewById(R.id.btn_ai).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_ratePage_to_aiPage);
            }
        });



        view.findViewById(R.id.btn_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_ratePage_to_helpPage);
            }
        });

        view.findViewById(R.id.btn_alert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_ratePage_to_alertPage);
            }
        });

        view.findViewById(R.id.btn_invest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_ratePage_to_cryptoPage);
            }
        });
        view.findViewById(R.id.btn_profile).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_ratePage_to_profileTestPage2));


        return view;
    }
}