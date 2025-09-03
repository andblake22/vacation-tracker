package com.example.vacationscheduler.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.vacationscheduler.entities.Report;

import java.util.List;

@Dao
public interface ReportDao {
    @Insert
    void insert(Report report);

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    List<Report> getAllReports();

    @Query("SELECT * FROM reports WHERE type = :type ORDER BY timestamp DESC")
    List<Report> getReportsByType(String type);

    @Delete
    void delete(Report report);
}
