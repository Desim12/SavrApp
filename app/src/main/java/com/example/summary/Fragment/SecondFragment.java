package com.example.summary.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.summary.Activities.MainActivity;
import com.example.summary.R;
import com.example.summary.databinding.FragmentSecondBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private FirebaseUser user;
    private String[] charges;
    private ArrayAdapter<String> adapter;
    private Context context;
    FirebaseFirestore db;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        context = getContext();
        db = FirebaseFirestore.getInstance();
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        MainActivity activity = (MainActivity) getActivity();
        this.user = activity.getUser();
        binding.helloText.setText("Hello, "+ user.getDisplayName() + "!");
        updateChargesList();
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(binding.newNameField.getText().toString()).build())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("Update Profile", "User profile updated.");
                                }
                            }
                        });
                binding.newNameField.setText("");
                binding.helloText.setText("Hello, " + user.getDisplayName() + "!");
            }
        });

        binding.newCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateChargesList(Long.parseLong(binding.newChargeField.getText().toString()));
            }
        });

        binding.backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateChargesList() {
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot > task) {
                if (task.isSuccessful()){
                    Log.d("DatabaseQuery", task.getResult().get("Charges").toString());
                    ArrayList<Long> result = (ArrayList<Long>) task.getResult().get("Charges");
                    Log.d("DatabaseQuery", "Successful Query");
                    charges = new String[result.size()];
                    for (int i = 0; i < result.size(); i++ ){
                        charges[i] = result.get(i).toString();
                    }
                    adapter = new ArrayAdapter<>(context, android.R.layout.simple_selectable_list_item, charges);
                    binding.totalCharges.setAdapter(adapter);
                }
                else{
                    Log.e("DatabaseQuery", "Query Failed.", task.getException());
                }
            }
        });
    }

    private void updateChargesList(Long newEntry) {
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot > task) {
                if (task.isSuccessful()){

                    ArrayList<Long> result = (ArrayList<Long>) task.getResult().get("Charges");
                    Log.d("Debug", result.toString());
                    db.collection("users").document(user.getUid()).update("Charges", result.add(newEntry));
                    charges = new String[result.size()];
                    for (int i = 0; i < result.size(); i++ ){
                        charges[i] = result.get(i).toString();
                    }
                    adapter = new ArrayAdapter<>(context, android.R.layout.simple_selectable_list_item, charges);
                    binding.totalCharges.setAdapter(adapter);
                }
                else{
                    Log.e("DatabaseQuery", "Query Failed.", task.getException());
                }
            }
        });
    }

}