package gr.hua.dit.moddrugmanager.data;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {TimeTerm.class, PrescriptionDrug.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "AppDatabase";
    private static final String DB_NAME = "drug_manager.db";

    // Fixed list from Comment #2, in chronological order during the day.
    private static final String[] TIME_TERMS_IN_ORDER = {
            "before-breakfast", "at-breakfast", "after-breakfast",
            "before-lunch", "at-lunch", "after-lunch",
            "before-dinner", "at-dinner", "after-dinner"
    };

    private static volatile AppDatabase instance;

    // Small fixed thread pool used for one-off DB writes (e.g. pre-population).
    // The DAO layer above is otherwise driven by LiveData or by background
    // components (WorkManager for requirement C, IntentService/Executor for exports).
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(2);

    public abstract TimeTermDao timeTermDao();

    public abstract PrescriptionDrugDao prescriptionDrugDao();

    public static AppDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .addCallback(prepopulateCallback)
                            .build();
                }
            }
        }
        return instance;
    }

    // Runs once, the first time the database file is created on disk.
    private static final RoomDatabase.Callback prepopulateCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.d(TAG, "Database created - scheduling TimeTerm pre-population");
            databaseWriteExecutor.execute(() -> {
                if (instance == null) return;
                TimeTermDao dao = instance.timeTermDao();
                List<TimeTerm> seed = new ArrayList<>();
                for (int i = 0; i < TIME_TERMS_IN_ORDER.length; i++) {
                    seed.add(new TimeTerm(TIME_TERMS_IN_ORDER[i], i));
                }
                dao.insertAll(seed);
                Log.d(TAG, "Pre-populated " + seed.size() + " TimeTerm rows");
            });
        }
    };
}