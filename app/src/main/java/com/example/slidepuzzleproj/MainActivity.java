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

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setupMenu();
        setupBoard();   /// showing the puzzle board component
        //setupMenuEvents();
    }

    private void setupMenu()
    {

        //// set up default layout
        RelativeLayout mainLayout = new RelativeLayout(this);
        mainLayout.setBackgroundColor(Color.LTGRAY);

        //// set up the menu title text
        TextView menutitle = new TextView(this);
        menutitle.setId(R.id.menutitle);
        menutitle.setText(getResources().getString(R.string.menu_title));
        menutitle.setTextColor(getResources().getColor(R.color.menu1));
        menutitle.setTextSize(menutitle.getTextSize() * 1.1f);

        //// set up the menu play button
        Button playbut = new Button(this);
        playbut.setId(R.id.menuplay);
        playbut.setText(getResources().getString(R.string.menu_play));
        playbut.setTextSize(playbut.getTextSize() * 1.1f);

        //// set up menu title layout params
        RelativeLayout.LayoutParams menutitleRules =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
        menutitleRules.addRule(RelativeLayout.CENTER_HORIZONTAL);
        menutitleRules.addRule(RelativeLayout.ABOVE, playbut.getId());
        menutitleRules.setMargins(0,0,0,100);

        //// set up menu play button layout params
        RelativeLayout.LayoutParams playbutRules =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
        playbutRules.addRule(RelativeLayout.CENTER_HORIZONTAL);
        playbutRules.addRule(RelativeLayout.CENTER_VERTICAL);

        //// set the layout params
        menutitle.setLayoutParams(menutitleRules);
        playbut.setLayoutParams(playbutRules);

        //// add views to the main layout
        mainLayout.addView(menutitle);
        mainLayout.addView(playbut);

        //// set the main layout to the content view
        setContentView(mainLayout);


    }

    public int min(int a, int b)
    {
        return (a < b) ? a : b;
    }

    private void setupBoard()
    {

        try {
            DisplayMetrics display = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(display);

            /// get the initial bitmap
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.knacko);
            int newWid = bm.getWidth();
            int newHei = bm.getHeight();


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



            bm = Bitmap.createScaledBitmap(bm, newWid, newHei, true);


            int w = 7, h = 4;
            PuzzleBoard puz = new PuzzleBoard(bm, w, h);

            //// test code below
            GridLayout boardLayout = new GridLayout(this);
            //boardLayout.setOrientation(GridLayout.HORIZONTAL);
            boardLayout.setRowCount(h);
            boardLayout.setColumnCount(w);
            boardLayout.setBackgroundColor(Color.DKGRAY);

            int i = 0;
            ImageView[] pieces = new ImageView[w*h];
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

                    if(i != puz.getBlankIndex())
                        pieces[i].setImageBitmap(puz.getPiece(i).getBitmap());
                    boardLayout.addView(pieces[i], boardRules);
                    i++;
                }
            }



            setContentView(boardLayout);
            ////// end test //////
        }
        catch(Exception e)
        {
            Log.i("[ERROR]", "Failed to splice bitmap");
        }

    }

    private void setupMenuEvents()
    {
        Button playbut = (Button) findViewById(R.id.menuplay);

        playbut.setOnClickListener(
                new Button.OnClickListener(){
                    @Override
                    public void onClick(View v) {

                        /// Load a new activity?

                        /// test code
                        Button butClick = (Button)v;

                        butClick.setTextColor(Color.RED);
                    }
                }
        );



    }


}
