package com.example.groupprojectapp2;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

public class aiCode {
    private StringBuilder jsonContent;
    //Get json hardcoded

    public void log(Context context) {
        String fileName = "ai.json";
        jsonContent = new StringBuilder();

        try (InputStream is = context.getAssets().open(fileName);
             BufferedReader x = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = x.readLine()) != null) {
                jsonContent.append(line.trim());
            }


            Log.i("Firestore", "Contents : " + jsonContent.toString());

        } catch (IOException e) {
            Log.e("Firestore", "Error reading json : " + fileName, e);
        }
    }

    public StringBuilder getJsonContent() {
        if (jsonContent == null) {
            Log.w("Firestore", "jsonContent is null. Ensure log() is called first.");
            return new StringBuilder();
        }
        return jsonContent;
    }
}
