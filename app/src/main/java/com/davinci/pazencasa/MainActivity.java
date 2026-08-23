package com.davinci.pazencasa;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final long DIA    = 86_400_000L;
    private static final long SEMANA = DIA * 7;
    private static final long MES    = DIA * 30;
    private static final long ANIO   = DIA * 365;

    private AppDatabase db;
    private AccionAdapter adapter;
    private ImageView ivMood;
    private TextView tvEstado, tvPeligro, tvUserEmail, tvVacio, tvSugerencia, tvRacha, tvPartnerName;
    private ProgressBar progressDanger;
    private ImageButton btnMenu;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private boolean muerteShown = false;
    private boolean eventoMostrado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivMood         = findViewById(R.id.iv_mood);
        tvEstado       = findViewById(R.id.tv_estado);
        tvPeligro      = findViewById(R.id.tv_peligro);
        tvUserEmail    = findViewById(R.id.tv_user_email);
        tvVacio        = findViewById(R.id.tv_vacio);
        tvSugerencia   = findViewById(R.id.tv_sugerencia);
        tvRacha        = findViewById(R.id.tv_racha);
        tvPartnerName  = findViewById(R.id.tv_partner_name);
        progressDanger = findViewById(R.id.progress_danger);
        btnMenu        = findViewById(R.id.btn_menu);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String nombre = user.getDisplayName();
            tvUserEmail.setText((nombre != null && !nombre.isEmpty())
                    ? nombre + " · " + user.getEmail()
                    : user.getEmail());
        }

        db = AppDatabase.getInstance(this);

        adapter = new AccionAdapter(new ArrayList<>(), null);
        RecyclerView rv = findViewById(R.id.rv_historial);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        findViewById(R.id.btn_acciones).setOnClickListener(v ->
            startActivity(new Intent(this, AccionesActivity.class)));

        findViewById(R.id.btn_historial_full).setOnClickListener(v ->
            startActivity(new Intent(this, HistorialActivity.class)));

        btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.action_change_password) {
                    mostrarCambioContrasena();
                    return true;
                } else if (id == R.id.action_delete_account) {
                    mostrarEliminarCuenta();
                    return true;
                }
                return false;
            });
            popup.show();
        });

        verificarNombrePareja();
    }

    @Override
    protected void onResume() {
        super.onResume();
        muerteShown = false;
        actualizar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventoMostrado = false;
    }

    private void verificarNombrePareja() {
        SharedPreferences prefs = getSharedPreferences("paz_prefs", MODE_PRIVATE);
        String nombre = prefs.getString("partner_name", null);
        if (nombre != null) {
            tvPartnerName.setText("Estado de " + nombre);
            return;
        }
        EditText input = new EditText(this);
        input.setHint(getString(R.string.setup_partner_hint));
        new AlertDialog.Builder(this)
            .setTitle(R.string.setup_partner_title)
            .setMessage(R.string.setup_partner_msg)
            .setView(input)
            .setCancelable(false)
            .setPositiveButton(R.string.confirm, (d, w) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty()) name = "tu pareja";
                prefs.edit().putString("partner_name", name).apply();
                tvPartnerName.setText("Estado de " + name);
            })
            .show();
    }

    private void actualizar() {
        executor.execute(() -> {
            long ahora = System.currentTimeMillis();
            AccionDao dao = db.accionDao();

            int palabrasHoy      = dao.contarDesde("PALABRAS",       ahora - DIA);
            int dulcesSemana     = dao.contarDesde("DULCES",         ahora - SEMANA);
            int floresMes        = dao.contarDesde("FLORES",         ahora - MES);
            int floresGrandesMes = dao.contarDesde("FLORES_GRANDES", ahora - MES);
            int joyeriaAnio      = dao.contarDesde("JOYERIA",        ahora - ANIO);
            int salidasMes       = dao.contarDesde("SALIDA",         ahora - MES);

            int felicidad = 20;
            if (palabrasHoy >= 4)        felicidad += 35;
            else if (palabrasHoy >= 1)   felicidad += 15;
            if (dulcesSemana >= 2)       felicidad += 20;
            else if (dulcesSemana >= 1)  felicidad += 10;
            if (floresMes >= 2)          felicidad += 20;
            else if (floresMes >= 1)     felicidad += 10;
            if (floresGrandesMes >= 1)   felicidad += 15;
            if (joyeriaAnio >= 1)        felicidad += 5;
            if (salidasMes >= 1)         felicidad += 10;
            if (dulcesSemana >= 5)       felicidad = Math.max(0, felicidad - 20);
            felicidad = Math.min(felicidad, 100);

            int racha = calcularRacha(dao, ahora);
            String sugerencia = generarSugerencia(palabrasHoy, dulcesSemana, floresMes,
                    floresGrandesMes, joyeriaAnio, salidasMes);
            List<Accion> historial = dao.listarRecientes();

            int f = felicidad;
            int d = dulcesSemana;
            runOnUiThread(() -> actualizarUI(f, historial, sugerencia, racha, d));
        });
    }

    private int calcularRacha(AccionDao dao, long ahora) {
        int racha = 0;
        long check = (ahora / DIA) * DIA;
        while (dao.contarEnDia(check, check + DIA) > 0) {
            racha++;
            check -= DIA;
            if (racha > 365) break;
        }
        return racha;
    }

    private String generarSugerencia(int palabrasHoy, int dulcesSemana, int floresMes,
                                      int floresGrandesMes, int joyeriaAnio, int salidasMes) {
        if (dulcesSemana >= 5)     return "🐄 Ya demasiados dulces esta semana...";
        if (palabrasHoy == 0)      return "💬 ¡No le dijiste nada lindo hoy! Empezá por ahí";
        if (palabrasHoy < 4)       return "💬 Le dijiste algo tierno (" + palabrasHoy + "/4). ¡Seguí!";
        if (dulcesSemana == 0)     return "🍫 Hace días sin chocolates. ¡Sorprendela!";
        if (dulcesSemana < 2)      return "🍫 Un detalle dulce más esta semana";
        if (floresMes == 0)        return "🌸 Ni una flor este mes... eso se nota";
        if (floresMes < 2)         return "🌸 Una flor más y cumplís el mes";
        if (floresGrandesMes == 0) return "💐 Un ramo grande marcaría la diferencia";
        if (salidasMes == 0)       return "🎬 Planeá una salida juntos este mes";
        if (joyeriaAnio == 0)      return "💍 Pensá en algo especial de joyería";
        return "✨ ¡Todo al día! Sos un campeón";
    }

    private void actualizarUI(int felicidad, List<Accion> historial, String sugerencia, int racha, int dulcesSemana) {
        progressDanger.setProgress(felicidad);
        tvPeligro.setText(felicidad + "%");
        tvSugerencia.setText(sugerencia);

        if (racha == 0)      tvRacha.setText("🌱 Arrancá la racha hoy");
        else if (racha == 1) tvRacha.setText("🔥 ¡Primer día de racha!");
        else                 tvRacha.setText("🔥 ¡" + racha + " días seguidos!");

        int colorRes;
        if (felicidad >= 76) {
            ivMood.setImageResource(R.drawable.mood_happy);
            tvEstado.setText(R.string.estado_tranquila);
            colorRes = R.color.moodHappy;
        } else if (felicidad >= 51) {
            ivMood.setImageResource(R.drawable.mood_warning);
            tvEstado.setText(R.string.estado_cuidado);
            colorRes = R.color.moodWarning;
        } else if (felicidad >= 26) {
            ivMood.setImageResource(R.drawable.mood_angry);
            tvEstado.setText(R.string.estado_enojada);
            colorRes = R.color.moodAngry;
        } else {
            ivMood.setImageResource(R.drawable.mood_furious);
            tvEstado.setText(R.string.estado_furiosa);
            colorRes = R.color.moodFurious;
        }

        int color = getColor(colorRes);
        tvEstado.setTextColor(color);
        tvPeligro.setTextColor(color);
        progressDanger.setProgressTintList(ColorStateList.valueOf(color));

        adapter.setAcciones(historial);
        tvVacio.setVisibility(historial.isEmpty() ? View.VISIBLE : View.GONE);

        if (felicidad == 0 && !muerteShown) {
            muerteShown = true;
            mostrarMuerte();
        }

        // "¿engordé?" — solo si ya puso nombre Y ya dio dulces 2 veces
        boolean nombreGuardado = getSharedPreferences("paz_prefs", MODE_PRIVATE)
                .getString("partner_name", null) != null;
        if (!eventoMostrado && nombreGuardado && dulcesSemana >= 2 && Math.random() < 0.35) {
            eventoMostrado = true;
            new Handler(Looper.getMainLooper()).postDelayed(this::mostrarEventoEngorde, 800);
        }
    }

    private void mostrarMuerte() {
        String name = getSharedPreferences("paz_prefs", MODE_PRIVATE)
                .getString("partner_name", "tu pareja");
        new AlertDialog.Builder(this)
            .setTitle(R.string.muerte_title)
            .setMessage("¡" + name + " te persiguió con una escoba! 🧹\n\nFuiste muy descuidado. Tenés que esforzarte más.")
            .setPositiveButton(R.string.muerte_btn, null)
            .setCancelable(false)
            .show();
    }

    private void mostrarEventoEngorde() {
        if (isFinishing() || isDestroyed()) return;
        String name = getSharedPreferences("paz_prefs", MODE_PRIVATE)
                .getString("partner_name", "tu pareja");
        new AlertDialog.Builder(this)
            .setTitle(R.string.event_peso_title)
            .setMessage("— ¿Me notás más llenita últimamente?")
            .setPositiveButton(R.string.event_peso_si, (d, w) ->
                Toast.makeText(this, "💢 ¡Se enojó mucho! Cuidado con lo que decís...", Toast.LENGTH_LONG).show())
            .setNegativeButton(R.string.event_peso_no, (d, w) ->
                Toast.makeText(this, "😊 " + name + " sonrió. ¡Bien jugado!", Toast.LENGTH_SHORT).show())
            .show();
    }

    private void mostrarCambioContrasena() {
        EditText input = new EditText(this);
        input.setHint(R.string.new_password_hint);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
            .setTitle(R.string.change_password_btn)
            .setView(input)
            .setPositiveButton(R.string.confirm, (d, w) -> {
                String pass = input.getText().toString().trim();
                if (!pass.isEmpty()) {
                    FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
                    if (u != null) cambiarContrasena(u, pass);
                }
            })
            .setNegativeButton(R.string.cancel, null).show();
    }

    private void mostrarEliminarCuenta() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete_account_btn)
            .setMessage(R.string.confirm_delete_msg)
            .setPositiveButton(R.string.confirm, (d, w) -> {
                FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
                if (u != null) eliminarCuenta(u);
            })
            .setNegativeButton(R.string.cancel, null).show();
    }

    private void cambiarContrasena(FirebaseUser user, String newPass) {
        user.updatePassword(newPass)
            .addOnSuccessListener(x -> Toast.makeText(this, R.string.password_updated, Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> {
                if (e instanceof FirebaseAuthRecentLoginRequiredException)
                    reautenticarY(() -> cambiarContrasena(user, newPass));
                else Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void eliminarCuenta(FirebaseUser user) {
        user.delete()
            .addOnSuccessListener(x -> {
                Toast.makeText(this, R.string.account_deleted, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            })
            .addOnFailureListener(e -> {
                if (e instanceof FirebaseAuthRecentLoginRequiredException)
                    reautenticarY(() -> eliminarCuenta(user));
                else Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    private void reautenticarY(Runnable luego) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        EditText passInput = new EditText(this);
        passInput.setHint(R.string.password_hint);
        passInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
            .setTitle(R.string.reauth_title).setMessage(R.string.reauth_msg)
            .setView(passInput)
            .setPositiveButton(R.string.confirm, (d, w) -> {
                String pass = passInput.getText().toString().trim();
                if (!pass.isEmpty() && user.getEmail() != null) {
                    AuthCredential cred = EmailAuthProvider.getCredential(user.getEmail(), pass);
                    user.reauthenticate(cred)
                        .addOnSuccessListener(x -> luego.run())
                        .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
                }
            })
            .setNegativeButton(R.string.cancel, null).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
