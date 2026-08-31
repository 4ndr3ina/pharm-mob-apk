package gr.hua.dit.moddrugmanager.ui;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import gr.hua.dit.moddrugmanager.R;
import gr.hua.dit.moddrugmanager.data.AppDatabase;
import gr.hua.dit.moddrugmanager.data.Doctor;

/**
 * Saves a reusable Doctor (name + address).
 */
public class AddDoctorActivity extends AppCompatActivity {

    private static final String TAG = "AddDoctorActivity";
    private static final String STUDENT_FULL_NAME = "Andriani Koui";

    private EditText etDoctorName, etDoctorAddress;
    private TextView tvGeocodeStatus;
    private Button btnSaveDoctor;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_doctor);

        db = AppDatabase.getInstance(getApplicationContext());

        // Safety check to prevent crash if layout ID is not found
        TextView tvFullName = findViewById(R.id.tvAndrianiKoui);
        if (tvFullName != null) {
            tvFullName.setText(STUDENT_FULL_NAME);
        }

        etDoctorName = findViewById(R.id.etDoctorName);
        etDoctorAddress = findViewById(R.id.etDoctorAddress);
        tvGeocodeStatus = findViewById(R.id.tvGeocodeStatus);
        btnSaveDoctor = findViewById(R.id.btnSaveDoctor);

        btnSaveDoctor.setOnClickListener(v -> onSaveClicked());
    }

    private void onSaveClicked() {
        String name = etDoctorName.getText().toString().trim();
        String address = etDoctorAddress.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        tvGeocodeStatus.setText("Looking up location...");
        btnSaveDoctor.setEnabled(false);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            Double lat = null, lng = null;
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(address, 1);
                if (results != null && !results.isEmpty()) {
                    lat = results.get(0).getLatitude();
                    lng = results.get(0).getLongitude();
                }
            } catch (Exception e) {
                Log.e(TAG, "Geocoding failed", e);
            }

            Doctor doctor = new Doctor(name, address, lat, lng);
            db.doctorDao().insert(doctor);

            runOnUiThread(() -> {
                Toast.makeText(this, "Doctor Saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
