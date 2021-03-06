package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordBtn;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.email_recover);
        resetPasswordBtn = findViewById(R.id.reset_btn);
        progressBar = findViewById(R.id.progress_bar);

        mAuth = FirebaseAuth.getInstance();

        resetPasswordBtn.setOnClickListener(view -> resetPassword());

    }

    public void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (email.isEmpty()) {
                emailEditText.setError("Empty");
            } else {
                emailEditText.setError("Provide a valid email");
            }
            emailEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ForgotPassword.this, "Check your email to reset your password", Toast.LENGTH_LONG).show();
                Intent i = new Intent(ForgotPassword.this, LoginActivity.class);
                i.putExtra("email", email);
                startActivity(i);
            } else {
                Toast.makeText(ForgotPassword.this, "Something failed. Try again", Toast.LENGTH_LONG).show();
            }
        });
        progressBar.setVisibility(View.VISIBLE);
    }
}