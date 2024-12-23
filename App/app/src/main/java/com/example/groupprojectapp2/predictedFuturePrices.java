package com.example.groupprojectapp2;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class predictedFuturePrices {

    //todo Look at permissions with https/internet services
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    // Fetch data using async
    public void fetchAndReturnData(String stockName, Callback callback) {
        String baseUrl = "https://kangaroo-pleased-notably.ngrok-free.app/asset=";
        String apiUrl = baseUrl + stockName;

        // Execute Service fetching data with async
        executorService.execute(() -> {
            String response = fetchData(apiUrl);
            mainHandler.post(() -> {
                if (response != null) {
                    // Clean up input with Delimiters (Thank you data analytics)
                    String input = extractValue(response, "\"input\":\"", "\"");
                    String apiResponse = extractValue(response, "\"response\":\"", "\"}");


                    String result = "Input: " + input + "\nResponse: " + apiResponse;


                    callback.onDataFetched(result);
                } else {

                    callback.onDataFetched("Failed to fetch data.");
                }
            });
        });
    }


    private String fetchData(String apiUrl) {
        StringBuilder result = new StringBuilder();
        try {

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); // Make sure this is GET


            int responseCode = connection.getResponseCode();


            if (responseCode == HttpURLConnection.HTTP_OK) {

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    result.append(inputLine);
                }
                in.close();
            } else {
                Log.e("ApiMachine", "GET request eerror Code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            Log.e("ApiMachine", "Error  " + e.toString());

            return null;
        }
        return result.toString();
    }


    private String extractValue(String str, String startDelimiter, String endDelimiter) {
        int startIndex = str.indexOf(startDelimiter) + startDelimiter.length();
        int endIndex = str.indexOf(endDelimiter, startIndex);
        if (startIndex != -1 && endIndex != -1) {
            return str.substring(startIndex, endIndex);
        }
        return "";
    }

    // Callback interface here
    public interface Callback {
        void onDataFetched(String result);
    }
}
