package com.example.emanager.views.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.emanager.R;
import com.example.emanager.adapters.AccountsAdapter;
import com.example.emanager.adapters.CategoryAdapter;
import com.example.emanager.databinding.FragmentAddTransactionBinding;
import com.example.emanager.databinding.ListDialogBinding;
import com.example.emanager.models.Account;
import com.example.emanager.models.Category;
import com.example.emanager.models.Transaction;
import com.example.emanager.utils.Constants;
import com.example.emanager.utils.Helper;
import com.example.emanager.views.activites.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddTransactionFragment extends BottomSheetDialogFragment {

    private FragmentAddTransactionBinding binding;
    private Transaction transaction;
    private Calendar calendar;

    public AddTransactionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = Calendar.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater);
        initializeViews();
        return binding.getRoot();
    }

    private void initializeViews() {
        // Initialize new transaction
        transaction = new Transaction();
        transaction.setType(Constants.INCOME); // Default type
        transaction.setDate(calendar.getTime()); // Default date (today)
        transaction.setId(calendar.getTimeInMillis());

        // Set default date text
        binding.date.setText(Helper.formatDate(calendar.getTime()));

        setupTransactionTypeButtons();
        setupDatePicker();
        setupCategorySelection();
        setupAccountSelection();
        setupSaveButton();
    }

    private void setupTransactionTypeButtons() {
        // Default state (Income selected)
        updateTransactionTypeUI(true);

        binding.incomeBtn.setOnClickListener(view -> {
            updateTransactionTypeUI(true);
            transaction.setType(Constants.INCOME);
        });

        binding.expenseBtn.setOnClickListener(view -> {
            updateTransactionTypeUI(false);
            transaction.setType(Constants.EXPENSE);
        });
    }

    private void updateTransactionTypeUI(boolean isIncome) {
        if (isIncome) {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.income_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.greenColor));
        } else {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.expense_selector));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.redColor));
        }
    }

    private void setupDatePicker() {
        binding.date.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (datePicker, year, month, day) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        Date selectedDate = calendar.getTime();
                        binding.date.setText(Helper.formatDate(selectedDate));
                        transaction.setDate(selectedDate);
                        transaction.setId(calendar.getTimeInMillis());
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // Set max date to today
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void setupCategorySelection() {
        binding.category.setOnClickListener(view -> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(getLayoutInflater());
            AlertDialog categoryDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Select Category")
                    .setView(dialogBinding.getRoot())
                    .create();

            CategoryAdapter categoryAdapter = new CategoryAdapter(
                    getContext(),
                    Constants.categories,
                    category -> {
                        binding.category.setText(category.getCategoryName());
                        transaction.setCategory(category.getCategoryName());
                        categoryDialog.dismiss();
                    }
            );

            dialogBinding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            dialogBinding.recyclerView.setAdapter(categoryAdapter);
            categoryDialog.show();
        });
    }

    private void setupAccountSelection() {
        binding.account.setOnClickListener(view -> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(getLayoutInflater());
            AlertDialog accountsDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Select Account")
                    .setView(dialogBinding.getRoot())
                    .create();

            ArrayList<Account> accounts = new ArrayList<>();
            accounts.add(new Account(0, "Cash"));
            accounts.add(new Account(0, "Bank"));
            accounts.add(new Account(0, "PayTM"));
            accounts.add(new Account(0, "EasyPaisa"));
            accounts.add(new Account(0, "Other"));

            AccountsAdapter adapter = new AccountsAdapter(
                    getContext(),
                    accounts,
                    account -> {
                        binding.account.setText(account.getAccountName());
                        transaction.setAccount(account.getAccountName());
                        accountsDialog.dismiss();
                    }
            );

            dialogBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            dialogBinding.recyclerView.setAdapter(adapter);
            accountsDialog.show();
        });
    }

    private void setupSaveButton() {
        binding.saveTransactionBtn.setOnClickListener(view -> {
            if (validateTransaction()) {
                try {
                    double amount = Double.parseDouble(binding.amount.getText().toString());
                    String note = binding.note.getText().toString().trim();

                    transaction.setAmount(amount);
                    transaction.setNote(note);

                    // Save transaction using ViewModel
                    ((MainActivity) requireActivity()).viewModel.addTransaction(transaction);
                    ((MainActivity) requireActivity()).getTransactions();

                    Toast.makeText(getContext(), "Transaction saved successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Error saving transaction: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateTransaction() {
        if (TextUtils.isEmpty(binding.amount.getText())) {
            binding.amount.setError("Please enter amount");
            return false;
        }

        try {
            double amount = Double.parseDouble(binding.amount.getText().toString());
            if (amount <= 0) {
                binding.amount.setError("Amount must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            binding.amount.setError("Invalid amount");
            return false;
        }

        if (TextUtils.isEmpty(binding.category.getText())) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(binding.account.getText())) {
            Toast.makeText(getContext(), "Please select an account", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}