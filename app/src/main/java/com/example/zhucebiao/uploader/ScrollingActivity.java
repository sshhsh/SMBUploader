package com.example.zhucebiao.uploader;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

public class ScrollingActivity extends AppCompatActivity {
    public static final int BUFFSIZE = 5 * 1024;
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1024;
    EditText text_name;
    EditText text_pass;
    EditText text_server;
    TextView textView_type;
    TextView textView_uri;
    TextView textView_path;
    ProgressBar progressBar;
    int index = 0;
    Handler handler = new Handler();
    File file;
    SmbFile smbFile;
    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        getPremission();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String server = text_server.getText().toString().trim();
                        String name = text_name.getText().toString().trim();
                        String pass = text_pass.getText().toString();

                        SharedPreferences.Editor editor = getSharedPreferences("lock", MODE_PRIVATE).edit();
                        editor.putString("server", server);
                        editor.putString("name", name);
                        editor.putString("pass", pass);
                        editor.apply();

                        if (flag == 1) try {
                            flag = 0;
                            showMessage("Uploading to " + server);
                            String smbUrl = "smb://" + name + ":" + pass + "@" + server + "/temp/filesFromUploader/" + file.getName();
                            Log.e("uri", smbUrl);
                            FileInputStream in = new FileInputStream(file);
                            smbFile = new SmbFile(smbUrl);
                            smbFile.connect();
                            if (!smbFile.exists())
                                smbFile.createNewFile();
                            SmbFileOutputStream out = new SmbFileOutputStream(smbFile);
                            OutputStream fout = new BufferedOutputStream(out);
                            byte b[] = new byte[BUFFSIZE];
                            int len;
                            index = 0;
                            long filelen = in.available();
                            progressBar.setMax((int) (filelen));
                            while ((len = in.read(b)) != -1) {
                                fout.write(b, 0, len);
                                index += BUFFSIZE;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setProgress(index);
                                    }
                                });
                            }
                            fout.flush();
                            fout.close();
                            showMessage("Uploading to " + server + " is completed!");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            showMessage("FileNotFoundException");
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                            showMessage("UnknownHostException");
                        } catch (SmbException e) {
                            e.printStackTrace();
                            showMessage("SmbException");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            showMessage("MalformedURLException");
                        } catch (IOException e) {
                            e.printStackTrace();
                            showMessage("IOException");
                        }
                        else {
                            showMessage("Nothing to upload, saving the properties");
                        }
                    }
                }).start();


            }
        });

        text_server = (EditText) findViewById(R.id.editText_server);
        text_name = (EditText) findViewById(R.id.editText_name);
        text_pass = (EditText) findViewById(R.id.editText_pass);
        textView_type = (TextView) findViewById(R.id.textView_type);
        textView_uri = (TextView) findViewById(R.id.textView_uri);
        textView_path = (TextView) findViewById(R.id.textView_path);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        SharedPreferences read = getSharedPreferences("lock", MODE_PRIVATE);
        text_server.setText(read.getString("server", "Server address"));
        text_name.setText(read.getString("name", "User Name"));
        text_pass.setText(read.getString("pass", "Password"));

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Log.e("action", action);
        if (Intent.ACTION_VIEW.equals(action) || Intent.ACTION_SEND.equals(action)) {
            if (type == null) {
                //doNothing
            } else {
                Uri uri = intent.getData();
                if (uri == null) {
                    uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                }
                String path = GetPathFromUri.getPath(this, uri);
                textView_uri.setText(uri.toString());
                textView_type.setText(type);
                textView_path.setText(path);
                file = new File(path);
                flag = 1;
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showMessage("This is stupid!");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMessage(String s) {
        Snackbar.make(findViewById(R.id.fab), s, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void getPremission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            showMessage("Didn't granted");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            showMessage("Granted");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
}
