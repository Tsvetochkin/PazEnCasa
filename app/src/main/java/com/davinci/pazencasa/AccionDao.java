package com.davinci.pazencasa;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AccionDao {
    @Query("SELECT * FROM Accion ORDER BY fecha DESC LIMIT 30")
    List<Accion> listarRecientes();

    @Query("SELECT COUNT(*) FROM Accion WHERE tipo = :tipo AND fecha >= :desde")
    int contarDesde(String tipo, long desde);

    @Insert
    void insertar(Accion accion);

    @Update
    void actualizar(Accion accion);

    @Delete
    void eliminar(Accion accion);

    @Query("SELECT COUNT(*) FROM Accion WHERE fecha >= :inicio AND fecha < :fin")
    int contarEnDia(long inicio, long fin);

    @Query("SELECT * FROM Accion ORDER BY fecha DESC")
    List<Accion> listarTodos();
}
