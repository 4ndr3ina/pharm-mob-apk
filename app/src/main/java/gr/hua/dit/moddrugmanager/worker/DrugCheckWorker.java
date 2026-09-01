package gr.hua.dit.moddrugmanager.worker;

import android.content.Context;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import gr.hua.dit.moddrugmanager.data.AppDatabase;
import gr.hua.dit.moddrugmanager.data.PrescriptionDrug;

public class DrugCheckWorker extends Worker {

    private static final String TAG = "DrugCheckWorker";

    public DrugCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Periodic check started");

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        long todayStart = startOfDay(System.currentTimeMillis());

        List<PrescriptionDrug> allDrugs = db.prescriptionDrugDao().getAllSync();
        int updatedCount = 0;

        for (PrescriptionDrug drug : allDrugs) {
            long drugStart = startOfDay(drug.getStartDate());
            long drugEnd = startOfDay(drug.getEndDate());

            boolean newIsActive = (todayStart >= drugStart) && (todayStart <= drugEnd);

            boolean newHasReceivedToday;
            Long lastReceived = drug.getLastDateReceived();
            if (lastReceived == null) {
                newHasReceivedToday = false;
            } else {
                newHasReceivedToday = (startOfDay(lastReceived) == todayStart);
            }

            boolean changed = (newIsActive != drug.isActive())
                    || (newHasReceivedToday != drug.isHasReceivedToday());

            if (changed) {
                db.prescriptionDrugDao().updateComputedFields(drug.getUid(), newIsActive, newHasReceivedToday);
                updatedCount++;
                Log.d(TAG, "Updated UID=" + drug.getUid() + " (" + drug.getShortName() + "): "
                        + "isActive " + drug.isActive() + "->" + newIsActive + ", "
                        + "hasReceivedToday " + drug.isHasReceivedToday() + "->" + newHasReceivedToday);
            }
        }

        Log.d(TAG, "Periodic check finished. Checked=" + allDrugs.size() + ", updated=" + updatedCount);
        return Result.success();
    }

    private long startOfDay(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
