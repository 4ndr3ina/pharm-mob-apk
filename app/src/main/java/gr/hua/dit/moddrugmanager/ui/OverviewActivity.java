package gr.hua.dit.moddrugmanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import gr.hua.dit.moddrugmanager.R;
import gr.hua.dit.moddrugmanager.data.AppDatabase;
import gr.hua.dit.moddrugmanager.data.PrescriptionDrugWithTimeTerms;

/**
 * Requirement (D): overview of ALL still-active Prescription Drugs, ordered by
 * their Time Term (Comment #2's chronological order: before-breakfast ... after-dinner).
 * Tapping an item opens DrugDetailActivity with the full record (Comment #4).
 *
 * This doubles as the app's natural home screen, with a shortcut to Add (A).
 */
public class OverviewActivity extends AppCompatActivity {

    private static final String TAG = "OverviewActivity";
    private static final String STUDENT_FULL_NAME = "Andriani Koui";

    public static final String EXTRA_UID = "extra_uid";

    private AppDatabase db;
    private MedicationAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        db = AppDatabase.getInstance(getApplicationContext());

        TextView tvFullName = findViewById(R.id.tvAndrianiKoui);
        tvFullName.setText(STUDENT_FULL_NAME);

        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        adapter = new MedicationAdapter(this::onMedicationClicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnAddNew).setOnClickListener(v ->
                startActivity(new Intent(this, AddDrugActivity.class)));

        findViewById(R.id.btnDeleteMedication).setOnClickListener(v ->
                startActivity(new Intent(this, DeleteDrugActivity.class)));

        findViewById(R.id.btnExport).setOnClickListener(v -> onExportClicked());

        // getActiveDrugsOrderedByTime() now returns a list of PrescriptionDrugWithTimeTerms
        db.prescriptionDrugDao().getActiveDrugsOrderedByTime().observe(this, this::onListChanged);
    }

    private void onListChanged(List<PrescriptionDrugWithTimeTerms> list) {
        adapter.setItems(list);
        boolean isEmpty = (list == null || list.isEmpty());
        tvEmptyMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        Log.d(TAG, "Active medication overview updated, count=" + (list == null ? 0 : list.size()));
    }

    private void onMedicationClicked(PrescriptionDrugWithTimeTerms item) {
        Log.d(TAG, "Opening detail screen for UID=" + item.drug.getUid());
        Intent intent = new Intent(this, DrugDetailActivity.class);
        intent.putExtra(EXTRA_UID, item.drug.getUid());
        startActivity(intent);
    }

    // Requirement (F): export all active drugs to an HTML file in Downloads.
    private void onExportClicked() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            gr.hua.dit.moddrugmanager.util.DrugExporter.exportActiveDrugs(
                    getApplicationContext(),
                    (success, message) -> runOnUiThread(() -> {
                        Log.d(TAG, "Export result: success=" + success + ", message=" + message);
                        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
                    })
            );
        });
    }
}