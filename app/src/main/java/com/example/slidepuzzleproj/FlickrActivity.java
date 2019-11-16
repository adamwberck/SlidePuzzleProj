package com.example.slidepuzzleproj;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FlickrActivity extends Activity {
    EditText searchKeyword;
    Button searchButton;
    ScrollView gallery;
    GridView resultGrid;
    ArrayList<String> resultList = new ArrayList<>();
    Bitmap choice;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        if(!isOnline()){
            Toast toast = Toast.makeText(this, "Cannot connect: network is unavailable", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_flickr); //attach the layout
        searchKeyword = findViewById(R.id.search_text);
        searchButton = findViewById(R.id.search_button);
        gallery = findViewById(R.id.flickr_scroll);
        resultGrid = findViewById(R.id.result_grid);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if(!isOnline()){
                    Toast toast = Toast.makeText(FlickrActivity.this, "Cannot connect: network is unavailable", Toast.LENGTH_SHORT);
                    toast.show();
                    finish();
                }
                resultList = new ArrayList<>();
                apiTask task = new apiTask();
                task.execute(resultList);
            }
        });
    }

    private class apiTask extends AsyncTask<ArrayList<String>, String, String> {
        ProgressDialog progressDialog;
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(ArrayList<String>... arrayLists) {
            callAPI(resultList);
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(FlickrActivity.this,
                    "Please Wait",
                    "Loading images...");
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
            populate(resultList);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void callAPI(ArrayList<String> resultL){
        OkHttpClient client = new OkHttpClient();
        String key = getResources().getString(R.string.api_key);
        String text = searchKeyword.getText().toString();
        String body = "api_key="+key+"&text="+text+"&per_page=50&safe_search=1&content_type=4&extras=original_format&media=photos&privacy_filter=1";
        String url = "https://api.flickr.com/services/rest/?method=flickr.photos.search&"+body;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String result = response.body().string();
            parseItems(resultL, result);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (XmlPullParserException e2) {
            e2.printStackTrace();
        }
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    void parseItems(ArrayList<String> items, String result) throws XmlPullParserException, IOException {
        String farm, server, secret, id, format, URL;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(result));
        int eventType = parser.next();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                farm = parser.getAttributeValue(null, "farm");
                server = parser.getAttributeValue(null, "server");
                secret = parser.getAttributeValue(null, "secret");
                id = parser.getAttributeValue(null, "id");
                format = parser.getAttributeValue(null, "originalformat");
                if(id!=null) {
                    String f = format!=null?format:"png";
                    URL = "https://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + "_z." + f;
                    items.add(URL);
                }
            }

            eventType = parser.next();
        }

    }

    void populate(ArrayList<String> resultList){
        resultGrid.setAdapter(new ImageAdapter(this, resultList));
    }


    protected class ImageAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> uriList;

        public ImageAdapter(Context context, ArrayList<String> uriList) {
            this.context = context;
            this.uriList = uriList;
        }

        public int getCount() {
            return this.uriList.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(this.context);
                imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }
            Picasso.get().load(this.uriList.get(position)).into(imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                    choice = drawable.getBitmap();
                    File photoFile = null;
                    try {
                        photoFile = SaveImage.createImage(FlickrActivity.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (photoFile != null) {
                        try (FileOutputStream out = new FileOutputStream(SaveImage.getPath())) {
                            choice.compress(Bitmap.CompressFormat.PNG, 100, out);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
            return imageView;
        }
    }
}
