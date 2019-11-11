package com.example.slidepuzzleproj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Dimension;

public class MenuActivity extends Activity {
    private View changeImageButton;
    private ImageView puzzleImageView;
    private Button playButton;
    private int width = 3 , height=3;
    private static final int PICK_IMAGE = 100;
    private static final int DIMENSION = 200;
    Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


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
                openGallery();
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


    private void openGallery()
    {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK  && requestCode == PICK_IMAGE)
        {
            imageUri = data.getData();
            puzzleImageView.setImageURI(imageUri);
        }
    }

}
