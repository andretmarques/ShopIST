package pt.ulisboa.tecnico.cmov.shopist;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText;
    EditText passwordEditText;
    TextView register;
    TextView forgotPassword;
    TextView guestUser;
    Button loginButton;
    ProgressBar loadingProgressBar;

    String emailRegistered;
    String passwordRegistered;

    FirebaseAuth mAuth;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String cachedUsername;
    String cachedPassword;
    String cachedUserID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        editor = prefs.edit();
        cachedUsername = prefs.getString("username", null);
        cachedPassword = prefs.getString("password", null);
        cachedUserID = prefs.getString("userid", null);

        if (cachedUserID != null) {
            cachedLogin();
        } else {
            setContentView(R.layout.activity_login);
            mAuth = FirebaseAuth.getInstance();

            emailEditText = findViewById(R.id.email_login);
            passwordEditText = findViewById(R.id.password_login);
            loginButton = findViewById(R.id.sign_in);
            loadingProgressBar = findViewById(R.id.loading);
            register = findViewById(R.id.register);
            forgotPassword = findViewById(R.id.forgotPassword);
            guestUser = findViewById(R.id.guest);

            register.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, RegisterUser.class)));
            forgotPassword.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, ForgotPassword.class)));
            loginButton.setOnClickListener(view -> userLogin());
            guestUser.setOnClickListener(view -> guestLogin());

            emailRegistered = getIntent().getStringExtra("email");
            passwordRegistered = getIntent().getStringExtra("password");
            if (emailRegistered != null) {
                emailEditText.setText(emailRegistered);
            }
            if (passwordRegistered != null) {
                passwordEditText.setText(passwordRegistered);
            }
        }

    }

    private void userLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        editor.putString("username", email);
        editor.putString("password", password);
        editor.apply();

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

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.putExtra("UserEmail", FirebaseAuth.getInstance().getCurrentUser().getUid());
                editor.putString("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                editor.apply();
                startActivity(i);
            } else {
                Toast.makeText(LoginActivity.this, "Failed to Login. Check your credentials", Toast.LENGTH_LONG).show();
                emailEditText.setText(null);
                passwordEditText.setText(null);

            }
            loadingProgressBar.setVisibility(View.INVISIBLE);
        });
    }

    public void cachedLogin() {
        Toast.makeText(LoginActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("UserEmail", cachedUserID);
        startActivity(i);
        finish();
    }

    public void guestLogin() {
        mAuth.signInAnonymously().addOnCompleteListener(LoginActivity.this, task -> {
            if (task.isSuccessful()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.common_google_signin_btn_icon_disabled)
                        .setNegativeButton("Go back", null)
                        .setPositiveButton("I understand", (dialogInterface, i) -> {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("UserEmail", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            editor.putString("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            editor.apply();
                            startActivity(intent);
                        })
                        .setMessage("⚠️ \nIf you Logout, all your stored data will be lost. \nPlease create an account to avoid it.\n⚠️")
                        .setTitle("Guest User");
                AlertDialog dialog = builder.show(); //builder is your just created builder
                TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
                messageText.setGravity(Gravity.CENTER);
                dialog.show();

            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });

    }

}