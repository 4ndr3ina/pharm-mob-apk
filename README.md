# **Medication Manager Application**

## Andriani Koui

## AM: it2023115

## 

### **Functionality Details**

 #### ***Requirment A: Record a new Prescription Drug***

**Description:** Users can add new medications by providing a short name, description, start/end dates, doctor details, and selecting specific time terms (e.g., "before-breakfast"). Data is stored in a relational Room database.

**For Functionality A :**  
The app uses **AddDrugActivity** to provide a simple form where users can enter medication details. When the user clicks save, the Room database (**AppDatabase**) stores the information using three main parts: **PrescriptionDrug** for the medication info, **Doctor** for the doctor's details, and **TimeTerm** for the daily schedule. Since one drug can be taken at several different times, the app uses **PrescriptionDrugTimeTermCrossRef** to link them all together. To keep the data safe, **PrescriptionDrugDao** handles the saving process in a single step, ensuring all the details are recorded correctly at once.

**For the database logic:**  
@Transaction  
public long insertWithTimeTerms(PrescriptionDrug drug, List\<Integer\> timeTermIds) {  
    // insert the main drug record and get its new ID  
    long newUid \= insert(drug);  
      
    // create the links for each selected time term (breakfast, dinner ktlp)  
    List\<PrescriptionDrugTimeTermCrossRef\> refs \= new ArrayList\<\>();  
    for (Integer timeTermId : timeTermIds) {  
        refs.add(new PrescriptionDrugTimeTermCrossRef((int) newUid, timeTermId));  
    }  
      
    // save all links to th table  
    insertCrossRefs(refs);  
    return newUid;  
}

**For the UI:**  
// create the drug object from UI input  
PrescriptionDrug drug \= new PrescriptionDrug(shortName, description,   
    startDate, endDate, doctorName, doctorAddress, lat, lng);

// save everything to the Room database  
AppDatabase.databaseWriteExecutor.execute(() \-\> {  
    // atomic insert of drug \+ all selected time term IDs  
    db.prescriptionDrugDao().insertWithTimeTerms(drug, selectedTimeIds);  
      
    // return to main screen after success  
    runOnUiThread(() \-\> {  
        Toast.makeText(this, "Medication Saved\!", Toast.LENGTH\_SHORT).show();  
        finish();  
    });  
});

 #### 

    #### ***Requirment B: Delete a Prescription Drug***

**Description:** Medications can be removed by selecting them from the delete list. The app confirms the action and displays a pop up showing exactly how many rows were affected in the database.

**Usage:** Navigate to "Delete a Medication", tap an item, and confirm in the dialog.

**For Functionality B:**  
The app uses DeleteDrugActivity to display a list of all recorded medications. When a user taps on a drug, an AlertDialog pops up to confirm the deletion. Once the user agrees, the app uses PrescriptionDrugDao to run the deleteById command, which removes that specific medication from the Room database. Finally, the app shows another alert to let the user know the deletion worked and reports exactly how many rows were affected in the database.

**For the Db:**  
@Query("DELETE FROM prescription\_drug WHERE uid \= :uid")  
public abstract int deleteById(int uid);

**For the UI:**  
// method triggered when a user taps an item in the list  
private void performDelete(int uid) {  
    AppDatabase.databaseWriteExecutor.execute(() \-\> {  
        // remove from database and get the row count  
        int rowsAffected \= db.prescriptionDrugDao().deleteById(uid);

        //inform the user on the Main Thread  
        runOnUiThread(() \-\> {  
            String message \= (rowsAffected \> 0\)  
                    ? "Deleted successfully.\\nRows affected: " \+ rowsAffected  
                    : "Nothing was deleted.";  
              
            new AlertDialog.Builder(this)  
                    .setTitle("Delete Result")  
                    .setMessage(message)  
                    .setPositiveButton("OK", null)  
                    .show();  
        });  
    }}  
}

 #### 

    #### 

       #### 

         #### 

         #### 

          #### 

         #### 

          #### 

          #### 

          #### 

         #### 

#### ***Requirment C : Periodic Background Check Of the DB***

**Description:** A DrugCheckWorker runs periodically to update system-managed fields: isActive (based on current date) and hasReceivedToday (resets daily).

**Usage:** This operates automatically in the background using WorkManager

**For Functionality C:**  
The application uses **WorkManager** to perform periodic background checks on all recorded medications. The **DrugCheckWorker** runs at set intervals (every hour ktlp) to revaluate two system managed fields: **isActive** (true if today falls between the medication's start and end dates) and **hasReceivedToday** (which resets to false every new day). By using a **startOfDay** helper to compare dates with day precision, the app ensures that medication statuses are updated automatically without the user having to open the app.

**Background logic:**

// logic inside doWork()  
long todayStart \= startOfDay(System.currentTimeMillis());  
List\<PrescriptionDrug\> allDrugs \= db.prescriptionDrugDao().getAllSync();

for (PrescriptionDrug drug : allDrugs) {  
    // calculate if the drug should be active today  
    boolean newIsActive \= (todayStart \>= startOfDay(drug.getStartDate()))   
                       && (todayStart \<= startOfDay(drug.getEndDate()));

    // check if the Received Today flag should be true or reset  
    boolean newHasReceivedToday \= false;  
    if (drug.getLastDateReceived() \!= null) {  
        newHasReceivedToday \= (startOfDay(drug.getLastDateReceived()) \== todayStart);  
    }

    // update the database if the status has changed  
    db.prescriptionDrugDao().updateComputedFields(drug.getUid(), newIsActive, newHasReceivedToday);  
}

**Scheduling the check of the data:**

// scheduling a periodic background task every hour  
PeriodicWorkRequest request \= new PeriodicWorkRequest.Builder(  
        DrugCheckWorker.class, 1, TimeUnit.HOURS)  
        .build();

WorkManager.getInstance(context).enqueueUniquePeriodicWork(  
        "MedicationCheck",  
        ExistingPeriodicWorkPolicy.KEEP,  
        request);

 #### ***Requirment D: View Active and Detailed Prescriptions***

**Description:** The app provides an overview of prescriptions with filters for Active, Future, Expired, and Taken Today. Selecting a drug opens a detailed view showing all recorded information.

**Usage:** Tap “View All Prescriptions” and select a filter or a specific drug.

**For Functionality D:**  
The application offers a comprehensive viewing system. It starts with a categorized overview in **AllPrescriptionsActivity**, where users can filter medications by their status (Active, Future, Expired, or Taken Today). Each category is populated via **LiveData** from the **PrescriptionDrugDao**. When a user selects a specific medication, the app navigates to **DrugDetailActivity**, which retrieves the full record (including doctor details and chronological time terms) from the relational database and displays it in a dedicated layout.

**UI filtering code:**  
// logic for switching filters and observing the corresponding data  
findViewById(R.id.btnFilterFuture).setOnClickListener(v \-\>   
    observeData(db.prescriptionDrugDao().getFuturePrescriptions(todayStart)));

findViewById(R.id.btnFilterExpired).setOnClickListener(v \-\>   
    observeData(db.prescriptionDrugDao().getExpiredPrescriptions(todayStart)));

private void observeData(LiveData\<List\<PrescriptionDrugWithTimeTerms\>\> liveData) {  
    // remove old observer to prevent data overlap  
    if (currentLiveData \!= null) currentLiveData.removeObservers(this);  
      
    currentLiveData \= liveData;  
    currentLiveData.observe(this, list \-\> {  
        adapter.setItems(list);  
        tvEmptyMessage.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);  
    });  
}

**Loading Detailed info:**  
private void loadDrug() {  
    AppDatabase.databaseWriteExecutor.execute(() \-\> {  
        // fetch the drug and its relationship data using the DAO  
        PrescriptionDrugWithTimeTerms item \= db.prescriptionDrugDao().getByIdWithTimeTermsSync(uid);  
          
        if (item \!= null) {  
            currentDrug \= item.drug;  
            runOnUiThread(() \-\> {  
                // populate all UI fields (UID, Dates, Doctor,ktlp)  
                tvName.setText(currentDrug.getShortName());  
                tvTimeTerm.setText(item.getTimeTermsDisplay());  
                tvDoctorName.setText(currentDrug.getDoctorName());  
            });  
        }  
    });}


#### ***Requirment E: Receive Medication \+ Google Maps Integration***

**Description:** In the detail screen, users can mark a drug as “received today”. If the doctor's location is saved, a button opens Google Maps to show the location.

**Usage:** Press “I RECEIVED THIS TODAY” or “VIEW DOCTOR ON MAP”

**For Functionality E:**  
The application enhances the medication detail screen (DrugDetailActivity) with interactive features. Users can confirm they have taken their medication by clicking the “I RECEIVED THIS TODAY” button, which updates the database in real time. To ensure data accuracy, the button is dynamically hidden if the medication is expired or has already been marked as taken for the day. Additionally, if the medication includes a doctor’s location, a “VIEW DOCTOR ON MAP” button appears. This feature uses a smart geo: Intent that prioritizes precise GPS coordinates over text addresses and includes a browser fallback to ensure the location is accessible even if a dedicated map app is not installed.

**Update of the db code:**  
// updates last\_date\_received with the current time and sets the today flag  
@Query("UPDATE prescription\_drug SET last\_date\_received \= :timestamp, " \+  
       "has\_received\_today \= 1 WHERE uid \= :uid")  
public abstract void markAsReceived(int uid, long timestamp);

**UI logic code:**  
// logic to show/hide the button based on status  
btnMarkReceived.setVisibility(drug.isActive() && \!drug.isHasReceivedToday()   
    ? View.VISIBLE : View.GONE);

// method to handle the confirmation click  
private void onMarkReceivedClicked() {  
    long now \= System.currentTimeMillis();  
    AppDatabase.databaseWriteExecutor.execute(() \-\> {  
        // atomic update of system-managed fields  
        db.prescriptionDrugDao().markAsReceived(uid, now);  
        runOnUiThread(() \-\> {  
            Toast.makeText(this, "Marked as received today", Toast.LENGTH\_SHORT).show();  
            loadDrug(); // Refresh UI to reflect changes  
        });  
    });  
}

 #### ***Requirment F: Export Active Prescriptions***

**Description:** The app can export all currently active medications into a formatted HTML file stored in the public Downloads directory.

**Usage:** Tap “EXPORT ACTIVE MEDICATIONS” from the main menu.  
File Location: /Downloads/prescription\_drugs\_export.html

**For Functionality F:**  
The application provides a mechanism to export all currently active medications into a structured HTML file. This is handled by the **DrugExporter** utility class. The app queries the **AppDatabase** via **PrescriptionDrugDao.getActiveDrugsOrderedByTimeSync()** to retrieve all relevant records. To ensure compatibility across different Android versions, the **exportActiveDrugs()** method uses the **MediaStore API on Android 10+ (API 29+)** to securely save the file to the shared Downloads directory without requiring broad storage permissions. For older devices, it falls back to a traditional file-writing approach using **Environment.getExternalStoragePublicDirectory()**. The generated HTML file includes a detailed table with UIDs, schedules, doctor information, and consumption history, allowing users to share their data with other applications or healthcare providers.

 **Export Logic and HTML Generation:**  
 // logic to build the HTML report  
private static String buildHtml(List\<PrescriptionDrugWithTimeTerms\> drugs) {  
    StringBuilder sb \= new StringBuilder();  
    sb.append("\<html\>\<body\>\<h1\>Active Prescription Drugs\</h1\>");  
    sb.append("\<table border='1'\>\<tr\>\<th\>UID\</th\>\<th\>Short Name\</th\>\<th\>Time Terms\</th\>...");  
      
    for (PrescriptionDrugWithTimeTerms item : drugs) {  
        sb.append("\<tr\>")  
            .append("\<td\>").append(item.drug.getUid()).append("\</td\>")  
            .append("\<td\>").append(item.drug.getShortName()).append("\</td\>")  
            .append("\<td\>").append(item.getTimeTermsDisplay()).append("\</td\>")  
            // other code  
            .append("\</tr\>");  
    }  
    sb.append("\</table\>\</body\>\</html\>");  
    return sb.toString();  
}

**Saving to Downloads code:**  
// saving the file to the shared Downloads folder via MediaStore  
private static void writeViaMediaStore(Context context, String fileName, String html) throws IOException {  
    ContentResolver resolver \= context.getContentResolver();  
    ContentValues values \= new ContentValues();  
    values.put(MediaStore.Downloads.DISPLAY\_NAME, fileName);  
    values.put(MediaStore.Downloads.MIME\_TYPE, "text/html");  
    values.put(MediaStore.Downloads.RELATIVE\_PATH, Environment.DIRECTORY\_DOWNLOADS);

    Uri targetUri \= resolver.insert(MediaStore.Downloads.EXTERNAL\_CONTENT\_URI, values);  
    try (OutputStream out \= resolver.openOutputStream(targetUri)) {  
        out.write(html.getBytes());  
    }  
}

**Export to prevent the app from freezing code:**  
private void onExportClicked() {  
    AppDatabase.databaseWriteExecutor.execute(() \-\> {  
        // runs the export utility and returns the result to the UI thread  
        DrugExporter.exportActiveDrugs(getApplicationContext(),   
            (success, message) \-\> runOnUiThread(() \-\> {  
                Toast.makeText(this, message, Toast.LENGTH\_LONG).show();  
            })  
        );  
    });  
}

 #### ***Requirment G: Content Provider***

**Description:** A DrugContentProvider is implemented to allow other applications to perform CRUD operations on the prescription database.

Code Snippet:  
Java  
public class DrugContentProvider extends ContentProvider {  
    public static final String AUTHORITY \= "gr.hua.dit.moddrugmanager.provider";  
    public static final Uri CONTENT\_URI \= Uri.parse("content://" \+ AUTHORITY \+ "/prescription\_drug");  
    // Standard insert/query/update/delete implementation...  
}

**For Functionality G:**  
The application implements a Content Provider to allow other Android applications to interact with its medication database. The **DrugContentProvider** class acts as a secure gateway, mapping standard URI requests to the underlying **Room database** using **SupportSQLiteDatabase**. To verify this interface, the app includes a **ContentResolverTestHelper** class that performs a full suite of **CRUD** (Create, Read, Update, Delete) operations using the system's ContentResolver. Users can trigger this validation directly from the **OverviewActivity** by pressing the “TEST CONTENT PROVIDER” button, which executes the **runFullCrudTest** method to ensure the data source is correctly accessible to external apps.

3\. Conclusion  
The application successfully meets all the technical requirements (A-G), providing a robust local data management system, background synchronization, and interoperability through file exports and Content Providers.
