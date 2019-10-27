package com.example.slidepuzzleproj;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.logging.Logger;

public class PlayActivity extends Activity {
    private View changeImageButton;
    private ImageView puzzleImageView;
    private Button undo;
    private PuzzleBoard currentBoard;


    @Override
    protected void onCreate(Bundle savedInstanceState){

    }
}