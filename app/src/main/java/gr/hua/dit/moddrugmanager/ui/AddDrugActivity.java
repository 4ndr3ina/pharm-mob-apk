package gr.hua.dit.moddrugmanager.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import gr.hua.dit.moddrugmanager.R;
import gr.hua.dit.moddrugmanager.data.AppDatabase;
import gr.hua.dit.moddrugmanager.data.Doctor;
import gr.hua.dit.moddrugmanager.data.PrescriptionDrug;
import gr.hua.dit.moddrugmanager.data.TimeTerm;

public class AddDrugActivity extends AppCompatActivity {

    private static final String TAG = "AddDrugActivity";
    private static final String STUDENT_FULL_NAME = "Andriani Koui";

    private EditText etShortName, etDescription;
    private TextView tvStartDate, tvEndDate, tvFullName;
    private LinearLayout timeTermCheckboxContainer;
    private Spinner spinnerDoctor;
    private Button btnStartDate, btnEndDate, btnSave, btnAddNewDoctor, btnViewDoctorOnMap;

    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private Long startDateMillis = null;
    private Long endDateMillis = null;

    private final List<CheckBox> timeTermCheckboxes = new ArrayList<>();
    private List<Doctor> doctors = new ArrayList<>();
    private Doctor selectedDoctor = null; 
    
    private boolean shouldReloadDoctors = false;
    private int restoredDoctorId = -1;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_drug);

        db = AppDatabase.getInstance(getApplicationContext());

        tvFullName = findViewById(R.id.tvAndrianiKoui);
        if (tvFullName != null) tvFullName.setText(STUDENT_FULL_NAME);

        etShortName = findViewById(R.id.etShortName);
        etDescription = findViewById(R.id.etDescription);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);

        timeTermCheckboxContainer = findViewById(R.id.timeTermCheckboxContainer);
        spinnerDoctor = findViewById(R.id.spinnerDoctor);
        btnAddNewDoctor = findViewById(R.id.btnAddNewDoctor);
        btnViewDoctorOnMap = findViewById(R.id.btnViewDoctorOnMap);

        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnSave = findViewById(R.id.btnSave);

        if (savedInstanceState != null) {
            restoredDoctorId = savedInstanceState.getInt("selected_doctor_id", -1);
            shouldReloadDoctors = savedInstanceState.getBoolean("should_reload", false);
            if (savedInstanceState.containsKey("start_date")) {
                startDateMillis = savedInstanceState.getLong("start_date");
                tvStartDate.setText("Start Date: " + displayFormat.format(startDateMillis));
            }
            if (savedInstanceState.containsKey("end_date")) {
                endDateMillis = savedInstanceState.getLong("end_date");
                tvEndDate.setText("End Date: " + displayFormat.format(endDateMillis));
            }
        }

        if (timeTermCheckboxContainer == null) {
            Log.e(TAG, "timeTermCheckboxContainer is NULL");
        }

        btnStartDate.setOnClickListener(v -> pickDate(true));
        btnEndDate.setOnClickListener(v -> pickDate(false));
        btnSave.setOnClickListener(v -> onSaveClicked());

        if (btnAddNewDoctor != null) {
            btnAddNewDoctor.setOnClickListener(v -> {
                shouldReloadDoctors = true;
                startActivity(new Intent(this, AddDoctorActivity.class));
            });
        }

        if (btnViewDoctorOnMap != null) {
            btnViewDoctorOnMap.setOnClickListener(v -> openSelectedDoctorOnMap());
        }

        spinnerDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && doctors != null && position <= doctors.size()) {
                    selectedDoctor = doctors.get(position - 1);
                    if (btnViewDoctorOnMap != null) btnViewDoctorOnMap.setVisibility(View.VISIBLE);
                } else {
                    selectedDoctor = null;
                    if (btnViewDoctorOnMap != null) btnViewDoctorOnMap.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDoctor = null;
                if (btnViewDoctorOnMap != null) btnViewDoctorOnMap.setVisibility(View.GONE);
            }
        });

        observeTimeTerms();
        loadDoctorsIntoSpinner();
        ensureTimeTermsExist();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedDoctor != null) {
            outState.putInt("selected_doctor_id", selectedDoctor.getId());
        }
        if (startDateMillis != null) outState.putLong("start_date", startDateMillis);
        if (endDateMillis != null) outState.putLong("end_date", endDateMillis);
        outState.putBoolean("should_reload", shouldReloadDoctors);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldReloadDoctors) {
            loadDoctorsIntoSpinner();
            shouldReloadDoctors = false;
        }
    }

    private void ensureTimeTermsExist() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int existing = db.timeTermDao().count();
            if (existing > 0) return;

            String[] names = {
                    "before-breakfast", "at-breakfast", "after-breakfast",
                    "before-lunch", "at-lunch", "after-lunch",
                    "before-dinner", "at-dinner", "after-dinner"
            };
            List<TimeTerm> seed = new ArrayList<>();
            for (int i = 0; i < names.length; i++) {
                seed.add(new TimeTerm(names[i], i));
            }
            db.timeTermDao().insertAll(seed);
        });
    }

    private void observeTimeTerms() {
        db.timeTermDao().getAll().observe(this, terms -> {
            if (timeTermCheckboxContainer == null || terms == null || terms.isEmpty()) return;

            timeTermCheckboxContainer.removeAllViews();
            timeTermCheckboxes.clear();

            for (TimeTerm term : terms) {
                CheckBox cb = new CheckBox(this);
                cb.setText(term.getTermName());
                cb.setTag(term.getId());
                cb.setTextSize(18);
                cb.setTextColor(getColor(R.color.text_dark));
                cb.setButtonTintList(
                        android.content.res.ColorStateList.valueOf(getColor(R.color.dark_blue)));
                cb.setPadding(8, 16, 8, 16);
                timeTermCheckboxContainer.addView(cb);
                timeTermCheckboxes.add(cb);
            }
        });
    }

    private void loadDoctorsIntoSpinner() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            doctors = db.doctorDao().getAllSync();
            runOnUiThread(() -> {
                if (spinnerDoctor == null) return;
                
                int idToRestore = (selectedDoctor != null) ? selectedDoctor.getId() : restoredDoctorId;
                
                List<String> names = new ArrayList<>();
                names.add("None");
                if (doctors != null) {
                    for (Doctor d : doctors) names.add(d.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDoctor.setAdapter(adapter);

                if (idToRestore != -1 && doctors != null) {
                    for (int i = 0; i < doctors.size(); i++) {
                        if (doctors.get(i).getId() == idToRestore) {
                            spinnerDoctor.setSelection(i + 1);
                            break;
                        }
                    }
                }
            });
        });
    }

    private void openSelectedDoctorOnMap() {
        if (selectedDoctor == null) return;

        Double lat = selectedDoctor.getLatitude();
        Double lng = selectedDoctor.getLongitude();
        String address = selectedDoctor.getAddress();

        Uri geoUri;
        String label = Uri.encode(selectedDoctor.getName());
        if (lat != null && lng != null) {
            geoUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + label + ")");
        } else {
            geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        }

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
        try {
            startActivity(mapIntent);
        } catch (android.content.ActivityNotFoundException e) {
            Uri webUri = (lat != null && lng != null)
                    ? Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng)
                    : Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address));
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    private void pickDate(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth, 0, 0, 0);
            picked.set(Calendar.MILLISECOND, 0);
            long millis = picked.getTimeInMillis();
            String formatted = displayFormat.format(picked.getTime());
            if (isStart) {
                startDateMillis = millis;
                tvStartDate.setText("Start Date: " + formatted);
            } else {
                endDateMillis = millis;
                tvEndDate.setText("End Date: " + formatted);
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void onSaveClicked() {
        String shortName = etShortName.getText().toString().trim();
        if (shortName.isEmpty() || startDateMillis == null || endDateMillis == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (endDateMillis <= startDateMillis) {
            Toast.makeText(this, "End Date must be after Start Date", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Integer> selectedIds = new ArrayList<>();
        for (CheckBox cb : timeTermCheckboxes) if (cb.isChecked()) selectedIds.add((Integer) cb.getTag());
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Select at least one time", Toast.LENGTH_SHORT).show();
            return;
        }

        PrescriptionDrug drug = new PrescriptionDrug(shortName, etDescription.getText().toString(), startDateMillis, endDateMillis, 
            selectedDoctor != null ? selectedDoctor.getName() : null,
            selectedDoctor != null ? selectedDoctor.getAddress() : null,
            selectedDoctor != null ? selectedDoctor.getLatitude() : null,
            selectedDoctor != null ? selectedDoctor.getLongitude() : null);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.prescriptionDrugDao().insertWithTimeTerms(drug, selectedIds);
            gr.hua.dit.moddrugmanager.worker.DrugCheckTrigger.runNow(getApplicationContext());
            runOnUiThread(() -> {
                Toast.makeText(this, "Saved: " + shortName, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
