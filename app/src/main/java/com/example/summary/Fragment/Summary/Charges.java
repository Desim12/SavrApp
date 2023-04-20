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
    private RecyclerViewAdapter adapter;
    private RecyclerViewAdapterBudget budgetAdapter;
    private RecyclerViewAdapterBudget.OnItemClickListener listenerBudget;
    private RecyclerViewAdapter.OnItemClickListener listener;
    ArrayList<Object[]> dbData, budgetData;
    TextView sumOfChargesView;
    Long sumOfCharges;
    public Map<String, Long> categoryBudgets;
    public Map<String, Long> categorySpending;
    PieChart chart1;
    // view[0] = recyclerview totalcharges, 1 = textview sumofcharges, 2-4 = piecharts 1-3



    public Charges(View[] views, Map<String, Map<String, Long>> charges, Map<String, Long> categoryBudgets, Context context){
        // Single line inits
        listOfCharges = (RecyclerView) views[0];
        listOfBudgets = (RecyclerView) views[3];
        this.sumOfChargesView = (TextView) views[1];
        chart1 = (PieChart) views[2];
        this.categoryBudgets = categoryBudgets;
        sumOfCharges = 0L;
        dbData = new ArrayList<>();
        convertToList(charges);
        // Initialize listOfCharges and components;
        listOfCharges.setLayoutManager(new LinearLayoutManager(context));
        listOfBudgets.setLayoutManager(new LinearLayoutManager(context));
        listOfCharges.setVisibility(View.GONE);
        listOfBudgets.setVisibility(View.GONE);
        listener = this;
        listenerBudget = this;
        categorySpending = new HashMap<>();
        initAdapter();
        initSpending();
        initBudget();
    }

    public void updateChargesList(Object[] newEntry) {
        dbData.add(newEntry);
        adapter.updateData(dbData);
        adapter.notifyItemInserted(dbData.size()-1);
        updateSpending(newEntry);
    }

    public Map<String, Object> convertForDatabase(){
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
                    adapter = new RecyclerViewAdapter(dbData);
                    adapter.setListener(listener);
                    listOfCharges.setAdapter(adapter);
                    listOfCharges.setVisibility(View.VISIBLE);
    }

    private void convertToList(Map<String, Map<String, Long>> data){
        for (Map.Entry<String, Map<String, Long>> category: data.entrySet()){
            for (Map.Entry<String, Long> nameAmount: category.getValue().entrySet()){
                dbData.add(new Object[]{category.getKey(), nameAmount.getKey(), nameAmount.getValue()});
            }
        }
    }

    // inits categorySpending and sumOfCharges
    private void initSpending(){
        for (Object[] charge: dbData){
            if (!categorySpending.containsKey(charge[0])){
                categorySpending.put(charge[0].toString(), 0L);
            }
            categorySpending.put(charge[0].toString(), categorySpending.get(charge[0]) + Long.parseLong(charge[2].toString()));
            sumOfCharges += Long.parseLong(charge[2].toString());
        }
        sumOfChargesView.setText(sumOfCharges.toString());
        updatePieCharts();
    }

    private void updateSpending(Object[] newEntry){
        categorySpending.put((String) newEntry[0], categorySpending.get(newEntry[0]) + Long.parseLong((String)newEntry[2]));
        sumOfCharges += Long.parseLong((String)newEntry[2]);
        sumOfChargesView.setText(sumOfCharges.toString());
        chart1.invalidate();
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
        chart1.setCenterText(sumOfChargesView.getText()+"\nTotal Spending");
        chart1.invalidate();
        chart1.animateXY(1000, 1000);
    }

    @Override
    public void onItemClick(int pos) {
        deleteListEntry(pos);
    }
}
