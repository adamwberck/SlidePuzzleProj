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
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/// finish implementing stats
/// then add legit saving when win/lose
/// then maybe add saving a board to be loaded later

public class StatsActivity extends Activity {

    Button sbut;
    Button cbut;
    LinearLayout statList;
    PlayerStats stats;
    String path;
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
        path = in.getStringExtra("path");

        arrWidth = stats.getBoardWidth();
        arrHeight = stats.getBoardHeight();
        arrWidthMin = stats.getMinBoardWidth();
        arrWidthMax = stats.getMaxBoardWidth();
        arrHeightMin = stats.getMinBoardHeight();
        arrHeightMax = stats.getMaxBoardHeight();

        //Toast.makeText(StatsActivity.this, "Loaded stat " + stats.getBoardHeight(), Toast.LENGTH_LONG).show();


        /// the view containing list of stats types
        statList = findViewById(R.id.stats_list);

        //// have one button show the total global stats

        Button totalBut = new Button(this);
        totalBut.setText("Total Overall Stats");
        totalBut.setBackground(getResources().getDrawable(R.drawable.back_border));
        totalBut.setTypeface(totalBut.getTypeface(), Typeface.BOLD);
        totalBut.setTextSize(30);
        totalBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                Intent intStats = new Intent(StatsActivity.this, StatsEntryActivity.class);

                intStats.putExtra("isGlobal", true);
                intStats.putExtra("numGames", stats.getGlobalNumGames());
                intStats.putExtra("numMoves", stats.getGlobalNumMoves());
                intStats.putExtra("minMoves", stats.getGlobalMinMoves());
                intStats.putExtra("maxMoves", stats.getGlobalMaxMoves());
                intStats.putExtra("numUndos", stats.getGlobalNumUndos());
                intStats.putExtra("minUndos", stats.getGlobalMinUndos());
                intStats.putExtra("maxUndos", stats.getGlobalMaxUndos());
                intStats.putExtra("totTime", stats.getGlobalTotalTime());
                intStats.putExtra("minTime", stats.getGlobalMinTime());
                intStats.putExtra("maxTime", stats.getGlobalMaxTime());
                intStats.putExtra("numWins", stats.getGlobalNumWins());
                intStats.putExtra("numLosses", stats.getGlobalNumLosses());
                intStats.putExtra("avgMoves", stats.getGlobalAverageMoves());
                intStats.putExtra("avgUndos", stats.getGlobalAverageUndos());
                intStats.putExtra("avgTime", stats.getGlobalAverageTime());
                intStats.putExtra("numClassic", stats.getGlobalNumClassicMode());
                intStats.putExtra("numTimed", stats.getGlobalNumTimedMode());
                intStats.putExtra("numHints", stats.getGlobalNumHints());

                /// load final stat entry activity to show all stats for the chosen board type
                startActivity(intStats);
                }catch(Exception e){Toast.makeText(StatsActivity.this, e.getMessage() + "|" + e.getCause(), Toast.LENGTH_LONG).show();}


            }
        });
        statList.addView(totalBut);


        for(int x = 0; x < arrWidth; x++){
            for(int y = 0; y < arrHeight; y++){
                int adjX = x+arrWidthMin;
                int adjY = y+arrHeightMin;
                /////// add an entry to the linear list for each board stats
                if(stats.getBoardNumGames(adjX, adjY) > 0) {
                    Button but = new Button(this);
                    //but.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    //        LinearLayout.LayoutParams.WRAP_CONTENT));
                    but.setText((adjX) + "x" + (adjY));
                    but.setBackground(getResources().getDrawable(R.drawable.back_border));
                    but.setTypeface(but.getTypeface(), Typeface.BOLD);
                    but.setTextSize(30);

                    but.setOnClickListener(new StatEntryOnClick(StatsActivity.this, stats, adjX, adjY));
                    statList.addView(but);
                }
            }
        }

        statList.invalidate();

        cbut = findViewById(R.id.stats_delete);
        cbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    stats = new PlayerStats(arrWidthMin, arrHeightMin, arrWidthMax, arrHeightMax);
                    FileOutputStream fos = StatsActivity.this.openFileOutput(path, Context.MODE_PRIVATE);
                    ObjectOutputStream os = new ObjectOutputStream(fos);
                    os.writeObject(stats);
                    os.close();
                    fos.close();
                    finish();
                    //Toast.makeText(MenuActivity.this, "CREATED A NEW SAVE " + savePath, Toast.LENGTH_LONG).show();
                }catch(Exception e){
                    //Log.i("BAD STATS WRITER", e.getMessage() + " | " + e.getCause());
                    Toast.makeText(StatsActivity.this, "ERROR SAVE " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

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
            int numClassic = stats.getBoardNumClassic(adjWidth, adjHeight);
            int numTimed = stats.getBoardNumTimed(adjWidth, adjHeight);
            int numHints = stats.getBoardNumHints(adjWidth, adjHeight);

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
            intStats.putExtra("numClassic", numClassic);
            intStats.putExtra("numTimed", numTimed);
            intStats.putExtra("numHints", numHints);

            /// load final stat entry activity to show all stats for the chosen board type
            startActivity(intStats);
        }
    }
}
