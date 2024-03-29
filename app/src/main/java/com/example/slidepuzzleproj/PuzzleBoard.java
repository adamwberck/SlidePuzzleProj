package com.example.slidepuzzleproj;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

//// fix bug where uneven sized board isnt processed properly
public class PuzzleBoard implements Serializable
{
    public static Direction getOpposite(Direction d) {
        if(d==Direction.Up)
            return Direction.Down;
        if(d==Direction.Down)
            return Direction.Up;
        if(d==Direction.Right)
            return Direction.Left;
        if(d==Direction.Left)
            return Direction.Right;
        return null;
    }

    public void restart() {
        PuzzlePiece[] tempPieces = new PuzzlePiece[pieces.length];
        for(PuzzlePiece p : pieces){
            p.currentPos = p.correctPos;

            p.width = p.correctPos%width;
            p.height = p.correctPos/width;

            p.isBlank = p.currentPos == pieces.length-1;//if its blank
            tempPieces[p.currentPos]=p;
        }
        blankIndex = pieces.length-1;
        pieces = tempPieces;
    }

    public PuzzlePiece[] getPieces() {
        return pieces;
    }

    enum Direction{
        Up,
        Down,
        Left,
        Right
    };


    private int width, height, length, blankIndex;
    private int bmwidth, bmheight, pixwidth, pixheight;
    private PuzzlePiece[] pieces;
    private Bitmap image;
    private boolean loaded;

    public PuzzleBoard(Bitmap img, int w, int h)
    {
        this.image = img;
        this.width = w;
        this.height = h;
        this.bmwidth = img.getWidth();
        this.bmheight = img.getHeight();
        this.pixwidth = this.bmwidth / w;
        this.pixheight = this.bmheight / h;
        this.length = w*h;
        this.blankIndex = this.length-1;
        this.loaded = false;
        Log.i("[PUZZLE BOARD]", ""+this.bmwidth + "," + this.bmheight);
        Log.i("[PUZZLE BOARD]", ""+this.pixwidth + "," + this.pixheight);
        sliceImageThreaded();
    }

    private void sliceImageThreaded()
    {
        boolean done = false;
        ImageSlicer[] workers = new ImageSlicer[this.length-1];

        this.pieces = new PuzzlePiece[this.length];

        for(int i = 0; i < this.length-1; i++)
        {
            workers[i] = new ImageSlicer(this.image, this.pieces, this.pixwidth, this.pixheight, this.width, this.height, i);
            workers[i].start();
        }

        while(!done)
        {
            Log.i("[PUZZLE BOARD]", "NOT DONE");
            done = true;
            for(int i = 0; i < this.length-1; i++)
            {
                if(!workers[i].done)
                    done = false;
            }
        }
        Log.i("[PUZZLE BOARD]", "DONE");
        this.pieces[this.blankIndex] = new PuzzlePiece(this.blankIndex, this.blankIndex,
                                                    true, this.pixwidth, this.pixheight, null);


    }

    /////////////////////
    /// Accessor methods
    public PuzzlePiece getPiece(int i)
    {
        if(isBetween(i, 0, this.length))
            return this.pieces[i];

        return null;
    }

    public int getBlankIndex()
    {
        return this.blankIndex;
    }

    public int getBlankX(){
        return this.blankIndex % this.width;
    }

    public int getBlankY()
    {
        return this.blankIndex / this.width;
    }

    public int getBoardWidth(){
        return this.width;
    }

    public int getBoardHeight()
    {
        return this.height;
    }

    public int indexNextToBlank(Direction d)throws IllegalArgumentException{
        int returnVal = -1;
        if(d==Direction.Up){
            returnVal = blankIndex-width;
        }else if(d==Direction.Left){
            returnVal = blankIndex-1;
        }else if(d==Direction.Right){
            returnVal = blankIndex+1;
        }else if(d==Direction.Down){
            returnVal = blankIndex+width;
        }
        if(returnVal<0 || returnVal>length){
            throw new IllegalArgumentException();
        }else{
            return returnVal;
        }
    }

    public Direction dirNextToBlank(int i)
    {
        return dirNextToBlank(i%this.width, i/this.width);
    }

    public Direction dirNextToBlank(int x, int y)
    {
        if(x == getBlankX() && y == getBlankY())
            return null;

        if( x-1 == getBlankX() && y == getBlankY()) {//blank goes right
            return Direction.Right;
        }
        if(x+1 == getBlankX() && y == getBlankY()) {//blank goes left
            return Direction.Left;
        }
        if(x == getBlankX() && y-1 == getBlankY()){//blank goes down
            return Direction.Down;
        }
        if(x == getBlankX() && y+1 == getBlankY()){//blank goes up
            return Direction.Up;
        }
        return null;
    }

    ////////////////////
    //// Check all pieces to see if theyre in the right positions
    public boolean checkWin()
    {
        for(int i = 0; i < this.length; i++){
            if(pieces[i].currentPos != pieces[i].correctPos)
                return false;
        }
        return true;
    }


    ///////////////////////////////////
    /// SLIDING THE BLANK PIECE
    /// DIFFERENT OVERLOADS
    public boolean slideBlank(Direction dir)
    {
        return slideBlankParsed(dir);
    }

    public Direction slideBlankRandom(long seed){
        List<Direction> dirs = slideBlankPossible();
        User.setSeed(seed);
        Collections.shuffle(dirs, new Random(seed));
        return dirs.get(0);
    }

    private List<Direction> slideBlankPossible()
    {
        List<Direction> dirs = new ArrayList<>(4);
        //up
        if(this.blankIndex - this.width >= 0)  ///if not on top row
        {
            dirs.add(Direction.Up);
        }
        //down
        if(this.blankIndex + this.width < this.length)
        {
            dirs.add(Direction.Down);
        }
        //left
        if(this.blankIndex % this.width != 0)
        {
            dirs.add(Direction.Left);
        }
        //right
        if(this.blankIndex % this.width != this.width - 1)
        {
            dirs.add(Direction.Right);
        }
        return dirs;
    }

    ///////////////////////////////
    //// The parsed direction version of slide blank
    private boolean slideBlankParsed(Direction dir)
    {
        switch(dir)
        {
            case Up: //up
                if(this.blankIndex - this.width >= 0)  ///if not on top row
                {
                    swapPieces(this.blankIndex, this.blankIndex - this.width);
                    this.blankIndex = this.blankIndex - this.width;
                    return true;
                }
                return false;

            case Down: //down
                if(this.blankIndex + this.width < this.length)
                {
                    swapPieces(this.blankIndex, this.blankIndex + this.width);
                    this.blankIndex = this.blankIndex + this.width;
                    return true;
                }
                return false;

            case Left://left
                if(this.blankIndex % this.width != 0)
                {
                    swapPieces(this.blankIndex, this.blankIndex - 1);
                    this.blankIndex = this.blankIndex - 1;
                    return true;
                }
                return false;

            case Right://right
                if(this.blankIndex % this.width != this.width - 1)
                {
                    swapPieces(this.blankIndex, this.blankIndex + 1);
                    this.blankIndex = this.blankIndex + 1;
                    return true;
                }
                return false;

            default:
                return false;
        }
    }

    ////////////////
    /// Utility method to swap two pieces
    private void swapPieces(int i, int j) {
        if (isBetween(i, 0, this.length) && isBetween(j, 0, this.length)) {
            PuzzlePiece temp = this.pieces[i];
            this.pieces[i] = this.pieces[j];
            this.pieces[j] = temp;

            this.pieces[i].currentPos = i;
            this.pieces[j].currentPos = j;
        }
    }



    public boolean isBetween(int a, int x, int y)
    {
        return a >= x && a < y;
    }

    ////////////////////////////////////////////
    ///// Class representing each puzzle piece
    protected class PuzzlePiece implements Serializable
    {
        private int correctPos;
        private int currentPos;
        private boolean isBlank;
        private int width;
        private int height;
        private Bitmap image;

        public PuzzlePiece(int corPos, int curPos, boolean blank, int w, int h, Bitmap img) {
            this.correctPos = corPos;
            this.currentPos = curPos;
            this.isBlank = blank;
            this.width = w;
            this.height = h;
            this.image = img;
        }

        public Bitmap getBitmap()
        {
            if(this.image == null) return null;
            return this.image;
        }

        public int getCorrectPos() {
            return this.correctPos;
        }
        public int getCurrentPos(){
            return this.currentPos;
        }
    }


    ///////////////////////////////////////////////
    //// Thread class that splice each section
    protected class ImageSlicer extends Thread
    {
        private Bitmap map;
        private int pixwidth, pixheight;
        private PuzzlePiece[] pieces;
        private int x1, y1, x2, y2;
        private int index;
        private boolean done;

        public ImageSlicer(Bitmap m, PuzzlePiece[] p, int pwid, int phei, int bwid, int bhei, int i)
        {
            this.map = m;
            this.pieces = p;
            this.x1 = i%bwid;
            this.y1 = i/bwid;
            this.x2 = (i+1)%bwid;
            this.y2 = (i+1)/bwid;
            this.pixwidth = pwid;
            this.pixheight = phei;
            this.index = i;

            this.done = false;
        }

        public void run()
        {
            slice(this.map, this.x1, this. pixwidth, this.y1, this.pixheight, this.pieces, this.index);

            this.done = true;
        }

        public Bitmap slice(Bitmap m, int x1, int pw, int y1, int ph, PuzzlePiece[] arr, int i)
        {
            Log.i("[CREATE BITMAP]", "" + m.getHeight()  + "/" + (y1*ph + ph));
                Bitmap bm = Bitmap.createBitmap(m, x1 * pw, y1 * ph, pw, ph);

                arr[i] = new PuzzlePiece(i, i, false, pw, ph, bm);
                return bm;
        }
    }


}
