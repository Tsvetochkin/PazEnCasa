package com.davinci.pazencasa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AccionAdapter extends RecyclerView.Adapter<AccionAdapter.ViewHolder> {
    public static final String[] TIPOS = {
        "PALABRAS", "DULCES", "FLORES", "FLORES_GRANDES", "JOYERIA", "SALIDA"
    };

    private List<Accion> acciones;
    private final OnEliminarListener listener;
    private final OnEditarListener editListener;

    public interface OnEliminarListener {
        void onEliminar(Accion accion);
    }

    public interface OnEditarListener {
        void onEditar(Accion accion);
    }

    public AccionAdapter(List<Accion> acciones, OnEliminarListener listener) {
        this(acciones, listener, null);
    }

    public AccionAdapter(List<Accion> acciones, OnEliminarListener listener, OnEditarListener editListener) {
        this.acciones = acciones;
        this.listener = listener;
        this.editListener = editListener;
    }

    public void setAcciones(List<Accion> acciones) {
        this.acciones = acciones;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_accion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Accion accion = acciones.get(position);
        holder.tvTipo.setText(nombreTipo(accion.tipo));
        holder.ivIcono.setImageResource(iconoTipo(accion.tipo));
        holder.tvTiempo.setText(tiempoRelativo(accion.fecha));
        if (listener != null) {
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(accion));
        } else {
            holder.btnEliminar.setVisibility(View.GONE);
        }
        if (editListener != null) {
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEditar.setOnClickListener(v -> editListener.onEditar(accion));
        } else {
            holder.btnEditar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return acciones != null ? acciones.size() : 0;
    }

    static String nombreTipo(String tipo) {
        switch (tipo) {
            case "PALABRAS": return "💬 Palabras tiernas";
            case "DULCES":   return "🍫 Dulces";
            case "FLORES":   return "🌸 Flores";
            case "FLORES_GRANDES": return "💐 Ramo grande";
            case "JOYERIA":  return "💍 Joyería";
            case "SALIDA":   return "🎬 Salida";
            default: return tipo;
        }
    }

    private int iconoTipo(String tipo) {
        switch (tipo) {
            case "PALABRAS": return R.drawable.ic_palabras;
            case "DULCES":   return R.drawable.ic_dulces;
            case "FLORES":   return R.drawable.ic_flores;
            case "FLORES_GRANDES": return R.drawable.ic_flores_grandes;
            case "JOYERIA":  return R.drawable.ic_joyeria;
            case "SALIDA":   return R.drawable.ic_salida;
            default: return R.drawable.ic_palabras;
        }
    }

    private String tiempoRelativo(long fecha) {
        long diff = System.currentTimeMillis() - fecha;
        long minutos = TimeUnit.MILLISECONDS.toMinutes(diff);
        long horas = TimeUnit.MILLISECONDS.toHours(diff);
        long dias = TimeUnit.MILLISECONDS.toDays(diff);

        if (minutos < 1) return "Ahora mismo";
        if (minutos < 60) return "Hace " + minutos + " min";
        if (horas < 24) return "Hace " + horas + " h";
        return "Hace " + dias + " día" + (dias > 1 ? "s" : "");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcono;
        TextView tvTipo, tvTiempo;
        ImageButton btnEliminar, btnEditar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcono = itemView.findViewById(R.id.iv_icono);
            tvTipo = itemView.findViewById(R.id.tv_tipo);
            tvTiempo = itemView.findViewById(R.id.tv_tiempo);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
            btnEditar = itemView.findViewById(R.id.btn_editar);
        }
    }
}
