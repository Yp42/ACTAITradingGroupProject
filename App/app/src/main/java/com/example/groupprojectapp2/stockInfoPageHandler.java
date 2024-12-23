package com.example.groupprojectapp2;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map;
import java.util.AbstractMap;
public class stockInfoPageHandler {

    private String asset = null;


    Map<String, String> descriptions = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("xrp", "Ripple (XRP) is a digital payment protocol and cryptocurrency focused on fast, low-cost international money transfers."),
            new AbstractMap.SimpleEntry<>("eth", "Ethereum (ETH) is a decentralized platform enabling smart contracts and decentralized applications (dApps)."),
            new AbstractMap.SimpleEntry<>("btc", "Bitcoin (BTC) is the first decentralized cryptocurrency, known for its peer-to-peer transactions and blockchain technology."),
            new AbstractMap.SimpleEntry<>("bcm", "BCM is a fictional or lesser-known asset; description can be customized as needed."),
            new AbstractMap.SimpleEntry<>("aapl", "Apple Inc. (AAPL) designs, manufactures, and sells consumer electronics, including the iPhone and Mac computers."),
            new AbstractMap.SimpleEntry<>("tsla", "Tesla Inc. (TSLA) is a leader in electric vehicles, energy storage solutions, and solar energy products."),
            new AbstractMap.SimpleEntry<>("intl", "Intel Corporation (INTL) is a global leader in semiconductor manufacturing and computing innovation."),
            new AbstractMap.SimpleEntry<>("amzn", "Amazon.com Inc. (AMZN) is an e-commerce giant and provider of cloud computing services through AWS."),
            new AbstractMap.SimpleEntry<>("amd", "Advanced Micro Devices (AMD) is a semiconductor company specializing in CPUs and GPUs for computing and graphics."),
            new AbstractMap.SimpleEntry<>("msft", "Microsoft Corporation (MSFT) develops software, hardware, and cloud services, including Windows, Office, and Azure."),
            new AbstractMap.SimpleEntry<>("meta", "Meta Platforms Inc. (META), formerly Facebook, focuses on social media, virtual reality, and the metaverse."),
            new AbstractMap.SimpleEntry<>("goog", "Alphabet Inc. (GOOG), parent company of Google, specializes in search, advertising, and various technology products."),
            new AbstractMap.SimpleEntry<>("nvda", "NVIDIA Corporation (NVDA) is a leader in graphics processing units (GPUs) and AI-related computing solutions.")
    );





    stockHandler StockHandler = stockHandler.getInstance();
    private static stockInfoPageHandler instance = null;
    private stockInfoPageHandler() {}
    public static stockInfoPageHandler getInstance() {
        if (instance == null) {
            instance = new stockInfoPageHandler();
        }
        return instance;
    }
    public void setAssetName(String asset){
        this.asset=asset;}

    public String getAssetName(){
        return asset;}

    public double getPrice(String stock) {
        List<String> assets = Arrays.asList("btc", "eth", "sol", "msft", "aapl", "tsla", "goog", "andr", "amzn", "amd", "meta", "nvda", "bcm");
        List<Double> prices = Arrays.asList(5500.00, 1800.00, 650.00, 300.00, 145.00, 275.00, 2750.00, 500.00, 3300.00, 120.00, 350.00, 700.00, 500.00);

        for (int i = 0; i < assets.size(); i++) {
            if (stock.equals(assets.get(i))) {
                return prices.get(i);
            }
        }

        return 1000.0;
    }
    public String getDescription(){
        return descriptions.get(getAssetName());


    }

public void buyStock(View view, String stock) {

    EditText editTextInput = view.findViewById(R.id.amount_input);

    String inputText = editTextInput.getText().toString().trim();
    Log.i("Firestore","Buying this stock now : "+stock);

    try {

        double amount = Float.parseFloat(inputText) / this.getPrice(stock);

        Log.i("Firestore", "Amount to buy "+amount);
        float currentBalance = StockHandler.getBalance();

        if (currentBalance >= amount) {

            Log.i("Firestore", "Amount to buy: " + amount);


            StockHandler.updateCryptosInFirestore(stock +","+ inputText+"," + amount);



        } else {
            Log.i("Error", "Insufficient balance!");

            Toast.makeText(view.getContext(), "Insufficient balance to buy stock.", Toast.LENGTH_SHORT).show();
        }

    } catch (NumberFormatException e) {

        Log.i("Error", "Invalid input: " + inputText);
        Toast.makeText(view.getContext(), "Please enter a valid amount.", Toast.LENGTH_SHORT).show();
    }
}

    public void sellStock(String stock) {

        Random random = new Random();
        float x = 1000;
        float fluctuation = (random.nextFloat() * 0.2f) - 0.2f;
        float currentPrice = x * (1 + fluctuation);

        if (stock.isEmpty()) {
            Log.i("Error", "Stock string is empty!");
            return;
        }

        try {

            String[] stockDetails = stock.split(",");
            if (stockDetails.length != 3) {
                Log.i("Error", "Invalid  stock format!");
                return;
            }

            String stockName = stockDetails[0];
            int quantity = Integer.parseInt(stockDetails[1]);
            float purchasePrice = Float.parseFloat(stockDetails[2]);


            float percentageDifference = ((currentPrice - purchasePrice) / purchasePrice) * 100;


            float totalProfit = (currentPrice - purchasePrice) * quantity;
            double profitToAdd = quantity * (1 + percentageDifference / 100);

            Log.i("SellStock", "Stock: " + stock);
            Log.i("SellStock", "Quantity: " + quantity);
            Log.i("SellStock", "Purchase Price: " + purchasePrice);
            Log.i("SellStock", "Current Price: " + currentPrice);
            Log.i("SellStock", "Percentage Difference: " + percentageDifference + "%");
            Log.i("SellStock", "Total Profit/Loss: " + percentageDifference*quantity);

            Log.i("SellStock", "Added this to your balance: " + profitToAdd);
            StockHandler.depositString(String.valueOf(profitToAdd));

            StockHandler.deleteCryptosInFirestore(stock);

        } catch (NumberFormatException e) {
            Log.i("Error", "Invalid number=");
        } catch (Exception e) {
            Log.i("Error", "error : " + e.toString());
        }
    }



}
