package com.davinci.pazencasa;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistorialActivity extends AppCompatActivity {

    private AppDatabase db;
    private AccionAdapter adapter;
    private TextView tvVacio;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        db = AppDatabase.getInstance(this);
        tvVacio = findViewById(R.id.tv_vacio);

        adapter = new AccionAdapter(new ArrayList<>(),
            accion ->
                new AlertDialog.Builder(this)
                    .setTitle(R.string.eliminar_accion)
                    .setMessage(R.string.confirm_delete_accion)
                    .setPositiveButton(R.string.confirm, (d, w) ->
                        executor.execute(() -> {
                            db.accionDao().eliminar(accion);
                            recargarYMostrar();
                        }))
                    .setNegativeButton(R.string.cancel, null)
                    .show(),
            this::mostrarEditar);

        RecyclerView rv = findViewById(R.id.rv_historial);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        cargarHistorial();
    }

    private void cargarHistorial() {
        executor.execute(this::recargarYMostrar);
    }

    private void recargarYMostrar() {
        List<Accion> lista = db.accionDao().listarTodos();
        runOnUiThread(() -> {
            adapter.setAcciones(lista);
            tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void mostrarEditar(Accion accion) {
        String[] tipos = AccionAdapter.TIPOS;
        String[] nombres = new String[tipos.length];
        int seleccionado = 0;
        for (int i = 0; i < tipos.length; i++) {
            nombres[i] = AccionAdapter.nombreTipo(tipos[i]);
            if (tipos[i].equals(accion.tipo)) seleccionado = i;
        }
        int[] elegido = {seleccionado};

        new AlertDialog.Builder(this)
            .setTitle(R.string.editar_accion)
            .setSingleChoiceItems(nombres, seleccionado, (d, which) -> elegido[0] = which)
            .setPositiveButton(R.string.confirm, (d, w) -> {
                accion.tipo = tipos[elegido[0]];
                executor.execute(() -> {
                    db.accionDao().actualizar(accion);
                    recargarYMostrar();
                });
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
