package gr.hua.dit.moddrugmanager.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents a single Prescription Drug, as described in Comment #1 of the spec.
 *
 * NOTE on Time Term: a drug can now be taken at MULTIPLE times of day (e.g.
 * before-breakfast AND after-dinner), so the old single time_term_id foreign
 * key was removed. The relationship now lives in
 * PrescriptionDrugTimeTermCrossRef (a many-to-many junction table).
 *
 * NOTE on Doctor: doctorName/doctorLocation remain plain text fields, exactly
 * as the spec requires (and as used by the (F) export). They can now be
 * auto-filled by picking a previously saved Doctor (see Doctor.java), which
 * also carries geocoded coordinates - doctorLatitude/doctorLongitude are an
 * optional denormalized copy of those coordinates, used to drop an exact pin
 * on Google Maps in requirement (E) instead of relying on a text search.
 *
 * System-managed fields (null/false at creation, updated later by the user or
 * by the periodic background check, see Comment #3): isActive,
 * lastDateReceived, hasReceivedToday.
 */
@Entity(tableName = "prescription_drug")
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

    @Nullable
    @ColumnInfo(name = "doctor_name")
    private String doctorName;

    @Nullable
    @ColumnInfo(name = "doctor_location")
    private String doctorLocation;

    @Nullable
    @ColumnInfo(name = "doctor_latitude")
    private Double doctorLatitude;

    @Nullable
    @ColumnInfo(name = "doctor_longitude")
    private Double doctorLongitude;

    // ---- system-managed fields (Comment #3) ----

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    @Nullable
    @ColumnInfo(name = "last_date_received")
    private Long lastDateReceived;

    @ColumnInfo(name = "has_received_today")
    private boolean hasReceivedToday;

    public PrescriptionDrug(@NonNull String shortName, @Nullable String description,
                            long startDate, long endDate,
                            @Nullable String doctorName, @Nullable String doctorLocation,
                            @Nullable Double doctorLatitude, @Nullable Double doctorLongitude) {
        this.shortName = shortName;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.doctorName = doctorName;
        this.doctorLocation = doctorLocation;
        this.doctorLatitude = doctorLatitude;
        this.doctorLongitude = doctorLongitude;
        this.isActive = false;
        this.lastDateReceived = null;
        this.hasReceivedToday = false;
    }

    public int getUid() { return uid; }
    public void setUid(int uid) { this.uid = uid; }

    @NonNull public String getShortName() { return shortName; }
    public void setShortName(@NonNull String shortName) { this.shortName = shortName; }

    @Nullable public String getDescription() { return description; }
    public void setDescription(@Nullable String description) { this.description = description; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getEndDate() { return endDate; }
    public void setEndDate(long endDate) { this.endDate = endDate; }

    @Nullable public String getDoctorName() { return doctorName; }
    public void setDoctorName(@Nullable String doctorName) { this.doctorName = doctorName; }

    @Nullable public String getDoctorLocation() { return doctorLocation; }
    public void setDoctorLocation(@Nullable String doctorLocation) { this.doctorLocation = doctorLocation; }

    @Nullable public Double getDoctorLatitude() { return doctorLatitude; }
    public void setDoctorLatitude(@Nullable Double doctorLatitude) { this.doctorLatitude = doctorLatitude; }

    @Nullable public Double getDoctorLongitude() { return doctorLongitude; }
    public void setDoctorLongitude(@Nullable Double doctorLongitude) { this.doctorLongitude = doctorLongitude; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Nullable public Long getLastDateReceived() { return lastDateReceived; }
    public void setLastDateReceived(@Nullable Long lastDateReceived) { this.lastDateReceived = lastDateReceived; }

    public boolean isHasReceivedToday() { return hasReceivedToday; }
    public void setHasReceivedToday(boolean hasReceivedToday) { this.hasReceivedToday = hasReceivedToday; }
}