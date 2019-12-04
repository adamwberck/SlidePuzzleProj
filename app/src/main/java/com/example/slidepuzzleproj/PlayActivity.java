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
    private final long PLAY_TIME = 3 * ONE_MINUTE;
    private final int GAP = 2;
    private ImageView[] pieces;
    private MediaPlayer menuBGM;

    private int moveInt = 0;    // save
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
    private boolean mode;

    private PlayerStats stats;
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

        mode = getIntent().getExtras().getBoolean("MODE");

        minb = Integer.parseInt(getResources().getString(R.string.min_board_size));
        maxb = Integer.parseInt(getResources().getString(R.string.max_board_size));
        stats = new PlayerStats(minb, minb, maxb, maxb);

        cw = new ContextWrapper(getApplicationContext());

        undo = findViewById(R.id.undoButton);
        tips = findViewById(R.id.tipsButton);
        menu = findViewById(R.id.menuButton);
        prev = findViewById(R.id.previewButton);
        timer = findViewById(R.id.time);


        moveNum = findViewById(R.id.moveNumber);
        moveNum.setText(String.format("%d", moveInt));

        menuBGM = MediaPlayer.create(this, R.raw.hometown_domina);
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
                if(!isLose && !undoStack.isEmpty()) {
                    PuzzleBoard.Direction d = undoStack.pop();
                    d = PuzzleBoard.getOpposite(d);
                    slideImages(d);
                    moveInt--;
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
                            case R.id.load_menu:
                                Toast.makeText(PlayActivity.this, "Load Menu Clicked", Toast.LENGTH_SHORT).show();
                                try{
                                    String path = getFilesDir() + "/test.bin";
                                    FileInputStream fis = openFileInput(getResources().getString(R.string.saveFile));
                                    ObjectInputStream is = new ObjectInputStream(fis);
                                    PlayerStats stats = (PlayerStats)is.readObject();
                                    is.close();
                                    fis.close();

                                    Toast.makeText(PlayActivity.this, "LOAD NEW STRING " + stats.toString(), Toast.LENGTH_LONG).show();
                                }catch(Exception e){
                                    Toast.makeText(PlayActivity.this, "ERROR ERROR LOAD", Toast.LENGTH_LONG).show();
                                }
                                return true;
                            case R.id.save_menu:
                                Toast.makeText(PlayActivity.this, "Save Menu Clicked", Toast.LENGTH_SHORT).show();

                                try{
                                    Toast.makeText(PlayActivity.this, minb + "," + maxb + "," + width + "," + height, Toast.LENGTH_LONG).show();
                                    //stats.updateStats(width, height, 0, 1, 100, 1, 0);
                                    String path = getFilesDir().getPath() + "/test.bin";
                                    Toast.makeText(PlayActivity.this, path, Toast.LENGTH_LONG).show();
                                    FileOutputStream fos = PlayActivity.this.openFileOutput("test.bin", Context.MODE_PRIVATE);
                                    Toast.makeText(PlayActivity.this, fos.toString(), Toast.LENGTH_LONG).show();
                                    //File f = new File(path);
                                    //FileInputStream
                                    ObjectOutputStream os = new ObjectOutputStream(fos);
                                    os.writeObject(stats);
                                    os.close();
                                    fos.close();

                                    Toast.makeText(PlayActivity.this, "SAVED NEW OBJECT " + stats.toString(), Toast.LENGTH_LONG).show();
                                }catch(Exception e){
                                    Log.i("BAD STATS WRITER", e.getMessage() + " | " + e.getCause());
                                    Toast.makeText(PlayActivity.this, "ERROR SAVE " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }

                                return true;
                            case R.id.statistics_menu:
                                Toast.makeText(PlayActivity.this, "Statistics Clicked", Toast.LENGTH_SHORT).show();
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

        //init the timer text
        String text = getString(R.string.time_string,
                PLAY_TIME/ONE_MINUTE,
                PLAY_TIME%ONE_MINUTE/ONE_SECOND);

        if(mode == true)
        {
            timer.setText(text);
            timer.setTextSize(30);
        }
        else
        {
            timer.setText(getString(R.string.classic_mode));
            timer.setTextSize(20);
        }


        this.timerTick = new CountDownTimer(PLAY_TIME, ONE_SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                //DecimalFormat df = new DecimalFormat("00");
                String text = getString(R.string.time_string,
                        millisUntilFinished/ONE_MINUTE,
                        millisUntilFinished%ONE_MINUTE/ONE_SECOND);
                timer.setText(text);
                timeElapsed = PLAY_TIME - millisUntilFinished;
                timeRemain = millisUntilFinished;
                timer.invalidate();
            }

            @Override
            public void onFinish() {
                timer.setText(getString(R.string.time_gameover));
                timeElapsed = PLAY_TIME;
                timeRemain = 0;
                lose(playSpace);
            }
        };


        Intent intent = getIntent();
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
            setupBoard(playSpace, getIntent().getIntExtra("WIDTH", 3),
                    getIntent().getIntExtra("HEIGHT", 3), bitmap);
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
                PLAY_TIME/ONE_MINUTE,
                PLAY_TIME%ONE_MINUTE/ONE_SECOND);
        timer.setText(text);

        moveInt = 0;
        moveNum.setText(String.format("%d", moveInt));
        moveNum.invalidate();

        timeElapsed = 0;
        timeRemain = PLAY_TIME;
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



    private void lose(GridLayout playSpace) {
        isLose = true;
        playSpace.setBackgroundColor(getResources().getColor(R.color.failColor));
        playSpace.invalidate();
        restartText.setVisibility(View.VISIBLE);
        restartText.invalidate();
    }


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
            if(!isScrambled && mode == true)
            {
                isScrambled = true;
                timerTick.start();
                scrambleBoard();
                timerTick.start();
                return;
            }

            if(!isScrambled && mode == false)
            {
                    isScrambled = true;
                    timer.setText(getString(R.string.classic_mode));
                    scrambleBoard();
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