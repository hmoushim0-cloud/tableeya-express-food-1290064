package sa.factory.tableeyaexpressfoo.domain.repository

import sa.factory.tableeyaexpressfoo.model.TableeyaExpressItem
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressOrder
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TableeyaExpressRepository {
    val catalogFlow: Flow<List<TableeyaExpressItem>>
    val activeCart: StateFlow<List<TableeyaExpressItem>>
    val telemetryFlow: Flow<TableeyaExpressTelemetry>
    
    fun addToCart(item: TableeyaExpressItem)
    fun removeFromCart(item: TableeyaExpressItem)
    fun clearCart()
    suspend fun checkout(paymentMethod: String): TableeyaExpressOrder
    suspend fun refreshCatalog()
}
