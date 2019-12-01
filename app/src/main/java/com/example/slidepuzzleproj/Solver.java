package com.example.slidepuzzleproj;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class Solver {
    private Graph<SolveBoard> mBoardTree;
    private Queue<SolveBoard> mQueue = new PriorityQueue<>();
    private int w;
    private int h;

    public Solver (PuzzleBoard firstBoard)throws CloneNotSupportedException{
        SolveBoard solveBoard = new SolveBoard(firstBoard.getBoardWidth(),
                firstBoard.getBoardHeight(),firstBoard.getBlankIndex(),firstBoard.getPieces());

        mBoardTree = new Graph<>(solveBoard);
        w = solveBoard.w;
        h = solveBoard.h;
        mBoardTree.addVertex(solveBoard);
        solveBoard.number = 0;
        solveBoard.depth = 0;
        mQueue.add(solveBoard);
        List<Direction> solution = solve();
        for(Direction d: solution){
            Log.i("Sol",d.name());
            System.out.println(d.name());
        }
    }

    public List<Direction> solve() throws CloneNotSupportedException {
        int mDist = 1;
        List<Direction> solution =  new ArrayList<>(10);
        while(!mQueue.isEmpty() && mDist>0) {
            SolveBoard qBoard = mQueue.poll();
            solution.add(qBoard.dir);
            List<Direction> dirs = qBoard.slideBlankPossible();
            Direction backDir =  calcBackDir(qBoard.dir);
            for (Direction d : dirs) {
                if(d!=backDir) {
                    SolveBoard b = qBoard.clone();//clone the current board
                    b.slideBlank(d);//slide in direction

                    b.depth = qBoard.depth + 1;//depth set
                    mDist = manhattanDist(b);
                    Log.i("Sol", "D|" + qBoard.depth + " " + d.name() + " = " + mDist);
                    b.number = mDist + b.depth;//set the number for the priority Q
                    b.dir = d;

                    mBoardTree.addVertex(b);
                    mBoardTree.addEdge(qBoard, b);
                    mQueue.add(b);
                    if (mDist == 0) {//done
                        solution.remove(0);//remove the dir which is null
                        return solution;
                    }
                }
            }
        }
        return null;
    }

    private Direction calcBackDir(Direction dir) {
        if(dir == Direction.Down){
            return Direction.Up;
        }
        if(dir == Direction.Up){
            return Direction.Down;
        }
        if(dir == Direction.Right){
            return Direction.Left;
        }
        if(dir == Direction.Left){
            return Direction.Right;
        }
        return null;
    }

    public int manhattanDist(SolveBoard board){
        int total = 0;
        for(SolveBoard.PuzzlePiece piece : board.pieces) {
            if(piece.goalPos!=8) {//don't count blank
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
        return total;
    }

    enum Direction{
        Up,
        Down,
        Left,
        Right
    };

    private class SolveBoard implements Cloneable,Comparable<SolveBoard>{
        public boolean isWin;
        private int w;
        private int h;
        private int blankIndex;
        private int size;
        private PuzzlePiece[] pieces;


        private int number;
        private Direction dir;
        private int depth;

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

        private List<Direction> slideBlankPossible()
        {
            List<Direction> dirs = new ArrayList<>(4);
            //up
            if(this.blankIndex - this.w >= 0)  ///if not on top row
            {
                dirs.add(Direction.Up);
            }
            //down
            if(this.blankIndex + this.w < this.size)
            {
                dirs.add(Direction.Down);
            }
            //left
            if(this.blankIndex % this.size != 0)
            {
                dirs.add(Direction.Left);
            }
            //right
            if(this.blankIndex % this.w != this.w - 1)
            {
                dirs.add(Direction.Right);
            }
            return dirs;
        }

        private boolean slideBlank(Direction dir)
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
