package com.millionair.omkar.gifmaker.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.millionair.omkar.gifmaker.R;

public class MyGifsAdapter extends RecyclerView.Adapter<MyGifsAdapter.MyGifsViewHolder> {

    @NonNull
    @Override
    public MyGifsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new MyGifsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyGifsViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyGifsViewHolder extends RecyclerView.ViewHolder {

        ImageView mImageView;

        public MyGifsViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.imageview);
        }
    }

}
