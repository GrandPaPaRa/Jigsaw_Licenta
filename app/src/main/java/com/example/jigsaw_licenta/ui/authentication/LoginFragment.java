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
import com.example.jigsaw_licenta.utils.NetworkUtils;
import com.example.jigsaw_licenta.viewmodel.GameViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginFragment extends Fragment {
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private View rootView;
    private Button offlineButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_login, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkAuthState(); // Check auth state first
        initializeViews();
        setupListeners();
    }

    private void checkAuthState() {
       if(NetworkUtils.isOfflineMode(requireActivity().getApplication()))
           navigateToMainActivity();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMainActivity();
        }

        //send toast if no internet connection to not waste time on login
        if(!NetworkUtils.isNetworkAvailable(requireContext())){
            showToast("No internet connection available, use offline mode");
        }
    }


    private void initializeViews() {
        emailEditText = rootView.findViewById(R.id.emailEditText);
        passwordEditText = rootView.findViewById(R.id.passwordEditText);
        loginButton = rootView.findViewById(R.id.loginButton);
        registerButton = rootView.findViewById(R.id.registerButton);
        progressBar = rootView.findViewById(R.id.progressBar);
        offlineButton = rootView.findViewById(R.id.offlineButton);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_register));
        offlineButton.setOnClickListener(v -> {
            NetworkUtils.setOfflineMode(requireActivity().getApplication(), true);
            navigateToMainActivity();
        });
    }

    private void loginUser() {
        if (!NetworkUtils.isNetworkAvailable(requireContext())){
            showToast("No internet connection use offline mode!");
            return;
        }
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateInputs(email, password)) {
            progressBar.setVisibility(View.VISIBLE);
            loginWithCredentials(email, password);
        }
    }

    private void loginWithCredentials(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        GameViewModel.clearSavedGames(requireActivity().getApplication());
                        navigateToMainActivity();
                    } else {
                        showToast("Login failed: " + task.getException().getMessage());
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please fill all fields");
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
    private void checkNetworkStatus() {
        boolean isNetworkAvailable = NetworkUtils.isNetworkAvailable(requireContext());
        boolean isOfflineMode = NetworkUtils.isOfflineMode(requireActivity().getApplication());

        if (!isNetworkAvailable && !isOfflineMode) {
            showToast("No internet connection available");
        }

    }

}