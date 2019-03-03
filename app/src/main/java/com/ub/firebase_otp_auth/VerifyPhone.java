package com.ub.firebase_otp_auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;


public class VerifyPhone extends AppCompatActivity
{
    private String mVerificationId;
    private FirebaseAuth mAuth;
    private EditText editTextCode;
    private Button b_signin;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        mAuth = FirebaseAuth.getInstance();

        editTextCode = (EditText)findViewById(R.id.editTextCode);
        progressBar=(ProgressBar)findViewById(R.id.progressbar);
        b_signin=(Button)findViewById(R.id.buttonSignIn);
        FirebaseApp.initializeApp(VerifyPhone.this);

        Intent intent = getIntent();
        String mobile = intent.getStringExtra("mobile");
        sendVerificationCode(mobile);

        b_signin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String code = editTextCode.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6)
                {
                    editTextCode.setError("Enter valid code");
                    editTextCode.requestFocus();
                    return;
                }

                //progressBar.setVisibility(View.VISIBLE);
                verifyVerificationCode(code);
            }
        });

    }
    private void sendVerificationCode(String mobile)
    {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber
                (
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
    {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
        {
            String code = phoneAuthCredential.getSmsCode();//automatically storing the code into varaible
            if (code != null)
            {
                editTextCode.setText(code);
                verifyVerificationCode(code);
                //progressBar.setVisibility(View.VISIBLE);
            }
        }
        @Override
        public void onVerificationFailed(FirebaseException e)
        {
            Toast.makeText(VerifyPhone.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken)
        {
            super.onCodeSent(s, forceResendingToken);
            mVerificationId = s;
            //snackbar.dismiss();
        }
    };


    private void verifyVerificationCode(String code)
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyPhone.this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Intent intent = new Intent(VerifyPhone.this, Profile.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//it will clear all activity in stack and open fresh activity
                            startActivity(intent);
                        }
                        else
                        {
                            String mess = "Somthing is wrong, technical team is working on it, wait for our call...";
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                            {
                                mess = "Invalid OTP entered...";
                            }
                        }
                    }
                });
    }
}