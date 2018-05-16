package com.millionair.omkar.gifmaker.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.millionair.omkar.gifmaker.GifMakerActivity;
import com.millionair.omkar.gifmaker.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC;
import static wseemann.media.FFmpegMediaMetadataRetriever.OPTION_CLOSEST;

public class MyGifsAdapter extends RecyclerView.Adapter<MyGifsAdapter.MyGifsViewHolder> {

    private Context mContext;
    private List<File> files;
    private OnItemClickedListener mOnItemClickedListener;

    public MyGifsAdapter(File[] files, Context context, OnItemClickedListener onItemClickedListener) {
        this.files = Arrays.asList(files);
        mContext = context;
        mOnItemClickedListener = onItemClickedListener;
    }

    @NonNull
    @Override
    public MyGifsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new MyGifsViewHolder(view);
    }

    private List<ImageView> imageViewList = new ArrayList<>();

    @Override
    public void onBindViewHolder(@NonNull final MyGifsViewHolder holder, final int position) {
        Glide.with(mContext).asBitmap().load(files.get(position)).into(holder.mImageView);
        imageViewList.add(holder.mImageView);

        holder.mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((AppCompatActivity) mContext).startSupportActionMode(new ActionBarCallBack());
                selectedItem(holder.getAdapterPosition(), holder.mImageView);
                return true;
            }
        });

        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionModeOn) {
                    selectedItem(holder.getAdapterPosition(), holder.mImageView);
                } else {
                    mOnItemClickedListener.onItemClicked(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class MyGifsViewHolder extends RecyclerView.ViewHolder {

        ImageView mImageView;

        public MyGifsViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.imageview);
        }
    }

    public interface OnItemClickedListener {
        void onItemClicked(int position);
    }

    private boolean actionModeOn = false;
    private List<ImageView> imageViews = new ArrayList<>();
    private List<File> filesToBeRemoved = new ArrayList<>();
    private Menu actionBarMenu;
    private ActionMode actionMode;

    private void selectedItem(int position, ImageView imageView) {
        File file = files.get(position);
        if (filesToBeRemoved.contains(file)) {
            filesToBeRemoved.remove(file);
            imageViews.remove(imageView);
            imageView.setColorFilter(Color.argb(0, 0, 0, 0));
            if (!filesToBeRemoved.containsAll(files)) {
                actionBarMenu.getItem(1).setVisible(true);
                actionBarMenu.getItem(2).setVisible(false);
            }
            if (filesToBeRemoved.size() == 0) {
                actionMode.finish();
            }
        } else {
            filesToBeRemoved.add(file);
            imageViews.add(imageView);
            imageView.setColorFilter(Color.argb(200, 0, 0, 0));
            if (filesToBeRemoved.containsAll(files)) {
                actionBarMenu.getItem(1).setVisible(false);
                actionBarMenu.getItem(2).setVisible(true);
            }
        }
    }

    public class ActionBarCallBack implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionModeOn = true;
            actionMode = mode;
            actionBarMenu = menu;
            ((AppCompatActivity) mContext).getMenuInflater().inflate(R.menu.contextual_actionbar, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_select_all:
                    filesToBeRemoved.addAll(files);
                    imageViews.addAll(imageViewList);
                    Log.d("Adapter: ", filesToBeRemoved.size() + "");
                    Log.d("Adapter: ", imageViews.size() + "");
                    for (ImageView imageView: imageViews) {
                        imageView.setColorFilter(Color.argb(200, 0, 0, 0));
                    }
                    actionBarMenu.getItem(1).setVisible(false);
                    actionBarMenu.getItem(2).setVisible(true);
                    break;
                case R.id.menu_unselect_all:
                    filesToBeRemoved.clear();
                    imageViews.clear();
                    Log.d("Adapter: ", filesToBeRemoved.size() + "");
                    Log.d("Adapter: ", imageViews.size() + "");
                    for (ImageView imageView: imageViews) {
                        imageView.setColorFilter(Color.argb(0, 0, 0, 0));
                    }
                    actionBarMenu.getItem(1).setVisible(true);
                    actionBarMenu.getItem(2).setVisible(false);
                    break;
                case R.id.menu_delete:
                    new AlertDialog.Builder(mContext)
                            .setMessage("Do you really want to delete?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int size = filesToBeRemoved.size();
                                    for (int i = 0; i < size; i++) {
                                        File file = filesToBeRemoved.get(i);
                                        if (files.contains(file)) {
                                            files.remove(file);
                                            MediaScannerConnection.scanFile(mContext,
                                                    new String[] { file.getAbsolutePath() }, null,
                                                    new MediaScannerConnection.OnScanCompletedListener() {
                                                        public void onScanCompleted(String path, Uri uri) {
                                                            //Log.i("ExternalStorage", "Scanned " + path + ":");
                                                            //Log.i("ExternalStorage", "-> uri=" + uri);
                                                        }
                                                    });
                                        }
                                        filesToBeRemoved.remove(i);
                                        imageViews.remove(i);
                                        notifyItemRemoved(i);
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    mode.finish();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionModeOn = false;
            filesToBeRemoved.clear();
            imageViews.clear();
            Log.d("Adapter: ", filesToBeRemoved.size() + "");
            Log.d("Adapter: ", imageViews.size() + "");
            for (ImageView imageView: imageViews) {
                imageView.setColorFilter(Color.argb(0, 0, 0, 0));
            }
            notifyDataSetChanged();
        }
    }

}
