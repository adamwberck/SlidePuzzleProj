package com.example.slidepuzzleproj;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.lang.NonNull;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

// Base Stitch Packages
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
// Stitch Authentication Packages
// MongoDB Service Packages
// Utility Packages

import org.bson.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends Activity {
    private TextView welcome;
    private LinearLayout inbox;
    private Button refresh;
    private Button logout;

    private StitchAppClient stitchClient;
    private RemoteMongoClient mongoClient;
    private RemoteMongoCollection itemsCollection;
    private Uri targetURI;

    List<Document> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        stitchClient = Stitch.getDefaultAppClient();
        mongoClient = stitchClient.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
        itemsCollection = mongoClient.getDatabase("MultiplayerChallenge").getCollection("Challenges");
        welcome = findViewById(R.id.welcome_text);
        inbox = findViewById(R.id.scroll_content);
        refresh = findViewById(R.id.refresh_button);
        logout = findViewById(R.id.logout_button);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.setID(null);
                Toast.makeText(InboxActivity.this, "Log off successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        welcome.setText("Welcome " + User.getID()+"!");
        refresh();
    }

    public void refresh(){
        results = new ArrayList<>();
        inbox.removeAllViews();
        FetchTask task = new FetchTask();
        task.execute();
    }

    private class FetchTask extends AsyncTask<ArrayList<String>, String, String> {
        ProgressDialog progressDialog;
        @Override
        protected String doInBackground(ArrayList<String>... arrayLists) {
            fetch();
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(InboxActivity.this,
                    "Please Wait",
                    "Loading inbox...");
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
        }

        public void fetch(){
            Document newItem = new Document()
                    .append("receiverID", User.getID());
            RemoteFindIterable findResults = itemsCollection
                    .find(newItem)
                    .projection(new Document().append("_id", 0))
                    .sort(new Document().append("name", 1));
            Task <List<Document>> itemsTask = findResults.into(new ArrayList<Document>());
            itemsTask.addOnCompleteListener(new OnCompleteListener <List<Document>> () {
                @Override
                public void onComplete(@NonNull Task<List<Document>> task) {
                    if (task.isSuccessful()) {
                        List<Document> items = task.getResult();
                        Log.d("app", String.format("successfully found %d documents", items.size()));
                        for (int i = 0; i<items.size(); i++) {
                            Document item = items.get(i);
                            Log.d("app", String.format("successfully found:  %s", item.toString()));
                            results.add(item);
                            TextView newChallenge = new TextView(InboxActivity.this);
                            newChallenge.setMinHeight(150);
                            newChallenge.setText("Challenger: " + item.getString("challengerID") + ". Dimension: " + item.get("width").toString() + " x " + item.get("height").toString()
                            +"\nMessage: "+ item.getString("message"));
                            int finalI = i;
                            newChallenge.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DownloadTask task = new DownloadTask();
                                    task.execute(finalI);
                                }
                            });
                            inbox.addView(newChallenge);
                        }
                    } else {
                        Log.e("app", "failed to find documents with: ", task.getException());
                    }
                }
            });
        }
    }

    private class DownloadTask extends AsyncTask<Integer, Integer, Integer> {
        ProgressDialog progressDialog;
        @Override
        protected Integer doInBackground(Integer... i) {
            int j = i[0];
            URL imageurl = null;
            try {
                imageurl = new URL(results.get(j).getString("url"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Bitmap tempBitmap = null;
            try {
                tempBitmap = BitmapFactory.decodeStream(imageurl.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            File photoFile = null;
            try {
                photoFile = SaveImage.createImage(InboxActivity.this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null && tempBitmap != null) {
                try (FileOutputStream out = new FileOutputStream(SaveImage.getPath())) {
                    tempBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    targetURI = Uri.parse(SaveImage.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(targetURI == null)
            {
                targetURI = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.wooloo2);
            }
            return j;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(InboxActivity.this,
                    "Please Wait",
                    "Downloading Image...");
        }

        @Override
        protected void onPostExecute(Integer index) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
            Intent data = new Intent(InboxActivity.this, PlayActivity.class);
            data.putExtra("WIDTH", Integer.valueOf(results.get(index).getString("width")));
            data.putExtra("HEIGHT", Integer.valueOf(results.get(index).getString("height")));
            data.putExtra("time", Long.valueOf(results.get(index).getString("time")));
            data.putExtra("seed", Long.valueOf( results.get(index).getString("seed")));
            data.putExtra("picture", targetURI);
            startActivity(data);
            finish();
        }
    }
}


