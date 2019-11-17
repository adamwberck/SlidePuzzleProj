package com.example.slidepuzzleproj;

import java.io.Serializable;

public class SerializablePuzzleBoard implements Serializable{
    private int width, height, length, blankIndex;
    private int bmwidth, bmheight, pixwidth, pixheight;
    private SerializablePiece[] pieces;
    private boolean loaded;
    public SerializablePuzzleBoard(int bw, int bh, int bl, int bb, int mw, int mh, int pw, int ph, SerializablePiece[] sp, boolean load)
    {
        this.width = bw;
        this.height = bh;
        this.length = bl;
        this.blankIndex = bb;
        this.bmwidth = mw;
        this.bmheight = mh;
        this.pixwidth = pw;
        this.pixheight = ph;
        this.loaded  = load;
        this.pieces = sp;
    }

    public int getWidth()
    {
        return this.width;
    }
    public int getHeight()
    {
        return this.height;
    }
    public int getLength(){
        return this.length;
    }
    public int getBlankIndex()
    {
        return this.blankIndex;
    }
    public int getBMWidth()
    {
        return this.bmwidth;
    }
    public int getBMHeight()
    {
        return this.bmheight;
    }
    public int getPixWidth(){
        return this.pixwidth;
    }
    public int getPixheight()
    {
        return this.pixheight;
    }
    public boolean getLoaded()
    {
        return this.loaded;
    }
    public SerializablePiece[] getPieces()
    {
        return this.pieces;
    }



}
