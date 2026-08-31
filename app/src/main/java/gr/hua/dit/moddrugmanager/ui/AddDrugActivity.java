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

import androidx.appcompat.app.AppCompatActivity;

import gr.hua.dit.moddrugmanager.R;
import gr.hua.dit.moddrugmanager.data.AppDatabase;
import gr.hua.dit.moddrugmanager.data.Doctor;
import gr.hua.dit.moddrugmanager.data.PrescriptionDrug;
import gr.hua.dit.moddrugmanager.data.TimeTerm;

/**
 * Requirement (A): Record a new Prescription Drug.
 * Supports multiple Time Terms (Checkboxes) and a Doctor Spinner.
 * Uses LiveData to observe TimeTerms, avoiding race conditions during initial seeding.
 *
 * NEW: selecting a saved Doctor reveals a "VIEW ON MAP" button so the user can
 * confirm the doctor's location is correct BEFORE saving the medication.
 */
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
    private Doctor selectedDoctor = null; // kept in sync with the spinner selection

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

        // Fail loudly instead of silently doing nothing, so a layout/ID
        // mismatch shows up immediately in Logcat instead of looking like
        // "the feature doesn't work" with no clue why.
        if (timeTermCheckboxContainer == null) {
            Log.e(TAG, "timeTermCheckboxContainer is NULL - check that activity_add_drug.xml " +
                    "has a view with android:id=\"@+id/timeTermCheckboxContainer\"");
        }

        btnStartDate.setOnClickListener(v -> pickDate(true));
        btnEndDate.setOnClickListener(v -> pickDate(false));
        btnSave.setOnClickListener(v -> onSaveClicked());

        if (btnAddNewDoctor != null) {
            btnAddNewDoctor.setOnClickListener(v -> {
                Log.d(TAG, "Navigating to AddDoctorActivity");
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
                    Log.d(TAG, "Doctor selected: " + selectedDoctor.getName());
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

    // SAFETY NET: whatever is/isn't working inside AppDatabase's own seeding
    // logic, this guarantees the 9 fixed TimeTerm rows exist by the time this
    // screen is used. Uses Room's normal insert() (not raw SQL), which is
    // guaranteed to notify the LiveData observer in observeTimeTerms() the
    // moment new rows land - so the checkboxes will appear automatically.
    private void ensureTimeTermsExist() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int existing = db.timeTermDao().count();
            Log.d(TAG, "TimeTerm row count check: " + existing);
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
            Log.d(TAG, "ensureTimeTermsExist(): inserted " + seed.size() + " TimeTerm rows via DAO");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDoctorsIntoSpinner();
    }

    private void observeTimeTerms() {
        // LiveData ensures the UI updates as soon as the database is seeded,
        // even if the checkboxes are requested before seeding finishes.
        db.timeTermDao().getAll().observe(this, terms -> {
            if (timeTermCheckboxContainer == null) return;

            if (terms == null || terms.isEmpty()) {
                Log.d(TAG, "Waiting for TimeTerms to be populated...");
                return;
            }

            Log.d(TAG, "Populating " + terms.size() + " time term checkboxes");
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
                cb.setMinHeight(64);
                cb.setClickable(true);
                cb.setFocusable(true);
                cb.setEnabled(true);
                final TimeTerm capturedTerm = term;
                cb.setOnClickListener(v -> {
                    boolean nowChecked = ((CheckBox) v).isChecked();
                    Log.d(TAG, "Checkbox CLICKED '" + capturedTerm.getTermName() + "' -> " + nowChecked);
                });
                cb.setOnCheckedChangeListener((buttonView, isChecked) ->
                        Log.d(TAG, "Checkbox '" + capturedTerm.getTermName() + "' -> " + isChecked));

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
                List<String> names = new ArrayList<>();
                names.add("None"); // Default option
                if (doctors != null) {
                    for (Doctor d : doctors) names.add(d.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDoctor.setAdapter(adapter);
                Log.d(TAG, "Loaded " + doctors.size() + " saved doctors into spinner");
            });
        });
    }

    // Opens the currently-selected doctor's location on the map, using their
    // saved coordinates if available (exact pin), or their address as a text
    // search otherwise. Launches directly and catches failure instead of
    // pre-checking with resolveActivity(), which can incorrectly report "no
    // app found" on Android 11+ due to package-visibility restrictions.
    private void openSelectedDoctorOnMap() {
        if (selectedDoctor == null) {
            Toast.makeText(this, "Please select a doctor first", Toast.LENGTH_SHORT).show();
            return;
        }

        Double lat = selectedDoctor.getLatitude();
        Double lng = selectedDoctor.getLongitude();
        String address = selectedDoctor.getAddress();

        Uri geoUri;
        if (lat != null && lng != null) {
            geoUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng
                    + "(" + Uri.encode(selectedDoctor.getName()) + ")");
            Log.d(TAG, "Opening map at exact coordinates for " + selectedDoctor.getName()
                    + " (" + lat + ", " + lng + ")");
        } else {
            geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
            Log.d(TAG, "Opening map via text search for " + selectedDoctor.getName());
        }

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, geoUri));
            return;
        } catch (android.content.ActivityNotFoundException e) {
            Log.d(TAG, "No geo: URI handler installed, falling back to browser");
        }

        try {
            Uri webUri = (lat != null && lng != null)
                    ? Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng)
                    : Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address));
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "No app available to show the map", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickDate(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
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
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void onSaveClicked() {
        String shortName = etShortName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (shortName.isEmpty() || startDateMillis == null || endDateMillis == null) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endDateMillis <= startDateMillis) {
            Toast.makeText(this, "End Date must be after Start Date", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> selectedIds = new ArrayList<>();
        for (CheckBox cb : timeTermCheckboxes) {
            if (cb.isChecked()) selectedIds.add((Integer) cb.getTag());
        }

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Please select at least one time", Toast.LENGTH_SHORT).show();
            return;
        }

        int docPos = spinnerDoctor.getSelectedItemPosition();
        String docName = null, docLoc = null;
        Double docLat = null, docLng = null;
        if (docPos > 0 && doctors != null && docPos <= doctors.size()) {
            Doctor d = doctors.get(docPos - 1);
            docName = d.getName();
            docLoc = d.getAddress();
            docLat = d.getLatitude();
            docLng = d.getLongitude();
        }

        PrescriptionDrug drug = new PrescriptionDrug(
                shortName,
                description.isEmpty() ? null : description,
                startDateMillis,
                endDateMillis,
                docName,
                docLoc,
                docLat,
                docLng
        );

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
