package com.example.slidepuzzleproj;


///// WIP  - Dan ////
/// finish up adding entries and test pull data from the menu activity context

import android.net.Uri;
import android.os.CountDownTimer;

import java.io.Serializable;
import java.util.Stack;


public class PlayerStats implements Serializable {

    BoardTypeStatEntry[][] entries;

    public PlayerStats(int maxboardwidth, int maxboardheight)
    {
        entries = new BoardTypeStatEntry[maxboardheight][maxboardwidth];
        for(int y = 0; y < maxboardheight; y++)
        {
            for(int x = 0; x < maxboardwidth; x++){
                entries[y][x] = new BoardTypeStatEntry(0,0,-1,-1,0,0);
            }
        }
    }

    public void updateEntry(int boardwidth, int boardheight, int numMoves, int numUndos,
                            int time, int win, int lose)
    {
        //if(entries[boardheight-1][boardwidth-1] == null){
        //    entries
        //}
    }

    @Override
    public String toString(){
        String output = "PlayerStats object";
        return output;
    }



    protected class BoardTypeStatEntry implements Serializable {
        private int numMoves;
        private int numUndos;
        private int shortestTime;
        private int longestTime;
        private int numWin;
        private int numLose;

        public BoardTypeStatEntry(int nm, int nu, int st, int lt, int nw, int nl){
            this.numMoves = nm;
            this.numUndos = nu;
            this.shortestTime = st;
            this.longestTime = lt;
            this.numWin = nw;
            this.numLose = nl;
        }

        protected void setAllProperties(int nm, int nu, int st, int lt, int nw, int nl){
            this.numMoves = nm;
            this.numUndos = nu;
            this.shortestTime = st;
            this.longestTime = lt;
            this.numWin = nw;
            this.numLose = nl;
        }


    }
    /*
    private int moveInt = 0;    // save
    private long timeElapsed;   //save
    private long timeRemain;    // save
    private CountDownTimer timerTick;   // save

    private String imageUri;   // save

    private boolean isWin;  // save
    private boolean isLose; // save

    private int width;  // save
    private int height; // save

    private boolean isScrambled = false; // save
    private boolean play; // save

    private Stack<PuzzleBoard.Direction> undoStack = new Stack<>(); //save;


    public PlayerStats(int mov, long te, long tr, CountDownTimer timer, SerializablePuzzleBoard board,
                       String img, boolean win, boolean lose, int w, int h,
                       boolean scram, Stack<PuzzleBoard.Direction> stack, boolean p)
    {
        moveInt = mov;
        timeElapsed = te;
        timeRemain = tr;
        timerTick    = timer;
        currentBoard = board;
        imageUri = img;
        isWin = win;
        isLose = lose;
        width = w;
        height = h;
        isScrambled = scram;
        play = p;
        undoStack = stack;

    }

    public int getMoveInt()
    {
        return moveInt;
    }

    public long getTimeElapsed()
    {
        return timeElapsed;
    }
    public long getTimeRemaining()
    {
        return timeRemain;
    }
    public CountDownTimer getTimerTick()
    {
        return timerTick;
    }
    public SerializablePuzzleBoard getCurrentBoard()
    {
        return currentBoard;
    }

    public Uri getImageUri()
    {
        return Uri.parse(this.imageUri);
    }
    public boolean getIsWin()
    {
        return isWin;
    }
    public boolean getIsLose()
    {
        return isLose;
    }
    public int getWidth()
    {
        return width;
    }
    public int getHeight()
    {
        return height;
    }
    public boolean getIsScrambled()
    {
        return isScrambled;
    }
    public boolean getPlay()
    {
        return play;
    }
    public Stack<PuzzleBoard.Direction> getUndoStack()
    {
        return undoStack;
    }


    public void setMoveInt(int x)
    {
        moveInt = x;
    }

    public void setTimeElapsed(long x)
    {
        timeElapsed = x;
    }
    public void setTimeRemaining(long x)
    {
        timeRemain = x;
    }
    public void setTimerTick(CountDownTimer x)
    {
        timerTick = x;
    }
    public void setCurrentBoard(SerializablePuzzleBoard x)
    {
        currentBoard = x;
    }


    public void setImageUri(Uri x)
    {
        imageUri = x.toString();
    }
    public void setIsWin(boolean x)
    {
        isWin = x;
    }
    public void setIsLose(boolean x)
    {
        isLose = x;
    }
    public void setWidth(int x)
    {
        width = x;
    }
    public void setHeight(int x)
    {
        height = x;
    }
    public void setIsScrambled(boolean x)
    {
        isScrambled = x;
    }
    public void setPlay(boolean x)
    {
        play = x;
    }
    public void setUndoStack(Stack<PuzzleBoard.Direction> x)
    {
        undoStack = x;
    }
    */
}
