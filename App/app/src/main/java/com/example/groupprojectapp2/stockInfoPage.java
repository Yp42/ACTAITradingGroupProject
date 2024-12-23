package com.example.groupprojectapp2;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link stockInfoPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class stockInfoPage extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private EditText depositInput;
    private String mParam1;
    private String mParam2;
    Model model = Model.getInstance();
    stockHandler StockHandler = stockHandler.getInstance();
    stockInfoPageHandler StockInfoPageHandler = stockInfoPageHandler.getInstance();

    public stockInfoPage() {
        // Required empty public constructor
    }

    public static stockInfoPage newInstance(String param1, String param2) {
        stockInfoPage fragment = new stockInfoPage();
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

        View view = inflater.inflate(R.layout.fragment_stock_info_page, container, false);
        TextView stockTextView = view.findViewById(R.id.rating_text);
        stockTextView.setText(StockInfoPageHandler.getAssetName());

        TextView priceTextView = view.findViewById(R.id.market_price);
        double currentMarketPrice = StockInfoPageHandler.getPrice(model.getCurrentstock());

        TextView descriptionTextView = view.findViewById(R.id.stock_info);
        descriptionTextView.setText(StockInfoPageHandler.getDescription());

        priceTextView.setText("Current Market Price : "+String.valueOf(currentMarketPrice));

        depositInput = view.findViewById(R.id.amount_input);



        TextView balance = view.findViewById(R.id.text_balance);
        try {
            float balanceValue = StockHandler.getBalance();

            if (balanceValue > 0) {
                balance.setText("Balance: €" + String.format("%.2f", balanceValue));
            } else {
                balance.setText("Balance: €0.00");
            }
        } catch (Exception e) {
            Log.e("Firestore", "Error retrieving balance", e);
            StockHandler.depositer("0");
            balance.setText("Balance: €0.00");
        }


        view.findViewById(R.id.btn_buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numberInput = depositInput.getText().toString().trim();
                Log.i("firestore", "Numberinput " + numberInput);


                if (numberInput.contains(".")) {
                    String[] parts = numberInput.split("\\.");
                    if (parts.length > 1 && parts[1].length() > 2 || StockHandler.getBalance() < 0) {
                        Toast.makeText(v.getContext(), "Number cant have more than 2 decimal places", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                StockInfoPageHandler.buyStock(view, model.getCurrentstock());
                StockHandler.withdraw(numberInput, view);
            }

        });


        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_stockInfoPage_to_cryptoPage)
        );

        return view;
    }
}
