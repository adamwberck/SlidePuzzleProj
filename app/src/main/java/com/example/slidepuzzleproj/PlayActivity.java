package com.example.slidepuzzleproj;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.util.logging.Logger;

import static java.lang.Math.min;

public class PlayActivity extends Activity {
    private TextView timer;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        undo = findViewById(R.id.undoButton);
        tips = findViewById(R.id.tipsButton);
        menu = findViewById(R.id.menuButton);
        timer = findViewById(R.id.time);
        playSpace = findViewById(R.id.playSpace);

        setupBoard(playSpace, getIntent().getIntExtra("WIDTH", 3),
                            getIntent().getIntExtra("HEIGHT", 3));
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
            PuzzleBoard puz = new PuzzleBoard(bm, w, h);
            ///////////////////////////////////////////////////


            ///////////////////////////////
            ////// setting up ticking timer
            this.timerTick = new CountDownTimer(PLAY_TIME, ONE_SECOND) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timer.setText(millisUntilFinished/ONE_MINUTE + ":" + millisUntilFinished%ONE_MINUTE/ONE_SECOND);
                }

                @Override
                public void onFinish() {

                }
            };
            /////////////////////////////

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
                    if(i != puz.getBlankIndex())
                        pieces[i].setImageBitmap(puz.getPiece(i).getBitmap());
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
        }
    }
}