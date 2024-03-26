package edu.ewubd.cse489project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnFourInARow).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, FourInARowActivity.class));
        });
        findViewById(R.id.btnSlidingPuzzle).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, SlidingPuzzleMenuActivity.class));
        });
        findViewById(R.id.btnExit).setOnClickListener(view -> {
            finish();
        });
    }
}