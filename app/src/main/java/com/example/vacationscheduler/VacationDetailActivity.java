package com.example.vacationscheduler;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.vacationscheduler.adaptors.ExcursionAdapter;
import com.example.vacationscheduler.database.AppDatabase;
import com.example.vacationscheduler.entities.Excursion;
import com.example.vacationscheduler.entities.Vacation;
import com.example.vacationscheduler.reciever.AlarmReceiver;
import com.example.vacationscheduler.util.DateValidator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VacationDetailActivity extends AppCompatActivity {
    private EditText titleEditText, hotelEditText, startDateEditText, endDateEditText;
    private AppDatabase db;
    private Vacation vacation;
    private RecyclerView excursionList;
    private ExcursionAdapter excursionAdapter;
    private List<Excursion> excursions = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    private EditText excursionSearchEditText;
    private Button excursionSearchButton;
    private Button excursionClearSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_detail);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "vacation-database")
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .build();

        titleEditText = findViewById(R.id.titleEditText);
        hotelEditText = findViewById(R.id.hotelEditText);
        startDateEditText = findViewById(R.id.startDateEditText);
        endDateEditText = findViewById(R.id.endDateEditText);
        excursionList = findViewById(R.id.excursionList);
        excursionSearchEditText = findViewById(R.id.excursionSearchEditText);
        excursionSearchButton = findViewById(R.id.excursionSearchButton);
        excursionClearSearchButton = findViewById(R.id.excursionClearSearchButton);

        excursionList.setLayoutManager(new LinearLayoutManager(this));
        excursionAdapter = new ExcursionAdapter(excursions, excursion -> {
            Intent intent = new Intent(this, ExcursionDetailActivity.class);
            intent.putExtra("excursionId", excursion.getId());
            intent.putExtra("vacationId", excursion.getVacationId());
            startActivity(intent);
        });
        excursionList.setAdapter(excursionAdapter);

        int vacationId = getIntent().getIntExtra("vacationId", -1);
        if (vacationId != -1) {
            loadVacation(vacationId);
        }

        findViewById(R.id.saveButton).setOnClickListener(v -> saveVacation());
        findViewById(R.id.deleteButton).setOnClickListener(v -> deleteVacation());
        findViewById(R.id.shareButton).setOnClickListener(v -> shareVacation());
        findViewById(R.id.addExcursionButton).setOnClickListener(v -> {
            if (vacation == null || vacation.getId() == 0) {
                Toast.makeText(this, "Please save the vacation first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, ExcursionDetailActivity.class);
            intent.putExtra("vacationId", vacationId);
            startActivity(intent);
        });

        excursionSearchButton.setOnClickListener(v -> performExcursionSearch());
        excursionClearSearchButton.setOnClickListener(v -> {
            excursionSearchEditText.setText("");
            reloadExcursions();
        });
    }

    private void loadVacation(int vacationId) {
        new Thread(() -> {
            vacation = db.vacationDao().getAllVacations().stream()
                    .filter(v -> v.getId() == vacationId)
                    .findFirst()
                    .orElse(null);
            excursions = db.excursionDao().getExcursionsForVacation(vacationId);
            runOnUiThread(() -> {
                if (vacation != null) {
                    titleEditText.setText(vacation.getTitle());
                    hotelEditText.setText(vacation.getHotel());
                    startDateEditText.setText(vacation.getStartDate());
                    endDateEditText.setText(vacation.getEndDate());
                    excursionAdapter.setExcursions(excursions);
                }
            });
        }).start();
    }

    private void reloadExcursions() {
        if (vacation == null) return;
        new Thread(() -> {
            List<Excursion> list = db.excursionDao().getExcursionsForVacation(vacation.getId());
            runOnUiThread(() -> excursionAdapter.setExcursions(list));
        }).start();
    }

    private void saveVacation() {
        String title = titleEditText.getText().toString();
        String hotel = hotelEditText.getText().toString();
        String startDate = startDateEditText.getText().toString();
        String endDate = endDateEditText.getText().toString();

        if (title.isEmpty() || hotel.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!DateValidator.isValidDate(startDate) || !DateValidator.isValidDate(endDate)) {
            Toast.makeText(this, "Invalid date format (use MM/DD/YYYY)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!DateValidator.isEndDateAfterStartDate(startDate, endDate)) {
            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
            return;
        }

        Vacation newVacation = new Vacation(title, hotel, startDate, endDate);
        if (vacation != null) {
            newVacation.setId(vacation.getId());
        }

        new Thread(() -> {
            if (vacation == null) {
                db.vacationDao().insert(newVacation);
            } else {
                db.vacationDao().update(newVacation);
            }
            setAlarms(newVacation);
            runOnUiThread(() -> {
                Toast.makeText(this, "Vacation saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void deleteVacation() {
        if (vacation == null) return;

        new Thread(() -> {
            int excursionCount = db.vacationDao().countExcursionsForVacation(vacation.getId());
            if (excursionCount > 0) {
                runOnUiThread(() -> Toast.makeText(this, "Cannot delete vacation with excursions", Toast.LENGTH_SHORT).show());
            } else {
                db.vacationDao().delete(vacation);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Vacation deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void shareVacation() {
        if (vacation == null) return;

        String shareText = "Vacation: " + vacation.getTitle() + "\n" +
                "Hotel: " + vacation.getHotel() + "\n" +
                "Start Date: " + vacation.getStartDate() + "\n" +
                "End Date: " + vacation.getEndDate();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Vacation"));
    }

    private void setAlarms(Vacation vacation) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        try {
            Date startDate = dateFormat.parse(vacation.getStartDate());
            Date endDate = dateFormat.parse(vacation.getEndDate());

            setAlarm(alarmManager, startDate.getTime(), vacation.getId(), "Vacation " + vacation.getTitle() + " starts");
            setAlarm(alarmManager, endDate.getTime(), vacation.getId() + 1, "Vacation " + vacation.getTitle() + " ends");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(AlarmManager alarmManager, long time, int requestCode, String message) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("message", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    private void performExcursionSearch() {
        if (vacation == null) {
            Toast.makeText(this, "Save vacation first", Toast.LENGTH_SHORT).show();
            return;
        }
        String query = excursionSearchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            reloadExcursions();
            return;
        }
        new Thread(() -> {
            List<Excursion> results = db.excursionDao().searchExcursionsForVacation(vacation.getId(), query);
            runOnUiThread(() -> excursionAdapter.setExcursions(results));
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int vacationId = getIntent().getIntExtra("vacationId", -1);
        if (vacationId != -1) {
            reloadExcursions();
        }
    }
}