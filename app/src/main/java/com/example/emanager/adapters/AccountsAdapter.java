package com.example.emanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emanager.R;
import com.example.emanager.databinding.RowAccountBinding;
import com.example.emanager.models.Account;
import com.example.emanager.models.Transaction;

import java.util.ArrayList;

import io.realm.Realm;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.AccountsViewHolder> {

    private Context context;
    private ArrayList<Account> accountArrayList;
    private AccountsClickListener accountsClickListener;
    private Realm realm;

    public interface AccountsClickListener {
        void onAccountSelected(Account account);
    }

    public AccountsAdapter(Context context, ArrayList<Account> accountArrayList, AccountsClickListener accountsClickListener) {
        this.context = context;
        this.accountArrayList = accountArrayList;
        this.accountsClickListener = accountsClickListener;
        this.realm = Realm.getDefaultInstance();
    }

    @NonNull
    @Override
    public AccountsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountsViewHolder(LayoutInflater.from(context).inflate(R.layout.row_account, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountsViewHolder holder, int position) {
        Account account = accountArrayList.get(position);

        // Set account name
        holder.binding.accountName.setText(account.getAccountName());

        // Set account balance
        holder.binding.accountBalance.setText(String.format("₹%.2f", account.getAccountAmount()));

        // Set transaction count
        int transactionCount = realm.where(Transaction.class)
                .equalTo("account", account.getAccountName())
                .findAll().size();
        holder.binding.transactionCount.setText(
                String.format("%d transaction%s",
                        transactionCount,
                        transactionCount == 1 ? "" : "s")
        );

        holder.itemView.setOnClickListener(v -> {
            if (accountsClickListener != null) {
                accountsClickListener.onAccountSelected(account);
            }
        });
    }

    @Override
    public int getItemCount() {
        return accountArrayList.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

    public static class AccountsViewHolder extends RecyclerView.ViewHolder {
        RowAccountBinding binding;

        public AccountsViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowAccountBinding.bind(itemView);
        }
    }
}