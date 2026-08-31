package gr.hua.dit.moddrugmanager.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Lookup table holding the fixed list of "Time Terms" a Prescription Drug can be
 * associated with (Comment #2 of the spec):
 * before-breakfast, at-breakfast, after-breakfast,
 * before-lunch,     at-lunch,     after-lunch,
 * before-dinner,    at-dinner,    after-dinner
 *
 * The table is pre-populated once, when the database is created (see AppDatabase).
 * orderIndex is used to sort Prescription Drugs by "time of day" in requirement (D).
 */
@Entity(tableName = "time_term")
public class TimeTerm {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "term_name")
    private String termName; // e.g. "before-breakfast"

    @ColumnInfo(name = "order_index")
    private int orderIndex; // 0..8, defines chronological order across the day

    public TimeTerm(@NonNull String termName, int orderIndex) {
        this.termName = termName;
        this.orderIndex = orderIndex;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getTermName() {
        return termName;
    }

    public void setTermName(@NonNull String termName) {
        this.termName = termName;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}