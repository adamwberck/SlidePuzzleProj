package com.example.slidepuzzleproj;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MenuActivity extends Activity {
    private View changeImageButton;
    private ImageView puzzleImageView;
    private Button playButton, dimenButton, changeButton;
    private int width, height;
    private static final int PICK_IMAGE = 100;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState){


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu); //attach the layout
        //currentBoard = PuzzleLab.get(this).getCurrentBoard();

        puzzleImageView = findViewById(R.id.puzzle_image);


        //puzzleImageView.setImageBitmap(currentBoard.getPuzzleImage());



        changeButton = (Button)findViewById(R.id.test);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                openGallery();
            }
        });



        dimenButton = findViewById(R.id.dimension_button);
        String[] dimen = dimenButton.getText().toString().split("x");
        this.width = Integer.parseInt(dimen[0]);
        this.height = Integer.parseInt(dimen[1]);

        dimenButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //TODO change dimension of board

            }
        });

        playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO goto puzzle activity
                Intent playIntent = new Intent(MenuActivity.this, PlayActivity.class);
                playIntent.putExtra("WIDTH", width);
                playIntent.putExtra("HEIGHT", height);
                startActivity(playIntent);
            }
        });

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
