package gr.hua.dit.moddrugmanager.worker;

import android.content.Context;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

/**
 * Lets any screen trigger an immediate run of DrugCheckWorker, e.g. from a
 * "Run Check Now" button. Useful for demonstrating requirement (C) in your
 * video without waiting for the real 15-minute periodic schedule to fire.
 */
public class DrugCheckTrigger {

    private static final String TAG = "DrugCheckTrigger";

    public static void runNow(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DrugCheckWorker.class).build();
        WorkManager.getInstance(context).enqueue(request);
        Log.d(TAG, "Manual one-time drug check enqueued");
    }
}