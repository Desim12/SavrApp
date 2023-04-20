package com.example.summary.Fragment.Summary;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.summary.CRUD;
import com.example.summary.RecyclerViewAdapter;
import com.example.summary.RecyclerViewAdapterBudget;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Charges implements RecyclerViewAdapter.OnItemClickListener, RecyclerViewAdapterBudget.OnItemClickListener {

    RecyclerView listOfCharges, listOfBudgets;
    FirebaseUser user;
    private CRUD crud;
    private RecyclerViewAdapter adapter;
    private RecyclerViewAdapterBudget budgetAdapter;
    private RecyclerViewAdapterBudget.OnItemClickListener listenerBudget;
    private RecyclerViewAdapter.OnItemClickListener listener;
    ArrayList<Object[]> dbData, budgetData;
    TextView sumOfCharges;
    public Map<String, Long> categoryBudgets;
    public Map<String, Long> categorySpending;
    PieChart chart1, chart2, chart3;
    // view[0] = recyclerview totalcharges, 1 = textview sumofcharges, 2-4 = piecharts 1-3
    View[] views;



    public Charges(View[] views, FirebaseUser user, Context context, CRUD crud){
        // Single line inits
        this.user = user;
        this.crud = crud;
        listOfCharges = (RecyclerView) views[0];
        listOfBudgets = (RecyclerView) views[3];
        this.sumOfCharges = (TextView) views[1];
        chart1 = (PieChart) views[2];
        // Initialize listOfCharges and components;
        listOfCharges.setLayoutManager(new LinearLayoutManager(context));
        listOfBudgets.setLayoutManager(new LinearLayoutManager(context));
        listOfCharges.setVisibility(View.GONE);
        listOfBudgets.setVisibility(View.GONE);
        listener = this;
        listenerBudget = this;
        categorySpending = new HashMap<String, Long>();
        initAdapter();
    }

    public void updateChargesList(Object[] newEntry) {
        Log.d("User id", user.getUid());
        crud.readDocument("users", user.getUid(), new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    Long tmp = Long.parseLong("0");
                    Map<String, Map<String, Long>> userDoc = (Map<String, Map<String, Long>>) task.getResult().getData().get("Charges");
                    if (userDoc.containsKey(newEntry[0])) userDoc.get(newEntry[0]).put((String) newEntry[1], Long.parseLong((String)newEntry[2]));
                    dbData.add(newEntry);
                    crud.updateDocument("users", user.getUid(), convertForDatabase());
                    adapter.updateData(dbData);
                    adapter.notifyItemInserted(dbData.size()-1);
                    updateSpending(userDoc);
                }
                else {
                    Log.e("Error", "Query Failed", task.getException());
                }
            }
        });
    }

    private Map<String, Object> convertForDatabase(){
        Map<String, Object> finalMap = new HashMap<>();
        Map<String, Map<String, Long>> chargesMap = new HashMap<>();

        for (Object[] item : dbData){
            String cat = (String) item[0];
            String name = (String) item[1];
            Long amount = Long.parseLong(item[2].toString());

            if (!chargesMap.containsKey(cat)){
                chargesMap.put(cat, new HashMap<>());
            }

            chargesMap.get(cat).put(name, amount);
        }
        finalMap.put("Charges", (Object) chargesMap);
        return finalMap;
    }

    private void initAdapter() {
        dbData = new ArrayList<>();
        crud.readDocument("users", user.getUid(), new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    Map<String, Map<String, Long>> data = (Map<String, Map<String, Long>>) task.getResult().getData().get("Charges");
                    categoryBudgets = (Map<String, Long>) task.getResult().getData().get("categoryBudgets");
                    updateSpending(data);
                    adapter = new RecyclerViewAdapter(dbData);
                    adapter.setListener(listener);
                    listOfCharges.setAdapter(adapter);
                    listOfCharges.setVisibility(View.VISIBLE);

                }
                else{
                    Log.e("DatabaseQuery", "Query Failed.", task.getException());
                }
            }
        });
    }

    // Updates categorySpending and sumOfCharges
    private void updateSpending(Map<String, Map<String, Long>> data){
        Long tmp = Long.parseLong("0");
        for (Map.Entry<String, Map<String, Long>> category: data.entrySet()
        ) {
            String categoryName = category.getKey();
            categorySpending.put(categoryName, Long.parseLong("0"));
            for (Map.Entry<String, Long> charge: category.getValue().entrySet()){
                categorySpending.put(categoryName, categorySpending.get(categoryName) + charge.getValue());
                tmp += charge.getValue();
                dbData.add(new Object[]{categoryName, charge.getKey(), charge.getValue()});
            }
            sumOfCharges.setText(tmp.toString());
        }
        updatePieCharts();
        initBudget();
    }

    private void initBudget() {
        budgetData = new ArrayList<>();
        ArrayList<String> encounteredCategories = new ArrayList<>();
        for (Map.Entry<String, Long> budget: categoryBudgets.entrySet()){
            if (!categorySpending.containsKey(budget.getKey())) budgetData.add(new Object[]{budget.getKey(), "0", budget.getValue()});
            else{
                if (!encounteredCategories.contains(budget.getKey())){
                    encounteredCategories.add(budget.getKey());
                    budgetData.add(new Object[]{budget.getKey(), categorySpending.get(budget.getKey()), budget.getValue()});
                }
            }
        }
        for (Object[] element : budgetData){
            Log.d("", element[0].toString() +", "+element[1].toString()+", "+element[2].toString()+"\n");
        }
        budgetAdapter = new RecyclerViewAdapterBudget(budgetData);
        budgetAdapter.setListener(listenerBudget);
        listOfBudgets.setAdapter(budgetAdapter);
        listOfBudgets.setVisibility(View.VISIBLE);
    }

    private void deleteListEntry(int index) {
        // TODO: Implement deleting entry for new database layout
/*        dbData.remove(index);
        crud.updateDocument("users", user.getUid(), dbData);
        crud.readDocument("users", user.getUid(), new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    Map<String, Object> userDoc = task.getResult().getData();
                    ArrayList<Object[]> chargesList = (ArrayList<Object[]>) userDoc.get("Charges");
                    chargesList.remove(index);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        userDoc.replace("Charges", chargesList);
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
        });*/
    }

    private void updatePieCharts(){
        List<PieEntry> chart1Entries = new ArrayList<>();
        for (Map.Entry<String, Long> charge: categorySpending.entrySet()){
            chart1Entries.add(new PieEntry(charge.getValue(), charge.getKey()));
        }
        PieDataSet chart1Data = new PieDataSet(chart1Entries, "Total Spending");
        chart1Data.setColors(ColorTemplate.COLORFUL_COLORS);
        chart1Data.setSliceSpace(2f);
        chart1Data.setValueTextColor(Color.WHITE);
        chart1Data.setValueTextSize(12f);
        chart1.getLegend().setEnabled(false);
        chart1.setData(new PieData(chart1Data));
        chart1.setCenterText(sumOfCharges.getText()+"\nTotal Spending");
        chart1.invalidate();
        chart1.animateXY(1000, 1000);
    }

    @Override
    public void onItemClick(int pos) {
        deleteListEntry(pos);
    }
}
