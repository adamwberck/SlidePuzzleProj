package com.example.slidepuzzleproj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MenuActivity extends Activity {
    private View changeImageButton;
    private ImageView puzzleImageView;
    private Button playButton;
    private PuzzleBoard currentBoard;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu); //attach the layout
        //currentBoard = PuzzleLab.get(this).getCurrentBoard();
        changeImageButton = findViewById(R.id.change_image);
        puzzleImageView = findViewById(R.id.puzzle_image);

        //puzzleImageView.setImageBitmap(currentBoard.getPuzzleImage());

        playButton = findViewById(R.id.play_button);
        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO add image change activity
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this,PuzzleActivity.class);
                startActivity(intent);
            }
        });
    }

}
