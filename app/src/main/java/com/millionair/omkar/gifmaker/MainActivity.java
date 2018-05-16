package com.millionair.omkar.gifmaker;

import android.Manifest;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.millionair.omkar.gifmaker.adapters.MyGifsAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.EventListener;

public class MainActivity extends AppCompatActivity implements MyGifsAdapter.OnItemClickedListener {

    Button mButton1;
    Button mButton2;
    Button mButton3;
    RecyclerView mRecyclerView;
    MyGifsAdapter mMyGifsAdapter;
    File gifMaker;
    File[] files;
    boolean b2Clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton1 = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);
        mButton3 = (Button) findViewById(R.id.button3);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
        mRecyclerView.setHasFixedSize(true);

        gifMaker = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Gif Maker");

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        } else {
            files = gifMaker.listFiles();
            //Log.d("Gif: ", files[0].getAbsolutePath());
            mMyGifsAdapter = new MyGifsAdapter(files, MainActivity.this, this);
            mRecyclerView.setAdapter(mMyGifsAdapter);
        }

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b2Clicked = true;
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Video"), 2);
                }
            }
        });

        mButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.CAMERA},
                            2);
                } else {
                    Intent intent = new Intent("android.media.action.VIDEO_CAMERA");
                    startActivityForResult(intent, 2);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (b2Clicked) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("video/*");
                        startActivityForResult(Intent.createChooser(intent, "Select Video"), 2);
                    } else {
                        files = gifMaker.listFiles();
                        mMyGifsAdapter = new MyGifsAdapter(files, MainActivity.this, this);
                        mRecyclerView.setAdapter(mMyGifsAdapter);
                    }
                } else {

                }
                break;
            case 2:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Intent intent = new Intent("android.media.action.VIDEO_CAMERA");
                    startActivityForResult(intent, 2);
                } else {

                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        ArrayList<String> uris = new ArrayList<>();
        switch (requestCode) {
            case 1:
                ClipData clipData = data.getClipData();
                Uri uri;
                Uri singleUri = data.getData();
                if (clipData != null) {
                    int size = clipData.getItemCount();
                    for (int i = 0; i < size; i++) {
                        uri = clipData.getItemAt(i).getUri();
                        //Log.d("MainActivityClipdata: ", uri.toString());
                        uris.add(uri.toString());
                    }
                } else if (singleUri != null) {
                    //Log.d("MainActivityData: ", uri1.toString());
                    uris.add(singleUri.toString());
                }
                startActivity(new Intent(MainActivity.this, GifMakerActivity.class).putStringArrayListExtra("URIS", uris).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
            case 2:
                Uri videoUri = data.getData();
                if (videoUri != null) {
                    Log.d("MainActivityData: ", videoUri.toString());
                    uris.add(videoUri.toString());
                }
                startActivity(new Intent(MainActivity.this, GifMakerActivity.class).putStringArrayListExtra("VIDEOURI", uris).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                break;
        }
    }

    @Override
    public void onItemClicked(int position) {
        startActivity(new Intent(MainActivity.this, GifMakerActivity.class).putExtra("GIFPATH",files[position].getAbsolutePath()).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Do you want to exit the app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
}
