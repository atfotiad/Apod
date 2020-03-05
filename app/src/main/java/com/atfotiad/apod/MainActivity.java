package com.atfotiad.apod;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    String imageUrl;
    TextView explanationView,titleView;

    private static final int PERMISSION_REQUEST_CODE = 1000;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission Granted",Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(this, "Permission Not Granted",Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.saveButton) {
            // do something here
            save();


        }
        return super.onOptionsItemSelected(item);
    }

    private void save() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSION_REQUEST_CODE);
        else {
            AlertDialog dialog = new SpotsDialog(MainActivity.this);
            dialog.show();
            dialog.setMessage("DownLoading...");

            String fileName = UUID.randomUUID().toString() + ".JPG";
            Picasso.get().load(imageUrl).into(new SaveImageHelper(getApplicationContext(), dialog,
                    getApplicationContext().getContentResolver(),
                    fileName, "Image Description"));
        }
    }


    public class DownloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... urls) {
            String result ="";
            URL url;
            HttpURLConnection urlConnection=null;

            try {
                url =new URL(urls[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;

            }catch (Exception e){
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("JSON",s);

            try{
                JSONObject jsonObject = new JSONObject(s);
                String explanation = jsonObject.getString("explanation");
                String title = jsonObject.getString("title");
                imageUrl = jsonObject.getString("url");
                explanationView.setText(explanation);
                titleView.setText(title);

                Picasso.get().load(imageUrl).fit().centerCrop().into(imageView);

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) !=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{},PERMISSION_REQUEST_CODE);



        imageView  = findViewById(R.id.imageView2);
        explanationView = findViewById(R.id.explanationView);
        titleView = findViewById(R.id.titleView);

        DownloadTask task =new DownloadTask();
        task.execute("https://api.nasa.gov/planetary/apod?api_key=l0939OYRfStSuIcwnSmo0tPzfOwK4qeVJVSRzv8o");



    }




}
