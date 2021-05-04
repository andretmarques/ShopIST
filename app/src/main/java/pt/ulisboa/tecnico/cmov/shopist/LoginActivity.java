package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText;
    EditText passwordEditText;
    TextView register;
    Button loginButton;
    ProgressBar loadingProgressBar;

    String emailRegistered;
    String passwordRegistered;

    FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email_login);
        passwordEditText = findViewById(R.id.password_login);
        loginButton = findViewById(R.id.sign_in);
        loadingProgressBar = findViewById(R.id.loading);
        register = findViewById(R.id.register);

        mAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, RegisterUser.class)));
        loginButton.setOnClickListener(view -> userLogin());

        emailRegistered = getIntent().getStringExtra("email");
        passwordRegistered = getIntent().getStringExtra("password");
        if (emailRegistered != null && passwordRegistered != null) {
            emailEditText.setText(emailRegistered);
            passwordEditText.setText(passwordRegistered);
        }
    }

    private void userLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (password.length() < 6) {
            if (password.isEmpty()) {
                passwordEditText.setError("Empty");
            } else {
                passwordEditText.setError("Minimum password length is 6 characters");
            }
            passwordEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (email.isEmpty()) {
                emailEditText.setError("Empty");
            } else {
                emailEditText.setError("Provide a valid email");
            }
            emailEditText.requestFocus();
            return;
        }

        loadingProgressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.putExtra("UserEmail", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    startActivity(i);
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to Login. Check your credentials", Toast.LENGTH_LONG).show();
                    emailEditText.setText(null);
                    passwordEditText.setText(null);

                }
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

}