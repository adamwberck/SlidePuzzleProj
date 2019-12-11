package com.example.slidepuzzleproj;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Stack;

public class PlayActivity extends Activity {
    private List<PuzzleBoard.Direction> mHints = null;

    private TextView timer;
    private TextView moveNum;

    private Button undo;
    private Button hint;
    private Button menu;
    private Button prev;
    private GridLayout playSpace;
    private TextView restartText;

    private int mGlowIndex = 0;

    private final long ONE_MINUTE = 60000;
    private final long ONE_SECOND = 1000;
    private long playTime = 3 * ONE_MINUTE;
    private final int GAP = 2;
    private TextView challengeText;
    private long seed = -1;
    private ImageView[] pieces;
    private MediaPlayer menuBGM;
    private boolean mIsThinking = false;
  
    private int moveInt = 0;    // save
    private int undoInt = 0;
    private int hintInt = 0;
    private long timeElapsed;   //save
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

    private PlayerStats stats = null;
    private String savePath;
    private int minb;
    private int maxb;
    private String shareTimer;
    private Solver mSolver;

    @Override
    protected void onPause()
    {
        super.onPause();
        menuBGM.pause();
    }


    @Override
    public void onBackPressed(){
        mSolver.cancel(true);
        mIsThinking = false;
        mSolver = null;
        super.onBackPressed();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(play) {
            menuBGM.start();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Intent intent = getIntent(); ///intent args
        stats = (PlayerStats)intent.getSerializableExtra("save"); /// get save file

        savePath = getResources().getString(R.string.saveFile);

        mode = getIntent().getExtras().getBoolean("MODE");

        minb = Integer.parseInt(getResources().getString(R.string.min_board_size));
        maxb = Integer.parseInt(getResources().getString(R.string.max_board_size));

        undo = findViewById(R.id.undoButton);
        hint = findViewById(R.id.tipsButton);
        menu = findViewById(R.id.menuButton);
        prev = findViewById(R.id.previewButton);
        timer = findViewById(R.id.time);


        moveNum = findViewById(R.id.moveNumber);
        moveNum.setText(String.format("%d", moveInt));

        menuBGM = MediaPlayer.create(this, R.raw.wotw);
        if(play) {
            menuBGM.start();
        }

        playSpace = findViewById(R.id.playSpace);
        restartText = findViewById(R.id.restartText);
        challengeText = findViewById(R.id.challengeText);

        restartText.setVisibility(View.INVISIBLE);
        challengeText.setVisibility(View.INVISIBLE);

        challengeText.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(User.getID()==null) {
                     Toast.makeText(PlayActivity.this, "You must first log in or sign up.", Toast.LENGTH_SHORT).show();
                     Intent playIntent = new Intent(PlayActivity.this, LoginActivity.class);
                     startActivity(playIntent);
                 } else {
                     String path = new String();
                     if (imageUri.toString().contains(".jpg") || imageUri.toString().contains(".png")) {
                         path = imageUri.toString();
                     } else {
                         File photoFile = null;
                         try {
                             photoFile = SaveImage.createImage(PlayActivity.this);
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                         if (photoFile != null) {
                             try (FileOutputStream out = new FileOutputStream(SaveImage.getPath())) {
                                 bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                             path = photoFile.getAbsolutePath();
                         }
                     }
                     String[] result = {path, String.valueOf(isWin), String.valueOf(playTime), String.valueOf(width), String.valueOf(height), shareTimer};
                     Intent shareIntent = new Intent(PlayActivity.this, ShareActivity.class);
                     shareIntent.putExtra("INFO", result);
                     startActivity(shareIntent);
                 }
             }
        });


        restartText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLose || isWin) {
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
                        challengeText.setVisibility(View.INVISIBLE);
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

                for(int i=0;i<popupMenu.getMenu().size();i++){
                    MenuItem item =  popupMenu.getMenu().getItem(i);
                    if(item.getItemId()==R.id.mute_menu) {
                        item.setTitle(play?"Mute":"Un-mute");
                    }
                }


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
                                    item.setTitle(play?"Mute" : "Un-mute");
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

        hint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(width>3 || height>3){
                    Toast.makeText(PlayActivity.this, "Hint Only Supported for 3 by 3"
                            ,Toast.LENGTH_SHORT)
                            .show();
                }
                else if(isScrambled ) {
                    if (mHints == null ) {
                        Toast.makeText(PlayActivity.this, "Thinking...",
                                Toast.LENGTH_SHORT)
                                .show();
                        mIsThinking = true;
                        mSolver = new Solver(currentBoard, PlayActivity.this);
                        mSolver.execute();
                    } else {
                        glowPiece(mHints.get(0));
                    }
                    hintInt++;
                }
            }
        });

        width = getIntent().getIntExtra("WIDTH", 3);
        height = getIntent().getIntExtra("HEIGHT", 3);
        playTime = intent.getLongExtra("time", (long)((float)(width+height)/2) * ONE_MINUTE);
        seed = intent.getLongExtra("seed", -1);
        
        //init the timer text
        String text = getString(R.string.time_string,
                playTime/ONE_MINUTE,
                playTime%ONE_MINUTE/ONE_SECOND);

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

        this.timerTick = new CountDownTimer(playTime, ONE_SECOND) {
            @Override
            public void onTick(long millisUntilFinished) {
                //DecimalFormat df = new DecimalFormat("00");
                String text = getString(R.string.time_string,
                        millisUntilFinished/ONE_MINUTE,
                        millisUntilFinished%ONE_MINUTE/ONE_SECOND);
                shareTimer = String.valueOf(timeElapsed);
                timer.setText(text);
                timeElapsed = playTime - millisUntilFinished;
                timer.invalidate();
            }

            @Override
            public void onFinish() {
                timer.setText(getString(R.string.time_gameover));
                timeElapsed = playTime;
                shareTimer = String.valueOf(timeElapsed);
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
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
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
        stopGlow();
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
        challengeText.setVisibility((View.INVISIBLE));
        playSpace.setBackgroundColor(Color.DKGRAY);

        challengeText.invalidate();
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
        hintInt = 0;
        moveNum.setText(String.format("%d", moveInt));
        moveNum.invalidate();

        timeElapsed = 0;
    }

    protected void setupBoard(final GridLayout playSpace, int w, int h,Bitmap bm){
        width = w; //getIntent().getIntExtra("WIDTH", 3);
        height = h; //getIntent().getIntExtra("HEIGHT", 3);
        setupBoard(playSpace,width,height);
    }
    protected void scrambleBoard(){
        Log.i("SEED", String.valueOf(seed));
        if(seed == -1) {
            seed = (int) System.currentTimeMillis();
            User.setSeed(seed);
            for (int i = 0; i < 1000; i++) {
                slideImages(currentBoard.slideBlankRandom(seed));
                seed++;
            }
        } else {
            for (int i = 0; i < 5; i++) {
                slideImages(currentBoard.slideBlankRandom(seed));
                seed++;
            }
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

    public void solutionFound(List<PuzzleBoard.Direction> solution) {
        mHints = solution;
        mIsThinking = false;
        if(solution!=null) {
            glowPiece(solution.get(0));
        }
    }

    private void glowPiece(PuzzleBoard.Direction direction)  {
        stopGlow();
        int index = currentBoard.indexNextToBlank(direction);
        mGlowIndex = index;
        pieces[index].setColorFilter(pieces[index].getContext().getResources().getColor(R.color.hint1),
                PorterDuff.Mode.SRC_ATOP);
        pieces[index].invalidate();
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
            stopGlow();
            if(mIsThinking){
                mIsThinking = false;
                mSolver.cancel(true);
            }

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
            else if(d!=null ){//so the solver doesn't have to resolve everytime
                if(mHints!=null) {
                    if (d == PlayActivity.this.mHints.get(0)) {
                        PlayActivity.this.mHints.remove(0);
                    } else {
                        PlayActivity.this.mHints = null;
                    }
                }
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

    private void stopGlow() {

        pieces[mGlowIndex].clearColorFilter();
        pieces[mGlowIndex].invalidate();
    }

    private void win() {
        isWin = true;
        restartText.setText(R.string.win);
        restartText.setVisibility(View.VISIBLE);
        challengeText.setVisibility(View.VISIBLE);
        playSpace.setBackgroundColor(getResources().getColor(R.color.winColor));
        timerTick.cancel();

        /// attempt to update save file
        try{
            int classic = mode ? 0 : 1;
            stats.updateStats(width, height, moveInt, undoInt, (int)(timeElapsed/ONE_SECOND), classic^1, 0, classic, hintInt);

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
        challengeText.setVisibility(View.VISIBLE);
        challengeText.invalidate();
        restartText.invalidate();
        timerTick.cancel();

        /// attempt to update save file
        try{
            int classic = mode ? 1 : 0;
            stats.updateStats(width, height, moveInt, undoInt, 0, 0, classic, 0, hintInt);
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