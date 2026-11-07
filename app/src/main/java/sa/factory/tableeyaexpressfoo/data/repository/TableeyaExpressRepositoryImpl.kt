package sa.factory.tableeyaexpressfoo.data.repository

import sa.factory.tableeyaexpressfoo.domain.repository.TableeyaExpressRepository
import sa.factory.tableeyaexpressfoo.hardware.HardwareSensorDriver
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressItem
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressOrder
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TableeyaExpressRepositoryImpl(
    private val sensorDriver: HardwareSensorDriver
) : TableeyaExpressRepository {

    private val _catalog = MutableStateFlow<List<TableeyaExpressItem>>(listOf(
        TableeyaExpressItem("1", "الباقة التنفيذية المعتمدة", "تكامل مباشر مع Kitchen Display System (KDS) TCP/IP Print Server", 180.0, "معتمد", "الخدمات الرئيسية"),
        TableeyaExpressItem("2", "خدمة المعالجة الفورية", "أداء أصيل بسرعة 60 إطار في الثانية", 95.0, "فوري", "المراقبة الحية"),
        TableeyaExpressItem("3", "باقة الصيانة والدعم السحابي", "تشفير كامل للبيانات المحلية ومزامنة آمنة", 250.0, "شامل", "التقارير الضريبية")
    ))
    override val catalogFlow: Flow<List<TableeyaExpressItem>> = _catalog.asStateFlow()

    private val _cart = MutableStateFlow<List<TableeyaExpressItem>>(emptyList())
    override val activeCart: StateFlow<List<TableeyaExpressItem>> = _cart.asStateFlow()

    override val telemetryFlow: Flow<TableeyaExpressTelemetry> = sensorDriver.observePeripheralStream()
        .map { frame ->
            TableeyaExpressTelemetry(
                peripheralName = frame.peripheralName,
                status = frame.connectionState,
                liveReading = frame.livePayload,
                batteryPct = 96,
                signalDbm = -54
            )
        }

    override fun addToCart(item: TableeyaExpressItem) {
        val current = _cart.value.toMutableList()
        current.add(item)
        _cart.value = current
    }

    override fun removeFromCart(item: TableeyaExpressItem) {
        val current = _cart.value.toMutableList()
        current.remove(item)
        _cart.value = current
    }

    override fun clearCart() {
        _cart.value = emptyList()
    }

    override suspend fun checkout(paymentMethod: String): TableeyaExpressOrder {
        val currentItems = _cart.value.toList()
        val subtotal = currentItems.sumOf { it.priceSar }
        val vat = subtotal * 0.15
        val total = subtotal + vat
        val order = TableeyaExpressOrder(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            items = currentItems,
            subtotalSar = subtotal,
            vatSar = vat,
            totalSar = total,
            paymentMethod = paymentMethod
        )
        clearCart()
        return order
    }

    override suspend fun refreshCatalog() {
        // Offline-first sync logic
    }
}
