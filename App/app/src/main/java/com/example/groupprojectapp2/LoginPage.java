package com.example.groupprojectapp2;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LoginPage extends Fragment {

    private String TAG = "GoogleAuth";
    private FirebaseFirestore dbRoot;
    private EditText emailInput;
    private EditText passwordInput;
    private FirebaseAuth mAuth;

    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private ActivityResultLauncher<IntentSenderRequest> signInLauncher;

    Model model = Model.getInstance();
    private String documentId;

    public LoginPage() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbRoot = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Google sign in start
        oneTapClient = Identity.getSignInClient(requireContext());
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId("528017370723-u543tjo2u2gubjnqonbslbv2c9iagrho.apps.googleusercontent.com")
                                .setFilterByAuthorizedAccounts(false)
                                .build())
                .build();

        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        try {
                            SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                            String idToken = credential.getGoogleIdToken();

                            if (idToken != null) {
                                firebaseAuthWithGoogle(idToken);
                            } else {
                                Log.e(TAG, "ID token has not been received");
                            }
                        } catch (ApiException e) {
                            Log.e(TAG, "Error with credentials", e);
                        }
                    } else {
                        Log.e(TAG, "Google Signin error/not working");
                    }
                }
        );
    }
    // Google sign in end
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_page, container, false);

        //todo Formatting++ Make it look good

        // Get Login details
        emailInput = view.findViewById(R.id.email_input);
        passwordInput = view.findViewById(R.id.password_input);
        Button loginButton = view.findViewById(R.id.login_btn);

        view.findViewById(R.id.btn_googles).setOnClickListener(v -> beginGoogleSignIn());

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                validateUser(email, password);
            } else {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });
        //todo Page nav register to login/login to register
        view.findViewById(R.id.register_text).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_loginPage_to_registerPage)
        );

        return view;
    }

    // Google sign in
    private void beginGoogleSignIn() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(getActivity(), result -> {
                    try {
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                        signInLauncher.launch(intentSenderRequest);
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting Google Sign-In intent", e);
                    }
                })
                .addOnFailureListener(getActivity(), e -> {
                    Log.e(TAG, "Google Sign-In failed", e);
                    Toast.makeText(getActivity(), "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    // Google sign in
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(getActivity(), "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            model.setCurrentUser(user.getEmail(), user.getUid());
                            Navigation.findNavController(getView()).navigate(R.id.action_loginPage_to_cryptoPage);
                        }
                    } else {
                        Log.w(TAG, "Firebase authentication failed", task.getException());
                        Toast.makeText(getActivity(), "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    // Firebase sign in
    private void validateUser(String email, String password) {
        dbRoot.collection("clients")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            documentId = document.getId();
                        }
                        model.setCurrentUser(email, documentId);
                        Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(getView()).navigate(R.id.action_loginPage_to_cryptoPage);
                    } else {
                        Toast.makeText(getActivity(), "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
