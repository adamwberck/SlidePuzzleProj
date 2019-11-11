package com.example.slidepuzzleproj;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class PreviewActivity extends Activity {

    Button pbut;
    ImageView pimg;
    Bitmap map;// = BitmapFactory.decodeResource(getResources(), R.drawable.ilya);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_layout);

        Intent in = getIntent();

        pbut = findViewById(R.id.prev_x);
        pbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        pbut.invalidate();


        pimg = findViewById(R.id.prev_img);
        pimg.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ilya));

        pimg.invalidate();
    }
}
