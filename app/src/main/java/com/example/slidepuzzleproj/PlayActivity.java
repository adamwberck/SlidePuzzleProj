package com.example.slidepuzzleproj;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.lang.Math.min;

public class PlayActivity extends Activity {
    private TextView timer;
    private TextView moveNum;
    private int moveInt;

    private Button undo;
    private Button tips;
    private Button menu;
    private GridLayout playSpace;
    private CountDownTimer timerTick;
    private PuzzleBoard currentBoard;
    private final long ONE_MINUTE = 60000;
    private final long ONE_SECOND = 1000;
    private final long PLAY_TIME = 6 * ONE_MINUTE;
    private ImageView[] pieces;

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

        width = getIntent().getIntExtra("WIDTH", 3);
        height = getIntent().getIntExtra("HEIGHT", 3);
        setupBoard(playSpace,width,height);
    }
    protected void scrambleBoard(){
        List<PuzzleBoard.Direction> results = new ArrayList<>(4);
        results.add(PuzzleBoard.Direction.Up);
        results.add(PuzzleBoard.Direction.Down);
        results.add(PuzzleBoard.Direction.Left);
        results.add(PuzzleBoard.Direction.Right);
        for(int i=0;i<100;i++){
            PuzzleBoard.Direction d = results.get(0);
            currentBoard.slideBlank(d);
        }
        playSpace.invalidate();
    }



    protected void setupBoard(final GridLayout playSpace, int w, int h){
        try {
            Log.i("[DEBUG BOARD]", playSpace.getWidth() + "," + playSpace.getHeight());

            //////////////////////////
            ////// setting up the bitmap dimension and puzzle board
            DisplayMetrics display = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(display);

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ilya);
            int newWid = bm.getWidth();
            int newHei = bm.getHeight();
            Log.i("[ORIGINAL DIMENSION]", newWid+ ", " + newHei);
            if(bm.getWidth() > bm.getHeight())
            {
                newWid = min(display.widthPixels, display.heightPixels);
                double ratio = (double)newWid/(double)bm.getWidth();
                newHei = (int)(newHei * ratio);
            }
            else{
                newHei = min(display.widthPixels, display.heightPixels);
                double ratio = (double)newHei/(double)bm.getHeight();
                newWid = (int)(newWid * ratio);
            }
            Log.i("[NEW DIMENSION]", newWid+ ", " + newHei);
            bm = Bitmap.createScaledBitmap(bm, newWid, newHei, true);
            currentBoard  = new PuzzleBoard(bm, w, h);
          
            ////// setting up ticking timer
            this.timerTick = new CountDownTimer(PLAY_TIME, ONE_SECOND) {
                @Override
                public void onTick(long millisUntilFinished) {
                    DecimalFormat df = new DecimalFormat("00");
                    timer.setText(millisUntilFinished/ONE_MINUTE + ":"
                            + df.format(millisUntilFinished%ONE_MINUTE/ONE_SECOND));
                }

                @Override
                public void onFinish() {

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

            this.timerTick.start();
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
            /*
            if(!scrambled){
                scrambled = true;
                scrambleBoard();
                return;
            }*/
            if(d!=null){
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

                moveInt++;
                moveNum.setText(moveInt+"");
                moveNum.invalidate();
            }
        }
    }
}