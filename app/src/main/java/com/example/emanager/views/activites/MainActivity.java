package com.example.emanager.views.activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.emanager.R;
import com.example.emanager.databinding.ActivityMainBinding;
import com.example.emanager.utils.Constants;
import com.example.emanager.viewmodels.MainViewModel;
import com.example.emanager.views.fragments.AccountsFragment;
import com.example.emanager.views.fragments.StatsFragment;
import com.example.emanager.views.fragments.TransactionsFragment;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Calendar calendar;
    public MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setSupportActionBar(binding.toolBar);
        getSupportActionBar().setTitle("Expense Manager");

        Constants.setCategories();
        calendar = Calendar.getInstance();

        // Set initial fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, new TransactionsFragment());
        transaction.commit();

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                if(item.getItemId() == R.id.transactions) {
                    getSupportFragmentManager().popBackStack();
                    return true;
                } else if(item.getItemId() == R.id.stats) {
                    transaction.replace(R.id.content, new StatsFragment());
                    transaction.addToBackStack(null);
                } else if(item.getItemId() == R.id.accounts) {
                    transaction.replace(R.id.content, new AccountsFragment());
                    transaction.addToBackStack(null);
                } else if(item.getItemId() == R.id.more) {
                    showMoreOptions();
                    return true;
                }

                transaction.commit();
                return true;
            }
        });
    }

    private void showMoreOptions() {
        String[] options = {"Settings", "Export Data", "About"};
        new AlertDialog.Builder(this)
                .setTitle("More Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Settings
                            Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show();
                            break;
                        case 1: // Export
                            Toast.makeText(this, "Export feature coming soon!", Toast.LENGTH_SHORT).show();
                            break;
                        case 2: // About
                            showAboutDialog();
                            break;
                    }
                })
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage("Expense Manager\nVersion 1.0\n\n" +
                        "Track your expenses and manage your finances effectively.\n\n" +
                        "Features:\n" +
                        "• Track income and expenses\n" +
                        "• Multiple accounts support\n" +
                        "• Detailed statistics\n" +
                        "• Category-wise tracking")
                .setPositiveButton("OK", null)
                .show();
    }

    public void getTransactions() {
        viewModel.getTransactions(calendar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}