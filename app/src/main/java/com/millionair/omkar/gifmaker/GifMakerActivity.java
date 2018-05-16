package com.millionair.omkar.gifmaker;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.gif.GifDrawableEncoder;
import com.millionair.omkar.gifmaker.classes.AnimatedGifEncoder;
import com.millionair.omkar.gifmaker.classes.AnimatedGifWriter;
import com.millionair.omkar.gifmaker.adapters.FramesAdapter;
import com.millionair.omkar.gifmaker.classes.MyAnimationDrawable;
import com.waynejo.androidndkgif.GifDecoder;
import com.waynejo.androidndkgif.GifEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC;
import static wseemann.media.FFmpegMediaMetadataRetriever.OPTION_CLOSEST;

public class GifMakerActivity extends AppCompatActivity implements FramesAdapter.ResetFrames {

    ImageView mImageView;
    ArrayList<String> uris;
    ArrayList<String> videouri;
    MyAnimationDrawable animation;
    Bitmap bitmap;

    Button mPlayButton;
    Button mPauseButton;
    SeekBar mSeekbar;
    TextView mNoOfFramesTextView;
    int duration;
    int framespersec;

    RecyclerView mRecyclerView;
    FramesAdapter mFramesAdapter;
    ArrayList<Bitmap> bitmaps;

    Button mAddImagesButton;
    String gifPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_maker);

        mImageView = (ImageView) findViewById(R.id.imageview);
        uris = new ArrayList<>();
        videouri = new ArrayList<>();
        animation = new MyAnimationDrawable();

        mPlayButton = (Button) findViewById(R.id.play_button);
        mPauseButton = (Button) findViewById(R.id.pause_button);
        mSeekbar = (SeekBar) findViewById(R.id.seekbar);
        mNoOfFramesTextView = (TextView) findViewById(R.id.noofframes_textview);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setHasFixedSize(true);
        bitmaps = new ArrayList<>();
        mAddImagesButton = (Button) findViewById(R.id.add_images_button);

        uris = getIntent().getStringArrayListExtra("URIS");
        if (uris != null) {
            try {
                for (String uri : uris) {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(uri));
                    bitmaps.add(bitmap);
                    animation.addFrame(new BitmapDrawable(getResources(), bitmap), duration);
                 }
                animation.setOneShot(false);
                mImageView.setImageDrawable(animation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        videouri = getIntent().getStringArrayListExtra("VIDEOURI");
        if (videouri != null) {
            extractFramesFromVideo(Uri.parse(videouri.get(0)));
            bitmaps.addAll(frameList);
            for (Bitmap bitmap: frameList) {
                Log.d("GifMakerActivity: ", bitmap.toString());
                animation.addFrame(new BitmapDrawable(getResources(), bitmap), duration);
            }
            animation.setOneShot(false);
            mImageView.setImageDrawable(animation);
        }

        gifPath = getIntent().getStringExtra("GIFPATH");
        if (gifPath != null) {
            Log.d("GifMakerActivity: ", gifPath);
            GifDecoder gifDecoder = new GifDecoder();
            boolean isSucceeded = gifDecoder.load(gifPath);
            if (isSucceeded) {
                for (int i = 0; i < gifDecoder.frameNum(); ++i) {
                    Bitmap bitmap = gifDecoder.frame(i);
                    bitmaps.add(bitmap);
                    animation.addFrame(new BitmapDrawable(getResources(), bitmap), duration);
                }
            }
            animation.setOneShot(false);
            mImageView.setImageDrawable(animation);
        }

        onChange(mSeekbar.getProgress());

        mFramesAdapter = new FramesAdapter(bitmaps, this, GifMakerActivity.this);
        mRecyclerView.setAdapter(mFramesAdapter);

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayButton.setVisibility(View.INVISIBLE);
                play();
                mPauseButton.setVisibility(View.VISIBLE);
            }
        });

        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPauseButton.setVisibility(View.INVISIBLE);
                pause();
                mPlayButton.setVisibility(View.VISIBLE);
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int p;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                p = progress;
                onChange(p);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(GifMakerActivity.this, duration + "", Toast.LENGTH_SHORT).show();
                onChange(p);
            }
        });

        mAddImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                if (mPauseButton.getVisibility() == View.VISIBLE) {
                    pause();
                    mPauseButton.setVisibility(View.INVISIBLE);
                    mPlayButton.setVisibility(View.VISIBLE);
                }
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Collections.swap(bitmaps, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                mFramesAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                resetAnimationFrames(bitmaps);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }
        }).attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        switch (requestCode) {
            case 1:
                ClipData clipData = data.getClipData();
                Uri uri;
                Uri singleUri = data.getData();
                if (clipData != null) {
                    int size = clipData.getItemCount();
                    try {
                        for (int i = 0; i < size; i++) {
                            uri = clipData.getItemAt(i).getUri();
                            //Log.d("MainActivityClipdata: ", uri.toString());
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            bitmaps.add(bitmap);
                            animation.addFrame(new BitmapDrawable(getResources(), bitmap), duration);
                        }
                        mFramesAdapter.setBitmaps(bitmaps);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (singleUri != null) {
                    //Log.d("MainActivityData: ", uri1.toString());
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), singleUri);
                        bitmaps.add(bitmap);
                        animation.addFrame(new BitmapDrawable(getResources(), bitmap), duration);
                        mFramesAdapter.setBitmaps(bitmaps);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void onChange(int progress) {
        if (progress >= 91 && progress <= 100) {
            duration = 100;
            framespersec = 10;
        } else if (progress >= 81 && progress <= 90) {
            duration = 111;
            framespersec = 9;
        } else if (progress >= 71 && progress <= 80) {
            duration = 125;
            framespersec = 8;
        } else if (progress >= 61 && progress <= 70) {
            duration = 142;
            framespersec = 7;
        } else if (progress >= 51 && progress <= 60) {
            duration = 166;
            framespersec = 6;
        } else if (progress >= 41 && progress <= 50) {
            duration = 200;
            framespersec = 5;
        } else if (progress >= 31 && progress <= 40) {
            duration = 250;
            framespersec = 4;
        } else if (progress >= 21 && progress <= 30) {
            duration = 333;
            framespersec = 3;
        } else if (progress >= 11 && progress <= 20) {
            duration = 500;
            framespersec = 2;
        } else if (progress >= 1 && progress <= 10) {
            duration = 1000;
            framespersec = 1;
        }
        mNoOfFramesTextView.setText(framespersec + "");
        if (mPlayButton.getVisibility() == View.VISIBLE) {

        } else {
            animation.setDuration(duration);
        }
    }

    private MyAnimationDrawable sphereAnimation;
    private MyAnimationDrawable sphereResume;
    private Drawable currentFrame;
    private Drawable checkFrame;
    private int frameIndex;
    boolean looping;

    private void pause()
    {
        looping = false;
        sphereResume = new MyAnimationDrawable();
        animation.stop();
        currentFrame = animation.getCurrent();
        sphereAnimation = animation;

        frameLoop:
        for(int i = 0; i < sphereAnimation.getNumberOfFrames(); i++)
        {
            checkFrame = animation.getFrame(i);

            if(checkFrame == currentFrame)
            {
                frameIndex = i;
                for(int k = frameIndex; k < animation.getNumberOfFrames(); k++)
                {
                    Drawable frame = animation.getFrame(k);
                    sphereResume.addFrame(frame, duration);
                }
                for(int k = 0; k < frameIndex; k++)
                {
                    Drawable frame = animation.getFrame(k);
                    sphereResume.addFrame(frame, duration);
                }
                animation = sphereResume;
                mImageView.setImageDrawable(animation);
                mImageView.invalidate();
                break frameLoop;
            }
        }
    }

    private void play()
    {
        looping = false;
        onChange(mSeekbar.getProgress());
        animation.setOneShot(false);
        animation.start();
    }

    private void stop()
    {
        looping = false;
        animation.stop();
        animation = sphereAnimation;
        mImageView.setImageDrawable(animation);
    }

    private void loop()
    {
        looping = true;
        animation.setOneShot(false);
        animation.start();
    }

    ArrayList<Bitmap> frameList;

    private void extractFramesFromVideo(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        System.out.println("GifAcrtivity: " + getPath(uri) + ", " + uri.toString());
        try {
            retriever.setDataSource(getPath(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }

        frameList = new ArrayList<Bitmap>();
        //String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        MediaPlayer mp = MediaPlayer.create(this, uri);
        int duration = mp.getDuration();
        mp.release();
        //int duration_millisec =  duration; //duration in millisec
        /*int duration_second = duration / 1000;  //millisec to sec.
        int frames_per_second = 2;  //no. of frames want to retrieve per second
        int numberOfFrameCaptured = frames_per_second * duration_second;
        for (int i = 0; i < numberOfFrameCaptured; i++)
        {
            //setting time position at which you want to retrieve frames
            frameList.add(retriever.getFrameAtTime(5000 * i, MediaMetadataRetriever.OPTION_CLOSEST_SYNC));
        }*/

        for(int i = 1000000; i < duration * 1000; i += 1000000){
            frameList.add(retriever.getFrameAtTime(i, OPTION_CLOSEST_SYNC));
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    private void saveGif() {
        new AsyncTask<Void, Integer, Void>() {
            ProgressDialog progressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = new ProgressDialog(GifMakerActivity.this);
                progressDialog.setMax(100);
                progressDialog.setMessage("Please wait..");
                progressDialog.setCancelable(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                File filePath;
                if (TextUtils.isEmpty(gifPath)) {
                    int random = (int) (Math.random() * 9000) + 1000;
                    File gifMaker = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Gif Maker");
                    if (!gifMaker.mkdir()) {
                        Log.e("GifMakerActivity: ", "Directory doesn't exist");
                    }
                    filePath = new File(gifMaker, "GifMaker_" + random + ".gif");
                } else {
                    filePath = new File(gifPath);
                }

                try {
                    int size = bitmaps.size();
                    int w = bitmaps.get(0).getWidth();
                    int h = bitmaps.get(0).getHeight();
                    GifEncoder gifEncoder = new GifEncoder();
                    gifEncoder.init(w, h, filePath.getAbsolutePath(), GifEncoder.EncodingType.ENCODING_TYPE_FAST);
                    for (Bitmap bitmap : bitmaps) {
                        gifEncoder.encodeFrame(Bitmap.createScaledBitmap(bitmap, w, h, false), duration);
                        publishProgress(100/size);
                    }
                    gifEncoder.close();
                } catch (FileNotFoundException e) {}

                if (progressDialog.getProgress() <= progressDialog.getMax()) {
                    publishProgress(progressDialog.getMax() - progressDialog.getProgress());
                }

                /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
                AnimatedGifEncoder encoder = new AnimatedGifEncoder();
                encoder.start(bos);
                Log.d("duration: ", duration + "");
                encoder.setDelay(duration);
                encoder.setRepeat(0);

                for (Bitmap bitmap : bitmaps) {
                    encoder.addFrame(bitmap);
                }

                encoder.finish();

                FileOutputStream outputStream;
                try {
                    outputStream = new FileOutputStream(gifMaker.getAbsolutePath());
                    outputStream.write(bos.toByteArray());
                    outputStream.flush();
                    outputStream.close();
                    bos.flush();
                    bos.close();
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }*/

                /*AnimatedGifWriter writer = new AnimatedGifWriter(true);
                try {
                    OutputStream os = new FileOutputStream(gifMaker);
                    writer.prepareForWrite(os, -1, -1);
                    for (Bitmap bitmap : bitmaps) {
                        writer.writeFrame(os, bitmap);
                    }
                    writer.finishWrite(os);
                } catch (Exception e) {

                }*/

                MediaScannerConnection.scanFile(GifMakerActivity.this,
                        new String[] { filePath.getAbsolutePath() }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                //Log.i("ExternalStorage", "Scanned " + path + ":");
                                //Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                progressDialog.incrementProgressBy(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (progressDialog.getProgress() == progressDialog.getMax()) {
                    progressDialog.dismiss();
                }
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gifmaker_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveGif();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void resetAnimationFrames(ArrayList<Bitmap> newBitmaps) {
        animation = null;
        animation = new MyAnimationDrawable();
        bitmaps = newBitmaps;
        for (Bitmap bitmap: bitmaps) {
            animation.addFrame(new BitmapDrawable(getResources(), bitmap), duration);
        }
        animation.setOneShot(false);
        mImageView.setImageDrawable(animation);
        if (mPauseButton.getVisibility() == View.VISIBLE) {
            pause();
            mPauseButton.setVisibility(View.INVISIBLE);
            mPlayButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(GifMakerActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}
