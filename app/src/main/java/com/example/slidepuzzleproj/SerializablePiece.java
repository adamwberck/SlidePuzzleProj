package com.example.slidepuzzleproj;

import java.io.Serializable;


    public class SerializablePiece implements Serializable {
        private int corPos;
        private int curPos;
        private boolean isBlank;
        public SerializablePiece(int cor, int cur, boolean blank)
        {
            this.corPos = cor;
            this.curPos  = cur;
            this.isBlank = blank;
        }

        public int getCorrectPos()
        {
            return this.corPos;
        }

        public int getCurrentPos()
        {
            return this.curPos;
        }

        public boolean getIsBlank()
        {
            return this.isBlank;
        }
    }
