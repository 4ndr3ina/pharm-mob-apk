package gr.hua.dit.moddrugmanager;

import android.app.Application;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import gr.hua.dit.moddrugmanager.worker.DrugCheckWorker;

/**
 * Schedules the periodic background check (requirement C) once, when the app
 * process starts. WorkManager persists the schedule across app restarts and
 * even device reboots, so this only needs to run once per install; the
 * KEEP policy below prevents duplicate/overlapping schedules on every launch.
 */
public class DrugManagerApplication extends Application {

    private static final String TAG = "DrugManagerApp";
    private static final String WORK_NAME = "periodic_drug_check";

    // WorkManager enforces a 15-minute minimum interval for periodic work
    // (battery-saving restriction built into Android, not something we can
    // lower). For a live demo/video, trigger it manually instead - see the
    // "Run Check Now" button we can wire to WorkManager.enqueue with a
    // OneTimeWorkRequest for instant feedback.
    private static final long INTERVAL_MINUTES = 15;

    @Override
    public void onCreate() {
        super.onCreate();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                DrugCheckWorker.class, INTERVAL_MINUTES, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );

        Log.d(TAG, "Periodic drug check scheduled every " + INTERVAL_MINUTES + " minutes");
    }
}