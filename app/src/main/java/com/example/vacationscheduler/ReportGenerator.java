package com.example.vacationscheduler;

import com.example.vacationscheduler.database.AppDatabase;
import com.example.vacationscheduler.entities.Excursion;
import com.example.vacationscheduler.entities.Report;
import com.example.vacationscheduler.entities.Vacation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportGenerator {
    private final AppDatabase db;
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    public ReportGenerator(AppDatabase db) {
        this.db = db;
    }

    public String generateVacationReport() {
        List<Vacation> vacations = db.vacationDao().getAllVacations();
        StringBuilder sb = new StringBuilder();
        sb.append("VACATION REPORT\n");
        sb.append("Generated: ").append(timestampFormat.format(new Date())).append("\n");
        sb.append("==================================================\n\n");
        sb.append(String.format(Locale.US, "%-4s %-18s %-18s %-12s %-12s %-10s\n", "ID", "TITLE", "HOTEL", "START", "END", "EXCURS."));
        sb.append("--------------------------------------------------------------------------------\n");
        for (Vacation v : vacations) {
            int count = db.vacationDao().countExcursionsForVacation(v.getId());
            sb.append(String.format(Locale.US, "%-4d %-18s %-18s %-12s %-12s %-10d\n",
                    v.getId(), truncate(v.getTitle(),18), truncate(v.getHotel(),18), v.getStartDate(), v.getEndDate(), count));
        }
        sb.append("\nTotal Vacations: ").append(vacations.size()).append('\n');
        return sb.toString();
    }

    public String generateExcursionReport() {
        List<Excursion> excursions = db.excursionDao().getAllExcursions();
        StringBuilder sb = new StringBuilder();
        sb.append("EXCURSION REPORT\n");
        sb.append("Generated: ").append(timestampFormat.format(new Date())).append("\n");
        sb.append("==================================================\n\n");
        sb.append(String.format(Locale.US, "%-4s %-26s %-12s %-18s\n", "ID", "TITLE", "DATE", "VACATION"));
        sb.append("---------------------------------------------------------------------\n");
        for (Excursion e : excursions) {
            Vacation v = db.vacationDao().getVacationById(e.getVacationId());
            sb.append(String.format(Locale.US, "%-4d %-26s %-12s %-18s\n",
                    e.getId(), truncate(e.getTitle(),26), e.getDate(), v != null ? truncate(v.getTitle(),18) : "N/A"));
        }
        sb.append("\nTotal Excursions: ").append(excursions.size()).append('\n');
        return sb.toString();
    }

    public String generateSummaryReport() {
        List<Vacation> vacations = db.vacationDao().getAllVacations();
        StringBuilder sb = new StringBuilder();
        sb.append("SUMMARY REPORT\n");
        sb.append("Generated: ").append(timestampFormat.format(new Date())).append("\n");
        sb.append("==================================================\n\n");
        int totalExcursions = 0;
        for (Vacation v : vacations) {
            totalExcursions += db.vacationDao().countExcursionsForVacation(v.getId());
        }
        sb.append("Total Vacations: ").append(vacations.size()).append('\n');
        sb.append("Total Excursions: ").append(totalExcursions).append('\n');
        sb.append('\n');
        sb.append("VACATIONS:\n");
        sb.append("-----------\n");
        for (Vacation v : vacations) {
            int count = db.vacationDao().countExcursionsForVacation(v.getId());
            sb.append(String.format(Locale.US, "• %s (%s - %s) - %d excursions\n", v.getTitle(), v.getStartDate(), v.getEndDate(), count));
        }
        return sb.toString();
    }

    public void saveReport(String title, String content, String type) {
        String timestamp = timestampFormat.format(new Date());
        Report report = new Report(title, content, timestamp, type);
        db.reportDao().insert(report);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0,max-3)) + "...";
    }
}

