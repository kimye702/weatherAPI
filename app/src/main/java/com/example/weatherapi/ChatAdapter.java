package com.example.weatherapi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapi.classInfo.ChatListInfo;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private ArrayList<ChatListInfo> mChatList;

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_talk_item_mine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        holder.onBind(mChatList.get(position));
    }

    public void setFriendList(ArrayList<ChatListInfo> list){
        this.mChatList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        TextView name;
        TextView message;
        TextView date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //profile = (ImageView) itemView.findViewById(R.id.profile);
            name = (TextView) itemView.findViewById(R.id.txt_Title);
            message = (TextView) itemView.findViewById(R.id.txt_message);
            //date=(TextView) itemView.findViewById(R.id.txt_date);
        }

        void onBind(ChatListInfo item){
            //profile.setImageResource(item.getResourceId());
            if (name != null) {
                name.setText(item.getName());
            }
            if (message != null) {
                message.setText(item.getMessage());
            }
            if (date != null) {
                date.setText(item.getDate());
            }

        }
    }
}
