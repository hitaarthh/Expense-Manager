package com.example.emanager.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.emanager.models.Transaction;
import com.example.emanager.utils.Constants;
import java.util.Calendar;
import java.util.Date;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainViewModel extends AndroidViewModel {

    public MutableLiveData<RealmResults<Transaction>> transactions = new MutableLiveData<>();
    public MutableLiveData<RealmResults<Transaction>> categoriesTransactions = new MutableLiveData<>();
    public MutableLiveData<Double> totalIncome = new MutableLiveData<>();
    public MutableLiveData<Double> totalExpense = new MutableLiveData<>();
    public MutableLiveData<Double> totalAmount = new MutableLiveData<>();

    private Realm realm;
    private Calendar calendar;

    public MainViewModel(@NonNull Application application) {
        super(application);
        Realm.init(application);
        setupDatabase();
    }

    public void getTransactions(Calendar calendar, String type) {
        this.calendar = calendar;
        resetCalendarTime(calendar);

        RealmResults<Transaction> newTransactions;
        if(Constants.SELECTED_TAB_STATS == Constants.DAILY) {
            Date startDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -1); // Reset calendar

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startDate)
                    .lessThan("date", endDate)
                    .equalTo("type", type)
                    .findAll();

        } else if(Constants.SELECTED_TAB_STATS == Constants.MONTHLY) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            Date startDate = calendar.getTime();

            calendar.add(Calendar.MONTH, 1);
            Date endDate = calendar.getTime();
            calendar.add(Calendar.MONTH, -1); // Reset calendar

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startDate)
                    .lessThan("date", endDate)
                    .equalTo("type", type)
                    .findAll();
        } else {
            newTransactions = realm.where(Transaction.class)
                    .equalTo("type", type)
                    .findAll();
        }

        categoriesTransactions.setValue(newTransactions);
    }

    public void getTransactions(Calendar calendar) {
        this.calendar = calendar;
        resetCalendarTime(calendar);

        RealmResults<Transaction> newTransactions;
        double income = 0;
        double expense = 0;

        if(Constants.SELECTED_TAB == Constants.DAILY) {
            Date startDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            Date endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -1); // Reset calendar

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startDate)
                    .lessThan("date", endDate)
                    .findAll();

            income = calculateIncomeForPeriod(startDate, endDate);
            expense = calculateExpenseForPeriod(startDate, endDate);

        } else if(Constants.SELECTED_TAB == Constants.MONTHLY) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            Date startDate = calendar.getTime();

            calendar.add(Calendar.MONTH, 1);
            Date endDate = calendar.getTime();
            calendar.add(Calendar.MONTH, -1); // Reset calendar

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startDate)
                    .lessThan("date", endDate)
                    .findAll();

            income = calculateIncomeForPeriod(startDate, endDate);
            expense = calculateExpenseForPeriod(startDate, endDate);
        } else {
            newTransactions = realm.where(Transaction.class).findAll();
            income = calculateTotalIncome();
            expense = calculateTotalExpense();
        }

        double total = income - expense;

        totalIncome.setValue(income);
        totalExpense.setValue(expense);
        totalAmount.setValue(total);
        transactions.setValue(newTransactions);
    }

    private double calculateIncomeForPeriod(Date startDate, Date endDate) {
        return realm.where(Transaction.class)
                .greaterThanOrEqualTo("date", startDate)
                .lessThan("date", endDate)
                .equalTo("type", Constants.INCOME)
                .sum("amount")
                .doubleValue();
    }

    private double calculateExpenseForPeriod(Date startDate, Date endDate) {
        return Math.abs(realm.where(Transaction.class)
                .greaterThanOrEqualTo("date", startDate)
                .lessThan("date", endDate)
                .equalTo("type", Constants.EXPENSE)
                .sum("amount")
                .doubleValue());
    }

    private double calculateTotalIncome() {
        return realm.where(Transaction.class)
                .equalTo("type", Constants.INCOME)
                .sum("amount")
                .doubleValue();
    }

    private double calculateTotalExpense() {
        return Math.abs(realm.where(Transaction.class)
                .equalTo("type", Constants.EXPENSE)
                .sum("amount")
                .doubleValue());
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null) return;

        realm.beginTransaction();
        try {
            // Validate and prepare the transaction
            if (transaction.getType().equals(Constants.EXPENSE)) {
                transaction.setAmount(Math.abs(transaction.getAmount()) * -1);
            } else {
                transaction.setAmount(Math.abs(transaction.getAmount()));
            }

            // If no date is set, use current date
            if (transaction.getDate() == null) {
                transaction.setDate(new Date());
            }

            // If no ID is set, use current timestamp
            if (transaction.getId() == 0) {
                transaction.setId(new Date().getTime());
            }

            realm.copyToRealmOrUpdate(transaction);
            realm.commitTransaction();

            // Refresh the transactions list
            if (calendar != null) {
                getTransactions(calendar);
            }
        } catch (Exception e) {
            realm.cancelTransaction();
            throw e;
        }
    }

    public void deleteTransaction(Transaction transaction) {
        if (transaction == null) return;

        realm.beginTransaction();
        try {
            transaction.deleteFromRealm();
            realm.commitTransaction();

            // Refresh the transactions list
            if (calendar != null) {
                getTransactions(calendar);
            }
        } catch (Exception e) {
            realm.cancelTransaction();
            throw e;
        }
    }

    public void addSampleTransactions() {
        realm.beginTransaction();
        try {
            // Sample income transactions
            realm.copyToRealmOrUpdate(new Transaction(
                    Constants.INCOME,
                    "Business",
                    "Cash",
                    "Monthly revenue",
                    new Date(),
                    5000,
                    new Date().getTime()
            ));

            // Sample expense transaction
            realm.copyToRealmOrUpdate(new Transaction(
                    Constants.EXPENSE,
                    "Investment",
                    "Bank",
                    "Stock market investment",
                    new Date(),
                    -1000,
                    new Date().getTime() + 1
            ));

            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();
            throw e;
        }
    }

    private void resetCalendarTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setupDatabase() {
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}