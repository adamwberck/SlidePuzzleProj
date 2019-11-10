package com.example.slidepuzzleproj;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.logging.Logger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.lang.Math.min;

public class PlayActivity extends Activity {
    private TextView timer;
    private TextView moveNum;
    private PlayerStats stats;
    private int moveInt;
    private long timeElapsed; //the current ellapsed time
    private long timeRemain; // the millisecond time remaining for the puzzle

    private Button undo;
    private Button tips;
    private Button menu;
    private GridLayout playSpace;
    private CountDownTimer timerTick;
    private PuzzleBoard currentBoard;
    private final long ONE_MINUTE = 60000;
    private final long ONE_SECOND = 1000;
    private final long PLAY_TIME = 3 * ONE_MINUTE;
    private ImageView[] pieces;
    private Bitmap bitmap;

    private int width;
    private int height;

    private boolean scrambled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        undo = findViewById(R.id.undoButton);
        tips = findViewById(R.id.tipsButton);
        menu = findViewById(R.id.menuButton);
        timer = findViewById(R.id.time);
        moveNum = findViewById(R.id.moveNumber);
        playSpace = findViewById(R.id.playSpace);

        Intent intent = getIntent();
        Uri imageUri = intent.getParcelableExtra("picture");
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        }catch(IOException e){
            Log.i("[TEST]", "TEST");
        }catch (NullPointerException npe){
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ilya);
        }

        setupBoard(playSpace, getIntent().getIntExtra("WIDTH", 3),
                getIntent().getIntExtra("HEIGHT", 3), bitmap);

    }

    protected void setupBoard(final GridLayout playSpace, int w, int h,Bitmap bm){
        width = getIntent().getIntExtra("WIDTH", 3);
        height = getIntent().getIntExtra("HEIGHT", 3);
        setupBoard(playSpace,width,height);
    }
    protected void scrambleBoard(){
        for(int i=0;i<250;i++){
            slideImages(currentBoard.slideBlankRandom());
        }
    }



    protected void setupBoard(final GridLayout playSpace, int w, int h){
        try {
            Log.i("[DEBUG BOARD]", playSpace.getWidth() + "," + playSpace.getHeight());

            //////////////////////////
            ////// setting up the bitmap dimension and puzzle board
            DisplayMetrics display = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(display);

            //bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ilya);
            
            int newWid = bitmap.getWidth();
            int newHei = bitmap.getHeight();
            Log.i("[ORIGINAL DIMENSION]", newWid+ ", " + newHei);
            if(bitmap.getWidth() > bitmap.getHeight())
            {
                newWid = min(display.widthPixels, display.heightPixels);
                double ratio = (double)newWid/(double)bitmap.getWidth();
                newHei = (int)(newHei * ratio);
            }
            else{
                newHei = min(display.widthPixels, display.heightPixels);
                double ratio = (double)newHei/(double)bitmap.getHeight();
                newWid = (int)(newWid * ratio);
            }
            Log.i("[NEW DIMENSION]", newWid+ ", " + newHei);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWid, newHei, true);
            currentBoard  = new PuzzleBoard(bitmap, w, h);
          
            ////// setting up ticking timer
            this.timerTick = new CountDownTimer(PLAY_TIME, ONE_SECOND) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //DecimalFormat df = new DecimalFormat("00");
                    String text = getString(R.string.time_string,
                            millisUntilFinished/ONE_MINUTE,
                            millisUntilFinished%ONE_MINUTE/ONE_SECOND);
                    timer.setText(text);
                    timeElapsed = PLAY_TIME - millisUntilFinished;
                    timeRemain = millisUntilFinished;
                }

                @Override
                public void onFinish() {
                    timer.setText(getString(R.string.time_gameover));
                    timeElapsed = PLAY_TIME;
                    timeRemain = 0;
                }
            };

            //// test code below
            playSpace.setRowCount(h);
            playSpace.setColumnCount(w);
            playSpace.setBackgroundColor(Color.DKGRAY);

            int i = 0;
            pieces = new ImageView[w*h];
            for(int y = 0; y < h; y++)
            {
                for(int x = 0; x < w; x++)
                {
                    GridLayout.Spec row = GridLayout.spec(y, 1);
                    GridLayout.Spec col = GridLayout.spec(x, 1);
                    GridLayout.LayoutParams boardRules = new GridLayout.LayoutParams(row, col);
                    boardRules.setMargins(2,2,2,2);
                    //boardRules.setGravity(Gravity.FILL);
                    pieces[i] = new ImageView(this);
                    pieces[i].setOnClickListener(new PieceListener(i));
                    if(i != currentBoard.getBlankIndex())
                        pieces[i].setImageBitmap(currentBoard.getPiece(i).getBitmap());
                    playSpace.addView(pieces[i], boardRules);
                    i++;
                }
            }
            ////// end test //////

            //this.timerTick.start();
        }
        catch(Exception e)
        {
            Log.i("[ERROR]", "Failed to splice bitmap");
        }
    }

    private class PieceListener implements View.OnClickListener {
        private int mNumOfView;
        private PieceListener(int i) {
            mNumOfView = i;
        }

        @Override
        public void onClick(View v) {
            Log.i("puz","clicked "+mNumOfView);
            PuzzleBoard.Direction d = currentBoard.dirNextToBlank(mNumOfView);
            if(!scrambled){
                scrambled = true;
                scrambleBoard();
                timerTick.start();
                return;
            }
            if(d!=null){
                slideImages(d);

                //increment number
                moveInt++;
                moveNum.setText(moveInt+"");
                moveNum.invalidate();
            }
        }
    }

    public void slideImages(PuzzleBoard.Direction d){
        currentBoard.slideBlank(d);
        for(int i=0;i<width*height;i++) {
            if(i!=currentBoard.getBlankIndex()) {
                pieces[i].setImageBitmap(currentBoard.getPiece(i).getBitmap());
                pieces[i].setVisibility(View.VISIBLE);
            }else{
                pieces[i].setVisibility(View.INVISIBLE);
            }
        }
        playSpace.invalidate();
    }
}