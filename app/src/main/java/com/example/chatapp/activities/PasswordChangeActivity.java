package com.example.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.chatapp.databinding.ActivityPasswordChangeBinding;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class PasswordChangeActivity extends BaseActivity {

    private ActivityPasswordChangeBinding binding;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordChangeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());

        setListeners();
    }

    private void setListeners() {
        binding.buttonSave.setOnClickListener(v -> {
            if (isValidSignUpDetails()) {
                updatePasswords();
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void updatePasswords() {
        loading(true);
        DocumentReference ref = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));


        ref
                .get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot documentSnapshot = task.getResult();
                String oldPassword = binding.inputOldPassword.getText().toString();

                if (Objects.equals(documentSnapshot.get(Constants.KEY_PASSWORD), oldPassword)) {
                    String newPassword = binding.inputPassword.getText().toString();

                    HashMap<String, Object> user = new HashMap<>();
                    user.put(Constants.KEY_PASSWORD, newPassword);

                    ref.update(user)
                            .addOnFailureListener(exception -> {
                                showToast(exception.getMessage());
                            })
                            .addOnCompleteListener(task2 -> {
                                showToast("Password success changed");
                                setResult(RESULT_OK);
                                finish();
                            });
                } else {
                    showToast("Old password is wrong!");
                }
            } else {
                showToast("Unable to load data");
            }
        }).addOnCompleteListener(task -> loading(false));
    }

    private Boolean isValidSignUpDetails() {
        if (binding.inputOldPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter old password");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm your password");
            return false;
        } else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password & confirm password must be same");
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSave.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSave.setVisibility(View.VISIBLE);
        }
    }
}
