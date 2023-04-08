package com.example.summary.Fragment.Summary;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.summary.Activities.MainActivity;
import com.example.summary.CRUD;
import com.example.summary.R;
import com.example.summary.databinding.FragmentSummaryBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class SummaryFragment extends Fragment {

    private FragmentSummaryBinding binding;
    private CRUD crud;
    private FirebaseUser user;
    private Context context;
    private Charges chargeSummary;
    FirebaseFirestore db;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        context = getContext();
        db = FirebaseFirestore.getInstance();
        crud = new CRUD(db);
        binding = FragmentSummaryBinding.inflate(inflater, container, false);
        MainActivity activity = (MainActivity) getActivity();
        assert activity != null;
        this.user = activity.getUser();
        chargeSummary = new Charges(binding.totalCharges, binding.newCharge, binding.newChargeField, this.user, context, crud);
        binding.budget.setText("900");
        binding.helloText.setText("Hello, "+ user.getDisplayName() + "!");
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.helloText.setText("Hello, " + user.getDisplayName() + "!");
        binding.sumOfCharges.setText("800");
        binding.backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SummaryFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}