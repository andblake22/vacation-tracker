package com.example.vacationscheduler.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vacationscheduler.entities.Vacation;

import java.util.List;

@Dao
public interface VacationDao {
    @Insert
    void insert(Vacation vacation);

    @Update
    void update(Vacation vacation);

    @Delete
    void delete(Vacation vacation);

    @Query("SELECT * FROM vacations")
    List<Vacation> getAllVacations();

    @Query("SELECT COUNT(*) FROM excursions WHERE vacationId = :vacationId")
    int countExcursionsForVacation(int vacationId);

    @Query("SELECT 1 FROM vacations WHERE id = :vacationId LIMIT 1")
    boolean exists(int vacationId);

    @Query("SELECT * FROM vacations WHERE id = :vacationId LIMIT 1")
    Vacation getVacationById(int vacationId);

    @Query("SELECT * FROM vacations WHERE title LIKE '%' || :query || '%' OR hotel LIKE '%' || :query || '%' ORDER BY startDate")
    List<Vacation> searchVacations(String query);
}