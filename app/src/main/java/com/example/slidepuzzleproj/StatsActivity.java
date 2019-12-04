package com.example.slidepuzzleproj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/// finish implementing stats
/// then add legit saving when win/lose
/// then maybe add saving a board to be loaded later

public class StatsActivity extends Activity {

    Button sbut;
    LinearLayout statList;
    PlayerStats stats;
    int arrWidth, arrHeight;
    int arrWidthMin, arrWidthMax;
    int arrHeightMin, arrHeightMax;
    Button[][] statButs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Intent in = getIntent();
        stats = (PlayerStats)in.getSerializableExtra("save");

        arrWidth = stats.getBoardWidth();
        arrHeight = stats.getBoardHeight();
        arrWidthMin = stats.getMinBoardWidth();
        arrWidthMax = stats.getMaxBoardWidth();
        arrHeightMin = stats.getMinBoardHeight();
        arrHeightMax = stats.getMaxBoardHeight();

        //Toast.makeText(StatsActivity.this, "Loaded stat " + stats.getBoardHeight(), Toast.LENGTH_LONG).show();

        //statButs = new Button[arrHeight][arrWidth];
        statList = findViewById(R.id.stats_list);
        //// have one button show the total stats
        Button totalBut = new Button(this);


        for(int x = 0; x < arrWidth; x++){
            for(int y = 0; y < arrHeight; y++){
                int adjX = x+arrWidthMin;
                int adjY = y+arrHeightMin;
                /////// add an entry to the linear list for each board stats
                //if(stats.getBoardNumGames(adjX, adjY) > 0) {
                    Button but = new Button(this);
                    but.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    but.setText((adjX) + "x" + (adjY));
                    but.setBackground(getResources().getDrawable(R.drawable.back_border));
                    but.setTypeface(but.getTypeface(), Typeface.BOLD);
                    but.setTextSize(30);

                    but.setOnClickListener(new StatEntryOnClick(StatsActivity.this, stats, adjX, adjY));
                    statList.addView(but);
                //}
            }
        }
        statList.invalidate();

        sbut = findViewById(R.id.stats_x);
        sbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Toast.makeText(StatsActivity.this, "Done stat", Toast.LENGTH_LONG).show();

    }

    protected class StatEntryOnClick implements View.OnClickListener {
        private Context cont;
        private PlayerStats stats2;
        private int adjWidth;
        private int adjHeight;
        public StatEntryOnClick(Context cont, PlayerStats stats2, int width, int height)
        {
            this.cont = cont;
            this.stats2 = stats;
            this.adjWidth = width;
            this.adjHeight = height;
        }
        @Override
        public void onClick(View v){
            /// load new StatsEntryActivity activity
            /// load the stats activity


            Intent intStats = new Intent(StatsActivity.this, StatsEntryActivity.class);
            //Toast.makeText(StatsActivity.this, "I did it", Toast.LENGTH_LONG).show();

            int numGames = stats.getBoardNumGames(adjWidth, adjHeight);
            int numMoves = stats.getBoardNumMoves(adjWidth, adjHeight);
            int minMoves = stats.getBoardMinMoves(adjWidth, adjHeight);
            int maxMoves = stats.getBoardMaxMoves(adjWidth, adjHeight);
            int numUndos = stats.getBoardNumUndos(adjWidth, adjHeight);
            int minUndos = stats.getBoardMinUndos(adjWidth, adjHeight);
            int maxUndos = stats.getBoardMaxUndos(adjWidth, adjHeight);
            int totTime = stats.getBoardTotalTime(adjWidth, adjHeight);
            int minTime = stats.getBoardMinTime(adjWidth, adjHeight);
            int maxTime = stats.getBoardMaxTime(adjWidth, adjHeight);
            int numWins = stats.getBoardNumWins(adjWidth, adjHeight);
            int numLosses = stats.getBoardNumLosses(adjWidth, adjHeight);
            int avgMoves = stats.getBoardAverageMoves(adjWidth, adjHeight);
            int avgUndos = stats.getBoardAverageUndos(adjWidth, adjHeight);
            int avgTime = stats.getBoardAverageTime(adjWidth, adjHeight);

            intStats.putExtra("adjWidth", this.adjWidth);
            intStats.putExtra("adjHeight", this.adjHeight);
            intStats.putExtra("numGames", numGames);
            intStats.putExtra("numMoves", numMoves);
            intStats.putExtra("minMoves", minMoves);
            intStats.putExtra("maxMoves", maxMoves);
            intStats.putExtra("numUndos", numUndos);
            intStats.putExtra("minUndos", minUndos);
            intStats.putExtra("maxUndos", maxUndos);
            intStats.putExtra("totTime", totTime);
            intStats.putExtra("minTime", minTime);
            intStats.putExtra("maxTime", maxTime);
            intStats.putExtra("numWins", numWins);
            intStats.putExtra("numLosses", numLosses);
            intStats.putExtra("avgMoves", avgMoves);
            intStats.putExtra("avgUndos", avgUndos);
            intStats.putExtra("avgTime", avgTime);

            /// load final stat entry activity to show all stats for the chosen board type
            startActivity(intStats);
        }
    }
}
