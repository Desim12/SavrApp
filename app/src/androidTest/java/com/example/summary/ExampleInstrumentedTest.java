package com.example.summary;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.example.summary.CRUD;
import com.example.summary.Fragment.Summary.Charges;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.summary", appContext.getPackageName());
    }

    @Test
    public void testDatabaseAccess() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        assertNotNull(db);
    }

    @Test
    public void testCRUDAccess() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CRUD crud = new CRUD(db);
        assertNotNull(crud);
    }

    @Test
    public void testCanRead() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CRUD crud = new CRUD(db);
        final ArrayList<Object[]>[] data = new ArrayList[]{new ArrayList<>()};
        crud.readDocument("users", "TD5dWK7CcwOYRuYHrDoUUI9h33t1", new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    data[0] = convertToList((Map<String, Map<String, Long>>) task.getResult().getData().get("Charges"));
                }
            }
        });
        assertNotNull(data[0]);
    }

    public ArrayList<Object[]> convertToList(Map<String, Map<String, Long>> data){
        ArrayList<Object[]> finalList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Long>> category: data.entrySet()){
            for (Map.Entry<String, Long> nameAmount: category.getValue().entrySet()){
                finalList.add(new Object[]{category.getKey(), nameAmount.getKey(), nameAmount.getValue()});
            }
        }
        return finalList;
    }
}