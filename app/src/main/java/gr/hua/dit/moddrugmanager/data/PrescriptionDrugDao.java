package gr.hua.dit.moddrugmanager.data;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Dao
public abstract class PrescriptionDrugDao {

    // ---- (A) Record a new Prescription Drug ----
    @Insert
    public abstract long insert(PrescriptionDrug drug);

    @Update
    public abstract void update(PrescriptionDrug drug);

    @Delete
    public abstract void delete(PrescriptionDrug drug);

    // Inserts a drug AND its (possibly several) time terms as one atomic
    // operation.
    @Transaction
    public long insertWithTimeTerms(PrescriptionDrug drug, List<Integer> timeTermIds) {
        long newUid = insert(drug);
        List<PrescriptionDrugTimeTermCrossRef> refs = new ArrayList<>();
        for (Integer timeTermId : timeTermIds) {
            refs.add(new PrescriptionDrugTimeTermCrossRef((int) newUid, timeTermId));
        }
        insertCrossRefs(refs);
        return newUid;
    }

    @Insert
    abstract void insertCrossRefs(List<PrescriptionDrugTimeTermCrossRef> crossRefs);

    // ---- (B) Delete by UID ----
    @Query("DELETE FROM prescription_drug WHERE uid = :uid")
    public abstract int deleteById(int uid);

    // ---- (C) Periodic background check ----
    @Query("SELECT * FROM prescription_drug")
    public abstract List<PrescriptionDrug> getAllSync();

    @Query("UPDATE prescription_drug SET is_active = :isActive, has_received_today = :hasReceivedToday WHERE uid = :uid")
    public abstract void updateComputedFields(int uid, boolean isActive, boolean hasReceivedToday);

    // ---- Used by the Delete screen (B) ----
    @Transaction
    @Query("SELECT * FROM prescription_drug ORDER BY uid ASC")
    public abstract LiveData<List<PrescriptionDrugWithTimeTerms>> getAllWithTimeTerms();

    // Used by requirement (E)
    @Query("UPDATE prescription_drug SET last_date_received = :timestamp, has_received_today = 1 WHERE uid = :uid")
    public abstract void markAsReceived(int uid, long timestamp);

    // ---- (D) Overview: order by EARLIEST time term (refactored to drugUid / timeTermId) ----
    @Transaction
    @Query("SELECT * FROM prescription_drug WHERE is_active = 1 " +
            "ORDER BY (SELECT MIN(time_term.order_index) FROM prescription_drug_time_term_cross_ref x " +
            "INNER JOIN time_term ON time_term.id = x.timeTermId " +
            "WHERE x.drugUid = prescription_drug.uid) ASC")
    public abstract LiveData<List<PrescriptionDrugWithTimeTerms>> getActiveDrugsOrderedByTime();

    // Synchronous variant, used by (F) export
    @Transaction
    @Query("SELECT * FROM prescription_drug WHERE is_active = 1 " +
            "ORDER BY (SELECT MIN(time_term.order_index) FROM prescription_drug_time_term_cross_ref x " +
            "INNER JOIN time_term ON time_term.id = x.timeTermId " +
            "WHERE x.drugUid = prescription_drug.uid) ASC")
    public abstract List<PrescriptionDrugWithTimeTerms> getActiveDrugsOrderedByTimeSync();

    // ---- (D)/(E) Detail screen ----
    @Transaction
    @Query("SELECT * FROM prescription_drug WHERE uid = :uid")
    public abstract PrescriptionDrugWithTimeTerms getByIdWithTimeTermsSync(int uid);

    @Query("SELECT * FROM prescription_drug WHERE uid = :uid")
    public abstract PrescriptionDrug getByIdSync(int uid);

    @Query("SELECT COUNT(*) FROM prescription_drug")
    public abstract int count();
}
