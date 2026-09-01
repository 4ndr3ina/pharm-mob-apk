package gr.hua.dit.moddrugmanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import gr.hua.dit.moddrugmanager.R;
import gr.hua.dit.moddrugmanager.data.AppDatabase;
import gr.hua.dit.moddrugmanager.data.PrescriptionDrugWithTimeTerms;

public class AllPrescriptionsActivity extends AppCompatActivity {

    private AppDatabase db;
    private MedicationAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;
    private LiveData<List<PrescriptionDrugWithTimeTerms>> currentLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_prescriptions);

        db = AppDatabase.getInstance(getApplicationContext());

        TextView tvFullName = findViewById(R.id.tvAndrianiKoui);
        tvFullName.setText("Andriani Koui");

        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        adapter = new MedicationAdapter(this::onMedicationClicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        long todayStart = getTodayStart();

        findViewById(R.id.btnFilterActive).setOnClickListener(v -> 
                observeData(db.prescriptionDrugDao().getActivePrescriptions()));
        
        findViewById(R.id.btnFilterFuture).setOnClickListener(v -> 
                observeData(db.prescriptionDrugDao().getFuturePrescriptions(todayStart)));
        
        findViewById(R.id.btnFilterExpired).setOnClickListener(v -> 
                observeData(db.prescriptionDrugDao().getExpiredPrescriptions(todayStart)));
        
        findViewById(R.id.btnFilterTakenToday).setOnClickListener(v -> 
                observeData(db.prescriptionDrugDao().getTakenTodayPrescriptions()));

        observeData(db.prescriptionDrugDao().getActivePrescriptions());
    }

    private long getTodayStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private void observeData(LiveData<List<PrescriptionDrugWithTimeTerms>> liveData) {
        if (currentLiveData != null) {
            currentLiveData.removeObservers(this);
        }
        currentLiveData = liveData;
        currentLiveData.observe(this, list -> {
            adapter.setItems(list);
            boolean isEmpty = (list == null || list.isEmpty());
            tvEmptyMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    private void onMedicationClicked(PrescriptionDrugWithTimeTerms item) {
        Intent intent = new Intent(this, DrugDetailActivity.class);
        intent.putExtra(OverviewActivity.EXTRA_UID, item.drug.getUid());
        startActivity(intent);
    }
}
