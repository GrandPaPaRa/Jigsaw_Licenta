package com.example.jigsaw_licenta.ui.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.jigsaw_licenta.R;
import com.example.jigsaw_licenta.viewmodel.GameSettingsViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private GameSettingsViewModel gameSettingsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        gameSettingsViewModel = new ViewModelProvider(this).get(GameSettingsViewModel.class);

        // Get the nav controller from the FragmentContainerView
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);
        NavController navController = navHostFragment.getNavController();

        // Link BottomNavigationView with NavController
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        bottomNav.setOnItemSelectedListener(item -> {
            int currentDestination = navController.getCurrentDestination().getId();
            if (item.getItemId() == R.id.menu_game) {
                if (currentDestination != R.id.menu_game) {
                    // Clear back stack and navigate to game
                    navController.popBackStack(R.id.menu_game, false);
                    navController.navigate(R.id.menu_game);
                }
                return true;
            }
            else if (item.getItemId() == R.id.menu_game_time_trial) {
                if (currentDestination != R.id.menu_game_time_trial) {
                    // Clear back stack and navigate to time trial
                    navController.popBackStack(R.id.menu_game_time_trial, false);
                    navController.navigate(R.id.menu_game_time_trial);
                }
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }
}