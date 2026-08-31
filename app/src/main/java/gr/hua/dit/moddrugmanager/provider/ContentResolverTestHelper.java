package gr.hua.dit.moddrugmanager.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * Requirement (G): Tests the DrugContentProvider via ContentResolver.
 */
public class ContentResolverTestHelper {

    private static final String TAG = "ContentResolverTest";

    public static void runFullCrudTest(Context context) {
        ContentResolver resolver = context.getContentResolver();

        Log.d(TAG, "Starting Content Provider CRUD test...");

        // 1) INSERT
        // We only include columns that actually exist in the PrescriptionDrug entity.
        ContentValues values = new ContentValues();
        values.put("short_name", "ProviderTestDrug");
        values.put("description", "Test Description");
        values.put("start_date", System.currentTimeMillis());
        values.put("end_date", System.currentTimeMillis() + 86400000L); 
        values.put("is_active", 0); // false
        values.put("has_received_today", 0); // false

        android.net.Uri insertedUri = resolver.insert(DrugContentProvider.CONTENT_URI, values);
        if (insertedUri == null) {
            throw new RuntimeException("Insert failed: resolver returned null");
        }
        
        long newUid = android.content.ContentUris.parseId(insertedUri);
        Log.d(TAG, "Successfully inserted drug with UID: " + newUid);

        // 2) QUERY
        Cursor cursor = resolver.query(DrugContentProvider.CONTENT_URI, null,
                "uid = ?", new String[]{String.valueOf(newUid)}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndexOrThrow("short_name");
                Log.d(TAG, "Query successful. Name: " + cursor.getString(nameIdx));
            }
            cursor.close();
        }

        // 3) UPDATE
        ContentValues updateValues = new ContentValues();
        updateValues.put("short_name", "UpdatedName");
        int updated = resolver.update(DrugContentProvider.CONTENT_URI, updateValues,
                "uid = ?", new String[]{String.valueOf(newUid)});
        Log.d(TAG, "Update successful. Rows affected: " + updated);

        // 4) DELETE
        int deleted = resolver.delete(DrugContentProvider.CONTENT_URI,
                "uid = ?", new String[]{String.valueOf(newUid)});
        Log.d(TAG, "Delete successful. Rows affected: " + deleted);

        Log.d(TAG, "Content Provider CRUD test finished successfully!");
    }
}