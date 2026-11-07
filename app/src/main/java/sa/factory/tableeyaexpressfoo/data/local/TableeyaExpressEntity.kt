package sa.factory.tableeyaexpressfoo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressItem

@Entity(tableName = "food_items_cache")
data class TableeyaExpressEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val priceSar: Double,
    val badge: String,
    val category: String,
    val status: String
) {
    fun toDomain(): TableeyaExpressItem = TableeyaExpressItem(
        id = id,
        title = title,
        description = description,
        priceSar = priceSar,
        badge = badge,
        category = category,
        status = status
    )
}
