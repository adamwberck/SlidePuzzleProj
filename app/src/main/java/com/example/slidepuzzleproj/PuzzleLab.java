package com.example.slidepuzzleproj;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class PuzzleLab implements Serializable {//this is a singleton
    private static PuzzleLab sPuzzleLab;
    private static final String FILE_NAME = "puzzle.lab";
    private List<PlayerStats> stats;
    private int mCurrentIndex;


    private PuzzleLab(){
        stats = new ArrayList<>(5);
        mCurrentIndex = 0;
    }

    public static PuzzleLab get(Context context){
        if(sPuzzleLab==null){
            try {
                sPuzzleLab = loadLab(context);
            } catch (IOException e) {
                Log.i(TAG, "IOException");
                sPuzzleLab = new PuzzleLab();
            } catch (ClassNotFoundException c) {
                c.printStackTrace();
                Log.i(TAG, "Class not found exception");
                sPuzzleLab = new PuzzleLab();
            }
            return sPuzzleLab;
        }
        return sPuzzleLab;
    }



    public static PuzzleLab loadLab(Context context) throws IOException,ClassNotFoundException{
        FileInputStream fis = context.openFileInput(FILE_NAME);
        ObjectInputStream ois = new ObjectInputStream(fis);
        PuzzleLab lab = (PuzzleLab) ois.readObject();
        fis.close();
        ois.close();
        return lab;
    }


    public static void saveLab(Context context){
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(sPuzzleLab);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerStats getCurrentBoard() {
        return stats.get(mCurrentIndex);
    }
}
