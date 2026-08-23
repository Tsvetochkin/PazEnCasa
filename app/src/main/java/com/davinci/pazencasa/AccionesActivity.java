package com.davinci.pazencasa;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccionesActivity extends AppCompatActivity {

    private static final long DIA    = 86_400_000L;
    private static final long SEMANA = DIA * 7;
    private static final long MES    = DIA * 30;
    private static final long ANIO   = DIA * 365;

    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean overdoseMostrado = false;

    private final int[] cardIds = {
        R.id.btn_palabras, R.id.btn_dulces, R.id.btn_flores,
        R.id.btn_flores_grandes, R.id.btn_joyeria, R.id.btn_salida
    };
    private final int[] statusIds = {
        R.id.tv_status_palabras, R.id.tv_status_dulces, R.id.tv_status_flores,
        R.id.tv_status_flores_grandes, R.id.tv_status_joyeria, R.id.tv_status_salida
    };
    private final String[] tipos = {
        "PALABRAS", "DULCES", "FLORES", "FLORES_GRANDES", "JOYERIA", "SALIDA"
    };
    private final int[] requeridos = {4, 2, 2, 1, 1, 1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acciones);

        db = AppDatabase.getInstance(this);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        findViewById(R.id.btn_listo).setOnClickListener(v -> finish());

        for (int i = 0; i < tipos.length; i++) {
            bindCard(i);
        }

        cargarEstados();
    }

    private void bindCard(int index) {
        View card = findViewById(cardIds[index]);
        String tipo = tipos[index];
        card.setOnClickListener(v -> {
            animarBoton(v);
            executor.execute(() -> {
                db.accionDao().insertar(new Accion(tipo));
                runOnUiThread(() -> {
                    Toast.makeText(this, mensajeAccion(tipo), Toast.LENGTH_SHORT).show();
                    cargarEstados();
                });
            });
        });
    }

    private void cargarEstados() {
        executor.execute(() -> {
            long ahora = System.currentTimeMillis();
            AccionDao dao = db.accionDao();
            int[] counts = {
                dao.contarDesde("PALABRAS",       ahora - DIA),
                dao.contarDesde("DULCES",         ahora - SEMANA),
                dao.contarDesde("FLORES",         ahora - MES),
                dao.contarDesde("FLORES_GRANDES", ahora - MES),
                dao.contarDesde("JOYERIA",        ahora - ANIO),
                dao.contarDesde("SALIDA",         ahora - MES)
            };

            runOnUiThread(() -> {
                for (int i = 0; i < 6; i++) {
                    TextView tv = findViewById(statusIds[i]);
                    if (counts[i] >= requeridos[i]) {
                        tv.setText("✅ Cumplido");
                        tv.setTextColor(0xFF2E7D32);
                    } else if (counts[i] > 0) {
                        tv.setText("🔄 " + counts[i] + "/" + requeridos[i]);
                        tv.setTextColor(0xFFF57F17);
                    } else {
                        tv.setText("❌ Pendiente");
                        tv.setTextColor(0xFFB71C1C);
                    }
                }

                // Overdose on sweets
                if (counts[1] >= 5 && !overdoseMostrado) {
                    overdoseMostrado = true;
                    SharedPreferences prefs = getSharedPreferences("paz_prefs", MODE_PRIVATE);
                    String name = prefs.getString("partner_name", "tu pareja");
                    new AlertDialog.Builder(this)
                        .setTitle(R.string.overdose_title)
                        .setMessage("\"¿Qué te pensás, que soy una vaca?\"\n— " + name + "\n\n(-20% de felicidad esta semana)")
                        .setPositiveButton("Ok, entendí 🐄", null)
                        .show();
                }
                if (counts[1] < 5) overdoseMostrado = false;
            });
        });
    }

    private void animarBoton(View v) {
        v.animate().scaleX(0.88f).scaleY(0.88f).setDuration(80)
            .withEndAction(() -> v.animate().scaleX(1.06f).scaleY(1.06f).setDuration(80)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                .start())
            .start();
    }

    private String mensajeAccion(String tipo) {
        switch (tipo) {
            case "PALABRAS":       return "💬 ¡Le dijiste algo lindo!";
            case "DULCES":         return "🍫 ¡Le llevaste algo dulce!";
            case "FLORES":         return "🌸 ¡Qué detalle con las flores!";
            case "FLORES_GRANDES": return "💐 ¡Romántico total!";
            case "JOYERIA":        return "💍 ¡Lo diste todo!";
            case "SALIDA":         return "🎬 ¡Planeaste una salida!";
            default:               return "✨ ¡Bien hecho!";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
