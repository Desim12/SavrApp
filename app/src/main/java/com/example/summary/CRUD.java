package com.example.summary;

import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CRUD {
    private FirebaseFirestore db;

    public CRUD(FirebaseFirestore db) {
        // Initialize Firestore client
        this.db = db;
    }


    // Create a new document with a generated ID
    public Task<String> createDocument(String collectionName, Map<String, Object> data) {
        CollectionReference collectionRef = db.collection(collectionName);
        DocumentReference docRef = collectionRef.document();
        return docRef.set(data).continueWith(task -> {
            if (task.isSuccessful()) {
                return docRef.getId();
            } else {
                throw task.getException();
            }
        });
    }

    // Read a single document
    public void readDocument(String collectionName, String documentID, OnCompleteListener<DocumentSnapshot> listener) {
        DocumentReference docRef = db.collection(collectionName).document(documentID);
        Task<DocumentSnapshot> task = docRef.get();
        task.addOnCompleteListener(listener);
    }

    // Update a document
    public Task<Void> updateDocument(String collectionName, String documentID, Map<String, Object> data) {
        DocumentReference docRef = db.collection(collectionName).document(documentID);
        return docRef.update(data);
    }

    // Delete a document
    public Task<Void> deleteDocument(String collectionName, String documentID) {
        DocumentReference docRef = db.collection(collectionName).document(documentID);
        return docRef.delete();
    }
}