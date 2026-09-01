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

    @Insert
    public abstract long insert(PrescriptionDrug drug);

    @Update
    public abstract void update(PrescriptionDrug drug);

    @Delete
    public abstract void delete(PrescriptionDrug drug);

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

    @Query("DELETE FROM prescription_drug WHERE uid = :uid")
    public abstract int deleteById(int uid);

    @Query("SELECT * FROM prescription_drug")
    public abstract List<PrescriptionDrug> getAllSync();

    @Query("UPDATE prescription_drug SET is_active = :isActive, has_received_today = :hasReceivedToday WHERE uid = :uid")
    public abstract void updateComputedFields(int uid, boolean isActive, boolean hasReceivedToday);

    @Transaction
    @Query("SELECT * FROM prescription_drug ORDER BY uid ASC")
    public abstract LiveData<List<PrescriptionDrugWithTimeTerms>> getAllWithTimeTerms();

    @Query("UPDATE prescription_drug SET last_date_received = :timestamp, has_received_today = 1 WHERE uid = :uid")
    public abstract void markAsReceived(int uid, long timestamp);

    @Transaction
    @Query("SELECT * FROM prescription_drug WHERE is_active = 1 " +
            "ORDER BY (SELECT MIN(time_term.order_index) FROM prescription_drug_time_term_cross_ref x " +
            "INNER JOIN time_term ON time_term.id = x.timeTermId " +
            "WHERE x.drugUid = prescription_drug.uid) ASC")
    public abstract LiveData<List<PrescriptionDrugWithTimeTerms>> getActiveDrugsOrderedByTime();

    @Transaction
    @Query("SELECT * FROM prescription_drug WHERE is_active = 1 ORDER BY uid ASC")
    public abstract LiveData<List<PrescriptionDrugWithTimeTerms>> getActivePrescriptions();

    @Transaction
    @Query("SELECT * FROM prescription_drug WHERE is_active = 0 AND end_date < :todayStart ORDER BY uid ASC")
    public abstract LiveData<List<PrescriptionDrugWithTimeTerms>> getExpiredPrescriptions(long todayStart);

    @Transaction
    @Query("SELECT * FROM prescription_drug WHERE is_active = 0 AND start_date > :todayStart ORDER BY uid ASC")
    public abstract LiveData<List<PrescriptionDrugWithTimeTerms>> getFuturePrescriptions(long todayStart);

    @Transaction
    @Query("SELECT * FROM prescription_drug WHERE has_received_today = 1 AND is_active = 1 ORDER BY uid ASC")
    public abstract LiveData<List<PrescriptionDrugWithTimeTerms>> getTakenTodayPrescriptions();

    @Transaction
    @Query("SELECT * FROM prescription_drug WHERE is_active = 1 " +
            "ORDER BY (SELECT MIN(time_term.order_index) FROM prescription_drug_time_term_cross_ref x " +
            "INNER JOIN time_term ON time_term.id = x.timeTermId " +
            "WHERE x.drugUid = prescription_drug.uid) ASC")
    public abstract List<PrescriptionDrugWithTimeTerms> getActiveDrugsOrderedByTimeSync();

    @Transaction
    @Query("SELECT * FROM prescription_drug WHERE uid = :uid")
    public abstract PrescriptionDrugWithTimeTerms getByIdWithTimeTermsSync(int uid);

    @Query("SELECT * FROM prescription_drug WHERE uid = :uid")
    public abstract PrescriptionDrug getByIdSync(int uid);

    @Query("SELECT COUNT(*) FROM prescription_drug")
    public abstract int count();
}
