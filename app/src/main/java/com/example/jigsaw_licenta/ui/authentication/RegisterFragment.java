package com.example.jigsaw_licenta.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.ui.main.MainActivity;
import com.example.jigsaw_licenta.utils.FirebaseStatsHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {
    private EditText emailEditText, passwordEditText, nicknameEditText;
    private Button registerButton, loginButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        initializeViews(view);
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        nicknameEditText = view.findViewById(R.id.nicknameEditText);
        registerButton = view.findViewById(R.id.registerButton);
        loginButton = view.findViewById(R.id.loginButton);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> registerUser());
        loginButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_register_to_login));
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String nickname = nicknameEditText.getText().toString().trim();

        if (validateInputs(email, password, nickname)) {
            progressBar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            saveUserProfile(nickname);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            showToast("Registration failed: " + task.getException().getMessage());
                        }
                    });
        }
    }

    private void saveUserProfile(String nickname) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            progressBar.setVisibility(View.GONE);
            showToast("User authentication failed");
            return;
        }

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("nickname", nickname);
        userProfile.put("email", user.getEmail());
        userProfile.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(user.getUid())
                .set(userProfile)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseStatsHelper statsHelper = new FirebaseStatsHelper();
                        statsHelper.initializeNewUserStats();

                        navigateToMainActivity();
                    } else {
                        showToast("Profile saved locally");
                        navigateToMainActivity();
                    }
                });
    }

    private boolean validateInputs(String email, String password, String nickname) {
        if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
            showToast("Please fill all fields");
            return false;
        }
        if (password.length() < 6) {
            showToast("Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(requireActivity(), MainActivity.class));
        requireActivity().finish();
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}