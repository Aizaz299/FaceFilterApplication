package com.gsrathoreniks.facefilter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText email,password;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.edt_email);
        password = findViewById(R.id.edt_password);
    }


    private void updateUI(FirebaseUser currentUser) {
        if (currentUser!=null){
            Intent intent = new Intent(this, SignupDetailActivity.class);
            intent.putExtra("uid", currentUser.getUid());
            startActivity(intent);
        }
    }

    public void signup(View view) {
        if (!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()){
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("Shahan", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignupActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                                updateUI(null);
                            }

                            // ...
                        }
                    });
        }
    }
    public void loginOpen(View view){
        startActivity(new Intent(this,LoginActivity.class));
    }
}
