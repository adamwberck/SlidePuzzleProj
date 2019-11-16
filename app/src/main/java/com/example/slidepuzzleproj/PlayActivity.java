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
import java.io.IOException;
import java.util.Stack;

import static java.lang.Math.min;

public class PlayActivity extends Activity {
    private ContextWrapper cw; //context / working directory of app

    private TextView timer;
    private TextView moveNum;

    private PlayerStats stats;
    private int moveInt = 0;
    private long timeElapsed; //the current ellapsed time
    private long timeRemain; // the millisecond time remaining for the puzzle

    private Button undo;
    private Button tips;
    private Button menu;
    private Button prev;
    private GridLayout playSpace;
    private CountDownTimer timerTick;
    private PuzzleBoard currentBoard;
    private TextView restartText;


    private final long ONE_MINUTE = 60000;
    private final long ONE_SECOND = 1000;
    private final long PLAY_TIME = 3 * ONE_MINUTE;
    private ImageView[] pieces;
  
    private Bitmap bitmap = null;
  
    private Uri imageUri;

    private boolean isWin;
    private boolean isLose;

    private int width;
    private int height;

    private boolean isScrambled = false;

    private FragmentManager fmnger;

    private Stack<PuzzleBoard.Direction> undoStack = new Stack<>();

    private MediaPlayer menuBGM;
    private boolean play;

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
                                return true;
                            case R.id.save_menu:
                                Toast.makeText(PlayActivity.this, "Save Menu Clicked", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PlayActivity.this, playSpace.getWidth() + "," + playSpace.getHeight(), Toast.LENGTH_SHORT).show();

            }
        });

        //init the timer text
        String text = getString(R.string.time_string,
                PLAY_TIME/ONE_MINUTE,
                PLAY_TIME%ONE_MINUTE/ONE_SECOND);

        timer.setText(text);

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



        ViewTreeObserver tree = playSpace.getViewTreeObserver();
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
        width = getIntent().getIntExtra("WIDTH", 3);
        height = getIntent().getIntExtra("HEIGHT", 3);
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
            DisplayMetrics display = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(display);

            //bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ilya);
            
            int newWid = bitmap.getWidth();
            int newHei = bitmap.getHeight();
            Log.i("[ORIGINAL DIMENSION]", newWid+ ", " + newHei);
            Toast.makeText(PlayActivity.this, playSpace.getWidth() + "," + playSpace.getHeight(), Toast.LENGTH_SHORT).show();

            newWid = playSpace.getWidth();
            double ratio = (double)newWid/(double)bitmap.getWidth();
            newHei = (int)(newHei * ratio);
            if(newHei>playSpace.getHeight()){
                newHei = playSpace.getHeight();
            }

            Log.i("[NEW DIMENSION]", newWid+ ", " + newHei);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWid, newHei, true);
            currentBoard  = new PuzzleBoard(bitmap, w, h);



            //// test code below
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