package edu.ewubd.cse489project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SlidingPuzzleCustomPictureListActivity extends AppCompatActivity {
    private final int TOTAL_PICTURES = 5;
    private List<Picture> pictureList;
    TextView btnAdd;
    RadioButton[] rbPicture;
    public static final int PICK_IMAGE = 1;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap picture = null;
        if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            // User selected an image from the gallery
            try {
                Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                picture = Bitmap.createScaledBitmap(bm, 250, 250, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == PICK_IMAGE && data != null) {
            // User captured a new image from the camera
            Bundle extras = data.getExtras();
            Bitmap bm = (Bitmap) extras.get("data");
            picture = Bitmap.createScaledBitmap(bm, 250, 250, true);
        }
        if (picture != null) {
            insertPicture(picture);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_puzzle_custom_picture_list);

        pictureList = new ArrayList<>();
        rbPicture = new RadioButton[TOTAL_PICTURES];
        btnAdd = findViewById(R.id.btnAdd);

        findViewById(R.id.btnExit).setOnClickListener(view -> {finish();});

        btnAdd.setOnClickListener(view -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
            pickIntent.setType("image/*");

            Intent chooserIntent = Intent.createChooser(pickIntent, "Open with");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {cameraIntent});

            startActivityForResult(chooserIntent, PICK_IMAGE);
        });

        btnAdd.setOnClickListener(view -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
            pickIntent.setType("image/*");

            Intent chooserIntent = Intent.createChooser(pickIntent, "Open with");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {cameraIntent});

            startActivityForResult(chooserIntent, PICK_IMAGE);
        });

        findViewById(R.id.btnStart).setOnClickListener(view -> {
            for (int i = 0; i < TOTAL_PICTURES; i++) {
                if (rbPicture[i].isChecked()) {
                    Intent intentMenu = this.getIntent();
                    Bundle bundle = intentMenu.getExtras();
                    bundle.putString("customPictureKey", pictureList.get(i).key);

                    Intent intent = new Intent(SlidingPuzzleCustomPictureListActivity.this, SlidingPuzzleActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });


        findViewById(R.id.btnDeletePicture0).setOnClickListener(view -> { deletePicture(0);});
        findViewById(R.id.btnDeletePicture1).setOnClickListener(view -> { deletePicture(1);});
        findViewById(R.id.btnDeletePicture2).setOnClickListener(view -> { deletePicture(2);});
        findViewById(R.id.btnDeletePicture3).setOnClickListener(view -> { deletePicture(3);});
        findViewById(R.id.btnDeletePicture4).setOnClickListener(view -> { deletePicture(4);});

        for (int i = 0; i < TOTAL_PICTURES; i++) {
            String viewIdString = "rbPicture" + i;
            int viewId = getResources().getIdentifier(viewIdString, "id", getPackageName());
            rbPicture[i] = findViewById(viewId);
            int finalI = i;
            rbPicture[i].setOnClickListener(view -> {
                rbPicture[finalI].setChecked(true);
                for (int j = 0; j < TOTAL_PICTURES; j++) {
                    if (j != finalI) {
                        rbPicture[j].setChecked(false);
                    }
                }
            });
        }

        setPictures();
    }

    private void setPictures() {
        pictureList.clear();

        KeyValueDB db = new KeyValueDB(SlidingPuzzleCustomPictureListActivity.this);
        Cursor rows = db.getAllKeyValues();
        if (rows.getCount() != 0) {
            //events = new Event[rows.getCount()];
            while (rows.moveToNext()) {
                String key = rows.getString(0);
                String encodedPicture = rows.getString(1);

                //decode base64 string to image
                byte[] byteArrayImage = Base64.decode(encodedPicture, Base64.DEFAULT);
                Bitmap decodedPicture = BitmapFactory.decodeByteArray(byteArrayImage, 0, byteArrayImage.length);

                pictureList.add(new Picture(key, encodedPicture, decodedPicture));
            }
        }
        db.close();

        showAndHideImageOptions();

        if (pictureList.size() == TOTAL_PICTURES)
            btnAdd.setVisibility(View.GONE);
        else
            btnAdd.setVisibility(View.VISIBLE);
    }

    private void showAndHideImageOptions() {
        for (int i = 0; i < TOTAL_PICTURES; i++) {
            String viewIdString = "llPicture" + i;
            int viewId = getResources().getIdentifier(viewIdString, "id", getPackageName());
            if (i < pictureList.size())
                findViewById(viewId).setVisibility(View.VISIBLE);
            else
                findViewById(viewId).setVisibility(View.GONE);
        }
    }

    private void insertPicture(Bitmap picture) {
        KeyValueDB db = new KeyValueDB(SlidingPuzzleCustomPictureListActivity.this);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArrayImage = baos.toByteArray();

        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        String key = String.valueOf(System.currentTimeMillis());

        db.insertKeyValue(key, encodedImage);
        db.close();

        setPictures();
    }

    private void deletePicture(int i) {
        Picture picture = pictureList.get(i);

        KeyValueDB db = new KeyValueDB(SlidingPuzzleCustomPictureListActivity.this);

        db.deleteDataByKey(picture.key);

        db.close();

        setPictures();
    }
}

class Picture{
    String key;
    String encodedPicture;
    Bitmap picture;

    Picture(String key, String encodedPicture, Bitmap picture) {
        this.key = key;
        this.encodedPicture = encodedPicture;
        this.picture = picture;
    }
}