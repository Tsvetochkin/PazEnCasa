package com.davinci.pazencasa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {
    EditText nombre, email, pass;
    Button regBtn;
    TextView toLogin;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        nombre = findViewById(R.id.reg_nombre);
        email = findViewById(R.id.reg_email);
        pass = findViewById(R.id.reg_pass);
        regBtn = findViewById(R.id.reg_btn);
        toLogin = findViewById(R.id.to_login);

        regBtn.setOnClickListener(v -> {
            String nombreStr = nombre.getText().toString().trim();
            String emailStr = email.getText().toString().trim();
            String passStr = pass.getText().toString().trim();

            if (nombreStr.isEmpty() || emailStr.isEmpty() || passStr.isEmpty()) {
                Toast.makeText(this, R.string.empty_field_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
                Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(emailStr, passStr)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        Runnable irAlInicio = () -> {
                            Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        };
                        if (user != null) {
                            user.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombreStr)
                                    .build())
                                .addOnCompleteListener(t -> irAlInicio.run());
                        } else {
                            irAlInicio.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, getString(R.string.auth_failed) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        toLogin.setOnClickListener(v -> finish());
    }
}
