package com.millionair.omkar.gifmaker.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.millionair.omkar.gifmaker.R;

import java.util.ArrayList;

public class FramesAdapter extends RecyclerView.Adapter<FramesAdapter.FramesViewHolder> {

    private ArrayList<Bitmap> bitmaps;
    private ResetFrames mResetFrames;
    private Context mContext;

    public FramesAdapter(ArrayList<Bitmap> bitmaps, ResetFrames resetFrames, Context context) {
        this.bitmaps = bitmaps;
        mResetFrames = resetFrames;
        mContext = context;
    }

    @NonNull
    @Override
    public FramesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new FramesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FramesViewHolder holder, final int position) {
        holder.mImageView.setImageBitmap(bitmaps.get(position));
        holder.mRemoveFrameImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmaps.size() > 2) {
                    bitmaps.remove(position);
                    notifyItemRemoved(position);
                    setBitmaps(bitmaps);
                    mResetFrames.resetAnimationFrames(bitmaps);
                } else {
                    Toast.makeText(mContext, "Need atleast 2 frames", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return bitmaps.size();
    }

    public class FramesViewHolder extends RecyclerView.ViewHolder {

        ImageView mImageView;
        ImageButton mRemoveFrameImageButton;

        public FramesViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.imageview);
            mRemoveFrameImageButton = (ImageButton) itemView.findViewById(R.id.remove_frame_imagebuttton);
        }
    }

    public void setBitmaps(ArrayList<Bitmap> bitmaps) {
        this.bitmaps = bitmaps;
        notifyDataSetChanged();
    }

    public interface ResetFrames {
        void resetAnimationFrames(ArrayList<Bitmap> bitmaps);
    }

}
