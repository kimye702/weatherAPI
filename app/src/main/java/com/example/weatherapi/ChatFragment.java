package com.example.weatherapi;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapi.classInfo.ChatListInfo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ChatListAdapter mRecyclerAdapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<ChatListInfo> chatItems;
    private FloatingActionButton floatingActionButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.chatlist, container, false);

        mRecyclerView=(RecyclerView) view.findViewById(R.id.recycler_chatlist);
        mRecyclerAdapter=new ChatListAdapter();
        layoutManager=new LinearLayoutManager(getActivity());
        floatingActionButton=(FloatingActionButton) view.findViewById(R.id.floating);

        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(layoutManager);

        chatItems=new ArrayList<>();
        for(int i=0;i<=30;i++){
            chatItems.add(new ChatListInfo("이름"+i, "메시지"+i, "3시"+i));
        }

        mRecyclerAdapter.setFriendList(chatItems);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getContext(), ChatWindowActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
