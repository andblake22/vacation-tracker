package com.example.vacationscheduler;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.vacationscheduler.adaptors.VacationAdapter;
import com.example.vacationscheduler.database.AppDatabase;
import com.example.vacationscheduler.entities.Vacation;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 100;
    private RecyclerView vacationList;
    private VacationAdapter adapter;
    private AppDatabase db;
    private List<Vacation> vacations = new ArrayList<>();
    private EditText vacationSearchEditText;
    private Button vacationSearchButton;
    private Button clearVacationSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "vacation-database")
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .build();

        vacationList = findViewById(R.id.vacationList);
        vacationList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VacationAdapter(vacations, vacation -> {
            Intent intent = new Intent(MainActivity.this, VacationDetailActivity.class);
            intent.putExtra("vacationId", vacation.getId());
            startActivity(intent);
        });
        vacationList.setAdapter(adapter);

        vacationSearchEditText = findViewById(R.id.vacationSearchEditText);
        vacationSearchButton = findViewById(R.id.vacationSearchButton);
        clearVacationSearchButton = findViewById(R.id.clearVacationSearchButton);

        vacationSearchButton.setOnClickListener(v -> performVacationSearch());
        clearVacationSearchButton.setOnClickListener(v -> {
            vacationSearchEditText.setText("");
            loadVacations();
        });

        findViewById(R.id.addVacationButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VacationDetailActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.reportsButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportsActivity.class);
            startActivity(intent);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        loadVacations();
    }

    private void performVacationSearch() {
        String query = vacationSearchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            loadVacations();
            return;
        }
        new Thread(() -> {
            List<Vacation> results = db.vacationDao().searchVacations(query);
            runOnUiThread(() -> adapter.setVacations(results));
        }).start();
    }

    private void loadVacations() {
        new Thread(() -> {
            vacations = db.vacationDao().getAllVacations();
            runOnUiThread(() -> adapter.setVacations(vacations));
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadVacations();
            } else {
                Toast.makeText(this, "Notification permission denied. Alerts will not be shown.", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadVacations();
    }
}