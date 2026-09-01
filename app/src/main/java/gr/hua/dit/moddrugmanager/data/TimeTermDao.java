package gr.hua.dit.moddrugmanager.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface TimeTermDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<TimeTerm> timeTerms);

    @Query("SELECT * FROM time_term ORDER BY order_index ASC")
    LiveData<List<TimeTerm>> getAll();

    @Query("SELECT * FROM time_term ORDER BY order_index ASC")
    List<TimeTerm> getAllSync();

    @Query("SELECT * FROM time_term WHERE id = :id")
    TimeTerm getByIdSync(int id);

    @Query("SELECT COUNT(*) FROM time_term")
    int count();
}
