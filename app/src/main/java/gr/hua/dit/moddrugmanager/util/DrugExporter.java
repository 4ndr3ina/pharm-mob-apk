package gr.hua.dit.moddrugmanager.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import gr.hua.dit.moddrugmanager.data.AppDatabase;
import gr.hua.dit.moddrugmanager.data.PrescriptionDrugWithTimeTerms;

public class DrugExporter {

    private static final String TAG = "DrugExporter";
    private static final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface ExportCallback {
        void onResult(boolean success, String message);
    }

    public static void exportActiveDrugs(Context context, ExportCallback callback) {
        AppDatabase db = AppDatabase.getInstance(context);
        List<PrescriptionDrugWithTimeTerms> activeDrugs = db.prescriptionDrugDao().getActiveDrugsOrderedByTimeSync();

        if (activeDrugs.isEmpty()) {
            callback.onResult(false, "No active medications to export");
            return;
        }

        String html = buildHtml(activeDrugs);
        String fileName = "prescription_drugs_export_" + System.currentTimeMillis() + ".html";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                writeViaMediaStore(context, fileName, html);
            } else {
                writeViaFile(fileName, html);
            }
            callback.onResult(true, "Exported " + activeDrugs.size() + " medications to Downloads/" + fileName);
        } catch (Exception e) {
            callback.onResult(false, "Export failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private static void writeViaMediaStore(Context context, String fileName, String html) throws IOException {
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/html");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri targetUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (targetUri == null) {
            throw new IOException("Could not create file in Downloads via MediaStore - insert() returned null");
        }

        try (OutputStream out = resolver.openOutputStream(targetUri)) {
            if (out == null) throw new IOException("Could not open output stream for " + targetUri);
            out.write(html.getBytes());
            out.flush();
        }
    }

    private static void writeViaFile(String fileName, String html) throws IOException {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) downloadsDir.mkdirs();
        File outFile = new File(downloadsDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(html.getBytes());
        }
    }

    private static String buildHtml(List<PrescriptionDrugWithTimeTerms> drugs) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset='utf-8'><title>Active Prescription Drugs</title></head><body>");
        sb.append("<h1>Active Prescription Drugs</h1>");
        sb.append("<p>Exported on: ").append(displayFormat.format(System.currentTimeMillis())).append("</p>");
        sb.append("<table border='1' cellpadding='6' cellspacing='0'>");
        sb.append("<tr>")
                .append("<th>UID</th><th>Short Name</th><th>Description</th>")
                .append("<th>Start Date</th><th>End Date</th><th>Time Term</th>")
                .append("<th>Doctor Name</th><th>Doctor Location</th>")
                .append("<th>Has Received Today</th><th>Last Date Received</th>")
                .append("</tr>");

        for (PrescriptionDrugWithTimeTerms item : drugs) {
            String desc = TextUtils.isEmpty(item.drug.getDescription()) ? "-" : item.drug.getDescription();
            String docName = TextUtils.isEmpty(item.drug.getDoctorName()) ? "-" : item.drug.getDoctorName();
            String docLoc = TextUtils.isEmpty(item.drug.getDoctorLocation()) ? "-" : item.drug.getDoctorLocation();
            String lastReceived = item.drug.getLastDateReceived() != null
                    ? displayFormat.format(item.drug.getLastDateReceived()) : "Never";
            String termName = item.getTimeTermsDisplay();

            sb.append("<tr>")
                    .append("<td>").append(item.drug.getUid()).append("</td>")
                    .append("<td>").append(item.drug.getShortName()).append("</td>")
                    .append("<td>").append(desc).append("</td>")
                    .append("<td>").append(displayFormat.format(item.drug.getStartDate())).append("</td>")
                    .append("<td>").append(displayFormat.format(item.drug.getEndDate())).append("</td>")
                    .append("<td>").append(termName).append("</td>")
                    .append("<td>").append(docName).append("</td>")
                    .append("<td>").append(docLoc).append("</td>")
                    .append("<td>").append(item.drug.isHasReceivedToday() ? "Yes" : "No").append("</td>")
                    .append("<td>").append(lastReceived).append("</td>")
                    .append("</tr>");
        }

        sb.append("</table></body></html>");
        return sb.toString();
    }
}
