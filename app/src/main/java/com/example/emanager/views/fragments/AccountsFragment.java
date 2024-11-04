package com.example.emanager.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.emanager.adapters.AccountsAdapter;
import com.example.emanager.databinding.FragmentAccountsBinding;
import com.example.emanager.models.Account;
import com.example.emanager.models.Transaction;
import com.example.emanager.views.activites.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

public class AccountsFragment extends Fragment {

    private FragmentAccountsBinding binding;
    private Realm realm;
    private AccountsAdapter accountsAdapter;
    private ArrayList<Account> accounts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountsBinding.inflate(inflater, container, false);
        realm = Realm.getDefaultInstance();

        setupAccountsList();
        setupAddAccountButton();
        return binding.getRoot();
    }

    private void setupAccountsList() {
        accounts = new ArrayList<>();
        Map<String, Double> accountBalances = calculateAccountBalances();
        Map<String, Integer> transactionCounts = getTransactionCounts();

        // Add default accounts with calculated balances
        accounts.add(new Account(accountBalances.getOrDefault("Cash", 0.0), "Cash"));
        accounts.add(new Account(accountBalances.getOrDefault("Bank", 0.0), "Bank"));
        accounts.add(new Account(accountBalances.getOrDefault("Card", 0.0), "Card"));
        accounts.add(new Account(accountBalances.getOrDefault("Other", 0.0), "Other"));

        accountsAdapter = new AccountsAdapter(getContext(), accounts, account ->
                showAccountDetails(account, transactionCounts.getOrDefault(account.getAccountName(), 0))
        );

        binding.accountsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.accountsRecyclerView.setAdapter(accountsAdapter);

        // Update total balance
        double totalBalance = accounts.stream()
                .mapToDouble(Account::getAccountAmount)
                .sum();
        binding.totalBalanceAmount.setText(String.format("₹%.2f", totalBalance));
    }

    private Map<String, Double> calculateAccountBalances() {
        Map<String, Double> balances = new HashMap<>();
        RealmResults<Transaction> transactions = realm.where(Transaction.class).findAll();

        for (Transaction transaction : transactions) {
            String accountName = transaction.getAccount();
            double currentBalance = balances.getOrDefault(accountName, 0.0);
            balances.put(accountName, currentBalance + transaction.getAmount());
        }

        return balances;
    }

    private Map<String, Integer> getTransactionCounts() {
        Map<String, Integer> counts = new HashMap<>();
        RealmResults<Transaction> transactions = realm.where(Transaction.class).findAll();

        for (Transaction transaction : transactions) {
            String accountName = transaction.getAccount();
            int currentCount = counts.getOrDefault(accountName, 0);
            counts.put(accountName, currentCount + 1);
        }

        return counts;
    }

    private void showAccountDetails(Account account, int transactionCount) {
        RealmResults<Transaction> accountTransactions = realm.where(Transaction.class)
                .equalTo("account", account.getAccountName())
                .findAll();

        double income = 0;
        double expense = 0;

        for (Transaction transaction : accountTransactions) {
            if (transaction.getAmount() > 0) {
                income += transaction.getAmount();
            } else {
                expense += Math.abs(transaction.getAmount());
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(account.getAccountName())
                .setMessage(String.format(
                        "Balance: ₹%.2f\n\n" +
                                "Total Income: ₹%.2f\n" +
                                "Total Expense: ₹%.2f\n\n" +
                                "Total Transactions: %d",
                        account.getAccountAmount(),
                        income,
                        expense,
                        transactionCount
                ))
                .setPositiveButton("OK", null)
                .show();
    }

    private void setupAddAccountButton() {
        binding.addAccountFab.setOnClickListener(v -> {
            // For now, show a message that custom accounts are coming soon
            androidx.appcompat.app.AlertDialog.Builder dialog =
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext());
            dialog.setTitle("Custom Accounts");
            dialog.setMessage("Custom account creation coming soon!");
            dialog.setPositiveButton("OK", null);
            dialog.show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realm != null) {
            realm.close();
        }
        binding = null;
    }
}