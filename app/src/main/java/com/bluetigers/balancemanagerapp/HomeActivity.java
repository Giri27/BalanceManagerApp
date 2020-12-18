package com.bluetigers.balancemanagerapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bluetigers.balancemanagerapp.utils.models.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HomeActivity";
    private static final int RC_SIGN_IN = 123;

    private FirebaseUser firebaseUser;

    private DatabaseReference databaseReference;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

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

        if (firebaseUser != null)
            setupHeader();
        else
            signIN();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (firebaseUser != null) {
            User user = new User(firebaseUser.getEmail(), firebaseUser.getDisplayName());
            databaseReference.child("users").child(firebaseUser.getUid()).setValue(user);
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
            } else {
                assert response != null;
                Log.e(TAG, "firebaseAuth: " + Objects.requireNonNull(response.getError()).getErrorCode());
            }
        }
    }
}