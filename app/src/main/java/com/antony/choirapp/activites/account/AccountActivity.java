package com.antony.choirapp.activites.account;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.antony.choirapp.BuildConfig;
import com.antony.choirapp.R;
import com.antony.choirapp.utils.AppController;
import com.google.firebase.database.FirebaseDatabase;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mUserNameTextView;
    private TextView mChangeNameTextView;
    private TextView mShareAppTextView;
    private TextView mAboutTextView;
    private SharedPreferences sharedPref;
    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initUi();
    }

    private void initUi() {

        mBottomNavigationView = findViewById(R.id.bottomNavigationView);
        mBottomNavigationView.setSelectedItemId(R.id.navigation_profile);

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.navigation_home:

                        onBackPressed();
                }
                return true;
            }
        });

        findViewById(R.id.toolbar).findViewById(R.id.toolbar_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        mUserNameTextView = findViewById(R.id.text_username);
        mUserNameTextView.setText(AppController.mUserName);

        mChangeNameTextView = findViewById(R.id.text_change_name);
        mChangeNameTextView.setOnClickListener(this);

        mShareAppTextView = findViewById(R.id.text_share_app);
        mShareAppTextView.setOnClickListener(this);

        mAboutTextView = findViewById(R.id.text_app_version);
        mAboutTextView.setText("App Version : " + BuildConfig.VERSION_NAME);
        mAboutTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.text_change_name:

                showDialog();
                break;

            case R.id.text_share_app:

                break;

            case R.id.text_app_version:

                break;
        }
    }

    private void showDialog() {

        sharedPref = this.getSharedPreferences(
                "mtc_choir", Context.MODE_PRIVATE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your Name");
        View view = getLayoutInflater().inflate(R.layout.layout_user_dialog, null);
        final EditText et = view.findViewById(R.id.et_username);
        builder.setView(view);
        builder.setPositiveButton("Ok", null);
        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {

                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!et.getText().toString().isEmpty()) {
                            AppController.mUserName = et.getText().toString();
                            sharedPref.edit().putString("user", AppController.mUserName).apply();
                            FirebaseDatabase db = FirebaseDatabase.getInstance();
                            db.getReference().child("Users").child(et.getText().toString()).setValue(et.getText().toString());
                            dialog.dismiss();
                            mUserNameTextView.setText(et.getText().toString());
                        }
                    }
                });

                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();
                    }
                });
            }
        });

        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBottomNavigationView.setSelectedItemId(R.id.navigation_profile);
    }

    @Override
    public void onBackPressed() {

        finish();
        overridePendingTransition(0, 0);
    }
}
