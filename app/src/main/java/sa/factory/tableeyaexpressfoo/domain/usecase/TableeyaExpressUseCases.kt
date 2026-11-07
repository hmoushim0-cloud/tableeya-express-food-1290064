package sa.factory.tableeyaexpressfoo.domain.usecase

import sa.factory.tableeyaexpressfoo.domain.repository.TableeyaExpressRepository
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressItem
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressOrder
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressTelemetry
import kotlinx.coroutines.flow.Flow

class GetCatalogUseCase(private val repository: TableeyaExpressRepository) {
    operator fun invoke(): Flow<List<TableeyaExpressItem>> = repository.catalogFlow
}

class ManageCartUseCase(private val repository: TableeyaExpressRepository) {
    val cart = repository.activeCart
    fun add(item: TableeyaExpressItem) = repository.addToCart(item)
    fun remove(item: TableeyaExpressItem) = repository.removeFromCart(item)
    fun clear() = repository.clearCart()
}

class ProcessCheckoutUseCase(private val repository: TableeyaExpressRepository) {
    suspend operator fun invoke(paymentMethod: String = "Mada Contactless EMV"): TableeyaExpressOrder =
        repository.checkout(paymentMethod)
}

class ObserveHardwareTelemetryUseCase(private val repository: TableeyaExpressRepository) {
    operator fun invoke(): Flow<TableeyaExpressTelemetry> = repository.telemetryFlow
}
