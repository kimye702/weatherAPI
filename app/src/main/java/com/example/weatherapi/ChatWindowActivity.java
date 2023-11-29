package com.example.weatherapi;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapi.classInfo.ChatListInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class ChatWindowActivity extends AppCompatActivity {
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private EditText editText;
    private Button sendButton;
    private String userName;
    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private RecyclerView mRecyclerView;
    private ChatAdapter mRecyclerAdapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<ChatListInfo> chatItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatroom);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        userName = "user" + new Random().nextInt(10000);  // 랜덤한 유저 이름 설정 ex) user1234
        editText = (EditText) findViewById(R.id.edt_message);
        sendButton = (Button) findViewById(R.id.btn_submit);

        mRecyclerView=(RecyclerView) findViewById(R.id.recycler_messages);
        mRecyclerAdapter=new ChatAdapter();
        layoutManager=new LinearLayoutManager(ChatWindowActivity.this);

        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(layoutManager);

        chatItems=new ArrayList<>();

        mRecyclerAdapter.setFriendList(chatItems);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNow = System.currentTimeMillis();
                mDate = new Date(mNow);
                ChatListInfo chatData = new ChatListInfo(userName, editText.getText().toString(), mFormat.format(mDate));  // 유저 이름과 메세지로 chatData 만들기
                databaseReference.child("message").push().setValue(chatData);  // 기본 database 하위 message라는 child에 chatData를 list로 만들기
                editText.setText("");

                databaseReference.child("message").addChildEventListener(new ChildEventListener() {  // message는 child의 이벤트를 수신합니다.
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        ChatListInfo chatData = dataSnapshot.getValue(ChatListInfo.class);  // chatData를 가져오고
                        chatItems.add(chatData);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) { }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });

            }
        });
    }

}
