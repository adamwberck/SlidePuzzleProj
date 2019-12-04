package com.example.slidepuzzleproj;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.lang.NonNull;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;

import org.bson.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static androidx.core.content.FileProvider.getUriForFile;

public class MenuActivity extends Activity {
    private View changeImageButton;
    private ImageView puzzleImageView;
    private Button playButton;
    private Button inboxButton;
    private int width = 3 , height=3;
    private static final int PICK_IMAGE = 100;
    private static final int DIMENSION = 200;
    Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        final StitchAppClient client = Stitch.getDefaultAppClient();
        client.getAuth().loginWithCredential(new AnonymousCredential()).addOnCompleteListener(
                new OnCompleteListener<StitchUser>() {
                    @Override
                    public void onComplete(@NonNull final Task<StitchUser> task) {
                        if (task.isSuccessful()) {
                            Log.d("myApp", String.format(
                                    "logged in as user %s with provider %s",
                                    task.getResult().getId(),
                                    task.getResult().getLoggedInProviderType()));
                        } else {
                            Log.e("myApp", "failed to log in", task.getException());
                        }
                    }
                });

        setContentView(R.layout.activity_menu); //attach the layout
        //currentBoard = PuzzleLab.get(this).getCurrentBoard();
        puzzleImageView = findViewById(R.id.puzzle_image);
        //puzzleImageView.setImageBitmap(currentBoard.getPuzzleImage());
        /// set default image
        puzzleImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.wooloo2));

        changeImageButton = findViewById(R.id.change_image);
        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                openDialog();
            }
        });

        inboxButton = findViewById(R.id.inbox_button);
        inboxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(User.getID()==null) {
                    Toast.makeText(MenuActivity.this, "You must first log in or sign up.", Toast.LENGTH_SHORT).show();
                    Intent playIntent = new Intent(MenuActivity.this, LoginActivity.class);
                    startActivity(playIntent);
                } else {
                    Intent getChallengeIntent = new Intent(MenuActivity.this, InboxActivity.class);
                    startActivity(getChallengeIntent);
                }
            }
        });

        playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playIntent = new Intent(MenuActivity.this, PlayActivity.class);
                playIntent.putExtra("WIDTH", width);
                playIntent.putExtra("HEIGHT", height);
                if(imageUri == null)
                {
                    imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.wooloo2);
                }
                playIntent.putExtra("picture",imageUri);
                startActivityForResult(playIntent, DIMENSION);
            }
        });
        ArrayAdapter<CharSequence> adapter
                = ArrayAdapter.createFromResource(
                this,R.array.numbers,R.layout.beter_spinner);
        adapter.setDropDownViewResource(R.layout.beter_spinner_item);

        Spinner spinnerW = findViewById(R.id.width);
        spinnerW.setAdapter(adapter);
        //spinnerW.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Spinner spinnerH = findViewById(R.id.height);
        spinnerH.setAdapter(adapter);
        //spinnerH.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        spinnerH.setOnItemSelectedListener(new DimenListener(0));
        spinnerW.setOnItemSelectedListener(new DimenListener(1));

    }

    private class DimenListener implements AdapterView.OnItemSelectedListener {
        private int heightOrWidth;

        private DimenListener(int heightOrWidth){
            this.heightOrWidth = heightOrWidth;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(heightOrWidth==0){
                height = position+3;
            }
            else{
                width = position+3;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private void openDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose Image Source")
                    .setView(R.layout.photoselect)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            sourceChoice(dialog);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            openGallery();
        }

    }

    private void sourceChoice(DialogInterface dialog){
        RadioGroup rg = ((AlertDialog) dialog).findViewById(R.id.source_radio);
        int checked = rg.getCheckedRadioButtonId();
        dialog.dismiss();
        //Log.e("Checked", "" + checked);
        switch (checked){
            case R.id.source1:
                openGallery();
                break;
            case R.id.source2:
                useCamera();
                break;
            case R.id.source3:
                onlineImage();
                break;
            default:
        }
    }

    public void useCamera() {
        PackageManager pm = this.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            try {
                photoFile = SaveImage.createImage(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoURI = getUriForFile(this, "com.example.slidepuzzleproj.provider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(cameraIntent,
                        1);

                return;
            }
        }
        Toast toast = Toast.makeText(this, "Cannot get photo", Toast.LENGTH_SHORT);
        toast.show();
    }

    private void onlineImage(){
        Intent flickrAPI = new Intent(MenuActivity.this, FlickrActivity.class);
        startActivityForResult(flickrAPI, 1);
    }

    private void openGallery()
    {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                imageUri = data.getData();
            } else {
                Log.i("INFO", SaveImage.getPath());
                imageUri = Uri.parse(SaveImage.getPath());
            }
            puzzleImageView.setImageURI(imageUri);
        }
    }
}