package edu.ewubd.cse489project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.RadioButton;

public class SlidingPuzzleMenuActivity extends AppCompatActivity {
    Bitmap customPicture = null;
    RadioButton rbCustomPicture, rbDefaultPicture1, rbNumberTiles, rbDefaultPicture2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_puzzle_menu);

        rbCustomPicture = findViewById(R.id.rbCustomPicture);
        rbDefaultPicture1 = findViewById(R.id.rbDefaultPicture1);
        rbNumberTiles = findViewById(R.id.rbNumberTiles);
        rbDefaultPicture2 = findViewById(R.id.rbDefaultPicture2);

        findViewById(R.id.btnExit).setOnClickListener(view -> {finish();});

        findViewById(R.id.btnStart).setOnClickListener(view -> {
            String difficulty = "";
            if (((RadioButton)findViewById(R.id.rbEasy)).isChecked()) difficulty = "Easy";
            else if (((RadioButton)findViewById(R.id.rbMedium)).isChecked()) difficulty = "Medium";
            else difficulty = "Hard";

            String pictureMode = "";
            if (rbNumberTiles.isChecked()) pictureMode = "NumberTiles";
            else if (rbDefaultPicture1.isChecked()) pictureMode = "DefaultPicture1";
            else if (rbDefaultPicture2.isChecked()) pictureMode = "DefaultPicture2";
            else pictureMode = "CustomPicture";

            Bundle bundle = new Bundle();
            bundle.putString("difficulty", difficulty);
            bundle.putString("pictureMode", pictureMode);
//            if (pictureMode == "CustomPicture") {
//                bundle.putParcelable("customPicture", customPicture);
//            }

            Intent intent;
            if (pictureMode.equals("CustomPicture"))
                intent = new Intent(SlidingPuzzleMenuActivity.this, SlidingPuzzleCustomPictureListActivity.class);
            else
                intent = new Intent(SlidingPuzzleMenuActivity.this, SlidingPuzzleActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        });
    }
}