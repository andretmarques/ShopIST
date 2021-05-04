package pt.ulisboa.tecnico.cmov.shopist;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText;
    EditText passwordEditText;
    TextView register;
    Button loginButton;
    ProgressBar loadingProgressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email_login);
        passwordEditText = findViewById(R.id.password_login);
        loginButton = findViewById(R.id.sign_in);
        loadingProgressBar = findViewById(R.id.loading);
        register = findViewById(R.id.register);
        register.setOnClickListener(view -> startActivityForResult(new Intent(LoginActivity.this, RegisterUser.class), 12119));
    }

}