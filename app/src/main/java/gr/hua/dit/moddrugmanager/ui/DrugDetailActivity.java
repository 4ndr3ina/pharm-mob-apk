package gr.hua.dit.moddrugmanager.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import gr.hua.dit.moddrugmanager.R;
import gr.hua.dit.moddrugmanager.data.AppDatabase;
import gr.hua.dit.moddrugmanager.data.PrescriptionDrug;
import gr.hua.dit.moddrugmanager.data.PrescriptionDrugWithTimeTerms;

/**
 * Requirement (D)/Comment #4: the detail screen shown after tapping an item in
 * the overview list. Displays every recorded field for one Prescription Drug.
 * Updated to support multiple Time Terms per drug.
 */
public class DrugDetailActivity extends AppCompatActivity {

    private static final String TAG = "DrugDetailActivity";
    private static final String STUDENT_FULL_NAME = "Andriani Koui";

    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private AppDatabase db;
    private int uid;

    private TextView tvName, tvUid, tvDescription, tvStartDate, tvEndDate, tvTimeTerm,
            tvDoctorName, tvDoctorLocation, tvIsActive, tvHasReceivedToday, tvLastDateReceived;
    private android.widget.Button btnMarkReceived, btnViewOnMap;

    private PrescriptionDrug currentDrug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_detail);

        db = AppDatabase.getInstance(getApplicationContext());
        uid = getIntent().getIntExtra(OverviewActivity.EXTRA_UID, -1);

        TextView tvFullName = findViewById(R.id.tvAndrianiKoui);
        if (tvFullName != null) tvFullName.setText(STUDENT_FULL_NAME);

        tvName = findViewById(R.id.tvName);
        tvUid = findViewById(R.id.tvUid);
        tvDescription = findViewById(R.id.tvDescription);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvTimeTerm = findViewById(R.id.tvTimeTerm);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDoctorLocation = findViewById(R.id.tvDoctorLocation);
        tvIsActive = findViewById(R.id.tvIsActive);
        tvHasReceivedToday = findViewById(R.id.tvHasReceivedToday);
        tvLastDateReceived = findViewById(R.id.tvLastDateReceived);
        btnMarkReceived = findViewById(R.id.btnMarkReceived);
        btnViewOnMap = findViewById(R.id.btnViewOnMap);

        if (uid == -1) {
            Log.d(TAG, "No UID passed to detail screen, closing");
            finish();
            return;
        }

        loadDrug();

        btnMarkReceived.setOnClickListener(v -> onMarkReceivedClicked());
        btnViewOnMap.setOnClickListener(v -> openInMaps());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            shareMedication();
            return true;
        } else if (id == R.id.action_edit) {
            Toast.makeText(this, "Edit feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_delete) {
            startActivity(new Intent(this, DeleteDrugActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareMedication() {
        if (currentDrug == null) return;
        String shareText = "Medication Details:\n" +
                "Name: " + currentDrug.getShortName() + "\n" +
                "Doctor: " + emptyIfNull(currentDrug.getDoctorName()) + "\n" +
                "Last Taken: " + (currentDrug.getLastDateReceived() != null ? 
                    displayFormat.format(currentDrug.getLastDateReceived()) : "Never");

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share medication via:"));
    }

    private void loadDrug() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            PrescriptionDrugWithTimeTerms item = db.prescriptionDrugDao().getByIdWithTimeTermsSync(uid);
            
            if (item == null || item.drug == null) {
                Log.d(TAG, "Drug with UID=" + uid + " not found");
                runOnUiThread(this::finish);
                return;
            }
            
            currentDrug = item.drug;
            runOnUiThread(() -> bindToViews(item));
            Log.d(TAG, "Loaded detail for UID=" + uid);
        });
    }

    private void bindToViews(PrescriptionDrugWithTimeTerms item) {
        PrescriptionDrug drug = item.drug;
        tvName.setText(drug.getShortName());
        tvUid.setText(String.valueOf(drug.getUid()));
        tvDescription.setText(emptyIfNull(drug.getDescription()));
        tvStartDate.setText(displayFormat.format(drug.getStartDate()));
        tvEndDate.setText(displayFormat.format(drug.getEndDate()));
        
        tvTimeTerm.setText(item.getTimeTermsDisplay());
        
        tvDoctorName.setText(emptyIfNull(drug.getDoctorName()));
        tvDoctorLocation.setText(emptyIfNull(drug.getDoctorLocation()));
        tvIsActive.setText(drug.isActive() ? "Yes" : "No");
        tvHasReceivedToday.setText(drug.isHasReceivedToday() ? "Yes" : "No");
        tvLastDateReceived.setText(drug.getLastDateReceived() != null
                ? displayFormat.format(drug.getLastDateReceived())
                : "Never");

        boolean hasLocation = !TextUtils.isEmpty(drug.getDoctorLocation()) || drug.getDoctorLatitude() != null;
        btnViewOnMap.setVisibility(hasLocation ? View.VISIBLE : View.GONE);

        // Hide "Mark Received" if the medication is not active or if it has already been taken today
        btnMarkReceived.setVisibility(drug.isActive() && !drug.isHasReceivedToday() ? View.VISIBLE : View.GONE);
    }

    private String emptyIfNull(String value) {
        return TextUtils.isEmpty(value) ? "-" : value;
    }

    private void openInMaps() {
        if (currentDrug == null) return;

        Double lat = currentDrug.getDoctorLatitude();
        Double lng = currentDrug.getDoctorLongitude();
        String address = currentDrug.getDoctorLocation();

        if (lat == null && lng == null && TextUtils.isEmpty(address)) {
            Toast.makeText(this, "No location data available for this doctor", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri geoUri;
        String label = Uri.encode(currentDrug.getDoctorName() != null ? currentDrug.getDoctorName() : "Doctor");
        if (lat != null && lng != null) {
            // geo:lat,lng?q=lat,lng(label)
            geoUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(" + label + ")");
        } else {
            // geo:0,0?q=address
            geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        }

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);
        // Do NOT setPackage("com.google.android.apps.maps") to allow other map apps to respond
        try {
            startActivity(mapIntent);
        } catch (android.content.ActivityNotFoundException e) {
            Log.d(TAG, "No map app found, falling back to browser");
            Uri webUri = (lat != null && lng != null)
                    ? Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng)
                    : Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address));
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    private void onMarkReceivedClicked() {
        if (uid == -1) return;
        long now = System.currentTimeMillis();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.prescriptionDrugDao().markAsReceived(uid, now);
            runOnUiThread(() -> {
                Toast.makeText(this, "Marked as received today", Toast.LENGTH_SHORT).show();
                loadDrug();
            });
        });
    }
}
