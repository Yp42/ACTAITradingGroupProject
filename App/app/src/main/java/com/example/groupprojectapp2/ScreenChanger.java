package com.example.groupprojectapp2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.Navigation;

public class ScreenChanger {

    public ScreenChanger(LayoutInflater inflater, ViewGroup container, int layoutResId) {

        View view = inflater.inflate(layoutResId, container, false);


        view.findViewById(R.id.btn_invest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_ratePage_to_cryptoPage2);
            }
        });

        view.findViewById(R.id.btn_ai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_ratePage_to_aiPage);
            }
        });


    }
}
