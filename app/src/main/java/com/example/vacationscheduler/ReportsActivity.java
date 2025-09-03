package com.example.vacationscheduler;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.vacationscheduler.database.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {
    private AppDatabase db;
    private ReportGenerator generator;
    private TextView reportContent;
    private String currentReportText = "";
    private String currentReportType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "vacation-database")
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .build();
        generator = new ReportGenerator(db);

        reportContent = findViewById(R.id.reportContentTextView);
        reportContent.setTypeface(Typeface.MONOSPACE);

        Button vacationBtn = findViewById(R.id.vacReportButton);
        Button excursionBtn = findViewById(R.id.excReportButton);
        Button summaryBtn = findViewById(R.id.summaryReportButton);
        Button saveBtn = findViewById(R.id.saveReportButton);
        Button shareBtn = findViewById(R.id.shareReportButton);

        vacationBtn.setOnClickListener(v -> generateAsync("VACATION"));
        excursionBtn.setOnClickListener(v -> generateAsync("EXCURSION"));
        summaryBtn.setOnClickListener(v -> generateAsync("SUMMARY"));
        saveBtn.setOnClickListener(v -> saveReport());
        shareBtn.setOnClickListener(v -> shareReport());
    }

    private void generateAsync(String type) {
        final String requestedType = type;
        new Thread(() -> {
            String text;
            String effectiveType = requestedType;
            switch (effectiveType) {
                case "VACATION":
                    text = generator.generateVacationReport();
                    break;
                case "EXCURSION":
                    text = generator.generateExcursionReport();
                    break;
                default:
                    text = generator.generateSummaryReport();
                    effectiveType = "SUMMARY";
            }
            final String finalType = effectiveType;
            final String finalText = text;
            runOnUiThread(() -> {
                currentReportText = finalText;
                currentReportType = finalType;
                reportContent.setText(finalText);
            });
        }).start();
    }

    private void saveReport() {
        if (currentReportText.isEmpty()) {
            Toast.makeText(this, R.string.no_report_generated, Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            String title = currentReportType + " Report - " + new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(new Date());
            generator.saveReport(title, currentReportText, currentReportType);
            runOnUiThread(() -> Toast.makeText(this, R.string.report_saved, Toast.LENGTH_SHORT).show());
        }).start();
    }

    private void shareReport() {
        if (currentReportText.isEmpty()) {
            Toast.makeText(this, R.string.no_report_generated, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentReportType + " Report");
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentReportText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_report)));
    }
}
