package gr.hua.dit.moddrugmanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import gr.hua.dit.moddrugmanager.R;
import gr.hua.dit.moddrugmanager.data.AppDatabase;

/**
 * Main menu of the application.
 * Removed active medications list from here, moving it to AllPrescriptionsActivity.
 */
public class OverviewActivity extends AppCompatActivity {

    private static final String TAG = "OverviewActivity";
    private static final String STUDENT_FULL_NAME = "Andriani Koui";

    public static final String EXTRA_UID = "extra_uid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        TextView tvFullName = findViewById(R.id.tvAndrianiKoui);
        if (tvFullName != null) tvFullName.setText(STUDENT_FULL_NAME);

        findViewById(R.id.btnViewAllPrescriptions).setOnClickListener(v ->
                startActivity(new Intent(this, AllPrescriptionsActivity.class)));

        findViewById(R.id.btnAddNew).setOnClickListener(v ->
                startActivity(new Intent(this, AddDrugActivity.class)));

        findViewById(R.id.btnDeleteMedication).setOnClickListener(v ->
                startActivity(new Intent(this, DeleteDrugActivity.class)));

        findViewById(R.id.btnExport).setOnClickListener(v -> onExportClicked());
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
