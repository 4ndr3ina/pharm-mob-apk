package gr.hua.dit.moddrugmanager.data;

import androidx.room.Embedded;
import androidx.room.Relation;

/**
 * Convenience POJO (not a table) that joins a PrescriptionDrug row with its
 * related TimeTerm row, so the UI (RecyclerView overview + detail screen,
 * requirement D) can show the term_name and order_index without a second query.
 */
public class PrescriptionDrugWithTimeTerm {

    @Embedded
    public PrescriptionDrug drug;

    @Relation(
            parentColumn = "time_term_id",
            entityColumn = "id"
    )
    public TimeTerm timeTerm;
}