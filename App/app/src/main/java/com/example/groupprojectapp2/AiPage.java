package com.example.groupprojectapp2;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AiPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AiPage extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    aiCode aicode = new aiCode();
    predictedFuturePrices predictedFuturePrices = new predictedFuturePrices();

    public AiPage() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AiPage.
     */
    //todo Dyanmic input editext
    public static AiPage newInstance(String param1, String param2) {
        AiPage fragment = new AiPage();
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

        View view = inflater.inflate(R.layout.fragment_ai_page, container, false);
        Button promptButton = view.findViewById(R.id.btn_prompt_response);


        view.findViewById(R.id.btn_invest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_aiPage_to_cryptoPage);
            }
        });

        view.findViewById(R.id.btn_ratings).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_aiPage_to_ratePage);
            }
        });



        view.findViewById(R.id.btn_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_aiPage_to_helpPage);
            }
        });

        view.findViewById(R.id.btn_alert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_aiPage_to_alertPage);
            }
        });

        view.findViewById(R.id.btn_ratings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_aiPage_to_ratePage);
            }
        });
        view.findViewById(R.id.btn_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_aiPage_to_profileTestPage3);
            }
        });

        TextView nameTextView = view.findViewById(R.id.large_paragraph_text);



        promptButton.setOnClickListener(v -> {
            EditText inputTextBox = view.findViewById(R.id.input_text_box);//Get input for prompt asset name eg btc, msft
            //inputTextBox.getText().toString().trim();
            aicode.log(requireContext());
            //nameTextView.setText(aicode.getJsonContent().toString());
            nameTextView.setText(inputTextBox.getText().toString());
            predictedFuturePrices.fetchAndReturnData(inputTextBox.getText().toString(), new predictedFuturePrices.Callback() {
                @Override
                public void onDataFetched(String result) {
                    // Set paragraph text
                    Log.i("ApiMachine", result);
                    nameTextView.setText(result);
                }
            });

        });



        return view;
    }
}