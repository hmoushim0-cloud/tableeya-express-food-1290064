package sa.factory.tableeyaexpressfoo.hardware

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import sa.factory.tableeyaexpressfoo.engine.CloudKitchenOrderLifecycleEngine
import sa.factory.tableeyaexpressfoo.engine.KdsStation
import sa.factory.tableeyaexpressfoo.engine.KdsTicket
import sa.factory.tableeyaexpressfoo.engine.KitchenOrderItem
import sa.factory.tableeyaexpressfoo.engine.MultiSocketKdsProtocolEngine
import sa.factory.tableeyaexpressfoo.engine.TicketItem

data class HardwareTelemetryFrame(
    val peripheralName: String,
    val connectionState: String,
    val livePayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HardwareSensorDriver {
    private val peripheral = "Kitchen Display System (KDS) TCP/IP Print Server"
    private val kdsEngine = MultiSocketKdsProtocolEngine()
    private val lifecycleEngine = CloudKitchenOrderLifecycleEngine()

    fun getKdsEngine(): MultiSocketKdsProtocolEngine = kdsEngine
    fun getLifecycleEngine(): CloudKitchenOrderLifecycleEngine = lifecycleEngine

    fun observePeripheralStream(): Flow<HardwareTelemetryFrame> = flow {
        emit(HardwareTelemetryFrame(peripheral, "CONNECTING", "تهيئة الاتصال بمحطات KDS ومزامنة منافذ TCP (9101-9104)..."))
        delay(300)

        // Seed initial kitchen orders and dispatch tickets
        val items = listOf(
            KitchenOrderItem("ITEM-01", "وجبة برجر لحم مشوي فاخر", 2, KdsStation.GRILL, 45.0, 10),
            KitchenOrderItem("ITEM-02", "سلطة يونانية طازجة", 1, KdsStation.SALAD_APPETIZER, 22.0, 4),
            KitchenOrderItem("ITEM-03", "مشروب موهيتو بارد", 2, KdsStation.BEVERAGE_DESSERT, 16.0, 3)
        )
        val order = lifecycleEngine.createOrder("ORD-9912", "عبدالله العتيبي", "جاهز (Jahez)", items)

        val ticketGrill = KdsTicket("TCK-01", order.orderId, KdsStation.GRILL, listOf(TicketItem("برجر لحم مشوي", 2, "بدون بصل")))
        val ticketSalad = KdsTicket("TCK-02", order.orderId, KdsStation.SALAD_APPETIZER, listOf(TicketItem("سلطة يونانية", 1)))
        kdsEngine.dispatchTicket(ticketGrill)
        kdsEngine.dispatchTicket(ticketSalad)

        emit(HardwareTelemetryFrame(peripheral, "ONLINE", "خادم KDS متصل بـ 4 محطات تحضير عبر بروتوكول TCP/IP"))

        var step = 0
        while (true) {
            delay(1500)
            step++
            if (step % 4 == 0) {
                lifecycleEngine.advanceOrderStatus(order.orderId)
            }
            val currentOrder = lifecycleEngine.getActiveOrders().find { it.orderId == order.orderId }
            val statusName = currentOrder?.status?.titleArabic ?: "قيد التحضير"
            val otp = currentOrder?.courierHandoffOtp ?: "4821"
            val sla = currentOrder?.targetSlaMinutes ?: 14
            val ticketsCount = kdsEngine.getDispatchedTickets().size

            val payload = "KDS: 4 محطات أونلاين | تذاكر: $ticketsCount | الطلب: ${order.orderId} ($statusName) | رمز المندوب: $otp | SLA: $sla د | المنصة: ${order.platformName}"

            emit(HardwareTelemetryFrame(peripheral, "ACTIVE_STREAMING", payload))
        }
    }
}
