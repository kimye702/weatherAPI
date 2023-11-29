package com.example.weatherapi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.weatherapi.classInfo.UserInfo;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.net.URL;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
    private FirebaseFirestore firestore=FirebaseFirestore.getInstance();
    private EditText signup_email;
    private EditText signup_password;
    private EditText signup_name;
    private Button signup_button;
    private ImageView signup_profile;
    private Uri imageUri;
    private String profile;

    private static final int PICK_FROM_ALBUM = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        mAuth = FirebaseAuth.getInstance();

        signup_email=findViewById(R.id.signup_email);
        signup_password=findViewById(R.id.signup_password);
        signup_button=findViewById(R.id.signup_button);
        signup_profile=findViewById(R.id.signup_profile);
        signup_name=findViewById(R.id.signup_name);

        signup_profile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });

        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=signup_name.getText().toString();
                String email=signup_email.getText().toString();
                String password=signup_password.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
//                                    Toast.makeText(SignupActivity.this, "success.",
//                                            Toast.LENGTH_SHORT).show();
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(SignupActivity.this, "정보 등록 중!\n완료 후 로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String uid=user.getUid();
                                    UserInfo userInfo=new UserInfo();

                                    firebaseStorage.getReference().child("profileImage").child(uid).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            UploadTask uploadTask = firebaseStorage.getReference().child("profileImage").child(uid).putFile(imageUri);
                                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                @Override
                                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                    if (!task.isSuccessful()) {
                                                        throw task.getException();
                                                    }

                                                    // Continue with the task to get the download URL
                                                    return firebaseStorage.getReference().child("profileImage").child(uid).getDownloadUrl();
                                                }
                                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        profile=task.toString();
                                                        userInfo.setId(email);
                                                        userInfo.setName(name);
                                                        userInfo.setProfile(profile);
                                                        userInfo.setPassword(password);
                                                        userInfo.setUid(uid);

                                                        firestore.collection("user").document(uid).set(userInfo);

                                                        Intent it = new Intent(SignupActivity.this, SigninActivity.class);
                                                        startActivity(it);
                                                    } else {
                                                        // Handle failures
                                                        // ...
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } try {
                                    throw task.getException(); // 예외 던지기
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    String error = e.getMessage(); // 오류 메시지 가져오기
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PICK_FROM_ALBUM && resultCode==RESULT_OK){
            signup_profile.setImageURI(data.getData());
            imageUri=data.getData();
        }
    }
}