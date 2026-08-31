package gr.hua.dit.moddrugmanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import gr.hua.dit.moddrugmanager.R;
import gr.hua.dit.moddrugmanager.data.AppDatabase;
import gr.hua.dit.moddrugmanager.provider.ContentResolverTestHelper;

/**
 * Main menu of the application.
 * Requirement (G): Includes a button to test the Content Provider on request.
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

        // Requirement (G): Trigger Content Provider CRUD test on request
        findViewById(R.id.btnTestProvider).setOnClickListener(v -> onTestProviderClicked());
    }

    private void onTestProviderClicked() {
        Toast.makeText(this, "Running Content Provider Test... Check Logcat", Toast.LENGTH_SHORT).show();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                ContentResolverTestHelper.runFullCrudTest(getApplicationContext());
                runOnUiThread(() -> Toast.makeText(this, "Content Provider Test Completed!", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e(TAG, "Content Provider Test Failed", e);
                runOnUiThread(() -> Toast.makeText(this, "Test Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
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
