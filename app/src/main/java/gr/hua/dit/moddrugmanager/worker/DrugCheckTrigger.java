package gr.hua.dit.moddrugmanager.worker;

import android.content.Context;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class DrugCheckTrigger {

    private static final String TAG = "DrugCheckTrigger";

    public static void runNow(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DrugCheckWorker.class).build();
        WorkManager.getInstance(context).enqueue(request);
        Log.d(TAG, "Manual one-time drug check enqueued");
    }
}
