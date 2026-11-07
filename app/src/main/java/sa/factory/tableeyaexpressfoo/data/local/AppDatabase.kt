package sa.factory.tableeyaexpressfoo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TableeyaExpressEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): TableeyaExpressDao
}
