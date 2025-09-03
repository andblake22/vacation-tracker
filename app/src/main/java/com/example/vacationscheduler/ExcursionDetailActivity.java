package com.example.vacationscheduler;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.example.vacationscheduler.database.AppDatabase;
import com.example.vacationscheduler.entities.Excursion;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExcursionDetailActivity extends AppCompatActivity {
    private EditText titleEditText, dateEditText;
    private AppDatabase db;
    private Excursion excursion;
    private int vacationId;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excursion_detail);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "vacation-database").build();

        titleEditText = findViewById(R.id.excursionTitleEditText);
        dateEditText = findViewById(R.id.excursionDateEditText);

        vacationId = getIntent().getIntExtra("vacationId", -1);
        int excursionId = getIntent().getIntExtra("excursionId", -1);
        if (excursionId != -1) {
            loadExcursion(excursionId);
        }

        findViewById(R.id.saveExcursionButton).setOnClickListener(v -> saveExcursion());
        findViewById(R.id.deleteExcursionButton).setOnClickListener(v -> deleteExcursion());
    }

    private void loadExcursion(int excursionId) {
        new Thread(() -> {
            excursion = db.excursionDao().getExcursionsForVacation(vacationId).stream()
                    .filter(e -> e.getId() == excursionId)
                    .findFirst()
                    .orElse(null);
            runOnUiThread(() -> {
                if (excursion != null) {
                    titleEditText.setText(excursion.getTitle());
                    dateEditText.setText(excursion.getDate());
                }
            });
        }).start();
    }

    private boolean isValidDate(String date) {
        try {
            dateFormat.setLenient(false);
            dateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void saveExcursion() {
        String title = titleEditText.getText().toString();
        String date = dateEditText.getText().toString();

        if (title.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDate(date)) {
            Toast.makeText(this, "Invalid date format (use MM/DD/YYYY)", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            if (vacationId == -1 || !db.vacationDao().exists(vacationId)) {
                runOnUiThread(() -> Toast.makeText(this, "Invalid vacation ID. Please select a valid vacation.", Toast.LENGTH_SHORT).show());
                return;
            }

            Excursion newExcursion = new Excursion(vacationId, title, date);
            if (excursion != null) {
                newExcursion.setId(excursion.getId());
            }

            if (excursion == null) {
                db.excursionDao().insert(newExcursion);
            } else {
                db.excursionDao().update(newExcursion);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Excursion saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void deleteExcursion() {
        if (excursion == null) return;

        new Thread(() -> {
            db.excursionDao().delete(excursion);
            runOnUiThread(() -> {
                Toast.makeText(this, "Excursion deleted", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}
