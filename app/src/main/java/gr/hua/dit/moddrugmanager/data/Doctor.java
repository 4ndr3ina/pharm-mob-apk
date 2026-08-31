package gr.hua.dit.moddrugmanager.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A saved doctor the user can reuse across multiple Prescription Drugs,
 * instead of retyping the name/address every time. Latitude/longitude are
 * filled in automatically (via Android's Geocoder) when the doctor is
 * created, so the app can drop an exact pin on the map instead of relying
 * on a text search.
 */
@Entity(tableName = "doctor")
public class Doctor {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "address")
    private String address;

    @Nullable
    @ColumnInfo(name = "latitude")
    private Double latitude;

    @Nullable
    @ColumnInfo(name = "longitude")
    private Double longitude;

    public Doctor(@NonNull String name, @NonNull String address,
                  @Nullable Double latitude, @Nullable Double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @NonNull public String getAddress() { return address; }
    public void setAddress(@NonNull String address) { this.address = address; }

    @Nullable public Double getLatitude() { return latitude; }
    public void setLatitude(@Nullable Double latitude) { this.latitude = latitude; }

    @Nullable public Double getLongitude() { return longitude; }
    public void setLongitude(@Nullable Double longitude) { this.longitude = longitude; }
}