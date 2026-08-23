package com.davinci.pazencasa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText email, pass;
    Button btn;
    TextView toRegister;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        verificarUsuarioActual();

        email = findViewById(R.id.email);
        pass = findViewById(R.id.pass);
        btn = findViewById(R.id.btn);
        toRegister = findViewById(R.id.to_register);
        progressBar = findViewById(R.id.progress_bar);

        btn.setOnClickListener(v -> {
            String e = email.getText().toString().trim();
            String p = pass.getText().toString().trim();

            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, R.string.empty_field_error, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                return;
            }

            mostrarCargando(true);
            FirebaseAuth.getInstance().signInWithEmailAndPassword(e, p)
                    .addOnSuccessListener(authResult -> {
                        mostrarCargando(false);
                        Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(err -> {
                        mostrarCargando(false);
                        Toast.makeText(this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                    });
        });

        toRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void verificarUsuarioActual() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void mostrarCargando(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        btn.setEnabled(!mostrar);
        email.setEnabled(!mostrar);
        pass.setEnabled(!mostrar);
    }
}
