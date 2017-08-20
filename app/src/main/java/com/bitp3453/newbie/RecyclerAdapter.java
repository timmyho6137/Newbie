package com.bitp3453.newbie;

/**
 * Created by Timmy Ho on 5/23/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private String[] ids, titles, details, dates, starts, ends, locations, categories, matrices;
    Context context;

    public RecyclerAdapter(String[] ids, String[] titles, String[] details, String[] dates, String[] starts, String[] ends, String[] locations, String[] categories, String[] matrices, Context context) {
        this.ids = ids;
        this.titles = titles;
        this.details = details;
        this.context = context;
    }

    private int[] images = { R.drawable.android_image_1,
            R.drawable.android_image_1,
            R.drawable.android_image_1,
            R.drawable.android_image_1,
            R.drawable.android_image_1,
            R.drawable.android_image_1,
            R.drawable.android_image_1,
            R.drawable.android_image_1 };

    class ViewHolder extends RecyclerView.ViewHolder{

        public int currentItem;
        public String itemId;
        public ImageView itemImage;
        public TextView itemTitle;
        public TextView itemDetail;
        public Button btnJoin;

        public ViewHolder(View itemView) {
            super(itemView);
            itemImage = (ImageView)itemView.findViewById(R.id.item_image);
            itemTitle = (TextView)itemView.findViewById(R.id.item_title);
            itemDetail = (TextView)itemView.findViewById(R.id.item_detail);
            btnJoin = (Button) itemView.findViewById(R.id.btnJoin);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if(!itemTitle.getText().toString().trim().equals("Empty")) {
                        int position = getAdapterPosition();
                        Intent intent = new Intent("custom-message");
                        intent.putExtra("position", position);
                        intent.putExtra("join", false);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                }
            });

            btnJoin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!itemTitle.getText().toString().trim().equals("Empty")){
                        int position = getAdapterPosition();
                        Intent intent = new Intent("custom-message");
                        intent.putExtra("position", position);
                        intent.putExtra("join", true);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.itemTitle.setText(titles[i]);
        viewHolder.itemDetail.setText(details[i]);
//        viewHolder.itemId = ids[i];
//        viewHolder.itemImage.setImageResource(R.drawable.android_image_1);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}