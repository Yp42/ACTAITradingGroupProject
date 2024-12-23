package com.example.groupprojectapp2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class CryptoPage extends Fragment {
    predictedFuturePrices predictedFuturePrices = new predictedFuturePrices();
    private static final String TAG = "CryptoPage";
    private FirebaseFirestore dbRoot;
    Model model = Model.getInstance();
    stockHandler StockHandler = stockHandler.getInstance();
    aiCode aicode = new aiCode();
    stockInfoPageHandler StockInfoPageHandler = stockInfoPageHandler.getInstance();

    public CryptoPage() {
        // Empty constructor required for Fragment
    }

    public static CryptoPage newInstance(String param1, String param2) {
        CryptoPage fragment = new CryptoPage();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbRoot = FirebaseFirestore.getInstance();
        if (dbRoot == null) {
            Log.e(TAG, "Firestore initialization failed");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crypto_page, container, false);

        // Display balance text
        TextView nameTextView = view.findViewById(R.id.text_balance);
        StockHandler.displayBalance(nameTextView);

        // Set onClickListener for adding coins to Firestore
        setOnClickListener(view);

        // Navigation buttons
        setupNavigationButtons(view);

        return view;
    }

    private void setOnClickListener(View view) {
        view.findViewById(R.id.btn_btc).setOnClickListener(v -> {
            model.setCurrentstock("btc");
            navigateToStockInfoPage(view, "btc");
        });

        view.findViewById(R.id.btn_eth).setOnClickListener(v -> {
            model.setCurrentstock("eth");
            navigateToStockInfoPage(view, "eth");
        });

        view.findViewById(R.id.btn_sol).setOnClickListener(v -> {
            model.setCurrentstock("sol");
            navigateToStockInfoPage(view, "sol");
        });

        view.findViewById(R.id.btn_bcm).setOnClickListener(v -> {
            model.setCurrentstock("bcm");
            navigateToStockInfoPage(view, "bcm");
        });

        view.findViewById(R.id.btn_aapl).setOnClickListener(v -> {
            model.setCurrentstock("aapl");
            navigateToStockInfoPage(view, "aapl");
        });

        view.findViewById(R.id.btn_tsla).setOnClickListener(v -> {
            model.setCurrentstock("tsla");
            navigateToStockInfoPage(view, "tsla");
        });

        view.findViewById(R.id.btn_intl).setOnClickListener(v -> {
            model.setCurrentstock("intl");
            navigateToStockInfoPage(view, "intl");
        });

        view.findViewById(R.id.btn_amzn).setOnClickListener(v -> {
            model.setCurrentstock("amzn");
            navigateToStockInfoPage(view, "amzn");
        });

        view.findViewById(R.id.btn_amd).setOnClickListener(v -> {
            model.setCurrentstock("amd");
            navigateToStockInfoPage(view, "amd");
        });

        view.findViewById(R.id.btn_msft).setOnClickListener(v -> {
            model.setCurrentstock("msft");
            navigateToStockInfoPage(view, "msft");
        });

        view.findViewById(R.id.btn_facebook).setOnClickListener(v -> {
            model.setCurrentstock("meta");
            navigateToStockInfoPage(view, "meta");
        });

        view.findViewById(R.id.btn_goog).setOnClickListener(v -> {
            model.setCurrentstock("goog");
            navigateToStockInfoPage(view, "goog");
        });
        view.findViewById(R.id.btn_nvda).setOnClickListener(v -> {
            model.setCurrentstock("nvda");
            navigateToStockInfoPage(view, "nvda");
        });
        view.findViewById(R.id.btn_dice).setOnClickListener(v -> {
                model.setCurrentstock("dice");
                //navigateToStockInfoPage(view, "dice");

                Toast.makeText(getActivity(), "Bough random stock : "+StockHandler.stockGambler(), Toast.LENGTH_SHORT).show();
                StockHandler.printPricelist();
                StockHandler.compareStockPrices(new stockHandler.OnPriceChangeListener() {
                    @Override
                    public void onPriceChange(String priceChanges) {
                        // Handle the result here

                        Log.i("Firestore", "Price changes:\n" + priceChanges);

                        // Split the string into a list using commas as the delimiter
                        String[] priceArray = priceChanges.split(",");

                        // Show each item in a Toast
                        for (String price : priceArray) {
                            // Trim spaces and show the price in a Toast
                            Toast.makeText(getActivity(), price.trim(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            //predictedFuturePrices apiMachine = new predictedFuturePrices();
            //apiMachine.fetchAndDisplayData("btc");

            //Log.i("Firestore",predictedFuturePrices.fetchData("https://jsonplaceholder.typicode.com/posts"));
                StockHandler.printAssetPrices();
        });


    }

    private void setupNavigationButtons(View view) {
        view.findViewById(R.id.btn_alert).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_cryptoPage_to_alertPage2));
        view.findViewById(R.id.btn_ratings).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_cryptoPage_to_ratePage));
        view.findViewById(R.id.btn_help).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_cryptoPage_to_helpPage));
        view.findViewById(R.id.btn_ai).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_cryptoPage_to_aiPage));
        try {
            view.findViewById(R.id.btn_profile).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_cryptoPage_to_profileTestPage));
        }
        catch(Exception e){
            Log.e("firebase","Error going to profile test"+e.toString());

        }
    }

    private void navigateToStockInfoPage(View view, String assetName) {
        StockInfoPageHandler.setAssetName(assetName);
        Navigation.findNavController(view).navigate(R.id.action_cryptoPage_to_stockInfoPage);
    }

    private void updateCryptosInFirestore(String cryptoName) {
        // Get current logged in user
        String currentUserId = model.getCurrentUser();
        Log.i(TAG, "CurrentUserID: " + currentUserId);

        // Query database using email
        Query query = dbRoot.collection("clients").whereEqualTo("Email", currentUserId);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                DocumentReference userDoc = documentSnapshot.getReference();

                // Add to the crypto array
                Map<String, Object> data = new HashMap<>();
                data.put("cryptosOwned", FieldValue.arrayUnion(cryptoName));

                // Check if document exists and update it
                userDoc.update(data)
                        .addOnSuccessListener(aVoid -> Log.i(TAG, "Cryptos added successfully"))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating cryptos: " + e.getMessage()));
            } else {
                Log.e(TAG, "No document found for email: " + currentUserId);
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching document: " + e.getMessage()));
    }
    private void deleteCryptosInFirestore(String cryptoName) {
        // Get the current logged-in user
        String currentUserId = model.getCurrentUser();
        Log.i(TAG, "CurrentUserID: " + currentUserId);

        // Query database using the user's email
        Query query = dbRoot.collection("clients").whereEqualTo("Email", currentUserId);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                DocumentReference userDoc = documentSnapshot.getReference();

                // Remove the crypto from the array
                Map<String, Object> data = new HashMap<>();
                data.put("cryptosOwned", FieldValue.arrayRemove(cryptoName));

                // Update the document
                userDoc.update(data)
                        .addOnSuccessListener(aVoid -> Log.i(TAG, "Crypto removed successfully: " + cryptoName))
                        .addOnFailureListener(e -> Log.e(TAG, "Error removing crypto: " + e.getMessage()));
            } else {
                Log.e(TAG, "No document found for email: " + currentUserId);
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error fetching document: " + e.getMessage()));
    }

}
