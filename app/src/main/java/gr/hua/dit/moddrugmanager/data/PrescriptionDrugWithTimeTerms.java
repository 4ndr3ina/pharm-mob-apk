package gr.hua.dit.moddrugmanager.data;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

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
