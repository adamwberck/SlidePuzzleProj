package com.example.slidepuzzleproj;

import android.app.Activity;
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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class StatsActivity extends Activity {

    Button sbut;
    LinearLayout statList;
    PlayerStats stats;
    int arrWidth, arrHeight;
    int arrWidthMin, arrWidthMax;
    int arrHeightMin, arrHeightMax;
    //Button[][] statButs;
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
        for(int x = 0; x < arrWidth; x++){
            for(int y = 0; y < arrHeight; y++){
                /////// add an entry to the linear list for each board stats
                if(stats.getBoardNumGames(x+arrWidthMin, y+arrHeightMin) > 0) {
                    Button but = new Button(this);
                    but.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    but.setText((x + arrWidthMin) + "x" + (y + arrHeightMin));
                    but.setBackground(getResources().getDrawable(R.drawable.back_border));
                    but.setTypeface(but.getTypeface(), Typeface.BOLD);
                    but.setTextSize(30);

                    but.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /// load new StatsEntryActivity activity

                        }
                    });
                    statList.addView(but);
                }
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


    protected class StatsEntryActivity extends Activity{
        @Override
        protected void onCreate(Bundle saveInstanceState){
            super.onCreate(saveInstanceState);

            /// setup the layout for each individual stat
            setContentView(R.layout.activity_stats);

            Intent in = getIntent();
            stats = (PlayerStats)in.getSerializableExtra("save");
        }
    }
}
