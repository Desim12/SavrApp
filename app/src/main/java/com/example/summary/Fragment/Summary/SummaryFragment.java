package com.example.summary.Fragment.Summary;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.summary.Activities.MainActivity;
import com.example.summary.CRUD;
import com.example.summary.R;
import com.example.summary.databinding.FragmentSummaryBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SummaryFragment extends Fragment {

    private FragmentSummaryBinding binding;
    private CRUD crud;
    private FirebaseUser user;
    private Context context;
    private Charges chargeSummary;
    FirebaseFirestore db;
    View newChargePopup, newBudgetPopup;
    String newChargeCategory, newChargeName, newChargeAmount, newBudgetCategory, newBudgetAmount;
    PopupWindow popupCharge, popupBudget;
    PieChart chart1;
    View[] views;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        context = getContext();
        db = FirebaseFirestore.getInstance();
        crud = new CRUD(db);
        binding = FragmentSummaryBinding.inflate(inflater, container, false);

        newChargePopup = inflater.inflate(R.layout.new_charge_entry, null);
        newBudgetPopup = inflater.inflate(R.layout.new_budget_entry, null);
        popupBudget = new PopupWindow(context);
        popupCharge = new PopupWindow(context);
        popupCharge.setContentView(newChargePopup);
        popupBudget.setContentView(newBudgetPopup);
        popupCharge.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupBudget.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupCharge.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupBudget.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupCharge.setFocusable(true);
        popupBudget.setFocusable(true);

        MainActivity activity = (MainActivity) getActivity();
        assert activity != null;
        this.user = activity.getUser();
        chart1 = binding.pieChart1;
        views = new View[]{binding.totalCharges, binding.sumOfCharges, chart1, binding.budgetList};
        crud.readDocument("users", user.getUid(), new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    chargeSummary = new Charges(views, convertToList((Map<String, Map<String, Long>>) task.getResult().getData().get("Charges")), (Map<String, Long>) task.getResult().getData().get("categoryBudgets"), context);
                }
                else {
                    Log.e("Error", "Query Failed", task.getException());
                }
            }
        });
        return binding.getRoot();
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

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.helloText.setText("Welcome back, " + user.getDisplayName() + "!");
        binding.backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SummaryFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        popupCharge.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                EditText cat = newChargePopup.findViewById(R.id.category);
                EditText name = newChargePopup.findViewById(R.id.name);
                EditText amount = newChargePopup.findViewById(R.id.amount);
                newChargeCategory = cat.getText().toString();
                newChargeAmount = amount.getText().toString();
                newChargeName = name.getText().toString();
                cat.setText("");
                name.setText("");
                amount.setText("");
                if (!Objects.equals(newChargeName, "") && !Objects.equals(newChargeCategory, "") && !Objects.equals(newChargeAmount, "")){
                    chargeSummary.updateChargesList(new Object[]{newChargeCategory, newChargeName, newChargeAmount});
                }
                crud.updateDocument("users", user.getUid(), chargeSummary.convertForDatabase()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("Upload", "Upload was successful");
                        }
                        else{
                            Log.e("Upload", "Upload unsuccessful", task.getException());
                        }
                    }
                });
            }
        });

        popupBudget.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                EditText cat = newBudgetPopup.findViewById(R.id.category);
                EditText amount = newBudgetPopup.findViewById(R.id.amount);
                newBudgetCategory = cat.getText().toString();
                newBudgetAmount = amount.getText().toString();
                cat.setText("");
                amount.setText("");
                Map<String, String> map = new HashMap<>();
                map.put(newBudgetCategory, newBudgetAmount);
                if (!Objects.equals(newBudgetCategory, "") && !Objects.equals(newBudgetAmount, "")) chargeSummary.updateBudgets(map);
            }
        });

        binding.editBudgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupBudget.showAsDropDown(view);
            }
        });
        binding.newChargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupCharge.showAsDropDown(view);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}