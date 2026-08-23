package com.davinci.pazencasa;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Accion.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instancia;

    public abstract AccionDao accionDao();

    public static AppDatabase getInstance(Context contexto) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(
                            contexto.getApplicationContext(),
                            AppDatabase.class,
                            "db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return instancia;
    }
}
