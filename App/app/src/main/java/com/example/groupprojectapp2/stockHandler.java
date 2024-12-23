package com.example.groupprojectapp2;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class stockHandler {
    private static stockHandler instance;
    private Model model = Model.getInstance();
    public Float balance = null;



    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    private stockHandler() {}

    // Singleton
    public static synchronized stockHandler getInstance() {
        if (instance == null) {
            instance = new stockHandler();
        }
        return instance;
    }
    // Takes textview and displays stocks
    public void displayStocks(TextView stocksTextView) {
        String currentUserDoc = model.getCurrentUserDoc();

        if (currentUserDoc == null || currentUserDoc.isEmpty()) {
            Log.e("Firestore", "No doc for the current user");
            return;
        }


        DocumentReference userDoc = db.collection("clients").document(currentUserDoc);
        // If userDoc.get return is good continue
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {

                    DocumentSnapshot document = task.getResult();


                    if (document.contains("crypto")) {
                        Object crypto = document.get("crypto");

                        if (crypto instanceof java.util.List) {
                            @SuppressWarnings("unchecked")
                            java.util.List<String> cryptoList = (java.util.List<String>) crypto;

                            // Append append append to output
                            StringBuilder output = new StringBuilder();
                            for (String cryptox : cryptoList) {
                                output.append(cryptox).append("\n");
                            }

                            // settext
                            stocksTextView.setText(output.toString());
                        } else {
                            Log.e("Firestore", "crypto array bad/error.");
                        }
                    } else {
                        Log.e("Firestore", "bad crypto field in doc");
                    }
                } else {
                    Log.e("Firestore", "Error getting document: " + (task.getException() != null ? task.getException().getMessage() : "weird error"));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firestore", "Failed to retrieve doc  : " + e.toString());
            }
        });
    }
    //Compare stock in clients portfolio with the current market price
    public void compareStockPrices(final OnPriceChangeListener listener) {
        Task<List<String>> assetsTask = fetchCryptosAndStocksFromAssets();
        Log.i("Firestore", "Assetprices: " + assetsTask.toString());

        Task<List<String>> clientTask = fetchCryptosAndStocksOwned();

        //Combine everything
        Tasks.whenAllSuccess(assetsTask, clientTask).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> results) {
                // Get both lists for client and asset market price
                List<String> assetsResult = (List<String>) results.get(0);
                List<String> clientResult = (List<String>) results.get(1);

                StringBuilder priceChanges = new StringBuilder();

                //Check lists and check for price changes
                for (String clientStock : clientResult) {
                    String[] clientStockDetails = clientStock.split(",");
                    String stockSymbol = clientStockDetails[0];
                    double clientPrice = Double.parseDouble(clientStockDetails[1]);
                    double clientQuantity = (clientStockDetails.length > 2) ? Double.parseDouble(clientStockDetails[2]) : 0;
                    double clientStockCost = clientPrice / (clientQuantity > 0 ? clientQuantity : 1);

                    // find corresponding stock in assetslist
                    for (String assetStock : assetsResult) {
                        String[] assetStockDetails = assetStock.split(",");
                        String assetStockSymbol = assetStockDetails[0];
                        double assetPrice = Double.parseDouble(assetStockDetails[1]);

                        Log.i("Firestore", "Comparing Client Stock: " + stockSymbol + " with Asset Stock: " + assetStockSymbol);

                        // if (stockSymbol.equals(assetStockSymbol)) {
                        if (stockSymbol.equals(assetStockSymbol)) {
                            double priceDifference = (assetPrice - clientStockCost);
                            String priceChangeMessage = "";

                            if (priceDifference > 0) {
                                priceChangeMessage = stockSymbol + " has increased by " + priceDifference + "%";
                            } else if (priceDifference < 0) {
                                priceChangeMessage = stockSymbol + " has decreased by " + Math.abs(priceDifference) + "%";
                            } else {
                                priceChangeMessage = stockSymbol + " has no price change.";
                            }

                            // Append to main string
                            priceChanges.append(priceChangeMessage).append(",");
                        }
                    }
                }

                // Return the price changes string via the listener callback
                // Callback because this needs time to go through not instant
                listener.onPriceChange(priceChanges.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("firestore", "error fetching or comparing stock data: " + e.toString());
                listener.onPriceChange("error: " + e.toString());
            }
        });
    }

    // Define an interface for callback to return the result
    public interface OnPriceChangeListener {
        void onPriceChange(String priceChanges);
    }








    private List<String> parseStockList(String data) {
        List<String> stockList = new ArrayList<>();

        String[] stocks = data.split("\n");
        for (String stock : stocks) {
            stockList.add(stock.trim());
        }
        return stockList;
    }

    public void printPricelist() {
        // Call fetchPricelistFromAssets get pricelist
        fetchPricelistFromAssets().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String pricelist) {
                // Print the fetched pricelist to the log
                Log.i("Firestore", "Pricelist: " + pricelist);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Print the error message to the log
                Log.e("Firestore", "Failed to get pricelist: " + e.toString());
            }
        });
    }
    public void printAssetPrices() {
        // Call fetchCryptosAndStocksFromAssets
        Task<List<String>> assetTask = fetchCryptosAndStocksFromAssets();

        // Listener for result
        assetTask.addOnSuccessListener(new OnSuccessListener<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                // Print the result (the list of stocks and cryptos)
                Log.i("Firestore", "Asset Prices : " + result.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Log the error if the task fails
                Log.e("Firestore", "Couldnt fetch asset price : " + e.toString());
            }
        });
    }
    public Task<List<String>> fetchCryptosAndStocksFromAssets() {
        String currentUserDoc = model.getCurrentUserDoc();

        // Use a taskCompletionsource to handle asyn return
        // Fetching takes time asyn waits
        TaskCompletionSource<List<String>> taskCompletionSource = new TaskCompletionSource<>();

        if (currentUserDoc == null || currentUserDoc.isEmpty()) {
            Log.e("Firestore", "No doc for this current user");
            taskCompletionSource.setException(new Exception("No doc for this current user"));
            return taskCompletionSource.getTask();
        }

        // Access the users doc
        // DocumentReference userDoc = db.collection("assets").document(currentUserDoc);
        DocumentReference userDoc = db.collection("assets").document("Hn8L8AQmYGc5WSPl8iAh");
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();

                    // Fetch the crypto array
                    List<String> combinedList = new ArrayList<>();

                    if (document.contains("crypto")) {
                        Object crypto = document.get("crypto");
                        if (crypto instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> cryptoList = (List<String>) crypto;
                            combinedList.addAll(cryptoList);
                        } else {
                            Log.e("Firestore", "crypto is not  valid");
                        }
                    } else {
                        Log.e("Firestore", "The doc does not contain a crypto ");
                    }

                    //todo 'shares' in assets not 'stocks'
                    if (document.contains("shares")) {
                        Object stocksOwned = document.get("shares");
                        if (stocksOwned instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> stockList = (List<String>) stocksOwned;
                            combinedList.addAll(stockList);
                        } else {
                            Log.e("Firestore", "'stocksOwned is not a valid");
                        }
                    } else {
                        Log.e("Firestore", " The doc does not contain a 'stocksOwned'");
                    }


                    taskCompletionSource.setResult(combinedList);
                } else {
                    String error = task.getException() != null ? task.getException().toString() : "error";
                    Log.e("Firestore", " Error getting doc : " + error);
                    taskCompletionSource.setException(new Exception("Error getting do : " + error));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firestore", "failed to get the doc : " + e.toString());
                taskCompletionSource.setException(e);
            }
        });

        return taskCompletionSource.getTask();
    }
    public Task<String> fetchPricelistFromAssets() {
        String currentUserDoc = model.getCurrentUserDoc();

        // async return
        TaskCompletionSource<String> taskCompletionSource = new TaskCompletionSource<>();

        if (currentUserDoc == null || currentUserDoc.isEmpty()) {

            Log.e("Firestore", "No doc path this current user");
            taskCompletionSource.setException(new Exception("path error"));
            return taskCompletionSource.getTask();
        }
        // todo fix names
        // Access the assets collection and get the document with the specific ID
        // Hardcode doc only 1 in assets to keep track of
        DocumentReference assetsDoc = db.collection("assets").document("Hn8L8AQmYGc5WSPl8iAh");

        assetsDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();

                    // Retrieve the "crypto" array
                    //List<String> x = new ArrayList<>();
                    List<String> cryptoList = new ArrayList<>();
                    if (document.contains("crypto")) {
                        Object crypto = document.get("crypto");
                        if (crypto instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> cryptoItems = (List<String>) crypto;
                            cryptoList.addAll(cryptoItems);
                        } else {
                            Log.e("Firestore", "crypto doesnt exist");
                        }
                    } else {
                        Log.e("Firestore ", "The doc doesnt contain crypto");
                    }

                    // Retrieve the shares
                    // todo make sure shares not stocks to keep track of current price
                    List<String> sharesList = new ArrayList<>();
                    if (document.contains("shares")) {
                        Object shares = document.get("shares");
                        if (shares instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> sharesItems = (List<String>) shares;
                            sharesList.addAll(sharesItems);
                        } else {
                            Log.e("Firestore", "'shares' is not a valid array.");
                        }
                    } else {


                        Log.e(" Firestore", "This doc doesnt contain shares field");
                    }

                    // Combine the lists into a single string for returning
                    String combinedPricelist = "Crypto: " + cryptoList.toString() + "\nShares: " + sharesList.toString();

                    // return the combined pricelist as a string
                    taskCompletionSource.setResult(combinedPricelist);
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Log.e("Firestore", "Error getting document: " + error);
                    taskCompletionSource.setException(new Exception("Error getting document: " + error));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firestore", "Failed to retrieve the document: " + e.toString());
                taskCompletionSource.setException(e);
            }
        });

        // Task async
        return taskCompletionSource.getTask();
    }



    public Task<List<String>> fetchCryptosAndStocksOwned() {
        String currentUserDoc = model.getCurrentUserDoc();


        TaskCompletionSource<List<String>> taskCompletionSource = new TaskCompletionSource<>();

        if (currentUserDoc == null || currentUserDoc.isEmpty()) {
            Log.e("Firestore", "Noo document path for the user");
            taskCompletionSource.setException(new Exception("No document for this  user"));
            return taskCompletionSource.getTask();
        }


        DocumentReference userDoc = db.collection("clients").document(currentUserDoc);

        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();


                    List<String> combinedList = new ArrayList<>();

                    if (document.contains("crypto")) {
                        Object crypto = document.get("crypto");
                        if (crypto instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> cryptoList = (List<String>) crypto;
                            combinedList.addAll(cryptoList); // Add crypto list to the combined list
                        } else {
                            Log.e("Firestore", "'crypto' is not a valid array.");
                        }
                    } else {
                        Log.e("Firestore", "The doc doesnt contain a 'crypto' field.");
                    }


                    if (document.contains("stocks")) {
                        Object stocksOwned = document.get("stocks");
                        if (stocksOwned instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> stockList = (List<String>) stocksOwned;
                            combinedList.addAll(stockList); // Add stock list to the combined list
                        } else {
                            Log.e("Firestore", "'stocksOwned' is not a valid array.");
                        }
                    } else {
                        Log.e("Firestore", "The document does not contain a 'stocksOwned' field.");
                    }


                    taskCompletionSource.setResult(combinedList);
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Log.e("Firestore", "Error getting document: " + error);
                    taskCompletionSource.setException(new Exception("Error getting document: " + error));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firestore", "Failed to retrieve the document: " + e.getMessage());
                taskCompletionSource.setException(e);
            }
        });

        return taskCompletionSource.getTask();
    }



    public void displayBalance(TextView nameTextView) {
        String currentUserDoc = model.getCurrentUserDoc();

        if (currentUserDoc == null || currentUserDoc.isEmpty()) {
            Log.e("Firestore", "No document path for the current user");
            return;
        }


        DocumentReference userDoc = db.collection("clients").document(currentUserDoc);

        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {

                    DocumentSnapshot document = task.getResult();


                    if (document.contains("deposit")) {
                        Object deposit = document.get("deposit");
                        float depositFloat = Float.parseFloat(deposit.toString());
                        depositFloat = Math.round(depositFloat * 100) / 100.0f;
                        stockHandler.this.balance = depositFloat;
                        Log.i("Firestore", "Deposit: " + depositFloat);


                        String balanceString = String.format("%.2f", depositFloat);
                        nameTextView.setText("Balance : €"+balanceString);
                    } else {
                        Log.e("Firestore", "The document doesnt contain a deposit");
                    }
                } else {
                    Log.e("Firestore", " Error getting document: " + (task.getException() != null ? task.getException().getMessage() : "err"));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firestore", "Failed to retrieve the document: " + e.getMessage());
            }
        });
    }
    public void leaveReview(String message,String rating) {
        String currentUserDoc = Model.getInstance().getCurrentUserDoc();
        if (currentUserDoc == null) {

            return;
        }

        DocumentReference docRef = db.collection("reviews").document(currentUserDoc);

        try {
            String reviewMessage = message;
            String reviewRating = rating;


            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {

                    Map<String, Object> items = new HashMap<>();
                    items.put("review", reviewMessage);
                    items.put("rating", reviewRating);


                    db.collection("reviews").add(items)
                            .addOnSuccessListener(aVoid -> {
                                Log.i("Firestore", "rating updated successfully : " + reviewMessage);

                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", " Error updating deposit", e);

                            });
                } else {
                    Log.e("Firestore", "Error fetching deposit", task.getException());

                }
            });
        } catch (NumberFormatException e) {
            Log.e("Deposit", "Invalid deposit amount", e);

        }
    }
    public void leaveSupport(String messagge,String subject) {
        String currentUserDoc = Model.getInstance().getCurrentUserDoc();
        if (currentUserDoc == null) {

            return;
        }

        DocumentReference docRef = db.collection("customersupport").document(currentUserDoc);

        try {
            String supportMessage = messagge;
            String supportSubject = subject;


            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {

                    Map<String, Object> items = new HashMap<>();
                    items.put("message", supportMessage);
                    items.put("subject", supportSubject);
                    items.put("userid", model.getCurrentUserDoc());

                    db.collection("customersupport").add(items)
                            .addOnSuccessListener(aVoid -> {
                                Log.i("Firestore", "Deposit updated successfully: " + supportMessage);

                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error updating deposit", e);

                            });
                } else {
                    Log.e("Firestore", "Error /  deposit", task.getException());

                }
            });
        } catch (NumberFormatException e) {
            Log.e("Deposit", "  ERROR  deposit amount", e);

        }
    }
    public void depositString(String depositAmount) {
        String currentUserDoc = Model.getInstance().getCurrentUserDoc();
        if (currentUserDoc == null) {

            return;
        }

        DocumentReference docRef = db.collection("clients").document(currentUserDoc);

        try {
            float newDepositAmount = Float.parseFloat(depositAmount);


            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {

                    Double currentDepositAmountDouble = task.getResult().getDouble("deposit");
                    float currentDepositAmount = (currentDepositAmountDouble != null) ? currentDepositAmountDouble.floatValue() : 0.0f;


                    float updatedDepositAmount = currentDepositAmount + newDepositAmount;


                    Map<String, Object> depositData = new HashMap<>();
                    depositData.put("deposit", updatedDepositAmount);

                    docRef.set(depositData, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Log.i("Firestore", "Deposit updated  : " + newDepositAmount);

                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error updating ", e);

                            });
                } else {
                    Log.e("Firestore", "Error fetchingdeposit", task.getException());

                }
            });
        } catch (NumberFormatException e) {
            Log.e("Deposit", "Error deposit amount", e);

        }
    }
    public void deposit(String depositAmount,View view){
        String currentUserDoc = Model.getInstance().getCurrentUserDoc(); // Get the current user's document ID
        if (currentUserDoc == null) {

            return;
        }

        DocumentReference docRef = db.collection("clients").document(currentUserDoc); // Use current user's document ID

        try {
            float newDepositAmount = Float.parseFloat(depositAmount);


            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {

                    Double currentDepositAmountDouble = task.getResult().getDouble("deposit");
                    float currentDepositAmount = (currentDepositAmountDouble != null) ? currentDepositAmountDouble.floatValue() : 0.0f;


                    setBalance(newDepositAmount);
                    float updatedDepositAmount = currentDepositAmount + newDepositAmount;


                    Map<String, Object> depositData = new HashMap<>();
                    depositData.put("deposit", updatedDepositAmount);

                    docRef.set(depositData, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Log.i("Firestore", "Deposit updated successfully :"+newDepositAmount);

                                TextView nameTextView = view.findViewById(R.id.text_balance);

                                displayBalance(nameTextView);

                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error updating deposit", e);

                            });
                } else {
                    Log.e("Firestore", "Error fetching current deposit", task.getException());

                }
            });
        } catch (NumberFormatException e) {
            //Toast
        }


    }
    public void depositer(String depositAmount) {
        String currentUserDoc = Model.getInstance().getCurrentUserDoc();
        if (currentUserDoc == null) {
            Log.e("Firestore", "No user logged in");
            return;
        }

        DocumentReference docRef = db.collection("clients").document(currentUserDoc);

        try {
            float newDepositAmount = Float.parseFloat(depositAmount);


            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Double currentDepositAmountDouble = task.getResult().getDouble("deposit");
                    float currentDepositAmount = (currentDepositAmountDouble != null) ? currentDepositAmountDouble.floatValue() : 0.0f;


                    float updatedDepositAmount = currentDepositAmount + newDepositAmount;


                    Map<String, Object> depositData = new HashMap<>();
                    depositData.put("deposit", updatedDepositAmount);


                    docRef.set(depositData, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> Log.i("Firestore", "Deposit updated successfully: " + newDepositAmount))
                            .addOnFailureListener(e -> Log.e("Firestore", "Error updatingg deposit", e));
                } else {
                    Log.e("Firestore", "Error fetching current deposit", task.getException());
                }
            });
        } catch (NumberFormatException e) {
            Log.e("Firestore", "Error xyz deposit amount: " + depositAmount, e);
        }
    }


    public void withdraw(String withdrawAmount, View view){
        String currentUserDoc = Model.getInstance().getCurrentUserDoc();
        if (currentUserDoc == null) {

            return;
        }
        Log.i("firestore","stockinfopage withdraw started"+withdrawAmount);
        DocumentReference docRef = db.collection("clients").document(currentUserDoc);

        try {
            float withdrawalAmount = Float.parseFloat(withdrawAmount);

            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Double currentDepositAmountDouble = task.getResult().getDouble("deposit");
                    float currentDepositAmount = (currentDepositAmountDouble != null) ? currentDepositAmountDouble.floatValue() : 0.0f;


                    if (withdrawalAmount > currentDepositAmount) {

                        return;
                    }


                    float updatedDepositAmount = currentDepositAmount - withdrawalAmount;


                    Map<String, Object> depositData = new HashMap<>();
                    depositData.put("deposit", updatedDepositAmount);

                    docRef.set(depositData, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Log.i("Firestore", "deposit updated  after withdrawal");
                                TextView nameTextView = view.findViewById(R.id.text_balance);
                                displayBalance(nameTextView);
                                //Toast.makeText(getActivity(), "Withdrawal successful", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore ", "error updating deposit after withdrawal", e);
                                //Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Log.e("Firestore", "Error fetching current deposit", task.getException());
                    //Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            //Toast
        }


    }
    public void alert(){




    }
    public void setBalance(float bal){this.balance = bal;}



    public float getBalance(){

        Log.i("Firestore","Get balance"+this.balance.toString());
        return this.balance;}
    public String stockGambler(){
        List<String> assets = Arrays.asList("btc", "eth", "sol", "msft", "aapl", "tsla", "goog", "intl", "amzn", "amd", "meta", "nvda", "bcm");


        Random random = new Random();
        String assetThere = assets.get(random.nextInt(assets.size()));
        Log.i("Firestore","Random asset"+assetThere);

        updateCryptosInFirestore(assetThere+",1500,1");
        return assetThere;
    }
    public void updateCryptosInFirestore(String cryptoName) {

        String currentUserId = model.getCurrentUser();
        Log.i("Firestore", "CurrentUserID: " + currentUserId);


        List<String> stockList = Arrays.asList("msft", "aapl", "tsla", "goog","intl", "amzn", "amd", "meta","nvda","bcm");
        List<String> cryptoList = Arrays.asList("btc", "eth", "sol");


        String[] nameParts = cryptoName.split(",");
        String name = nameParts[0].trim();


        Query query = db.collection("clients").whereEqualTo("email", currentUserId);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                    DocumentReference userDoc = documentSnapshot.getReference();


                    userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot docSnapshot = task.getResult();
                                List<String> crypto = (List<String>) docSnapshot.get("crypto");
                                List<String> stocks = (List<String>) docSnapshot.get("stocks");

                                if (crypto == null) {
                                    crypto = new ArrayList<>();
                                }
                                if (stocks == null) {
                                    stocks = new ArrayList<>();
                                }


                                if (cryptoList.contains(name)) {
                                    crypto.add(cryptoName);
                                } else if (stockList.contains(name)) {
                                    stocks.add(cryptoName);
                                }

                                // Update the doc
                                Map<String, Object> data = new HashMap<>();
                                data.put("crypto", crypto);
                                data.put("stocks", stocks);

                                userDoc.update(data)
                                        .addOnSuccessListener(aVoid -> Log.i("Firestore", "Cryptos and stocks updated"))
                                        .addOnFailureListener(e -> Log.e("Firestore", "Error updating crypto : " + e.toString()));
                            } else {
                                Log.e("Firestore", "Error fetching user doc: " + task.getException());
                            }
                        }
                    });
                } else {
                    Log.e("Firestore", "No docfor email: " + currentUserId);
                }
            }
        }).addOnFailureListener(e -> Log.e("Firestore", " Error getting doc: " + e.toString()));
    }


    // sell Crypto
    public void deleteCryptosInFirestore(String cryptoName) {

        String currentUserId = model.getCurrentUser();
        Log.i("sellstock"," Current user to delete crypto : "+currentUserId);


        Query query = db.collection("clients").whereEqualTo("email", currentUserId);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                DocumentReference userDoc = documentSnapshot.getReference();


                Map<String, Object> data = new HashMap<>();
                data.put("crypto", FieldValue.arrayRemove(cryptoName));


                userDoc.update(data)
                        .addOnSuccessListener(aVoid -> Log.i("sellstock ", " Crypto removed : " + cryptoName))
                        .addOnFailureListener(e -> Log.e("sellstock", "Error removing crypto: " + e.toString()));
            } else {
                Log.e("sellstock", "No doc found for email: " + currentUserId);
            }
        }).addOnFailureListener(e -> Log.e("sellstock", "Error doc: " + e.toString()));
    }


}
