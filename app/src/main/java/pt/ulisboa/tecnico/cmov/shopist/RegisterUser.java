package pt.ulisboa.tecnico.cmov.shopist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterUser extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView banner;
    private EditText emailEditText, passwordEditText;
    private ProgressBar progressBar;
    private Button registerBtn;
    private DatabaseReference myRef;

    String email;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://shopist-310217-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        banner = findViewById(R.id.banner);
        emailEditText = findViewById(R.id.email_register);
        passwordEditText = findViewById(R.id.password_register);
        progressBar = findViewById(R.id.progress_bar);
        registerBtn = findViewById(R.id.register_btn);

        registerBtn.setOnClickListener(view -> {
            if (registerUser()) {
                newUser();
            }
        });
    }

    private Boolean registerUser() {
        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();
        if (password.length() < 6) {
            if (password.isEmpty()) {
                passwordEditText.setError("Empty");
            } else {
                passwordEditText.setError("Minimum password length is 6 characters");
            }
            passwordEditText.requestFocus();
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (email.isEmpty()) {
                emailEditText.setError("Empty");
            } else {
                emailEditText.setError("Provide a valid email");
            }
            emailEditText.requestFocus();
            return false;
        }
        else {
            return true;
        }
    }

    public void newUser() {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = new User(email);

                myRef.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(user).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(RegisterUser.this, "User successfully registered", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(RegisterUser.this, LoginActivity.class);
                                i.putExtra("email", email);
                                i.putExtra("password", password);
                                startActivity(i);
                            } else {
                                Toast.makeText(RegisterUser.this, "Failed to register. Try again", Toast.LENGTH_LONG).show();
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        });
            } else {
                Toast.makeText(RegisterUser.this, "Failed to register. Try again", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

}