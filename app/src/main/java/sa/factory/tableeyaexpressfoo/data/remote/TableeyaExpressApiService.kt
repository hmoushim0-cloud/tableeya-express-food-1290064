package sa.factory.tableeyaexpressfoo.data.remote

import sa.factory.tableeyaexpressfoo.model.TableeyaExpressItem
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressOrder

interface TableeyaExpressApiService {
    suspend fun fetchCatalog(): List<TableeyaExpressItem>
    suspend fun submitOrder(order: TableeyaExpressOrder): Boolean
}
