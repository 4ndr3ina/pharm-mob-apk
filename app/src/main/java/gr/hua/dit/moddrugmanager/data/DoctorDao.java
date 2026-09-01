package gr.hua.dit.moddrugmanager.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface DoctorDao {

    @Insert
    long insert(Doctor doctor);

    @Query("SELECT * FROM doctor ORDER BY name ASC")
    LiveData<List<Doctor>> getAll();

    @Query("SELECT * FROM doctor ORDER BY name ASC")
    List<Doctor> getAllSync();

    @Query("SELECT * FROM doctor WHERE id = :id")
    Doctor getByIdSync(int id);
}
