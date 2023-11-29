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

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder>{

    private ArrayList<ChatListInfo> mChatList;

    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_chat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.ViewHolder holder, int position) {
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
            name = (TextView) itemView.findViewById(R.id.chatname);
            message = (TextView) itemView.findViewById(R.id.chatrecent);
            date=(TextView) itemView.findViewById(R.id.chatrecentdate);
        }

        void onBind(ChatListInfo item){
            //profile.setImageResource(item.getResourceId());
            name.setText(item.getName());
            message.setText(item.getMessage());
            date.setText(item.getDate());
        }
    }
}
