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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class SummaryFragment extends Fragment {

    private FragmentSummaryBinding binding;
    private CRUD crud;
    private FirebaseUser user;
    private Context context;
    private Charges chargeSummary;
    FirebaseFirestore db;
    View newChargePopup;
    String newChargeCategory, newChargeName, newChargeAmount;
    PopupWindow popupWindow;
    PieChart chart1, chart2, chart3;
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
        popupWindow = new PopupWindow(context);
        popupWindow.setContentView(newChargePopup);
        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);

        MainActivity activity = (MainActivity) getActivity();
        assert activity != null;
        this.user = activity.getUser();
        chart1 = binding.pieChart1;
        views = new View[]{binding.totalCharges, binding.sumOfCharges, chart1, binding.budgetList, chart3};
        chargeSummary = new Charges(views, this.user, context, crud);
        return binding.getRoot();
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.helloText.setText("Hello, " + user.getDisplayName() + "!");
        binding.backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SummaryFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
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
            }
        });
        binding.newChargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.showAsDropDown(view);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}