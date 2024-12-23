package com.example.groupprojectapp2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegisterPage extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private FirebaseFirestore dbRoot;
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    Model model = Model.getInstance();
    public RegisterPage() {
        // Required empty public constructor
    }

    public static RegisterPage newInstance(String param1, String param2) {
        RegisterPage fragment = new RegisterPage();
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

        dbRoot = FirebaseFirestore.getInstance();
        Log.i("Firestore", "firebase instance init");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_page, container, false);

        nameInput = view.findViewById(R.id.name_input);
        emailInput = view.findViewById(R.id.email_input);
        passwordInput = view.findViewById(R.id.password_input);
        Button registerButton = view.findViewById(R.id.register_btn);

        Log.i("Firestore", "starting data");


        registerButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            Log.i("Firestore", "clicked register button");

            if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                Log.i("Firestore", "inserted data");
                checkDuplicate(view ,name, email, password);
            } else {
                Log.i("Firestore", "not inserted data");

            }



        });

        view.findViewById(R.id.login_text).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_registerPage_to_loginPage)
        );
        return view;
    }
    private void checkDuplicate( View view,String name, String email, String password) {

        dbRoot.collection("clients")
                .whereEqualTo("Email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // Email already exists
                                Toast.makeText(getActivity(), "Account with this email already exists", Toast.LENGTH_SHORT).show();
                            } else {
                                // Email is 1 of 1
                                insertData(name, email, password,view);
                                Navigation.findNavController(view).navigate(R.id.action_registerPage_to_cryptoPage);
                            }
                        } else {
                            Log.e("Firestore", "Error checking email: " + task.getException());
                        }
                    }
                });
    }
    private void insertData(String name, String email, String password,View view) {
        Map<String, String> items = new HashMap<>();
        items.put("name", name);
        items.put("email", email);
        items.put("password", password);


        dbRoot.collection("clients").add(items)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            // Get doc ids
                            DocumentReference documentReference = task.getResult();
                            String documentId = documentReference.getId(); // This is the document ID


                            nameInput.setText("");
                            emailInput.setText("");
                            passwordInput.setText("");

                            // Set user session
                            model.setCurrentUser(email, documentId);

                            Log.i("Firestore", "Data successfully added with Document ID: " + documentId);


                            try{

                                NavController navController = Navigation.findNavController(requireView());
                                navController.navigate(R.id.action_registerPage_to_cryptoPage);
                            }catch(Exception e)
                            {
                                Log.e("Firestore",e.getMessage());

                            }

                        } else {
                            Log.e("Firestore", "Error adding data", task.getException());
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error: " + e.getMessage()));
    }

}
