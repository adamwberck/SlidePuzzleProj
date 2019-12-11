package com.example.slidepuzzleproj;

import android.os.AsyncTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;


public class Solver extends AsyncTask<Void,Void,List<PuzzleBoard.Direction>>{
    private PlayActivity mPlayActivity;
    private int w;
    private int h;
    private Queue<SolveBoard> mQueue = new PriorityQueue<>();

    public Solver (PuzzleBoard firstBoard,PlayActivity playActivity){
        SolveBoard solveBoard = new SolveBoard(firstBoard.getBoardWidth(),
                firstBoard.getBoardHeight(),firstBoard.getBlankIndex(),firstBoard.getPieces());
        mPlayActivity = playActivity;
        w = solveBoard.w;
        h = solveBoard.h;
        solveBoard.number = 0;
        mQueue.add(solveBoard);
    }



    protected List<PuzzleBoard.Direction> doInBackground(Void... v){
        try {
            return solve();
        }
        catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        return null;
    }


    protected void onPostExecute(List<PuzzleBoard.Direction> solution){
        mPlayActivity.solutionFound(solution);
    }





    public List<PuzzleBoard.Direction> solve() throws CloneNotSupportedException {
        mQueue.peek().gScore = 0;
        mQueue.peek().number = manhattanDist(mQueue.peek());
        Map<SolveBoard,SolveBoard> cameFrom = new HashMap<>();
        while(!mQueue.isEmpty()) {
            SolveBoard currentBoard = mQueue.poll();
            if(currentBoard.isWin){
                //done
                return reconstructPath(cameFrom,currentBoard);
            }
            List<PuzzleBoard.Direction> dirs = currentBoard.slideBlankPossible();
            PuzzleBoard.Direction backDir =  calcBackDir(currentBoard.dir);
            for (PuzzleBoard.Direction d : dirs) {
                if(d!=backDir) {
                    /*
                    SolveBoard neighborBoard = currentBoard.clone();//clone the current board
                    neighborBoard.slideBlank(d);//slide in direction
                    */
                    SolveBoard neighborBoard = getBoard(currentBoard,d);
                    int tentative_gScore = currentBoard.gScore + 1;
                    if( tentative_gScore < neighborBoard.gScore || neighborBoard.gScore<0){
                        //record the score
                        cameFrom.put(neighborBoard,currentBoard);
                        neighborBoard.gScore = tentative_gScore;
                        neighborBoard.dir = d;
                        int mDist =  manhattanDist(neighborBoard);
                        //int lin = linearConflicts(neighborBoard);
                        neighborBoard.number = tentative_gScore+mDist;//+lin*2;
                        if(!mQueue.contains(neighborBoard)){
                            mQueue.add(neighborBoard);
                        }
                    }
                }
            }
        }
        return null;
    }

    private SolveBoard getBoard(SolveBoard currentBoard, PuzzleBoard.Direction d)throws CloneNotSupportedException{
        SolveBoard board = currentBoard.clone();
        board.number = 0;
        board.gScore = -1;
        board.dir = null;
        board.isWin = false;
        board.slideBlank(d);
        for (SolveBoard b : mQueue){
            boolean matched =  true;
            for(int i=0;i<b.pieces.length;i++){
                if(b.pieces[i].goalPos != board.pieces[i].goalPos){
                    matched = false;
                    i=b.pieces.length;
                }
            }
            if(matched){
                return b;
            }
        }
        return board;
    }

    private int linearConflicts(SolveBoard board){
        int conflicts = 0;
        //check rows
        for(int r=0;r<h;r++) {
            for (int i = 0; i < w; i++) {
                for (int j = i + 1; j < w; j++) {
                    if(board.pieces[i+r*w].goalPos/w == r) {
                        if (board.pieces[i + r * w].goalPos / w == board.pieces[j + r * w].goalPos / w  //are supposed be on this line
                                && board.pieces[i + r * w].goalPos > board.pieces[j + r * w].goalPos) {//are in the wrong order
                            conflicts++;
                        }
                    }
                }
            }
        }
        //check column
        for(int r=0;r<w;r++) {
            for (int i = 0; i < h; i++) {
                for (int j = i + 1; j < h; j++) {
                    if(board.pieces[i*w+r].goalPos%w == r) {
                        if (board.pieces[i * w + r].goalPos / w == board.pieces[j * w + r].goalPos / w  //are supposed be on this line
                                && board.pieces[i * w + r].goalPos > board.pieces[j * w + r].goalPos) {//are in the wrong order
                            conflicts++;
                        }
                    }
                }
            }
        }
        return conflicts;
    }

    private List<PuzzleBoard.Direction> reconstructPath(Map<SolveBoard,SolveBoard> cameFrom, SolveBoard current){
        List<PuzzleBoard.Direction> path = new LinkedList<>();
        path.add(current.dir);
        while(cameFrom.containsKey(current)){
            current = cameFrom.get(current);
            path.add(0,current.dir);
        }
        path.remove(0);
        return path;
    }

    private PuzzleBoard.Direction calcBackDir(PuzzleBoard.Direction dir) {
        if(dir == PuzzleBoard.Direction.Down){
            return PuzzleBoard.Direction.Up;
        }
        if(dir == PuzzleBoard.Direction.Up){
            return PuzzleBoard.Direction.Down;
        }
        if(dir == PuzzleBoard.Direction.Right){
            return PuzzleBoard.Direction.Left;
        }
        if(dir == PuzzleBoard.Direction.Left){
            return PuzzleBoard.Direction.Right;
        }
        return null;
    }

    public int manhattanDist(SolveBoard board){
        int total = 0;
        for(SolveBoard.PuzzlePiece piece : board.pieces) {
            if(piece.goalPos!=board.pieces.length-1) {//don't count blank
                int pos = piece.pos;
                int goalPos = piece.goalPos;

                int x = pos % w;
                int goalX = goalPos % w;
                int y = pos / w;
                int goalY = goalPos / w;

                int distX = Math.abs(x - goalX);
                int distY = Math.abs(y - goalY);
                total += distX + distY;
            }
        }
        if(total==0){
            board.isWin = true;
        }
        return total;
    }


    private class SolveBoard implements Cloneable,Comparable<SolveBoard>{
        public boolean isWin = false;
        private int gScore = -1;
        private int number;

        private int w;
        private int h;
        private int blankIndex;
        private int size;
        private PuzzlePiece[] pieces;


        private PuzzleBoard.Direction dir;
        public SolveBoard clone() throws CloneNotSupportedException{
            SolveBoard clone = (SolveBoard) super.clone();
            clone.pieces = new PuzzlePiece[pieces.length];
            for(int i=0;i<pieces.length;i++){
                clone.pieces[i] = new PuzzlePiece(pieces[i].pos,pieces[i].goalPos);
            }
            return clone;
        }

        private SolveBoard(int w,int h, int blankIndex,PuzzleBoard.PuzzlePiece[] pbPieces){
            this.w = w;
            this.h = h;
            this.size = w*h;
            this.blankIndex = blankIndex;

            int i=0;
            this.pieces = new PuzzlePiece[size];
            for(PuzzleBoard.PuzzlePiece p : pbPieces){
                this.pieces[i++] = new PuzzlePiece(p.getCurrentPos(),p.getCorrectPos());
            }


        }

        @NotNull
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            for(PuzzlePiece p : pieces){
                str.append(p.goalPos);
                str.append(" ");
            }
            return str.toString();
        }


        private class PuzzlePiece{
            private int pos;
            private int goalPos;

            private PuzzlePiece(int currentPos, int correctPos) {
                pos = currentPos; goalPos = correctPos;
            }
        }

        @Override
        public int compareTo(SolveBoard other){
            if(other.number == number)
                return 0;
            return number>other.number ? 1 : -1 ;
        }

        private List<PuzzleBoard.Direction> slideBlankPossible()
        {
            List<PuzzleBoard.Direction> dirs = new ArrayList<>(4);
            //up
            if(this.blankIndex - this.w >= 0)  ///if not on top row
            {
                dirs.add(PuzzleBoard.Direction.Up);
            }
            //down
            if(this.blankIndex + this.w < this.size)
            {
                dirs.add(PuzzleBoard.Direction.Down);
            }
            //left
            if(this.blankIndex % this.size != 0)
            {
                dirs.add(PuzzleBoard.Direction.Left);
            }
            //right
            if(this.blankIndex % this.w != this.w - 1)
            {
                dirs.add(PuzzleBoard.Direction.Right);
            }
            return dirs;
        }

        private boolean slideBlank(PuzzleBoard.Direction dir)
        {
            switch(dir)
            {
                case Up: //up
                    if(this.blankIndex - this.w >= 0)  ///if not on top row
                    {
                        swapPieces(this.blankIndex, this.blankIndex - this.w);
                        this.blankIndex = this.blankIndex - this.w;
                        return true;
                    }
                    return false;

                case Down: //down
                    if(this.blankIndex + this.w < this.size)
                    {
                        swapPieces(this.blankIndex, this.blankIndex + this.w);
                        this.blankIndex = this.blankIndex + this.w;
                        return true;
                    }
                    return false;

                case Left://left
                    if(this.blankIndex % this.w != 0)
                    {
                        swapPieces(this.blankIndex, this.blankIndex - 1);
                        this.blankIndex = this.blankIndex - 1;
                        return true;
                    }
                    return false;

                case Right://right
                    if(this.blankIndex % this.w != this.w - 1)
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

        private void swapPieces(int i, int j) {
            if (isBetween(i, 0, this.size) && isBetween(j, 0, this.size)) {
                PuzzlePiece temp = this.pieces[i];
                this.pieces[i] = this.pieces[j];
                this.pieces[j] = temp;

                this.pieces[i].pos = i;
                this.pieces[j].pos = j;
            }
        }

        private boolean isBetween(int a, int b, int y) {
            return a >= b && a < y;
        }

    }
}
