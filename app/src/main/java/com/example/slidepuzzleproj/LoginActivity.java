package com.example.slidepuzzleproj;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.lang.NonNull;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

// Base Stitch Packages
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
// Stitch Authentication Packages
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
// MongoDB Service Packages
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
// Utility Packages
import com.mongodb.stitch.core.internal.common.BsonUtils;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
    EditText id;
    EditText pw;
    EditText repw;
    Button signup;
    Button login;
    Document newItem;

    private StitchAppClient stitchClient;
    private RemoteMongoClient mongoClient;
    private RemoteMongoCollection itemsCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        try {
            stitchClient = Stitch.getDefaultAppClient();
            mongoClient = stitchClient.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
            itemsCollection = mongoClient.getDatabase("MultiplayerChallenge").getCollection("Users");

            setContentView(R.layout.activity_login);
            id = findViewById(R.id.user_id);
            pw = findViewById(R.id.password);
            repw = findViewById(R.id.re_password);
            signup = findViewById(R.id.signup);
            login = findViewById(R.id.login);
            newItem = new Document();
            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (User.getID() == null) {
                        String uid = id.getText().toString();
                        String pass = pw.getText().toString();
                        String repass = repw.getText().toString();
                        if (uid.length() < 1 || pass.length() < 1 || repass.length() < 1) {
                            Toast.makeText(LoginActivity.this, "One or more fields are missing. Please try again.", Toast.LENGTH_SHORT).show();
                        } else if (!pass.equals(repass)) {
                            Toast.makeText(LoginActivity.this, "Passwords must match", Toast.LENGTH_SHORT).show();
                        } else {
                            SignUpTask task = new SignUpTask();
                            newItem.append("user_id", uid);
                            newItem.append("pw", pass);
                            task.execute(newItem);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Please log out and try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (User.getID() == null) {
                        String uid = id.getText().toString();
                        String pass = pw.getText().toString();
                        if (uid.length() < 1 || pass.length() < 1) {
                            Toast.makeText(LoginActivity.this, "One or more fields are missing. Please try again.", Toast.LENGTH_SHORT).show();
                        } else {
                            LoginTask task = new LoginTask();
                            newItem.append("user_id", uid);
                            newItem.append("pw", pass);
                            task.execute(newItem);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Please log out and try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch(Exception e){
            Log.i("[LOGINACT]", e.getMessage() + " | " + e.getCause());
            Toast.makeText(this, e.getMessage() + "|" + e.getCause(), Toast.LENGTH_SHORT).show();
        }
    }

    public class SignUpTask extends AsyncTask<Document, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(Document... d) {
            signUp(newItem);
            try {
                Thread.sleep( 3 * 1000 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(User.getID()!=null){
                return "Signed Up Successfully! You can now access Inbox.";
            } else {
                return "Network not available or userID has been taken!";
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LoginActivity.this,
                    "Please Wait",
                    "Signing Up...");
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    public class LoginTask extends AsyncTask<Document, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(Document... d) {
            login(newItem);
            try {
                Thread.sleep( 3 * 1000 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(User.getID()!=null){
                return "Login Successfully! You can now access Inbox.";
            } else {
                return "Network not available or wrong User ID / password!";
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LoginActivity.this,
                    "Please Wait",
                    "Login in...");
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    public void signUp(Document newItem){

        final Task<RemoteInsertOneResult> insertTask = itemsCollection.insertOne(newItem);
        insertTask.addOnCompleteListener(new OnCompleteListener<RemoteInsertOneResult>() {
            @Override
            public void onComplete(@NonNull Task <RemoteInsertOneResult> task) {
                if (task.isSuccessful()) {
                    Log.d("app", String.format("successfully inserted item with id %s",
                            task.getResult().getInsertedId()));
                    User.setID(newItem.getString("user_id"));
                } else {
                    Log.e("app", "failed to insert document with: ", task.getException());
                }
            }
        });
    }

    public void login(Document newItem){

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
                    User.setID(items.get(0).getString("user_id"));
                } else {
                    Log.e("app", "failed to find documents with: ", task.getException());
                }
            }
        });
    }
}
