package com.example.weatherapi;

import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapi.classInfo.ChatInfo;
import com.example.weatherapi.classInfo.ChatListInfo;
import com.example.weatherapi.classInfo.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

public class ChatWindowActivity extends AppCompatActivity {
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseFirestore firestore;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private EditText editText;
    private Button sendButton;
    private String userName;
    private TextView chatTitle;

    private String uid;
    private String friendUid;
    private String chatRoomUid;

    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private RecyclerView mRecyclerView;
    private ChatAdapter mRecyclerAdapter;
    private LinearLayoutManager layoutManager;
    private ArrayList<ChatListInfo> chatItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatroom);

        uid=mAuth.getCurrentUser().getUid();
        friendUid=getIntent().getStringExtra("friendUid");
        editText = (EditText) findViewById(R.id.edt_message);
        sendButton = (Button) findViewById(R.id.btn_submit);
        chatTitle=(TextView) findViewById(R.id.txt_Title);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        firestore = FirebaseFirestore.getInstance();

        firestore.collection("user").document(friendUid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        userName=task.getResult().get("name").toString();
                        chatTitle.setText(userName);
                    }
                });

        ChatInfo chatModel = new ChatInfo();
        chatModel.users.put(uid, true);
        chatModel.users.put(friendUid, true);

//        if(chatRoomUid==null){
//            sendButton.setEnabled(false);
//            firebaseDatabase.getReference().child("chatrooms").push().setValue(chatModel)
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void unused) {
//                            checkChatRoom();
//                        }
//                    });
//        }
//        else{
//            ChatInfo.Comment comment=new ChatInfo.Comment();
//            comment.uid=uid;
//            comment.message=editText.getText().toString();
//            comment.timestamp= ServerValue.TIMESTAMP;
//
//            firebaseDatabase.getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment)
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            editText.setText("");
//                        }
//                    });
//        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatInfo chatModel = new ChatInfo();
                chatModel.users.put(uid, true);
                chatModel.users.put(friendUid, true);

                if(chatRoomUid==null){
                    sendButton.setEnabled(false);
                    firebaseDatabase.getReference().child("chatrooms").push().setValue(chatModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    checkChatRoom();
                                }
                            });
                }
                else{
                    ChatInfo.Comment comment=new ChatInfo.Comment();
                    comment.uid=uid;
                    comment.message=editText.getText().toString();
                    comment.timestamp= ServerValue.TIMESTAMP;

                    firebaseDatabase.getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    editText.setText("");
                                }
                            });
                }
            }
        });

        checkChatRoom();

        mRecyclerView=(RecyclerView) findViewById(R.id.recycler_messages);
    }

    void checkChatRoom(){
        firebaseDatabase.getReference().child("chatrooms").orderByChild("users/"+uid).equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot item : snapshot.getChildren()){
                            ChatInfo chatModel=item.getValue(ChatInfo.class);
                            if(chatModel.users.containsKey(friendUid)){
                                chatRoomUid=item.getKey();
                                sendButton.setEnabled(true);
                                mRecyclerView.setLayoutManager(new LinearLayoutManager(ChatWindowActivity.this));
                                mRecyclerView.setAdapter(new RecyclerViewAdapter());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<ChatInfo.Comment> comments;
        UserInfo userInfo;
        public RecyclerViewAdapter(){
            comments=new ArrayList<>();
            firestore.collection("user").document(friendUid).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    userInfo=document.toObject(UserInfo.class);
                                    getMessageList();
                                }
                            } else {
                                // 문서 가져오기 실패 시 예외 처리
                            }
                        }
                    });
        }

        void getMessageList(){
            firebaseDatabase.getReference().child("chatrooms").child(chatRoomUid).child("comments")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            comments.clear();

                            for(DataSnapshot item: snapshot.getChildren()){
                                comments.add(item.getValue(ChatInfo.Comment.class));
                            }

                            notifyDataSetChanged();
                            mRecyclerView.scrollToPosition(comments.size()-1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_talk_item_mine, parent, false);

            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder messageViewHolder=((MessageViewHolder)holder);

            if(comments.get(position).uid.equals(uid)){
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.background_talk_friend);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
            }else{
                storageRef.child("profileImage/"+friendUid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(ChatWindowActivity.this).load(uri).circleCrop().into(messageViewHolder.imageView_profile);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        messageViewHolder.imageView_profile.setImageResource(R.drawable.friend);
                    }
                });
                messageViewHolder.textView_name.setText(userInfo.getName());
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);
            }

            long unixTime=(long)comments.get(position).timestamp;
            Date date = new Date(unixTime);
            mFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            mFormat.applyPattern("M월 d일 a h시 m분");
            String time = mFormat.format(date);

            if (comments.get(position).timestamp != null) {
                messageViewHolder.txt_date.setText(time);
            } else {
                // timestamp가 null일 때 처리 로직, 예를 들어 현재 시간 표시
                String currentTime = mFormat.format(new Date());
                messageViewHolder.txt_date.setText(currentTime);
            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder{
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public TextView txt_date;

            public MessageViewHolder(View view){
                super(view);
                textView_message=(TextView) view.findViewById(R.id.txt_message);
                textView_name=(TextView) view.findViewById(R.id.messageItem_textview_name);
                imageView_profile=(ImageView) view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_destination=(LinearLayout) view.findViewById(R.id.messageItem_linearlayout_destination);
                linearLayout_main=(LinearLayout) view.findViewById(R.id.background);
                txt_date=(TextView) view.findViewById(R.id.txt_date);
            }
        }
    }
}
