package com.example.slidepuzzleproj;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShareActivity extends Activity {
    EditText shareID;
    EditText shareMess;
    Button shareButton;
    String resultURL;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Map config = new HashMap();
        config.put("cloud_name", "trungnguyen");
        MediaManager.init(this, config);

        setContentView(R.layout.activity_share);
        shareID = findViewById(R.id.shareID);
        shareMess = findViewById(R.id.share_message);
        shareButton = findViewById(R.id.share_confirm);
        Intent intent = getIntent();
        String[] shareResult = intent.getStringArrayExtra("INFO");

        String URI = shareResult[0];
        if(shareResult[1].equals("true")){
            shareMess.setText("Your friend abc123 completed a " + shareResult[3]+ " x " + shareResult[4] + " puzzle in "+shareResult[2]+" . Can you do better?");
        } else {
            shareMess.setText("Your friend abc123 failed to complete a " + shareResult[3]+ " x " + shareResult[4] + " puzzle "+URI+" within "+shareResult[2]+" . Can you do better?");
        }
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadTask task = new UploadTask();
                task.execute(URI);
            }
        });
    }

    public class UploadTask extends AsyncTask<String, String, String> {

    ProgressDialog progressDialog;

    @Override
    protected String doInBackground(String... URI) {
        return upload(URI[0]);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(ShareActivity.this,
                "Please Wait",
                "Uploading images...");
    }

    @Override
    protected void onPostExecute(String result) {
        // execution of result of Long time consuming operation
        progressDialog.dismiss();
        Toast.makeText(ShareActivity.this, "Successfully shared the puzzle!", Toast.LENGTH_SHORT).show();
    }

    }

    private String upload(String uri){
        Cloudinary cloudinary = new Cloudinary();
        Log.i("HERE", uri);
        String requestId = MediaManager.get().upload(uri)
                .unsigned("ydogg4wb").dispatch();
        return null;
    }
}
