package com.example.slidepuzzleproj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Stack;

import static java.lang.Math.min;

public class PlayActivity extends Activity {
    private ContextWrapper cw; //context / working directory of app

    private TextView timer;
    private TextView moveNum;

    //private PlayerStats stats;
    //private PuzzleLab lab;

    private Button undo;
    private Button tips;
    private Button menu;
    private Button prev;
    private GridLayout playSpace;
    private TextView restartText;
    private Button statbut;

    private final long ONE_MINUTE = 60000;
    private final long ONE_SECOND = 1000;
    private long playTime = 3 * ONE_MINUTE;
    private final int GAP = 2;
    private ImageView[] pieces;
    private MediaPlayer menuBGM;

    private int moveInt = 0;    // save
    private int undoInt = 0;
    private long timeElapsed;   //save
    private long timeRemain;    // save
    private CountDownTimer timerTick;   // save
    private PuzzleBoard currentBoard;   // save
    private Bitmap bitmap = null;   //save
    private Uri imageUri;   // save
    private boolean isWin;  // save
    private boolean isLose; // save
    private int width;  // save
    private int height; // save
    private boolean isScrambled = false; // save
    private Stack<PuzzleBoard.Direction> undoStack = new Stack<>(); //save
    private boolean play; // save

    private PlayerStats stats = null;
    private String savePath;
    //private boolean foundFile = false;
    private int minb;
    private int maxb;

    @Override
    protected void onPause()
    {
        super.onPause();
        menuBGM.pause();
        play = false;
    }

    @Override
    protected void onResume(){
        super.onResume();
        menuBGM.start();
        play = true;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Intent intent = getIntent(); ///intent args
        stats = (PlayerStats)intent.getSerializableExtra("save"); /// get save file

        savePath = getResources().getString(R.string.saveFile);
        minb = Integer.parseInt(getResources().getString(R.string.min_board_size));
        maxb = Integer.parseInt(getResources().getString(R.string.max_board_size));
        //stats = new PlayerStats(minb, minb, maxb, maxb);

        cw = new ContextWrapper(getApplicationContext());

        undo = findViewById(R.id.undoButton);
        tips = findViewById(R.id.tipsButton);
        menu = findViewById(R.id.menuButton);
        prev = findViewById(R.id.previewButton);
        timer = findViewById(R.id.time);


        moveNum = findViewById(R.id.moveNumber);
        moveNum.setText(String.format("%d", moveInt));

        menuBGM = MediaPlayer.create(this, R.raw.wotw);
        menuBGM.start();
        play = true;

        playSpace = findViewById(R.id.playSpace);
        restartText = findViewById(R.id.restartText);
        restartText.setVisibility(View.INVISIBLE);
        restartText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLose || isWin){
                   restart();
                }

            }
        });

        undo.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View v) {
                if(!isLose && !isWin && !undoStack.isEmpty()) {
                    PuzzleBoard.Direction d = undoStack.pop();
                    d = PuzzleBoard.getOpposite(d);
                    slideImages(d);
                    moveInt--;
                    undoInt++;
                    moveNum.setText(String.format("%d", moveInt));
                    moveNum.invalidate();
                    //TODO fix the timer after winning
                    //This works except the timer is broken
                    if(isWin) {
                        restartText.setVisibility(View.INVISIBLE);
                        playSpace.setBackgroundColor(Color.DKGRAY);
                        isWin = false;
                    }

                }
            }
        });

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                PopupMenu popupMenu = new PopupMenu(PlayActivity.this, menu);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.main_menu:
                                //Intent intent = new Intent(PlayActivity.this, MenuActivity.class);
                                //startActivity(intent);
                                finish();
                                return true;

                            case R.id.statistics_menu:
                                /// load the stats activity
                                Intent intStats = new Intent(PlayActivity.this, StatsActivity.class);

                                intStats.putExtra("path", savePath);
                                intStats.putExtra("save", stats);

                                startActivity(intStats);
                                return true;
                            case R.id.mute_menu:
                                if (play == true) {
                                    menuBGM.pause();
                                    play = false;
                                }
                                else if (play == false)
                                {
                                    menuBGM.start();
                                    play = true;
                                }
                                return true;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new Solver(currentBoard);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        });

        width = getIntent().getIntExtra("WIDTH", 3);
        height = getIntent().getIntExtra("HEIGHT", 3);
        playTime = (long)(1.5 * ONE_MINUTE);//(long)(((float)(width+height)/2.0) * ONE_MINUTE);

        //init the timer text
        String text = getString(R.string.time_string,
                playTime/ONE_MINUTE,
                playTime%ONE_MINUTE/ONE_SECOND);

        timer.setText(text);

        this.timerTick = new CountDownTimer(playTime, ONE_SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                //DecimalFormat df = new DecimalFormat("00");
                String text = getString(R.string.time_string,
                        millisUntilFinished/ONE_MINUTE,
                        millisUntilFinished%ONE_MINUTE/ONE_SECOND);
                timer.setText(text);
                timeElapsed = playTime - millisUntilFinished;
                timeRemain = millisUntilFinished;
                timer.invalidate();
            }

            @Override
            public void onFinish() {
                timer.setText(getString(R.string.time_gameover));
                timeElapsed = playTime;
                timeRemain = 0;
                lose(playSpace);
            }
        };


        /// get the puzzle board picture
        imageUri = intent.getParcelableExtra("picture");
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            if (bitmap==null){
                File f= new File(imageUri.toString());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            }
        } catch (IOException e2) {
            File f= new File(imageUri.toString());
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (NullPointerException npe) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ilya);
            //imageUri
        }

        if(bitmap == null) {
            Log.i("[TEST]", "BITMAP IS NULL");
        }



        ViewTreeObserver tree = findViewById(R.id.mainLayout).getViewTreeObserver();
        tree.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {@Override
        public void onGlobalLayout() {
            setupBoard(playSpace, width, height, bitmap);
            playSpace.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
        });



        //// image preview
        prev.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try {
                    if(currentBoard != null) {
                        Intent intPrev = new Intent(PlayActivity.this, PreviewActivity.class);

                        intPrev.putExtra("img", imageUri);

                        startActivity(intPrev);
                    }
                }catch(Exception e){ Log.i("[ PREVIEW ]", e.getMessage());}

            }
        });

    }

    private void restart() {
        isScrambled = false;
        currentBoard.restart();
        undoStack.clear();

        for(int i=0;i<width*height;i++) {
            if (i != currentBoard.getBlankIndex()) {
                pieces[i].setImageBitmap(currentBoard.getPiece(i).getBitmap());
                pieces[i].setVisibility(View.VISIBLE);
            } else {
                pieces[i].setVisibility(View.INVISIBLE);
            }
        }

        restartText.setVisibility(View.INVISIBLE);
        playSpace.setBackgroundColor(Color.DKGRAY);

        restartText.invalidate();
        playSpace.invalidate();
        isLose = false;
        isWin = false;

        String text = getString(R.string.time_string,
                playTime/ONE_MINUTE,
                playTime%ONE_MINUTE/ONE_SECOND);
        timer.setText(text);

        moveInt = 0;
        undoInt = 0;
        moveNum.setText(String.format("%d", moveInt));
        moveNum.invalidate();

        timeElapsed = 0;
        timeRemain = playTime;
    }

    protected void setupBoard(final GridLayout playSpace, int w, int h,Bitmap bm){
        width = w; //getIntent().getIntExtra("WIDTH", 3);
        height = h; //getIntent().getIntExtra("HEIGHT", 3);
        setupBoard(playSpace,width,height);
    }
    protected void scrambleBoard(){
        for(int i=0;i<1000;i++){
            slideImages(currentBoard.slideBlankRandom());
        }
    }

    protected void setupBoard(final GridLayout playSpace, int w, int h){
        try {

            //////////////////////////
            ////// setting up the bitmap dimension and puzzle board
            /// rescaling bitmap to fit the playfield

            bitmap = scaleBitmapCenteredFit(bitmap, w, h);
            currentBoard = new PuzzleBoard(bitmap, w, h);

            ///////////////////////////////


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
                    if(i != currentBoard.getBlankIndex())
                        pieces[i].setImageBitmap(currentBoard.getPiece(i).getBitmap());
                    playSpace.addView(pieces[i], boardRules);
                    i++;
                }
            }
            ////// end test //////

            //this.timerTick.start();
        }
        catch(Exception e)
        {
            Log.i("[ERROR]", "Failed to splice bitmap");
        }
    }

///////////////////////////////////////////////
///////////////////////////////////////////
    //// BITMAP SCALING FUNCTIONS
    protected Bitmap scaleBitmapNaive(Bitmap src, int w, int h)
    {
        int newWid = src.getWidth();
        int newHei = src.getHeight();
        newWid = playSpace.getWidth();
        double ratio = (double)newWid/(double)bitmap.getWidth();
        newHei = (int)(newHei * ratio);
        if(newHei>playSpace.getHeight()){
            newHei = playSpace.getHeight();
        }

        src = Bitmap.createScaledBitmap(bitmap, newWid - 2*w*GAP, newHei - 2*h*GAP, true);
        return src;
    }

    /// uses the global variables:
    /// - Bitmap, width, and height
    /// - playSpace, prev
    /// args: bitmap, width of board, height of board
    protected Bitmap scaleBitmapCenteredFit(Bitmap src, int w, int h)
    {
        int newWid = playSpace.getWidth(); //bitmap.getWidth();
        int newHei = (int)prev.getY() - findViewById(R.id.top_bar).getHeight(); //bitmap.getHeight();
        int bmwid = src.getWidth();
        int bmhei = src.getHeight();
        float ratio1 = (float)newWid / (float)newHei;
        float ratio2 = (float)bmwid / (float)bmhei;
        int x1 = 0;
        int y1 = 0;
        int x2 = bmwid;
        int y2 = bmhei;

        if(ratio2 > ratio1)
        {
            x2 = (int)(y2 * ratio1);
            x1 = bmwid/2 - x2/2;
        }
        else if(ratio2 < ratio1)
        {
            y2 = (int)(bmwid/ratio1);
            y1 = bmhei/2 - y2/2;
        }

        src = Bitmap.createBitmap(src, x1, y1, x2, y2);
        src = Bitmap.createScaledBitmap(src, newWid - 2*w*GAP, newHei - 2*h*GAP, true);
        return src;
    }

    protected Bitmap scaleBitmapCenteredSquare(Bitmap src, int w, int h)
    {
        int min = playSpace.getWidth();
        if(playSpace.getHeight() < playSpace.getWidth())
            min = playSpace.getHeight();
        int bmwid = src.getWidth();
        int bmhei = src.getHeight();
        int x1 = 0;
        int y1 = 0;
        int x2 = bmwid;
        int y2 = bmhei;

        if(bmwid < bmhei)
        {
            y1 = bmhei/2 - bmwid/2;
            y2 = bmwid;
        }
        else if(bmwid > bmhei)
        {
            x1 = bmwid/2 - bmhei/2;
            x2 = bmhei;
        }

        src = Bitmap.createBitmap(src, x1, y1, x2, y2);
        src = Bitmap.createScaledBitmap(src, min - 2*w*GAP, min - 2*h*GAP, true);
        return src;
    }
    ////// END BITMAP SCALING FUNCTIONS
/////////////////////////////////////////






    private class PieceListener implements View.OnClickListener {
        private int mNumOfView;
        private PieceListener(int i) {
            mNumOfView = i;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onClick(View v) {
            Log.i("puz","clicked "+mNumOfView);
            PuzzleBoard.Direction d = currentBoard.dirNextToBlank(mNumOfView);
            if(!isScrambled){
                isScrambled = true;
                timerTick.start();
                scrambleBoard();
                timerTick.start();
                return;
            }

            if(isLose || isWin){
                restart();
            }
            else if(d!=null){
                slideImages(d);

                //add to undo
                undoStack.push(d);

                //increment number
                moveNum.setText(String.format("%d", ++moveInt));
                moveNum.invalidate();
            }

            if(isScrambled && !isLose && currentBoard.checkWin()){
                win();
            }
        }
    }

    private void win() {
        isWin = true;
        restartText.setText(R.string.win);
        restartText.setVisibility(View.VISIBLE);
        playSpace.setBackgroundColor(getResources().getColor(R.color.winColor));
        timerTick.cancel();

        /// attempt to update save file
        try{
            stats.updateStats(width, height, moveInt, undoInt, (int)(timeElapsed/ONE_SECOND), 1, 0);

            FileOutputStream fos = PlayActivity.this.openFileOutput(savePath, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(stats);
            os.close();
            fos.close();

        }catch(Exception e){
            Toast.makeText(PlayActivity.this, "WIN ERROR SAVE " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void lose(GridLayout playSpace) {
        isLose = true;
        playSpace.setBackgroundColor(getResources().getColor(R.color.failColor));
        playSpace.invalidate();
        restartText.setVisibility(View.VISIBLE);
        restartText.invalidate();
        timerTick.cancel();

        /// attempt to update save file
        try{

            stats.updateStats(width, height, moveInt, undoInt, 0, 0, 1);
            FileOutputStream fos = PlayActivity.this.openFileOutput(savePath, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(stats);
            os.close();
            fos.close();

        }catch(Exception e){
            Toast.makeText(PlayActivity.this, "LOSE ERROR SAVE " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void slideImages(PuzzleBoard.Direction d){
        currentBoard.slideBlank(d);
        for(int i=0;i<width*height;i++) {
            if(i!=currentBoard.getBlankIndex()) {
                pieces[i].setImageBitmap(currentBoard.getPiece(i).getBitmap());
                pieces[i].setVisibility(View.VISIBLE);
            }else{
                pieces[i].setVisibility(View.INVISIBLE);
            }
        }
        playSpace.invalidate();
    }
}