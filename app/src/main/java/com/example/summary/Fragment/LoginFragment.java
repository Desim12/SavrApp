package com.example.summary.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.summary.databinding.FragmentLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.util.Log;
import android.widget.Toast;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;
    private String email;
    private String password;

    public interface OnSuccessfulLogin {
        public void onSuccessfulLogin(FirebaseUser user);
    }

    OnSuccessfulLogin mCallback;

    public void setSuccessfulLoginListener(Activity activity){
        mCallback = (OnSuccessfulLogin) activity;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        setSuccessfulLoginListener(getActivity());
        mAuth = FirebaseAuth.getInstance();
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = binding.emailField.getText().toString();
                password = binding.passwordField.getText().toString();
                binding.passwordField.setText("");
                binding.emailField.setText("");
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d("login", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d("Debug", user.getEmail());
                            mCallback.onSuccessfulLogin(user);
                            //updateUI(user);
//                            NavHostFragment.findNavController(LoginFragment.this)
//                                    .navigate(R.id.action_FirstFragment_to_SecondFragment);
                        }
                        else{
                            Log.w("Login", "signInWithEmail:Failure", task.getException());
                            binding.incorrect.setText("Incorrect username or password. Please try again.");
                            return;
                        }
                    }
                }
                );

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}