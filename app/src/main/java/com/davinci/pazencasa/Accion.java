package com.davinci.pazencasa;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Accion {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String tipo;
    public long fecha;

    public Accion() {}

    public Accion(String tipo) {
        this.tipo = tipo;
        this.fecha = System.currentTimeMillis();
    }
}
