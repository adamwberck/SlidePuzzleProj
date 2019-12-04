package com.example.slidepuzzleproj;


///// WIP  - Dan ////
/// finish up adding entries and test pull data from the menu activity context

import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Stack;


public class PlayerStats implements Serializable
{
    /// stats accross all boards
    private int minBoardWidth;
    private int minBoardHeight;
    private int maxBoardWidth;
    private int maxBoardHeight;
    private int boardWidth;
    private int boardHeight;
    private int globNumGames;
    private int globNumMoves;
    private int globMinMoves;
    private int globMaxMoves;
    private int globNumUndos;
    private int globMinUndos;
    private int globMaxUndos;
    private int globTotalTime;
    private int globMinTime;
    private int globMaxTime;
    private int globNumWins;
    private int globNumLosses;
    private int globNumClassicMode;

    private BoardTypeStatEntry[][] entries;

    // private savedBoard  // the current saved board. only 1 saved at a time.

    public PlayerStats(int minboardwidth, int minboardheight, int maxboardwidth, int maxboardheight)
    {
        this.minBoardWidth = minboardwidth;
        this.minBoardHeight = minboardheight;
        this.maxBoardWidth = maxboardwidth;
        this.maxBoardHeight = maxboardheight;
        this.boardWidth = maxboardwidth - minboardwidth + 1;
        this.boardHeight = maxboardheight - minboardheight + 1;
        Log.i("[YO]", minboardwidth + "|" + maxboardwidth + "|" +
                minboardheight + "|" + maxboardheight + "|" + boardWidth + "|" + boardHeight);


        this.globNumGames = 0;
        this.globNumMoves = -1;
        this.globMinMoves = -1;
        this.globMaxMoves = -1;
        this.globNumUndos = -1;
        this.globMinUndos = -1;
        this.globMaxUndos = -1;
        this.globTotalTime = -1;
        this.globMinTime = -1;
        this.globMaxTime = -1;
        this.globNumWins = -1;
        this.globNumLosses = -1;
        this.globNumClassicMode = -1;


        this.entries = new BoardTypeStatEntry[boardHeight][boardWidth];
        for(int y = 0; y < boardHeight; y++)
        {
            for(int x = 0; x < boardWidth; x++){
                this.entries[y][x] = new BoardTypeStatEntry(0, -1,-1,-1,-1,
                                                    -1,-1,-1,-1,-1,-1,-1, -1);
            }
        }

    }

    public void updateStats(int boardwidth, int boardheight, int numMoves, int numUndos,
                            int time, int win, int lose, int classic)
    {
        /// add new data to the board entry of that size
        BoardTypeStatEntry ent = this.entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];

        /// updating the global data that range across all boards
        if(this.globNumGames == 0) /// set the first initial global data
        {
            this.globNumGames = 1;
            this.globNumMoves = numMoves;
            this.globMinMoves = numMoves;
            this.globMaxMoves = numMoves;
            this.globNumUndos = numUndos;
            this.globMinUndos = numUndos;
            this.globMaxUndos = numUndos;
            this.globTotalTime = time;
            this.globMinTime = time;
            this.globMaxTime = time;
            this.globNumWins = win;
            this.globNumLosses = lose;
            this.globNumClassicMode = classic;
        }
        else {
            this.globNumGames += 1;
            this.globNumMoves += numMoves;
            this.globNumUndos += numUndos;
            this.globTotalTime += time;
            this.globNumWins += win;
            this.globNumLosses += lose;
            this.globNumClassicMode += classic;

            if (lose != 1)  /// dont update lowest/highest on lose
            {
                if (numMoves < this.globMinMoves) this.globMinMoves = numMoves;
                else if (numMoves > this.globMaxMoves) this.globMaxMoves = numMoves;
                if (numUndos < this.globMinUndos) this.globMinUndos = numUndos;
                else if (numUndos > this.globNumUndos) this.globMaxUndos = numUndos;
                if (time < this.globMinTime) this.globMinTime = time;
                else if (time > this.globMaxTime) this.globMaxTime = time;
            }
        }


        if(ent.getNumGames() == 0) /// initial set if its first game
        {
            ent.setAllProperties(1, numMoves, numMoves, numMoves,
                    numUndos, numUndos, numUndos,
                    time, time, time, win, lose, classic);
        }
        else {
            ent.setNumGames(ent.getNumGames() + 1);
            ent.setNumMoves(ent.getNumMoves() + numMoves);
            ent.setNumUndos(ent.getNumUndos() + numUndos);
            ent.setTotalTime(ent.getTotalTime() + time);
            ent.setNumWins(ent.getNumWins() + win);
            ent.setNumLosses(ent.getNumLosses() + lose);
            ent.setNumClassic(ent.getNumClassic() + classic);

            if (lose != 1) /// dont update lowest/highest on lose
            {
                if (numMoves < ent.getMinMoves()) ent.setMinMoves(numMoves);
                else if (numMoves > ent.getMaxMoves()) ent.setMaxMoves(numMoves);
                if (numUndos < ent.getMinUndos()) ent.setMinUndos(numUndos);
                else if (numUndos > ent.getMaxUndos()) ent.setMaxUndos(numUndos);
                if (time < ent.getMinTime()) ent.setMinTime(time);
                else if (time > ent.getMaxTime()) ent.setMaxTime(time);
            }
        }

    }


    public int getMinBoardWidth(){ return this.minBoardWidth; }
    public int getMinBoardHeight(){ return this.minBoardHeight; }
    public int getMaxBoardWidth(){ return this.maxBoardWidth; }
    public int getMaxBoardHeight(){ return this.maxBoardHeight; }
    public int getBoardWidth(){ return this.boardWidth; }
    public int getBoardHeight(){ return this.boardHeight; }

    //// accessors for global data
    public int getGlobalNumGames(){
        if(this.globNumGames == 0) return 0;
        else return this.globNumGames;
    }
    public int getGlobalNumMoves(){
        if(this.globNumGames == 0) return -1;
        return this.globNumMoves;
    }
    public int getGlobalMinMoves(){
        if(this.globNumGames == 0) return -1;
        return this.globMinMoves;
    }
    public int getGlobalMaxMoves(){
        if(this.globNumGames == 0) return -1;
        return this.globMaxMoves;
    }
    public int getGlobalNumUndos(){
        if(this.globNumGames == 0) return -1;
        return this.globNumUndos;
    }
    public int getGlobalMinUndos(){
        if(this.globNumGames == 0) return -1;
        return this.globMinUndos;
    }
    public int getGlobalMaxUndos(){
        if(this.globNumGames == 0) return -1;
        return this.globMaxUndos;
    }
    public int getGlobalTotalTime(){
        if(this.globNumGames == 0) return -1;
        return this.globTotalTime;
    }
    public int getGlobalMinTime(){
        if(this.globNumGames == 0) return -1;
        return this.globMaxUndos;
    }
    public int getGlobalMaxTime(){
        if(this.globNumGames == 0) return -1;
        return this.globMaxTime;
    }
    public int getGlobalNumWins(){
        if(this.globNumGames == 0) return -1;
        return this.globNumWins;
    }
    public int getGlobalNumLosses(){
        if(this.globNumGames == 0) return -1;
        return this.globNumLosses;
    }
    public int getGlobalAverageMoves(){
        if(this.globNumGames == 0) return -1;
        return this.globNumMoves/this.globNumGames;
    }
    public int getGlobalAverageUndos(){
        if(this.globNumGames == 0) return -1;
        return this.globNumUndos/this.globNumGames;
    }
    public int getGlobalAverageTime(){
        if(this.globNumGames == 0 || this.globNumWins == 0) return -1;
        return this.globTotalTime/this.globNumWins;
    }
    public int getGlobalNumClassicMode(){
        if(this.globNumGames == 0) return -1;
        return this.globNumClassicMode;
    }
    public int getGlobalNumTimedMode(){
        if(this.globNumGames == 0) return -1;
        return this.globNumGames - this.globNumClassicMode;
    }

    ////////////////////////////////////////
    /// accessors for individual board data
    public int getBoardNumGames(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return 0;
        else return temp.getNumGames();
    }
    public int getBoardNumMoves(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getNumMoves();
    }
    public int getBoardMinMoves(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getMinMoves();
    }
    public int getBoardMaxMoves(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getMaxMoves();
    }
    public int getBoardNumUndos(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getNumUndos();
    }
    public int getBoardMinUndos(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getMinUndos();
    }
    public int getBoardMaxUndos(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getMaxUndos();
    }
    public int getBoardTotalTime(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getTotalTime();
    }
    public int getBoardMinTime(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getMinTime();
    }
    public int getBoardMaxTime(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getMaxTime();
    }
    public int getBoardNumWins(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getNumWins();
    }
    public int getBoardNumLosses(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getNumLosses();
    }
    public int getBoardAverageMoves(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getAverageMoves();
    }
    public int getBoardAverageUndos(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getAverageUndos();
    }
    public int getBoardAverageTime(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getAverageTime();
    }
    public int getBoardNumClassic(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getNumClassic();
    }
    public int getBoardNumTimed(int boardwidth, int boardheight){
        BoardTypeStatEntry temp = entries[boardheight-this.minBoardHeight][boardwidth-this.minBoardWidth];
        if(temp.getNumGames() == 0) return -1;
        else return temp.getNumGames() - temp.getNumClassic();
    }



    @Override
    public String toString(){
        String output = Integer.toString(this.globNumGames) + "," +
                        Integer.toString(this.globNumMoves) + "," +
                        Integer.toString(this.globNumUndos);
        return output;
    }


    /// data class for each array entry in PlayerStats class
    protected class BoardTypeStatEntry implements Serializable
    {
        private int numGames;
        private int numMoves;
        private int minMoves;
        private int maxMoves;
        private int numUndos;
        private int minUndos;
        private int maxUndos;
        private int minTime;
        private int maxTime;
        private int totalTime;
        private int numWins;
        private int numLosses;
        private int numClassic;

        public BoardTypeStatEntry(int ng, int nm, int minm, int maxm, int nu, int minu, int maxu,
                                  int st, int lt, int tott, int nw, int nl, int nclass){
            this.numGames = ng;
            this.numMoves = nm;
            this.minMoves = minm;
            this.maxMoves = maxm;
            this.numUndos = nu;
            this.minUndos = minu;
            this.maxUndos = maxu;
            this.minTime = st;
            this.maxTime = lt;
            this.totalTime = tott;
            this.numWins = nw;
            this.numLosses = nl;
            this.numClassic = nclass;
        }

        public void setAllProperties(int ng, int nm, int minm, int maxm, int nu, int minu, int maxu,
                                        int st, int lt, int tott, int nw, int nl, int nclass){
            this.numGames = ng;
            this.numMoves = nm;
            this.minMoves = minm;
            this.maxMoves = maxm;
            this.numUndos = nu;
            this.minUndos = minu;
            this.maxUndos = maxu;
            this.minTime = st;
            this.maxTime = lt;
            this.totalTime = tott;
            this.numWins = nw;
            this.numLosses = nl;
            this.numClassic = nclass;
        }

        public int getNumGames(){
            return this.numGames;
        }
        public int getNumMoves(){
            return this.numMoves;
        }
        public int getMinMoves(){
            return this.minMoves;
        }
        public int getMaxMoves(){
            return this.maxMoves;
        }
        public int getAverageMoves(){
            return this.numMoves/this.numGames;
        }
        public int getNumUndos(){
            return this.numUndos;
        }
        public int getMinUndos(){
            return this.minUndos;
        }
        public int getMaxUndos(){
            return this.maxUndos;
        }
        public int getAverageUndos(){
            return this.numUndos/this.numGames;
        }
        public int getMinTime(){
            return this.minTime;
        }
        public int getMaxTime(){
            return this.maxTime;
        }
        public int getTotalTime(){
            return this.totalTime;
        }
        public int getAverageTime(){
            if(this.numWins == 0) return -1;
            return this.totalTime/this.numWins;
        }
        public int getNumWins(){
            return this.numWins;
        }
        public int getNumLosses(){
            return this.numLosses;
        }
        public int getNumClassic(){ return this.numClassic; }

        ///////////////////////////
        //// sets new value and RETURN THE delta value
        public int setNumGames(int val){
            int toret = val - this.numGames;
            this.numGames = val;
            return toret;
        }
        public int setNumMoves(int val){
            int toret = val - this.numMoves;
            this.numMoves = val;
            return toret;
        }
        public int setMinMoves(int val){
            int toret = val - this.minMoves;
            this.minMoves = val;
            return toret;
        }
        public int setMaxMoves(int val){
            int toret = val - this.maxMoves;
            this.maxMoves = val;
            return toret;
        }
        public int setNumUndos(int val){
            int toret = val - this.numUndos;
            this.numUndos = val;
            return toret;
        }
        public int setMinUndos(int val){
            int toret = val - this.minUndos;
            this.minUndos = val;
            return toret;
        }
        public int setMaxUndos(int val){
            int toret = val - this.maxUndos;
            this.maxUndos = val;
            return toret;
        }
        public int setMinTime(int val){
            int toret = val - this.minTime;
            this.minTime = val;
            return toret;
        }
        public int setMaxTime(int val){
            int toret = val - this.maxTime;
            this.maxTime = val;
            return toret;
        }
        public int setTotalTime(int val){
            int toret = val - this.totalTime;
            this.totalTime = val;
            return toret;
        }
        public int setNumWins(int val){
            int toret = val - this.numWins;
            this.numWins = val;
            return toret;
        }
        public int setNumLosses(int val){
            int toret = val - this.numLosses;
            this.numLosses = val;
            return toret;
        }
        public int setNumClassic(int val){
            int toret = val - this.numClassic;
            this.numClassic = val;
            return toret;
        }
    }
}
