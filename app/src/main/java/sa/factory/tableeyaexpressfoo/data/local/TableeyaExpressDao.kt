package sa.factory.tableeyaexpressfoo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TableeyaExpressDao {
    @Query("SELECT * FROM food_items_cache ORDER BY id ASC")
    fun observeAllItems(): Flow<List<TableeyaExpressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TableeyaExpressEntity>)

    @Query("DELETE FROM food_items_cache")
    suspend fun clearAll()
}
