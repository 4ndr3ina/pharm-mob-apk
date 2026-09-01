package gr.hua.dit.moddrugmanager.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "time_term")
public class TimeTerm {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "term_name")
    private String termName;

    @ColumnInfo(name = "order_index")
    private int orderIndex;

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
