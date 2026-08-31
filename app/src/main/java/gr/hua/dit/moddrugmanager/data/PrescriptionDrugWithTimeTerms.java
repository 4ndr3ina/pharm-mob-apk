package gr.hua.dit.moddrugmanager.data;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

/**
 * Convenience POJO (not a table) joining a PrescriptionDrug with ALL of its
 * associated TimeTerms via the many-to-many cross-reference table. Replaces
 * the old one-time-term-only PrescriptionDrugWithTimeTerm.
 */
public class PrescriptionDrugWithTimeTerms {

    @Embedded
    public PrescriptionDrug drug;

    @Relation(
            parentColumn = "uid",
            entityColumn = "id",
            associateBy = @Junction(
                    value = PrescriptionDrugTimeTermCrossRef.class,
                    parentColumn = "drugUid",
                    entityColumn = "timeTermId"
            )
    )
    public List<TimeTerm> timeTerms;

    // Human-readable, chronologically sorted list e.g. "before-breakfast, at-lunch"
    public String getTimeTermsDisplay() {
        if (timeTerms == null || timeTerms.isEmpty()) return "-";
        List<TimeTerm> sorted = new java.util.ArrayList<>(timeTerms);
        sorted.sort((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(sorted.get(i).getTermName());
        }
        return sb.toString();
    }
}
