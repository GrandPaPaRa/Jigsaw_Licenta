package com.example.jigsaw_licenta.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.ui.authentication.AuthenticationActivity;
import com.example.jigsaw_licenta.viewmodel.GameSettingsViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private GameSettingsViewModel gameSettingsViewModel;
    private FirebaseAuth mAuth;
    private boolean isOnline = true;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Check authentication
        checkAuthentication();

        // Check network connection
        checkNetworkConnection();

        initializeViewModel();
        setupNavigation();
    }

    private void checkAuthentication() {
        if (mAuth.getCurrentUser() == null) {
            // No user logged in, redirect to LoginActivity
            startActivity(new Intent(this, AuthenticationActivity.class));
            finish();
        }
    }

    private void checkNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isOnline = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isOnline) {
            Toast.makeText(this, "Offline mode activated", Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViewModel() {
        gameSettingsViewModel = new ViewModelProvider(this).get(GameSettingsViewModel.class);
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);
        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        NavigationUI.setupWithNavController(bottomNav, navController);

        bottomNav.setOnItemSelectedListener(item -> {
            int currentDestination = navController.getCurrentDestination().getId();
            if (item.getItemId() == R.id.menu_game) {
                if (currentDestination != R.id.menu_game) {
                    navController.popBackStack(R.id.menu_game, false);
                    navController.navigate(R.id.menu_game);
                }
                return true;
            } else if (item.getItemId() == R.id.menu_game_time_trial) {
                if (currentDestination != R.id.menu_game_time_trial) {
                    navController.popBackStack(R.id.menu_game_time_trial, false);
                    navController.navigate(R.id.menu_game_time_trial);
                }
                return true;
            }
            else if (item.getItemId() == R.id.userStatisticsFragment) {
                if (currentDestination != R.id.userStatisticsFragment) {
                    navController.popBackStack(R.id.userStatisticsFragment, false);
                    navController.navigate(R.id.userStatisticsFragment);
                }
                return true;
            }
            else if (item.getItemId() == R.id.leaderBoardFragment) {
                if (currentDestination != R.id.leaderBoardFragment) {
                    navController.popBackStack(R.id.leaderBoardFragment, false);
                    navController.navigate(R.id.leaderBoardFragment);
                }
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void signOut() {
        mAuth.signOut();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_logged_in", false);
        editor.remove("user_email");
        editor.apply();

        startActivity(new Intent(this, AuthenticationActivity.class));
        finish();
    }
}