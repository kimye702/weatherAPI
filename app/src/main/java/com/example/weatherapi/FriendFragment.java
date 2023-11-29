package com.example.weatherapi;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapi.classInfo.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.common.util.KakaoCustomTabsClient;
import com.kakao.sdk.link.LinkClient;
import com.kakao.sdk.link.WebSharerClient;
import com.kakao.sdk.link.model.LinkResult;
import com.kakao.sdk.template.model.Link;
import com.kakao.sdk.template.model.TextTemplate;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class FriendFragment extends Fragment {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    EditText editText;
    Activity activity;
    UserInfo userInfo = new UserInfo();
    ImageView addButton;
    ImageView plusFriend;
    ConstraintLayout constraintCode;
    ConstraintLayout constraintKakao;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friendlist, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_friendlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        FriendAdapter friendAdapter = new FriendAdapter();
        recyclerView.setAdapter(friendAdapter);

        plusFriend=view.findViewById(R.id.plus_friend);

        View bottomview = inflater.inflate(R.layout.add_bottom_sheet, null, false);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(bottomview);

        constraintCode=bottomview.findViewById(R.id.constraint_code);
        constraintKakao=bottomview.findViewById(R.id.constraint_kakao);
        editText = bottomview.findViewById(R.id.edit_code);
        addButton=bottomview.findViewById(R.id.send_code);

        plusFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.show();
            }
        });

        String uid = firebaseAuth.getCurrentUser().getUid();

        DocumentReference docRef = firestore.collection("user").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userInfo.setName(document.getData().get("name").toString());
                        userInfo.setUid(document.getData().get("uid").toString());
                        //Toast.makeText(getActivity(),userInfo.getName(), Toast.LENGTH_LONG).show();
                        Log.d(TAG, "userName: "+userInfo.getName());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        KakaoSdk.init(getActivity(), getString(R.string.kakao_app_key));

        constraintKakao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KakaoLink(getContext());
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String friendUid = editText.getText().toString();
                firestore.collection("user").document(userInfo.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        List<String> friendList = (List<String>) document.get("friendList");
                                        if (friendList == null) {
                                            friendList = new ArrayList<>();
                                            friendList.add(friendUid);
                                            document.getReference().update("friendList", friendList)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            friendAdapter.loadUserModels(); // 업데이트 호출
                                                            Toast.makeText(getActivity(), "친구 등록에 성공했어요!", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }
                                        else if (!friendList.contains(friendUid)) {
                                            friendList.add(friendUid);
                                            document.getReference().update("friendList", friendList)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            friendAdapter.loadUserModels(); // 업데이트 호출
                                                            Toast.makeText(getActivity(), "친구 등록에 성공했어요!", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(getActivity(), "이미 추가된 친구입니다.", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getActivity(), "존재하지 않는 친구코드에요!", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "오류 발생: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                firestore.collection("user").document(friendUid).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        List<String> friendList = (List<String>) document.get("friendList");
                                        if (friendList == null) {
                                            friendList = new ArrayList<>();
                                            friendList.add(userInfo.getUid());
                                            document.getReference().update("friendList", friendList)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                        }
                                                    });
                                        }
                                        else {
                                            friendList.add(userInfo.getUid());
                                            document.getReference().update("friendList", friendList)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                        }
                                                    });
                                        }
                                    } else {
                                    }
                                } else {
                                }
                            }
                        });
            }
        });


        return view;
    }

    public void KakaoLink(Context context) {
        // 원하는 메세지 형태의 template 생성
        TextTemplate textTemplate = new TextTemplate(userInfo.getName()+"님이 친구 신청을 보내셨어요!\n" +
                userInfo.getName()+"님의 친구 코드 : "+ userInfo.getUid(),new Link("https://www.daum.net","https://m.daum.net/"));
        // 코드 시작
        // 카카오톡 설치여부 확인
        if(LinkClient.getInstance().isKakaoLinkAvailable(context)){
            LinkClient.getInstance().defaultTemplate(context, textTemplate,null,new Function2<LinkResult, Throwable, Unit>() {
                @Override
                public Unit invoke(LinkResult linkResult, Throwable throwable) {
                    if (throwable != null) {
                        Log.e("TAG", "카카오링크 보내기 실패", throwable);
                    }
                    else if (linkResult != null) {
                        context.startActivity(linkResult.getIntent());
                        // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                        Log.w("TAG", "Warning Msg: "+ linkResult.getWarningMsg());
                        Log.w("TAG", "Argument Msg: "+ linkResult.getArgumentMsg());
                    }
                    return null;
                }
            });
        }else{
            //카카오톡 미설치 시 웹으로 실행
            Uri sharerUrl = WebSharerClient.getInstance().defaultTemplateUri(textTemplate);
            try {
                KakaoCustomTabsClient.INSTANCE.openWithDefault(context,sharerUrl);
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            }
        }
    }

    class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
        List<UserInfo> userModels;

        public FriendAdapter() {
            userModels = new ArrayList<>();
            loadUserModels();
            notifyDataSetChanged();
        }

        @Override
        public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_friend_item, parent, false);
            return new FriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
            ConstraintLayout view = holder.itemView.findViewById(R.id.cardView);

            UserInfo userInfo = userModels.get(position);

            CircleImageView imageView;
            TextView textView;
            ConstraintLayout friendWeather;
            ConstraintLayout friendChat;

            imageView = view.findViewById(R.id.friend_profile);
            textView = view.findViewById(R.id.friendname);

            LayoutInflater layoutInflater = LayoutInflater.from(holder.itemView.getContext());
            View friendSheetView = layoutInflater.inflate(R.layout.friend_bottom_sheet, null, false);
            BottomSheetDialog friendSheetDialog = new BottomSheetDialog(holder.itemView.getContext());
            friendSheetDialog.setContentView(friendSheetView);

            friendWeather = friendSheetView.findViewById(R.id.weather_friend);
            friendChat = friendSheetView.findViewById(R.id.chat_friend);


            storageRef.child("profileImage/"+userInfo.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getContext()).load(uri).circleCrop().into(imageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    imageView.setImageResource(R.drawable.friend);
                }
            });

            if(textView!=null){
                textView.setText(userInfo.getName());
            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View friendSheetView = LayoutInflater.from(getContext()).inflate(R.layout.friend_bottom_sheet, null, false);
                    BottomSheetDialog friendSheetDialog = new BottomSheetDialog(getContext());
                    friendSheetDialog.setContentView(friendSheetView);

                    ConstraintLayout friendWeather = friendSheetView.findViewById(R.id.weather_friend);
                    ConstraintLayout friendChat = friendSheetView.findViewById(R.id.chat_friend);

                    friendWeather.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "Weather clicked for position: " + position);
                            Intent intent = new Intent(getContext(), getLocationActivity.class);
                            intent.putExtra("friendUid", userModels.get(holder.getAdapterPosition()).getUid());
                            startActivity(intent);
                        }
                    });

                    friendChat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "Chat clicked for position: " + position);
                            Intent intent = new Intent(getContext(), ChatWindowActivity.class);
                            intent.putExtra("friendUid", userModels.get(holder.getAdapterPosition()).getUid());
                            startActivity(intent);
                        }
                    });

                    friendSheetDialog.show();
                }
            });

            friendWeather.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), getLocationActivity.class);
                    intent.putExtra("friendUid", userModels.get(position).getUid());
                    startActivity(intent);
                }
            });

            friendChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), ChatWindowActivity.class);
                    intent.putExtra("friendUid", userModels.get(position).getUid());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }

        public class FriendViewHolder extends RecyclerView.ViewHolder {

            public FriendViewHolder(View view) {
                super(view);
            }
        }

        public void loadUserModels() {
            String uid = firebaseAuth.getCurrentUser().getUid();

            firestore.collection("user").document(uid).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    List<String> friendList = (List<String>) document.get("friendList");
                                    if (friendList != null) {
                                        userModels.clear(); // 기존 목록 초기화
                                        for (String friendUid : friendList) {
                                            firestore.collection("user").document(friendUid).get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentSnapshot userDocument = task.getResult();
                                                                if (userDocument.exists()) {
                                                                    UserInfo userInfo = userDocument.toObject(UserInfo.class);
                                                                    userModels.add(userInfo);
                                                                    notifyDataSetChanged(); // 새로운 데이터로 목록 업데이트
                                                                }
                                                            } else {
                                                                // 예외 처리
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                }
                            } else {
                                // 문서 가져오기 실패 시 예외 처리
                            }
                        }
                    });
        }

    }

}