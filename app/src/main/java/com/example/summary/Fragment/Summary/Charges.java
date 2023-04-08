package com.example.summary.Fragment.Summary;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.summary.CRUD;
import com.example.summary.RecyclerViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class Charges implements RecyclerViewAdapter.OnItemClickListener {

    RecyclerView listOfCharges;
    Button newCharge;
    EditText textField;
    FirebaseUser user;
    private CRUD crud;
    private RecyclerViewAdapter adapter;
    private RecyclerViewAdapter.OnItemClickListener listener;
    ArrayList<Long> dbData;
    private Long sumOfCharges;

    public Charges(RecyclerView totalCharges, Button newCharge, EditText textField, FirebaseUser user, Context context, CRUD crud){
        // Single line inits
        this.user = user;
        this.newCharge = newCharge;
        this.textField = textField;
        this.crud = crud;
        listOfCharges = totalCharges;

        // Initialize listOfCharges and components;
        listOfCharges.setLayoutManager(new LinearLayoutManager(context));
        listOfCharges.setVisibility(View.GONE);
        listener = this;
        initAdapter();

        // Set button behavior
        this.newCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateChargesList(Long.parseLong(textField.getText().toString()));
                textField.setText("");
            }
        });
    }

    public Long SumOfCharges(){
        return sumOfCharges;
    }

    private void updateChargesList(Long newEntry) {
        Log.d("User id", user.getUid());
        crud.readDocument("users", user.getUid(), new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    sumOfCharges = Long.valueOf(0);
                    Map<String, Object> userDoc = task.getResult().getData();
                    ArrayList<Long> chargesList = (ArrayList<Long>) userDoc.get("charges");
                    chargesList.add(newEntry);
                    for (Long charge : chargesList) {
                        sumOfCharges += charge;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        userDoc.replace("charges", chargesList);
                    }
                    crud.updateDocument("users", user.getUid(), userDoc);
                    adapter.updateData(chargesList);
                    adapter.notifyItemInserted(chargesList.size()-1);
                }
                else {
                    Log.e("Error", "Query Failed", task.getException());
                }
            }
        });

    }

    private void initAdapter() {
        crud.readDocument("users", user.getUid(), new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    dbData = (ArrayList<Long>) task.getResult().getData().get("charges");
                    adapter = new RecyclerViewAdapter(dbData);
                    adapter.setListener(listener);
                    listOfCharges.setAdapter(adapter);
                    listOfCharges.setVisibility(View.VISIBLE);
                    Log.d("Adapter", "completed initializing adapter");
                }
                else{
                    Log.e("DatabaseQuery", "Query Failed.", task.getException());
                }
            }
        });
    }

    private void deleteListEntry(int index) {
        crud.readDocument("users", user.getUid(), new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    Map<String, Object> userDoc = task.getResult().getData();
                    ArrayList<Long> chargesList = (ArrayList<Long>) userDoc.get("charges");
                    chargesList.remove(index);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        userDoc.replace("charges", chargesList);
                    }
                    crud.updateDocument("users", user.getUid(), userDoc);
                    adapter.updateData(chargesList);
                    adapter.notifyItemRemoved(index);
                    Log.d("Remove Entry", "Removed Entry");
                }
                else {
                    Log.e("Error", "Query Failed", task.getException());
                }
            }
        });
    }


    @Override
    public void onItemClick(int pos) {
        deleteListEntry(pos);
    }
}
