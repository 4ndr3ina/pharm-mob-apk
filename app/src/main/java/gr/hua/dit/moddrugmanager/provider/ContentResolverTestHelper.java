package gr.hua.dit.moddrugmanager.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * Demonstrates that DrugContentProvider works, by performing a full CRUD
 * cycle through the standard ContentResolver API - exactly as an external
 * app would - as allowed by the spec ("for testing purposes, you can use
 * the Content Resolver... in the same App, on request").
 *
 * Call ContentResolverTestHelper.runFullCrudTest(context) from a background
 * thread (e.g. a "Test Content Provider" debug button) and watch Logcat.
 */
public class ContentResolverTestHelper {

    private static final String TAG = "ContentResolverTest";

    public static void runFullCrudTest(Context context) {
        ContentResolver resolver = context.getContentResolver();

        // 1) INSERT a test row
        ContentValues values = new ContentValues();
        values.put("short_name", "TestDrugViaProvider");
        values.put("description", "Inserted through ContentResolver");
        values.put("start_date", System.currentTimeMillis());
        values.put("end_date", System.currentTimeMillis() + 86_400_000L * 7); // +7 days
        values.put("time_term_id", 1); // assumes TimeTerm id 1 exists (before-breakfast)
        values.put("is_active", 0);
        values.put("has_received_today", 0);

        android.net.Uri insertedUri = resolver.insert(DrugContentProvider.CONTENT_URI, values);
        Log.d(TAG, "INSERT result uri=" + insertedUri);
        if (insertedUri == null) return;

        long newUid = android.content.ContentUris.parseId(insertedUri);

        // 2) QUERY it back
        Cursor cursor = resolver.query(DrugContentProvider.CONTENT_URI, null,
                "uid = ?", new String[]{String.valueOf(newUid)}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndexOrThrow("short_name");
                Log.d(TAG, "QUERY result: uid=" + newUid + " short_name=" + cursor.getString(nameIdx));
            }
            cursor.close();
        }

        // 3) UPDATE it
        ContentValues updateValues = new ContentValues();
        updateValues.put("short_name", "TestDrugUpdated");
        int updatedRows = resolver.update(DrugContentProvider.CONTENT_URI, updateValues,
                "uid = ?", new String[]{String.valueOf(newUid)});
        Log.d(TAG, "UPDATE rowsAffected=" + updatedRows);

        // 4) DELETE it (cleanup)
        int deletedRows = resolver.delete(DrugContentProvider.CONTENT_URI,
                "uid = ?", new String[]{String.valueOf(newUid)});
        Log.d(TAG, "DELETE rowsAffected=" + deletedRows);

        Log.d(TAG, "Full CRUD test via ContentResolver completed successfully");
    }
}