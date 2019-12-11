package com.example.slidepuzzleproj;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StatsEntryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);

        /// setup the layout for each individual stat
        setContentView(R.layout.activity_statsentry);

        //// a bunch of values will get passed
        Intent intStats = getIntent();
        boolean isGlobal = intStats.getBooleanExtra("isGlobal", false);
        int adjX = intStats.getIntExtra("adjWidth", -1);
        int adjY = intStats.getIntExtra("adjHeight", -1);
        int numGames = intStats.getIntExtra("numGames", 0);
        int numMoves = intStats.getIntExtra("numMoves", -1);
        int minMoves = intStats.getIntExtra("minMoves", -1);
        int maxMoves = intStats.getIntExtra("maxMoves", -1);
        int numUndos = intStats.getIntExtra("numUndos", -1);
        int minUndos = intStats.getIntExtra("minUndos", -1);
        int maxUndos = intStats.getIntExtra("maxUndos", -1);
        int totTime = intStats.getIntExtra("totTime", -1);
        int minTime = intStats.getIntExtra("minTime", -1);
        int maxTime = intStats.getIntExtra("maxTime", -1);
        int numWins = intStats.getIntExtra("numWins", -1);
        int numLosses = intStats.getIntExtra("numLosses", -1);
        int avgMoves = intStats.getIntExtra("avgMoves", -1);
        int avgUndos = intStats.getIntExtra("avgUndos", -1);
        int avgTime = intStats.getIntExtra("avgTime", -1);
        int numClassic = intStats.getIntExtra("numClassic", -1);
        int numTimed = intStats.getIntExtra("numTimed", -1);
        int numHints = intStats.getIntExtra("numHints", -1);

        LinearLayout elist = findViewById(R.id.stats_entry_list);
        /////////
        // add all the stat values to the list
        //

        if (!isGlobal) {
            TextView v1 = new TextView(this);
            v1.setTextSize(20);
            v1.setTextColor(Color.BLACK);
            v1.setTypeface(v1.getTypeface(), Typeface.BOLD);
            v1.setPadding(10, 2, 2, 2);
            v1.setBackground(getResources().getDrawable(R.drawable.back_border));
            v1.setText("Board Width: " + adjX);
            elist.addView(v1);

            TextView v2 = new TextView(this);
            v2.setTextSize(20);
            v2.setTextColor(Color.BLACK);
            v2.setTypeface(v2.getTypeface(), Typeface.BOLD);
            v2.setPadding(10, 2, 2, 2);
            v2.setBackground(getResources().getDrawable(R.drawable.back_border));
            v2.setText("Board Height: " + adjY);
            elist.addView(v2);
        }

        TextView v3 = new TextView(this);
        v3.setTextSize(20);
        v3.setTextColor(Color.BLACK);
        v3.setTypeface(v3.getTypeface(), Typeface.BOLD);
        v3.setPadding(10, 2, 2, 2);
        v3.setBackground(getResources().getDrawable(R.drawable.back_border));
        v3.setText("Total # of Games: " + numGames);
        elist.addView(v3);

        TextView v19 = new TextView(this);
        v19.setTextSize(20);
        v19.setTextColor(Color.BLACK);
        v19.setTypeface(v19.getTypeface(), Typeface.BOLD);
        v19.setPadding(10, 2, 2, 2);
        v19.setBackground(getResources().getDrawable(R.drawable.back_border));
        v19.setText("Total # of Classic Mode: " + numClassic);
        elist.addView(v19);

        TextView v18 = new TextView(this);
        v18.setTextSize(20);
        v18.setTextColor(Color.BLACK);
        v18.setTypeface(v18.getTypeface(), Typeface.BOLD);
        v18.setPadding(10, 2, 2, 2);
        v18.setBackground(getResources().getDrawable(R.drawable.back_border));
        v18.setText("Total # of Timed Mode: " + numTimed);
        elist.addView(v18);

        TextView v16 = new TextView(this);
        v16.setTextSize(20);
        v16.setTextColor(Color.BLACK);
        v16.setTypeface(v16.getTypeface(), Typeface.BOLD);
        v16.setPadding(10, 2, 2, 2);
        v16.setBackground(getResources().getDrawable(R.drawable.back_border));
        v16.setText("Number of Timed Wins: " + numWins);
        elist.addView(v16);

        TextView v17 = new TextView(this);
        v17.setTextSize(20);
        v17.setTextColor(Color.BLACK);
        v17.setTypeface(v17.getTypeface(), Typeface.BOLD);
        v17.setPadding(10, 2, 2, 2);
        v17.setBackground(getResources().getDrawable(R.drawable.back_border));
        v17.setText("Number of Timed Losses: " + numLosses);
        elist.addView(v17);

        TextView v20 = new TextView(this);
        v20.setTextSize(20);
        v20.setTextColor(Color.BLACK);
        v20.setTypeface(v20.getTypeface(), Typeface.BOLD);
        v20.setPadding(10, 2, 2, 2);
        v20.setBackground(getResources().getDrawable(R.drawable.back_border));
        v20.setText("Number of Hints Used: " + numHints);
        elist.addView(v20);

        TextView v4 = new TextView(this);
        v4.setTextSize(20);
        v4.setTextColor(Color.BLACK);
        v4.setTypeface(v4.getTypeface(), Typeface.BOLD);
        v4.setPadding(10, 2, 2, 2);
        v4.setBackground(getResources().getDrawable(R.drawable.back_border));
        v4.setText("Total # of Moves: " + numMoves);
        elist.addView(v4);

        TextView v5 = new TextView(this);
        v5.setTextSize(20);
        v5.setTextColor(Color.BLACK);
        v5.setTypeface(v5.getTypeface(), Typeface.BOLD);
        v5.setPadding(10, 2, 2, 2);
        v5.setBackground(getResources().getDrawable(R.drawable.back_border));
        v5.setText("Minimum # of Moves: " + minMoves);
        elist.addView(v5);

        TextView v6 = new TextView(this);
        v6.setTextSize(20);
        v6.setTextColor(Color.BLACK);
        v6.setTypeface(v6.getTypeface(), Typeface.BOLD);
        v6.setPadding(10, 2, 2, 2);
        v6.setBackground(getResources().getDrawable(R.drawable.back_border));
        v6.setText("Maximum # of Moves: " + maxMoves);
        elist.addView(v6);

        TextView v7 = new TextView(this);
        v7.setTextSize(20);
        v7.setTextColor(Color.BLACK);
        v7.setTypeface(v7.getTypeface(), Typeface.BOLD);
        v7.setPadding(10, 2, 2, 2);
        v7.setBackground(getResources().getDrawable(R.drawable.back_border));
        v7.setText("Average # of Moves: " + avgMoves);
        elist.addView(v7);

        TextView v8 = new TextView(this);
        v8.setTextSize(20);
        v8.setTextColor(Color.BLACK);
        v8.setTypeface(v8.getTypeface(), Typeface.BOLD);
        v8.setPadding(10, 2, 2, 2);
        v8.setBackground(getResources().getDrawable(R.drawable.back_border));
        v8.setText("Total # of Undo: " + numUndos);
        elist.addView(v8);

        TextView v9 = new TextView(this);
        v9.setTextSize(20);
        v9.setTextColor(Color.BLACK);
        v9.setTypeface(v9.getTypeface(), Typeface.BOLD);
        v9.setPadding(10, 2, 2, 2);
        v9.setBackground(getResources().getDrawable(R.drawable.back_border));
        v9.setText("Minimum # of Undo: " + minUndos);
        elist.addView(v9);

        TextView v10 = new TextView(this);
        v10.setTextSize(20);
        v10.setTextColor(Color.BLACK);
        v10.setTypeface(v10.getTypeface(), Typeface.BOLD);
        v10.setPadding(10, 2, 2, 2);
        v10.setBackground(getResources().getDrawable(R.drawable.back_border));
        v10.setText("Maximum # of Undo: " + maxUndos);
        elist.addView(v10);

        TextView v11 = new TextView(this);
        v11.setTextSize(20);
        v11.setTextColor(Color.BLACK);
        v11.setTypeface(v11.getTypeface(), Typeface.BOLD);
        v11.setPadding(10, 2, 2, 2);
        v11.setBackground(getResources().getDrawable(R.drawable.back_border));
        v11.setText("Average # of Undo: " + avgUndos);
        elist.addView(v11);

        TextView v12 = new TextView(this);
        v12.setTextSize(20);
        v12.setTextColor(Color.BLACK);
        v12.setTypeface(v12.getTypeface(), Typeface.BOLD);
        v12.setPadding(10, 2, 2, 2);
        v12.setBackground(getResources().getDrawable(R.drawable.back_border));
        v12.setText("Total Time Taken: " + totTime);
        elist.addView(v12);

        TextView v13 = new TextView(this);
        v13.setTextSize(20);
        v13.setTextColor(Color.BLACK);
        v13.setTypeface(v13.getTypeface(), Typeface.BOLD);
        v13.setPadding(10, 2, 2, 2);
        v13.setBackground(getResources().getDrawable(R.drawable.back_border));
        v13.setText("Minimum Time Taken: " + minTime);
        elist.addView(v13);

        TextView v14 = new TextView(this);
        v14.setTextSize(20);
        v14.setTextColor(Color.BLACK);
        v14.setTypeface(v14.getTypeface(), Typeface.BOLD);
        v14.setPadding(10, 2, 2, 2);
        v14.setBackground(getResources().getDrawable(R.drawable.back_border));
        v14.setText("Maximum Time Taken: " + maxTime);
        elist.addView(v14);

        TextView v15 = new TextView(this);
        v15.setTextSize(20);
        v15.setTextColor(Color.BLACK);
        v15.setTypeface(v15.getTypeface(), Typeface.BOLD);
        v15.setPadding(10, 2, 2, 2);
        v15.setBackground(getResources().getDrawable(R.drawable.back_border));
        v15.setText("Average Time Taken: " + avgTime);
        elist.addView(v15);

        elist.invalidate();

        Button sebut = findViewById(R.id.stats_entry_x);
        sebut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
