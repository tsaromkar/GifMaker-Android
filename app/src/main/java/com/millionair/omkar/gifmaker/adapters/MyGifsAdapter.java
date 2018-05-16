package com.millionair.omkar.gifmaker.adapters;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.millionair.omkar.gifmaker.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC;
import static wseemann.media.FFmpegMediaMetadataRetriever.OPTION_CLOSEST;

public class MyGifsAdapter extends RecyclerView.Adapter<MyGifsAdapter.MyGifsViewHolder> {

    private Context mContext;
    private File[] files;
    private OnItemClickedListener mOnItemClickedListener;

    public MyGifsAdapter(File[] files, Context context, OnItemClickedListener onItemClickedListener) {
        this.files = files;
        mContext = context;
        mOnItemClickedListener = onItemClickedListener;
    }

    @NonNull
    @Override
    public MyGifsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new MyGifsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyGifsViewHolder holder, int position) {
        Glide.with(mContext).asBitmap().load(files[position]).into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    public class MyGifsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mImageView;

        public MyGifsViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.imageview);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnItemClickedListener.onItemClicked(getAdapterPosition());
        }
    }

    public interface OnItemClickedListener {
        void onItemClicked(int position);
    }

}
