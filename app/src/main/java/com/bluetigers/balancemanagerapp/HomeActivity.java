package com.bluetigers.balancemanagerapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bluetigers.balancemanagerapp.dialogs.EarningsDialog;
import com.bluetigers.balancemanagerapp.dialogs.OutgoingsDialog;
import com.bluetigers.balancemanagerapp.utils.DatabaseConnection;
import com.bluetigers.balancemanagerapp.utils.models.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


// TODO create movements activity
public class HomeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, EarningsDialog.EarningsDialogListener,
        OutgoingsDialog.OutgoingsDialogListener {

    private static final String TAG = "HomeActivity";
    private static final int RC_SIGN_IN = 123;

    private float earningsAmount = 0;
    private float outgoingsAmount = 0;

    private String username;

    private FirebaseUser firebaseUser;

    private DatabaseReference databaseReference;
    private DatabaseConnection databaseConnection;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private TextView earningsTxtView;
    private TextView outgoingsTxtView;
    private TextView balanceTxtView;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseConnection.closeConnection();
        Log.v(TAG, "dbConnection: Disconnected!");
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        mDrawerLayout = findViewById(R.id.home_drawer_layout);
        mNavigationView = findViewById(R.id.home_nav_view);
        Toolbar mToolbar = findViewById(R.id.home_toolbar);

        setSupportActionBar(mToolbar);

        mNavigationView.bringToFront();
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(R.id.nav_dashboard);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mDrawerLayout.addDrawerListener(drawerToggle);

        drawerToggle.syncState();

        databaseConnection = new DatabaseConnection();

        if (firebaseUser != null) {
            setupHeader();
            getDataFromDatabase();

            earningsTxtView = findViewById(R.id.home_earnings_textView);
            earningsTxtView.setText(earningsAmount + " €");

            Button earningsBtn = findViewById(R.id.home_earnings_addBtn);
            earningsBtn.setOnClickListener(v -> openEarningsDialog());

            outgoingsTxtView = findViewById(R.id.home_outgoings_textView);
            outgoingsTxtView.setText(outgoingsAmount + " €");

            Button outgoingsBtn = findViewById(R.id.home_outgoiings_addBtn);
            outgoingsBtn.setOnClickListener(v -> openOutgoingsDialog());

            balanceTxtView = findViewById(R.id.home_balance_textView);
            balanceTxtView.setText(earningsAmount - outgoingsAmount + " €");
        } else
            signIN();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (firebaseUser != null) {
            User user = new User(firebaseUser.getEmail(), firebaseUser.getDisplayName());
            databaseReference.child("users").child(firebaseUser.getUid()).setValue(user);

            checkForUsersExists();
            getDataFromDatabase();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_logout:
                logout();
                signIN();
                break;
            case R.id.nav_balance:
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void signIN() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.splash_logo)
                        .build(),
                RC_SIGN_IN);
    }

    private void logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    Log.v(TAG, "firebaseAuth: Logout successfully!");
                    firebaseUser = null;
                });
    }

    private void setupHeader() {
        View headerView = mNavigationView.getHeaderView(0);

        TextView usernameTxt = headerView.findViewById(R.id.header_username);
        usernameTxt.setText(firebaseUser.getDisplayName());
    }

    private void getDataFromDatabase() {

        Statement statement = databaseConnection.getStatement();

        try {
            ResultSet set = statement.executeQuery("SELECT * FROM public.users WHERE email = '" + firebaseUser.getEmail() + "';");

            while (set.next())
                username = set.getString("username");

            String sql = "CREATE SCHEMA IF NOT EXISTS " + username;

            int response = databaseConnection.getStatement().executeUpdate(sql);

            if (response != 0)
                Log.e(TAG, "dbQuery: Creation of schema " + username + " failed!");

            sql = "CREATE TABLE IF NOT EXISTS " + username + ".earnings(" +
                    "id serial PRIMARY KEY," +
                    "description text DEFAULT 'Entrata'," +
                    "amount float NOT NULL," +
                    "date DATE NOT NULL);";

            response = databaseConnection.getStatement().executeUpdate(sql);

            if (response != 0)
                Log.e(TAG, "dbQuery: Creation of table earnings failed!");

            sql = "CREATE TABLE IF NOT EXISTS " + username + ".outgoings(" +
                    "id serial PRIMARY KEY," +
                    "description text DEFAULT 'Uscita'," +
                    "amount float NOT NULL," +
                    "date DATE NOT NULL);";

            response = databaseConnection.getStatement().executeUpdate(sql);

            if (response != 0)
                Log.e(TAG, "dbQuery: Creation of table outgoings failed!");

            getEarningsAndOutgoings();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void checkForUsersExists() {
        try {
            ResultSet set = databaseConnection.getStatement().executeQuery("SELECT * FROM public.users " +
                    "WHERE email = '" + firebaseUser.getEmail() + "';");

            String dbEmail = "";

            while (set.next())
                dbEmail = set.getString("email");

            String[] name = Objects.requireNonNull(firebaseUser.getDisplayName()).split(" ");

            if (!dbEmail.equals(firebaseUser.getEmail())) {
                Log.w(TAG, "dbQuery: user not registered!" + firebaseUser.getDisplayName());

                String sql = "INSERT INTO public.users (email, password, username)" +
                        "VALUES (?, ?, ?);";

                PreparedStatement preparedStatement = databaseConnection.getConnection()
                        .prepareStatement(sql);
                preparedStatement.clearParameters();

                preparedStatement.setString(1, firebaseUser.getEmail());
                preparedStatement.setString(2, firebaseUser.getUid());
                preparedStatement.setString(3, name[0]);

                int response = preparedStatement.executeUpdate();

                Log.v(TAG, "dbQuery: registration result - " + response);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void getEarningsAndOutgoings() throws SQLException {
        // Retrive earnings and outgoings

        ResultSet set = databaseConnection.getStatement().executeQuery("SELECT SUM(amount) FROM " + username + ".earnings;");

        while (set.next())
            earningsAmount = set.getFloat("sum");

        set = databaseConnection.getStatement().executeQuery("SELECT SUM(amount) FROM " + username + ".outgoings;");

        while (set.next())
            outgoingsAmount = set.getFloat("sum");
    }

    @SuppressLint("SetTextI18n")
    private void refreshTextViews() {
        try {
            getEarningsAndOutgoings();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        earningsTxtView.setText(earningsAmount + " €");
        outgoingsTxtView.setText(outgoingsAmount + " €");
        balanceTxtView.setText(earningsAmount - outgoingsAmount + " €");
    }

    private void openEarningsDialog() {
        EarningsDialog earningsDialog = new EarningsDialog();
        earningsDialog.show(getSupportFragmentManager(), "earnings dialog");
    }

    private void openOutgoingsDialog() {
        OutgoingsDialog outgoingsDialog = new OutgoingsDialog();
        outgoingsDialog.show(getSupportFragmentManager(), "outgoings dialog");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                Log.v(TAG, "firebaseAuth: Login successfully!");
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                mNavigationView.setCheckedItem(R.id.nav_dashboard);

                setupHeader();
                checkForUsersExists();
                getDataFromDatabase();
            } else {
                assert response != null;
                Log.e(TAG, "firebaseAuth: " + Objects.requireNonNull(response.getError()).getErrorCode());
            }
        }
    }

    @Override
    public void applyEarnings(float amount, Date date, String description) {
        String sql = "INSERT INTO " + username + ".earnings (description, amount, date)" +
                "VALUES (?, ?, ?);";

        PreparedStatement preparedStatement;

        try {
            preparedStatement = databaseConnection.getConnection()
                    .prepareStatement(sql);

            preparedStatement.clearParameters();

            preparedStatement.setString(1, description);
            preparedStatement.setFloat(2, amount);
            preparedStatement.setDate(3, date);

            int response = preparedStatement.executeUpdate();

            Log.v(TAG, "dbQuery: earnings response - " + response);

            refreshTextViews();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void applyOutgoings(float amount, Date date, String description) {
        String sql = "INSERT INTO " + username + ".outgoings (description, amount, date)" +
                "VALUES (?, ?, ?);";

        PreparedStatement preparedStatement;

        try {
            preparedStatement = databaseConnection.getConnection()
                    .prepareStatement(sql);

            preparedStatement.clearParameters();

            preparedStatement.setString(1, description);
            preparedStatement.setFloat(2, amount);
            preparedStatement.setDate(3, date);

            int response = preparedStatement.executeUpdate();

            Log.v(TAG, "dbQuery: outgoings response - " + response);

            refreshTextViews();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}