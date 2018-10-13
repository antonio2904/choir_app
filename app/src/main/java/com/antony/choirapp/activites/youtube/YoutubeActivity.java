package com.antony.choirapp.activites.youtube;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.antony.choirapp.R;
import com.antony.choirapp.utils.AppController;

public class YoutubeActivity extends AppCompatActivity {

    private BottomNavigationView mBottomNavigationview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        initUi();

    }

    private void initUi() {

        mBottomNavigationview = findViewById(R.id.bottomNavigationView);
        mBottomNavigationview.setSelectedItemId(R.id.navigation_youtube);
        mBottomNavigationview.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {

                    case R.id.navigation_home:

//                        onBackPressed();
                        break;

                    case R.id.navigation_karaoke:

                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBottomNavigationview != null)
            mBottomNavigationview.setSelectedItemId(R.id.navigation_youtube);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
