package gr.hua.dit.moddrugmanager.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface PrescriptionDrugTimeTermCrossRefDao {

    @Insert
    void insertAll(List<PrescriptionDrugTimeTermCrossRef> crossRefs);

    @Query("SELECT timeTermId FROM prescription_drug_time_term_cross_ref WHERE drugUid = :drugUid")
    List<Integer> getTimeTermIdsForDrug(int drugUid);
}
