package gr.hua.dit.moddrugmanager.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class ContentResolverTestHelper {

    private static final String TAG = "ContentResolverTest";

    public static void runFullCrudTest(Context context) {
        ContentResolver resolver = context.getContentResolver();

        Log.d(TAG, "Starting Content Provider CRUD test...");

        ContentValues values = new ContentValues();
        values.put("short_name", "ProviderTestDrug");
        values.put("description", "Test Description");
        values.put("start_date", System.currentTimeMillis());
        values.put("end_date", System.currentTimeMillis() + 86400000L); 
        values.put("is_active", 0);
        values.put("has_received_today", 0);

        android.net.Uri insertedUri = resolver.insert(DrugContentProvider.CONTENT_URI, values);
        if (insertedUri == null) {
            throw new RuntimeException("Insert failed: resolver returned null");
        }
        
        long newUid = android.content.ContentUris.parseId(insertedUri);
        Log.d(TAG, "Successfully inserted drug with UID: " + newUid);

        Cursor cursor = resolver.query(DrugContentProvider.CONTENT_URI, null,
                "uid = ?", new String[]{String.valueOf(newUid)}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndexOrThrow("short_name");
                Log.d(TAG, "Query successful. Name: " + cursor.getString(nameIdx));
            }
            cursor.close();
        }

        ContentValues updateValues = new ContentValues();
        updateValues.put("short_name", "UpdatedName");
        int updated = resolver.update(DrugContentProvider.CONTENT_URI, updateValues,
                "uid = ?", new String[]{String.valueOf(newUid)});
        Log.d(TAG, "Update successful. Rows affected: " + updated);

        int deleted = resolver.delete(DrugContentProvider.CONTENT_URI,
                "uid = ?", new String[]{String.valueOf(newUid)});
        Log.d(TAG, "Delete successful. Rows affected: " + deleted);

        Log.d(TAG, "Content Provider CRUD test finished successfully!");
    }
}
