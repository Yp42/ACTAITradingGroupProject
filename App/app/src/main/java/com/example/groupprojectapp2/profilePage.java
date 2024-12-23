package com.example.groupprojectapp2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;




public class profilePage extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    stockHandler StockHandler = stockHandler.getInstance();
    private String mParam1;
    private String mParam2;
    aiCode aicode = new aiCode();
    private FirebaseFirestore db;
    private EditText depositInput;

    public profilePage() {
        // Required empty public constructor
    }

    public static profilePage newInstance(String param1, String param2) {
        profilePage fragment = new profilePage();
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
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_page, container, false);



        TextView nameTextView = view.findViewById(R.id.text_balance);
        TextView stockTextView = view.findViewById(R.id.text_stocksowned);
        StockHandler.getInstance().displayStocks(stockTextView);

        StockHandler.displayBalance(nameTextView);



        // StockHandler.updateCryptosInFirestore("sol");





        depositInput = view.findViewById(R.id.deposit_input);
        Button saveDepositButton = view.findViewById(R.id.deposit_btn);
        Button withdrawButton = view.findViewById(R.id.withdraw_btn);

        // Handle Save / Deposit button
        saveDepositButton.setOnClickListener(v -> {
            String depositAmount = depositInput.getText().toString().trim();
            if (!depositAmount.isEmpty()) {
                    aicode.log(requireContext());
                Log.i("Firestore", "Retrieved json Content: " + aicode.getJsonContent().toString());

                StockHandler.deposit(depositAmount,view);
                //TextView balanceText = view.findViewById(R.id.text_balance);
                //balanceText.setText(Float.toString(StockHandler.getBalance()));
            } else {
                Toast.makeText(getActivity(), "Please enter a deposit amount", Toast.LENGTH_SHORT).show();
            }
        });

        // handle withdraw button
        withdrawButton.setOnClickListener(v -> {
            String withdrawAmount = depositInput.getText().toString().trim();
            if (!withdrawAmount.isEmpty()) {
                StockHandler.withdraw(withdrawAmount,view);

            } else {
                Toast.makeText(getActivity(), "Please enter a withdrawal amount", Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.btn_alert).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_profilePage_to_alertPage));
        view.findViewById(R.id.btn_ratings).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_profilePage_to_ratePage));
        view.findViewById(R.id.btn_help).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_profilePage_to_helpPage));
        view.findViewById(R.id.btn_ai).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_profilePage_to_aiPage));
        view.findViewById(R.id.btn_invest).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_profilePage_to_cryptoPage));
        return view;
    }


}
