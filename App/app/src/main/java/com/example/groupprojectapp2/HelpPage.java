package com.example.groupprojectapp2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;



import androidx.fragment.app.Fragment;

import androidx.navigation.Navigation;



public class HelpPage extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    stockHandler StockHandler = stockHandler.getInstance();
    public HelpPage() {

    }

    public static HelpPage newInstance(String param1, String param2) {
        HelpPage fragment = new HelpPage();
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
        View view = inflater.inflate(R.layout.fragment_help_page, container, false);

        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    //todo Stockhandler user id, For tracking
                   // Submit to stockhandler then database
                    EditText textInput = view.findViewById(R.id.input_text); // Replace with your TextInputEditText ID
                    EditText subjectInput = view.findViewById(R.id.subject_text); // Replace with your TextInputEditText ID


                    String text = textInput.getText().toString();
                    String subject = subjectInput.getText().toString();
                    StockHandler.leaveSupport(text,subject);


                    Log.i("Firestore", text);
                }
                catch(Exception e){Log.e("Firestore",e.toString());}
            }
        });


        view.findViewById(R.id.btn_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:123456798"));
                startActivity(intent);
            }
        });


        view.findViewById(R.id.btn_invest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_helpPage_to_cryptoPage);
            }
        });

        view.findViewById(R.id.btn_ratings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_helpPage_to_ratePage);
            }
        });

        view.findViewById(R.id.btn_ai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_helpPage_to_aiPage);
            }
        });

        view.findViewById(R.id.btn_alert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_helpPage_to_alertPage);
            }
        });
        view.findViewById(R.id.btn_profile).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_helpPage_to_profileTestPage2));

        return view;
    }
}
