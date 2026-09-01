package gr.hua.dit.moddrugmanager.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "prescription_drug_time_term_cross_ref",
        primaryKeys = {"drugUid", "timeTermId"},
        foreignKeys = {
                @ForeignKey(entity = PrescriptionDrug.class, 
                        parentColumns = "uid", 
                        childColumns = "drugUid", 
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = TimeTerm.class, 
                        parentColumns = "id", 
                        childColumns = "timeTermId", 
                        onDelete = ForeignKey.RESTRICT)
        },
        indices = {@Index("drugUid"), @Index("timeTermId")}
)
public class PrescriptionDrugTimeTermCrossRef {

    @ColumnInfo(name = "drugUid")
    private int drugUid;

    @ColumnInfo(name = "timeTermId")
    private int timeTermId;

    public PrescriptionDrugTimeTermCrossRef(int drugUid, int timeTermId) {
        this.drugUid = drugUid;
        this.timeTermId = timeTermId;
    }

    public int getDrugUid() { return drugUid; }
    public void setDrugUid(int drugUid) { this.drugUid = drugUid; }

    public int getTimeTermId() { return timeTermId; }
    public void setTimeTermId(int timeTermId) { this.timeTermId = timeTermId; }
}
