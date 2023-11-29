package com.example.weatherapi;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.zip.Inflater;

public class ProfileFragment extends Fragment {
    private Button logoutButton;
    private FirebaseAuth mFirebaseAuth; //파이어베이스 인증
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile, container, false);

        logoutButton=view.findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getContext().getSharedPreferences("pref", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit(); //Editor라는 Inner class가 정의되어 있음
                editor.putString("check", "false");
                editor.commit();//이 때 이제 저장이 되는 거임
                mFirebaseAuth.signOut();
                getActivity().finish();
            }
        });

        return view;
    }
}
