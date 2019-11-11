package com.example.slidepuzzleproj;


///// WIP  - Dan ////

import java.io.Serializable;
import java.util.List;

public class PlayerStats implements Serializable {
    private List<BoardStats> completedBoards;
    private List<BoardStats> inProgressBoards;

    private boolean scrambled = false;


    public PlayerStats()
    {
        ///load existing stats data

    }

    public PlayerStats(PlayerStats existing){
        this.completedBoards = existing.getCompletedBoard();
        this.inProgressBoards = existing.getInProgressBoards();
    }

    public List<BoardStats> getCompletedBoard(){
        return this.completedBoards;
    }
    public List<BoardStats> getInProgressBoards(){
        return this.inProgressBoards;
    }

    public class BoardStats implements Serializable{
        private PuzzleBoard board;
        private int moveInt;
        private int undoUsed;
        private int hintUsed;

        private boolean isTimed; // true if game mode is timed mode
        private long timeElapsed; //the current elapsed time
        private long timeRemain; // the millisecond time remaining for the puzzle

        private long ONE_MINUTE = 60000;
        private long ONE_SECOND = 1000;
        private long PLAY_TIME = 3 * ONE_MINUTE;

        public BoardStats(){

        }
    }
}
