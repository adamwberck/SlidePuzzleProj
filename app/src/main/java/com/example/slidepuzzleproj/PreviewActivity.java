package com.example.slidepuzzleproj;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
    }
}
