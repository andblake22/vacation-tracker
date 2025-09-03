package com.example.vacationscheduler.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vacationscheduler.entities.Excursion;

import java.util.List;

@Dao
public interface ExcursionDao {
    @Insert
    void insert(Excursion excursion);

    @Update
    void update(Excursion excursion);

    @Delete
    void delete(Excursion excursion);

    @Query("SELECT * FROM excursions WHERE vacationId = :vacationId")
    List<Excursion> getExcursionsForVacation(int vacationId);

    @Query("SELECT * FROM excursions")
    List<Excursion> getAllExcursions();

    @Query("SELECT * FROM excursions WHERE vacationId = :vacationId AND (title LIKE '%' || :query || '%' OR date LIKE '%' || :query || '%') ORDER BY date")
    List<Excursion> searchExcursionsForVacation(int vacationId, String query);
}