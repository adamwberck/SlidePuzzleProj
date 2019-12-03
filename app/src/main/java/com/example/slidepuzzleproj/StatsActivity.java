package com.example.slidepuzzleproj;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);


        try {
            Intent in = getIntent();
            stats = (PlayerStats)in.getSerializableExtra("save");


            Toast.makeText(StatsActivity.this, "Loaded stat " + stats.getGlobalBoardMaxMoves(), Toast.LENGTH_LONG).show();

        }
        catch(Exception e){ Toast.makeText(StatsActivity.this, "ERROR STATS", Toast.LENGTH_LONG).show(); }
        /// add to the list //linear layout

        statList = findViewById(R.id.stats_list);

        sbut = findViewById(R.id.stats_x);
        sbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Toast.makeText(StatsActivity.this, "Done stat", Toast.LENGTH_LONG).show();

        /*
        Uri uri = in.getParcelableExtra("img");

        pimg = findViewById(R.id.prev_img);
        try {
            map = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            if (map==null) {
                File f = new File(uri.toString());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                map = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            }
        }catch(Exception e){
            File f= new File(uri.toString());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                map = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        pimg.setImageBitmap(map);
        pimg.invalidate();

         */
    }

}
