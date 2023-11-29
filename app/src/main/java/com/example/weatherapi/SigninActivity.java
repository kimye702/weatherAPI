package com.example.weatherapi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapi.classInfo.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class SigninActivity extends AppCompatActivity {

    private FirebaseAuth mAuth=FirebaseAuth.getInstance();
    private FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
    private FirebaseFirestore firestore=FirebaseFirestore.getInstance();
    private FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    private EditText signin_email;
    private EditText signin_password;
    private Button signin_button;
    private Button signin_signupbutton;
    private CheckBox login_checkbox;

    String auto_check;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);

        signin_email=findViewById(R.id.signin_email);
        signin_password=findViewById(R.id.signin_password);
        signin_button=findViewById(R.id.signin_button);
        signin_signupbutton=findViewById(R.id.signin_signupbutton);
        login_checkbox=findViewById(R.id.login_checkbox);

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);

        if (pref !=null){
            auto_check = pref.getString("check",""); //name이라는 키 값으로 받는 것.
            if (mAuth.getCurrentUser() != null && auto_check.equals("true")) {
                // User is signed in (getCurrentUser() will be null if not signed in)
                Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                // or do some other stuff that you want to do
            }
        }

        signin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=signin_email.getText().toString();
                String password=signin_password.getText().toString();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    DatabaseReference friendLocationRef = firebaseDatabase.getReference("user").child(user.getUid());
                                    friendLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.exists()) {
                                                Intent intent = new Intent(SigninActivity.this, SetLocationActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.w("DBError", "Failed to read value.", databaseError.toException());
                                        }
                                    });



                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(SigninActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        signin_signupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SigninActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void onPause() {
        super.onPause();
        if(login_checkbox.isChecked()==true){
            SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit(); //Editor라는 Inner class가 정의되어 있음
            editor.putString("check","true");
            editor.commit();//이 때 이제 저장이 되는 거임

        }

        else{
            SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit(); //Editor라는 Inner class가 정의되어 있음
            editor.putString("check", "false");
            editor.commit();//이 때 이제 저장이 되는 거임
        }

    }
}
