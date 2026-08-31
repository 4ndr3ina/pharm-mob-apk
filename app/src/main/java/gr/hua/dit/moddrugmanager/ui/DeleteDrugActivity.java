package gr.hua.dit.moddrugmanager.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import gr.hua.dit.moddrugmanager.R;
import gr.hua.dit.moddrugmanager.data.AppDatabase;
import gr.hua.dit.moddrugmanager.data.PrescriptionDrugWithTimeTerms;
/**
 * Requirement (B): Delete an existing Prescription Drug.
 * Shows a list of all medications (UID, Short Name, Time-Term - Comment #4 style).
 * Tapping one asks for confirmation, then deletes it and informs the user via a
 * pop-up how many rows were affected.
 */
public class DeleteDrugActivity extends AppCompatActivity {

    private static final String TAG = "DeleteDrugActivity";
    private static final String STUDENT_FULL_NAME = "Full Name Here";

    private AppDatabase db;
    private MedicationAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_drug);

        db = AppDatabase.getInstance(getApplicationContext());

        TextView tvFullName = findViewById(R.id.tvAndrianiKoui);
        tvFullName.setText(STUDENT_FULL_NAME);

        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        adapter = new MedicationAdapter(this::onMedicationClicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // LiveData automatically refreshes the list whenever the DB changes
        // (e.g. right after a delete), so we don't need to manually re-query.
        db.prescriptionDrugDao().getAllWithTimeTerms().observe(this, this::onListChanged);
    }

    private void onListChanged(List<PrescriptionDrugWithTimeTerms> list) {
        adapter.setItems(list);
        boolean isEmpty = (list == null || list.isEmpty());
        tvEmptyMessage.setVisibility(isEmpty ? android.view.View.VISIBLE : android.view.View.GONE);
        recyclerView.setVisibility(isEmpty ? android.view.View.GONE : android.view.View.VISIBLE);
        Log.d(TAG, "Medication list updated, count=" + (list == null ? 0 : list.size()));
    }

    private void onMedicationClicked(PrescriptionDrugWithTimeTerms item) {
        String name = item.drug.getShortName();
        int uid = item.drug.getUid();

        new AlertDialog.Builder(this)
                .setTitle("Delete Medication")
                .setMessage("Are you sure you want to delete \"" + name + "\" (ID: " + uid + ")?")
                .setPositiveButton("Yes, Delete", (dialog, which) -> performDelete(uid))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDelete(int uid) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int rowsAffected = db.prescriptionDrugDao().deleteById(uid);
            Log.d(TAG, "Delete requested for UID=" + uid + ", rowsAffected=" + rowsAffected);

            runOnUiThread(() -> {
                String message = (rowsAffected > 0)
                        ? "Deleted successfully.\nRows affected: " + rowsAffected
                        : "Nothing was deleted.\nRows affected: " + rowsAffected;
                new AlertDialog.Builder(this)
                        .setTitle("Delete Result")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show();
            });
        });
    }
}