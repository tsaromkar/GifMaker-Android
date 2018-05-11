package com.millionair.omkar.gifmaker;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.millionair.omkar.gifmaker.adapters.MyGifsAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button mButton1;
    Button mButton2;
    Button mButton3;
    RecyclerView mRecyclerView;
    MyGifsAdapter mMyGifsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton1 = (Button) findViewById(R.id.button1);
        mButton2 = (Button) findViewById(R.id.button2);
        mButton3 = (Button) findViewById(R.id.button3);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
        mRecyclerView.setHasFixedSize(true);

        mButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(Intent.createChooser(intent, "Select Video"), 2);
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
                }
                Intent intent = new Intent("android.media.action.VIDEO_CAMERA");
                startActivityForResult(intent, 2);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                } else {

                }
                break;
            case 2:
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                } else {

                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (data == null) {
            return;
        }
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
                startActivity(new Intent(MainActivity.this, GifMakerActivity.class).putStringArrayListExtra("URIS", uris));
                break;
            case 2:
                Uri videoUri = data.getData();
                if (videoUri != null) {
                    //Log.d("MainActivityData: ", uri1.toString());
                    uris.add(videoUri.toString());
                }
                startActivity(new Intent(MainActivity.this, GifMakerActivity.class).putStringArrayListExtra("VIDEOURI", uris));
                break;
        }
    }
}
