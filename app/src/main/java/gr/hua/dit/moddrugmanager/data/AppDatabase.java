package gr.hua.dit.moddrugmanager.data;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {
                TimeTerm.class,
                PrescriptionDrug.class,
                Doctor.class,
                PrescriptionDrugTimeTermCrossRef.class
        },
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "AppDatabase";
    private static final String DB_NAME = "drug_manager.db";

    private static final String[] TIME_TERMS_IN_ORDER = {
            "before-breakfast", "at-breakfast", "after-breakfast",
            "before-lunch", "at-lunch", "after-lunch",
            "before-dinner", "at-dinner", "after-dinner"
    };

    private static volatile AppDatabase instance;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(2);

    public abstract TimeTermDao timeTermDao();
    public abstract PrescriptionDrugDao prescriptionDrugDao();
    public abstract DoctorDao doctorDao();
    public abstract PrescriptionDrugTimeTermCrossRefDao crossRefDao();

    public static AppDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .addCallback(prepopulateCallback)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    private static final RoomDatabase.Callback prepopulateCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            seedTimeTerms(db);
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            try (android.database.Cursor cursor = db.query("SELECT COUNT(*) FROM time_term", null)) {
                int count = 0;
                if (cursor != null && cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
                if (count == 0) {
                    Log.d(TAG, "TimeTerm table empty onOpen - seeding now");
                    for (int i = 0; i < TIME_TERMS_IN_ORDER.length; i++) {
                        db.execSQL("INSERT INTO time_term (term_name, order_index) VALUES (?, ?)",
                                new Object[]{TIME_TERMS_IN_ORDER[i], i});
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Seeding check failed", e);
            }
        }

        private void seedTimeTerms(SupportSQLiteDatabase db) {
            Log.d(TAG, "Seeding default TimeTerms");
            for (int i = 0; i < TIME_TERMS_IN_ORDER.length; i++) {
                db.execSQL("INSERT INTO time_term (term_name, order_index) VALUES (?, ?)",
                        new Object[]{TIME_TERMS_IN_ORDER[i], i});
            }
        }
    };
}
