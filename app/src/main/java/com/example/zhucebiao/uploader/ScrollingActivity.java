package com.example.zhucebiao.uploader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.File;

public class ScrollingActivity extends AppCompatActivity {
    EditText text_name;
    EditText text_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        String action = getIntent().getAction();
        Log.e("action", action);
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                Log.e("uri", uri.getPath());
                File file = new File(uri.getPath());
                if (file.exists()) {
                    Log.e("uri", "exist");

                } else Log.e("uri", "null");
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = text_name.getText().toString().trim();
                String pass = text_pass.getText().toString();

                SharedPreferences.Editor editor = getSharedPreferences("lock", MODE_PRIVATE).edit();
                editor.putString("name", name);
                editor.putString("pass", pass);
                editor.commit();
            }
        });

        text_name = (EditText) findViewById(R.id.editText_name);
        text_pass = (EditText) findViewById(R.id.editText_pass);

        SharedPreferences read = getSharedPreferences("lock", MODE_PRIVATE);
        text_name.setText(read.getString("name", "Samba address"));
        text_pass.setText(read.getString("pass", "Password"));
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
            Snackbar.make(findViewById(R.id.fab), "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
