package gr.hua.dit.moddrugmanager.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents a single Prescription Drug, as described in Comment #1 of the spec.
 *
 * User-provided fields: shortName, description, startDate, endDate, timeTermId,
 * doctorName (nullable), doctorLocation (nullable).
 *
 * System-managed fields (null/false at creation, updated later by the user or by
 * the periodic background check, see Comment #3): isActive, lastDateReceived,
 * hasReceivedToday.
 *
 * Dates are stored as epoch millis (long) so we can easily compare against
 * System.currentTimeMillis() / "today" when computing isActive and hasReceivedToday.
 */
@Entity(
        tableName = "prescription_drug",
        foreignKeys = @ForeignKey(
                entity = TimeTerm.class,
                parentColumns = "id",
                childColumns = "time_term_id",
                onDelete = ForeignKey.RESTRICT
        ),
        indices = {@Index("time_term_id")}
)
public class PrescriptionDrug {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    @NonNull
    @ColumnInfo(name = "short_name")
    private String shortName;

    @Nullable
    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "start_date")
    private long startDate; // epoch millis, day-precision

    @ColumnInfo(name = "end_date")
    private long endDate; // epoch millis, day-precision

    @ColumnInfo(name = "time_term_id")
    private int timeTermId; // FK -> TimeTerm.id

    @Nullable
    @ColumnInfo(name = "doctor_name")
    private String doctorName;

    @Nullable
    @ColumnInfo(name = "doctor_location")
    private String doctorLocation;

    // ---- system-managed fields (Comment #3) ----

    @ColumnInfo(name = "is_active")
    private boolean isActive; // false/null-equivalent at creation

    @Nullable
    @ColumnInfo(name = "last_date_received")
    private Long lastDateReceived; // null at creation; epoch millis once set

    @ColumnInfo(name = "has_received_today")
    private boolean hasReceivedToday; // false at creation

    public PrescriptionDrug(@NonNull String shortName, @Nullable String description,
                            long startDate, long endDate, int timeTermId,
                            @Nullable String doctorName, @Nullable String doctorLocation) {
        this.shortName = shortName;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.timeTermId = timeTermId;
        this.doctorName = doctorName;
        this.doctorLocation = doctorLocation;
        // system-managed defaults on creation
        this.isActive = false;
        this.lastDateReceived = null;
        this.hasReceivedToday = false;
    }

    // ---- getters / setters ----

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    @NonNull
    public String getShortName() {
        return shortName;
    }

    public void setShortName(@NonNull String shortName) {
        this.shortName = shortName;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public int getTimeTermId() {
        return timeTermId;
    }

    public void setTimeTermId(int timeTermId) {
        this.timeTermId = timeTermId;
    }

    @Nullable
    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(@Nullable String doctorName) {
        this.doctorName = doctorName;
    }

    @Nullable
    public String getDoctorLocation() {
        return doctorLocation;
    }

    public void setDoctorLocation(@Nullable String doctorLocation) {
        this.doctorLocation = doctorLocation;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Nullable
    public Long getLastDateReceived() {
        return lastDateReceived;
    }

    public void setLastDateReceived(@Nullable Long lastDateReceived) {
        this.lastDateReceived = lastDateReceived;
    }

    public boolean isHasReceivedToday() {
        return hasReceivedToday;
    }

    public void setHasReceivedToday(boolean hasReceivedToday) {
        this.hasReceivedToday = hasReceivedToday;
    }
}