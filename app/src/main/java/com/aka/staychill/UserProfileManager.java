package com.aka.staychill;

import android.net.Uri;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class UserProfileManager {
    private final FirebaseFirestore db;
    private final StorageReference storageRef;

    public UserProfileManager() {
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public void saveUserProfile(String userId, Map<String, Object> profileData) {
        db.collection("users").document(userId).set(profileData)
                .addOnSuccessListener(aVoid -> {
                    // Éxito
                })
                .addOnFailureListener(e -> {
                    // Error
                });
    }

    public void getUserProfile(String userId, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(onCompleteListener);
    }

    public void uploadProfileImage(String userId, Uri imageUri, OnSuccessListener<Uri> onSuccessListener, OnFailureListener onFailureListener) {
        StorageReference profileImageRef = storageRef.child("profile_images/" + userId + ".jpg");
        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl().addOnSuccessListener(onSuccessListener))
                .addOnFailureListener(onFailureListener);
    }
}
