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
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.utils.ObjectUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.lang.NonNull;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShareActivity extends Activity {
    EditText shareID;
    EditText shareMess;
    Button shareButton;
    private final long ONE_MINUTE = 60000;
    private final long ONE_SECOND = 1000;
    String resultURL;

    private StitchAppClient stitchClient;
    private RemoteMongoClient mongoClient;
    private RemoteMongoCollection usersCollection;
    private RemoteMongoCollection challengesCollection;
    Document receiver;
    String width;
    String height;
    String seed;
    String timer;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Map config = new HashMap();
        config.put("cloud_name", "trungnguyen");
        MediaManager.init(this, config);

        receiver = new Document();
        stitchClient = Stitch.getDefaultAppClient();
        mongoClient = stitchClient.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
        usersCollection = mongoClient.getDatabase("MultiplayerChallenge").getCollection("Users");
        challengesCollection = mongoClient.getDatabase("MultiplayerChallenge").getCollection("Challenges");

        setContentView(R.layout.activity_share);
        shareID = findViewById(R.id.shareID);
        shareMess = findViewById(R.id.share_message);
        shareButton = findViewById(R.id.share_confirm);
        Intent intent = getIntent();
        String[] shareResult = intent.getStringArrayExtra("INFO");

        String URI = shareResult[0];

        timer = shareResult[2];
        width = shareResult[3];
        height = shareResult[4];
        seed = String.valueOf(User.getSeed());

        Log.i("Timer", timer);
        String minSecTimer = Long.parseLong(shareResult[5]) / ONE_MINUTE + ":" + Long.parseLong(shareResult[5]) % ONE_MINUTE / ONE_SECOND;
        if(shareResult[1].equals("true")){
            shareMess.setText("Your friend "+User.getID()+" completed a " + shareResult[3]+ " x " + shareResult[4] + " puzzle in "+minSecTimer+" . Can you do better?");
        } else {
            shareMess.setText("Your friend "+User.getID()+" failed to complete a " + shareResult[3]+ " x " + shareResult[4] + " puzzle "+URI+" within "+minSecTimer+" . Can you do better?");
        }
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shareID.getText().length()<1){
                    Toast.makeText(ShareActivity.this, "Must supply receiver ID", Toast.LENGTH_SHORT).show();
                } else {
                    message = shareMess.getText().toString();
                    receiver.append("user_id", shareID.getText().toString());
                    UploadTask task = new UploadTask();
                    task.execute(URI);
                }
            }
        });
    }

    public class UploadTask extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... URI) {
        RemoteFindIterable findResults = usersCollection
                .find(receiver)
                .projection(new Document().append("_id", 0))
                .sort(new Document().append("name", 1));
        Task<List<Document>> itemsTask = findResults.into(new ArrayList<Document>());
        itemsTask.addOnCompleteListener(new OnCompleteListener<List<Document>>() {
            @Override
            public void onComplete(@NonNull Task<List<Document>> task) {
                if (task.isSuccessful()) {
                    List<Document> items = task.getResult();
                    Log.d("app", String.format("successfully found %d documents", items.size()));
                    if(items.size()>=1){
                        upload(URI[0]);
                    } else {
                        Toast.makeText(ShareActivity.this, "User ID not found!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("app", "failed to find documents with: ", task.getException());
                }
            }
        });
        return null;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(String result) {
    }

    }

    private String upload(String uri){
        Cloudinary cloudinary = new Cloudinary();
        Log.i("HERE", uri);
        String requestId = MediaManager.get().upload(uri)
                .unsigned("ydogg4wb").callback(new UploadCallback() {
                    ProgressDialog progressDialog;
                    @Override
                    public void onStart(String requestId) {
                    }
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        resultURL = resultData.get("url").toString();
                        InsertTask task = new InsertTask();
                        task.execute();
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(ShareActivity.this, "Sharing failed. Please check connection and try again", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }}).dispatch();
        return null;
    }

    public class InsertTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        @Override
        protected String doInBackground(String... s) {
            Document challenge = new Document()
                    .append("challengerID", User.getID())
                    .append("receiverID", receiver.get("user_id"))
                    .append("url", resultURL)
                    .append("width", width)
                    .append("height", height)
                    .append("time", timer)
                    .append("seed", seed)
                    .append("message", message);
            final Task<RemoteInsertOneResult> insertTask = challengesCollection.insertOne(challenge);
            insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
                @Override
                public void onComplete(@NonNull Task <RemoteInsertOneResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ShareActivity.this, "Successfully shared the puzzle!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ShareActivity.this, "Failed to insert to database", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ShareActivity.this,
                    "Please Wait",
                    "Inserting into Database...");
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
        }

    }
}


