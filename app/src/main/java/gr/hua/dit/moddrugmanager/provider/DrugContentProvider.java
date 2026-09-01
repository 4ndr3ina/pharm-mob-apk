package gr.hua.dit.moddrugmanager.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.sqlite.db.SupportSQLiteDatabase;

import gr.hua.dit.moddrugmanager.data.AppDatabase;

public class DrugContentProvider extends ContentProvider {

    private static final String TAG = "DrugContentProvider";

    public static final String AUTHORITY = "gr.hua.dit.moddrugmanager.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/prescription_drug");

    private static final String TABLE_NAME = "prescription_drug";

    private static final int DRUGS = 1;
    private static final int DRUG_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, "prescription_drug", DRUGS);
        uriMatcher.addURI(AUTHORITY, "prescription_drug/#", DRUG_ID);
    }

    private AppDatabase db;

    @Override
    public boolean onCreate() {
        db = AppDatabase.getInstance(getContext().getApplicationContext());
        return true;
    }

    private SupportSQLiteDatabase writableDb() {
        return db.getOpenHelper().getWritableDatabase();
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = uriMatcher.match(uri);
        String finalSelection = selection;
        String[] finalArgs = selectionArgs;

        if (match == DRUG_ID) {
            finalSelection = "uid = ?";
            finalArgs = new String[]{uri.getLastPathSegment()};
        }

        String query = "SELECT * FROM " + TABLE_NAME
                + (finalSelection != null ? " WHERE " + finalSelection : "")
                + (sortOrder != null ? " ORDER BY " + sortOrder : "");

        Cursor cursor = writableDb().query(query, finalArgs != null ? finalArgs : new Object[0]);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);
        if (match == DRUGS) {
            return "vnd.android.cursor.dir/" + AUTHORITY + ".prescription_drug";
        } else if (match == DRUG_ID) {
            return "vnd.android.cursor.item/" + AUTHORITY + ".prescription_drug";
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (values == null) return null;

        long newId = writableDb().insert(TABLE_NAME,
                android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, values);

        if (newId <= 0) return null;
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(CONTENT_URI, newId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        String finalSelection = selection;
        String[] finalArgs = selectionArgs;

        if (match == DRUG_ID) {
            finalSelection = "uid = ?";
            finalArgs = new String[]{uri.getLastPathSegment()};
        }

        int rows = writableDb().delete(TABLE_NAME, finalSelection, finalArgs);

        if (rows > 0) getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        if (values == null) return 0;

        int match = uriMatcher.match(uri);
        String finalSelection = selection;
        String[] finalArgs = selectionArgs;

        if (match == DRUG_ID) {
            finalSelection = "uid = ?";
            finalArgs = new String[]{uri.getLastPathSegment()};
        }

        int rows = writableDb().update(TABLE_NAME,
                android.database.sqlite.SQLiteDatabase.CONFLICT_ABORT, values, finalSelection, finalArgs);

        if (rows > 0) getContext().getContentResolver().notifyChange(uri, null);
        return rows;
    }
}
