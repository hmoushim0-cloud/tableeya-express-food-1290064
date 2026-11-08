package sa.factory.tableeyaexpressfoo.engine

import kotlin.math.max
import kotlin.random.Random

enum class KitchenOrderStatus(val titleArabic: String) {
    RECEIVED("تم الاستلام وجدولة المحطات"),
    PREPARING("قيد التحضير المباشر"),
    READY_FOR_EXPEDITE("جاهز في محطة التجميع"),
    DISPATCHED_TO_COURIER("تم التسليم لمندوب التوصيل"),
    DELIVERED("تم التوصيل للعميل بنجاح")
}

data class KitchenOrderItem(
    val itemId: String,
    val nameArabic: String,
    val quantity: Int,
    val station: KdsStation,
    val unitPriceSar: Double,
    val prepMinutes: Int
)

data class CloudKitchenOrder(
    val orderId: String,
    val customerName: String,
    val platformName: String, // Jahez, HungerStation, Ninja, Direct
    val items: List<KitchenOrderItem>,
    val status: KitchenOrderStatus = KitchenOrderStatus.RECEIVED,
    val targetSlaMinutes: Int,
    val elapsedSeconds: Int = 0,
    val courierHandoffOtp: String,
    val totalSar: Double
) {
    val isReadyForPickup: Boolean get() = status == KitchenOrderStatus.READY_FOR_EXPEDITE
}

class CloudKitchenOrderLifecycleEngine {
    private val activeOrders = mutableListOf<CloudKitchenOrder>()

    fun getActiveOrders(): List<CloudKitchenOrder> = activeOrders.toList()

    fun calculateSlaTargetMinutes(items: List<KitchenOrderItem>): Int {
        if (items.isEmpty()) return 10
        val maxPrepTime = items.maxOf { it.prepMinutes }
        // Base prep time + buffer for packaging and multi-station consolidation
        return max(8, maxPrepTime + 4)
    }

    fun generateCourierOtp(): String {
        return "%04d".format(Random.nextInt(1000, 9999))
    }

    fun createOrder(
        orderId: String,
        customerName: String,
        platformName: String,
        items: List<KitchenOrderItem>
    ): CloudKitchenOrder {
        val sla = calculateSlaTargetMinutes(items)
        val total = items.sumOf { it.unitPriceSar * it.quantity }
        val order = CloudKitchenOrder(
            orderId = orderId,
            customerName = customerName,
            platformName = platformName,
            items = items,
            status = KitchenOrderStatus.RECEIVED,
            targetSlaMinutes = sla,
            elapsedSeconds = 0,
            courierHandoffOtp = generateCourierOtp(),
            totalSar = total
        )
        activeOrders.add(order)
        return order
    }

    fun advanceOrderStatus(orderId: String): CloudKitchenOrder? {
        val index = activeOrders.indexOfFirst { it.orderId == orderId }
        if (index == -1) return null

        val current = activeOrders[index]
        val nextStatus = when (current.status) {
            KitchenOrderStatus.RECEIVED -> KitchenOrderStatus.PREPARING
            KitchenOrderStatus.PREPARING -> KitchenOrderStatus.READY_FOR_EXPEDITE
            KitchenOrderStatus.READY_FOR_EXPEDITE -> KitchenOrderStatus.DISPATCHED_TO_COURIER
            KitchenOrderStatus.DISPATCHED_TO_COURIER -> KitchenOrderStatus.DELIVERED
            KitchenOrderStatus.DELIVERED -> KitchenOrderStatus.DELIVERED
        }

        val updated = current.copy(
            status = nextStatus,
            elapsedSeconds = current.elapsedSeconds + 180
        )
        activeOrders[index] = updated
        return updated
    }

    fun isSlaBreached(orderId: String): Boolean {
        val order = activeOrders.find { it.orderId == orderId } ?: return false
        val targetSeconds = order.targetSlaMinutes * 60
        return order.elapsedSeconds > targetSeconds && order.status != KitchenOrderStatus.DELIVERED
    }
}
