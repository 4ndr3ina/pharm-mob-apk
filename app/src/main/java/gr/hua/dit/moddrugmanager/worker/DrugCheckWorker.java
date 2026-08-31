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

/**
 * Requirement (C): periodically checks every Prescription Drug and recomputes
 * two system-managed fields, exactly as described in Comment #3:
 *
 * - isActive: true if today is on/after startDate AND on/before endDate.
 * - hasReceivedToday: true only if lastDateReceived falls on today's date;
 *   otherwise false (a new day always resets it).
 *
 * All comparisons are done at day precision (time-of-day stripped), since a
 * user might open the app at any time and dates are stored as whole days.
 *
 * Runs on a background thread automatically (Worker.doWork() is already
 * off the main thread), so direct DB access here is safe.
 */
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

    // Strips the time-of-day portion so date comparisons only look at the calendar day.
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