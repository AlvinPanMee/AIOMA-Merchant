package com.bignerdranch.android.aiomamerchant;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RewardAdapterClass extends RecyclerView.Adapter<RewardAdapterClass.RewardListViewHolder>{

    ArrayList<CreatedRewards> list;

    public RewardAdapterClass(ArrayList<CreatedRewards> list){
        this.list = list;
    }

    @NonNull
    @Override
    public RewardAdapterClass.RewardListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reward_list_layout,parent,false);

        return new RewardListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RewardListViewHolder holder, int position) {
        holder.rewardTitle.setText(list.get(position).getRewardTitle());
        Glide.with(holder.rewardPic.getContext()).load(list.get(position).rewardIconUrl)
                .into(holder.rewardPic);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent singleRewardIntent = new Intent(v.getContext(), EditRewardActivity.class);
                singleRewardIntent.putExtra("rewardID", list.get(position).rewardID);

                v.getContext().startActivity(singleRewardIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class RewardListViewHolder extends RecyclerView.ViewHolder {
        ImageView rewardPic;
        TextView rewardTitle;

        public RewardListViewHolder (View itemView){
            super(itemView);
            rewardPic = itemView.findViewById(R.id.reward_pic);
            rewardTitle = itemView.findViewById(R.id.reward_title);
        }
    }


}


