package sa.factory.tableeyaexpressfoo

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import sa.factory.tableeyaexpressfoo.data.repository.TableeyaExpressRepositoryImpl
import sa.factory.tableeyaexpressfoo.engine.CloudKitchenOrderLifecycleEngine
import sa.factory.tableeyaexpressfoo.engine.KdsSocketState
import sa.factory.tableeyaexpressfoo.engine.KdsStation
import sa.factory.tableeyaexpressfoo.engine.KdsTicket
import sa.factory.tableeyaexpressfoo.engine.KitchenOrderItem
import sa.factory.tableeyaexpressfoo.engine.KitchenOrderStatus
import sa.factory.tableeyaexpressfoo.engine.MultiSocketKdsProtocolEngine
import sa.factory.tableeyaexpressfoo.engine.TicketItem
import sa.factory.tableeyaexpressfoo.hardware.HardwareSensorDriver
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressItem

class TableeyaExpressRegressionTest {

    @Test
    fun testCheckoutCalculationAndTax() = runBlocking {
        val sensorDriver = HardwareSensorDriver()
        val repository = TableeyaExpressRepositoryImpl(sensorDriver)

        val item1 = TableeyaExpressItem("t1", "طلب مطبخ تجريبي أ", "الوصف", 100.0, "معتمد", "عام")
        val item2 = TableeyaExpressItem("t2", "طلب مطبخ تجريبي ب", "الوصف", 200.0, "معتمد", "عام")

        repository.addToCart(item1)
        repository.addToCart(item2)

        val order = repository.checkout("Mada EMV")

        assertEquals(300.0, order.subtotalSar, 0.01)
        assertEquals(45.0, order.vatSar, 0.01)
        assertEquals(345.0, order.totalSar, 0.01)
        assertEquals(0, repository.activeCart.value.size)
    }

    @Test
    fun testKdsStationTcpPortMappings() {
        assertEquals(9101, KdsStation.GRILL.tcpPort)
        assertEquals(9102, KdsStation.SALAD_APPETIZER.tcpPort)
        assertEquals(9103, KdsStation.ASSEMBLY_EXPEDITE.tcpPort)
        assertEquals(9104, KdsStation.BEVERAGE_DESSERT.tcpPort)

        assertEquals("GRILL", KdsStation.GRILL.code)
        assertEquals("192.168.10.11", KdsStation.GRILL.ipAddress)
    }

    @Test
    fun testKdsItemStationRouting() {
        val kds = MultiSocketKdsProtocolEngine()
        assertEquals(KdsStation.GRILL, kds.routeItemToStation("برجر لحم مشوي عالفحم"))
        assertEquals(KdsStation.SALAD_APPETIZER, kds.routeItemToStation("سلطة سيزر بالدجاج"))
        assertEquals(KdsStation.BEVERAGE_DESSERT, kds.routeItemToStation("عصير برتقال طبيعي"))
        assertEquals(KdsStation.ASSEMBLY_EXPEDITE, kds.routeItemToStation("وجبة التوفير العائلية"))
    }

    @Test
    fun testEscPosTicketByteRendering() {
        val kds = MultiSocketKdsProtocolEngine()
        val ticket = KdsTicket(
            ticketId = "TCK-881",
            orderId = "ORD-402",
            station = KdsStation.GRILL,
            items = listOf(TicketItem("ستيك لحم ريب آي", 1, "طهي متوسط"))
        )

        val bytes = kds.renderEscPosBytes(ticket)
        assertTrue(bytes.isNotEmpty())
        // Verify ESC @ (0x1B, 0x40) printer initialization prefix
        assertEquals(0x1B.toByte(), bytes[0])
        assertEquals(0x40.toByte(), bytes[1])
        // Verify GS V cut paper sequence at the end
        val last4 = bytes.takeLast(4)
        assertEquals(0x1D.toByte(), last4[0])
        assertEquals(0x56.toByte(), last4[1])
    }

    @Test
    fun testKdsTicketDispatchAndStateManagement() {
        val kds = MultiSocketKdsProtocolEngine()
        val ticket = KdsTicket(
            ticketId = "TCK-990",
            orderId = "ORD-771",
            station = KdsStation.SALAD_APPETIZER,
            items = listOf(TicketItem("متبل باذنجان", 2))
        )

        val dispatched = kds.dispatchTicket(ticket)
        assertTrue(dispatched)
        assertEquals(1, kds.getDispatchedTickets().size)
        assertEquals(KdsSocketState.CONNECTED_READY, kds.getStationState(KdsStation.SALAD_APPETIZER))
    }

    @Test
    fun testCloudKitchenOrderCreationAndTotal() {
        val engine = CloudKitchenOrderLifecycleEngine()
        val items = listOf(
            KitchenOrderItem("ITM-1", "برجر واغيو مدخن", 2, KdsStation.GRILL, 65.0, 12),
            KitchenOrderItem("ITM-2", "بطاطس مبهرة", 2, KdsStation.GRILL, 15.0, 5)
        )

        val order = engine.createOrder("ORD-501", "سعود الدوسري", "جاهز (Jahez)", items)
        assertEquals("ORD-501", order.orderId)
        assertEquals(160.0, order.totalSar, 0.01)
        assertEquals(KitchenOrderStatus.RECEIVED, order.status)
        assertEquals(1, engine.getActiveOrders().size)
    }

    @Test
    fun testCloudKitchenSlaTargetCalculation() {
        val engine = CloudKitchenOrderLifecycleEngine()
        val items = listOf(
            KitchenOrderItem("ITM-1", "بيتزا مارجريتا نابولية", 1, KdsStation.GRILL, 50.0, 14),
            KitchenOrderItem("ITM-2", "مشروب غازي", 1, KdsStation.BEVERAGE_DESSERT, 8.0, 2)
        )

        val sla = engine.calculateSlaTargetMinutes(items)
        // Max prep 14 + 4 buffer = 18 minutes
        assertEquals(18, sla)
    }

    @Test
    fun testCourierHandoffOtpFormat() {
        val engine = CloudKitchenOrderLifecycleEngine()
        val otp = engine.generateCourierOtp()

        assertEquals(4, otp.length)
        assertTrue(otp.all { it.isDigit() })
    }

    @Test
    fun testOrderLifecycleProgression() {
        val engine = CloudKitchenOrderLifecycleEngine()
        val items = listOf(KitchenOrderItem("ITM-1", "وجبة شاورما دجاج", 1, KdsStation.GRILL, 28.0, 8))
        val order = engine.createOrder("ORD-602", "فيصل المطيري", "هنقرستيشن (HungerStation)", items)

        // 1. ADVANCE TO PREPARING
        val step1 = engine.advanceOrderStatus(order.orderId)
        assertNotNull(step1)
        assertEquals(KitchenOrderStatus.PREPARING, step1!!.status)

        // 2. ADVANCE TO READY_FOR_EXPEDITE
        val step2 = engine.advanceOrderStatus(order.orderId)
        assertNotNull(step2)
        assertEquals(KitchenOrderStatus.READY_FOR_EXPEDITE, step2!!.status)
        assertTrue(step2.isReadyForPickup)

        // 3. ADVANCE TO DISPATCHED
        val step3 = engine.advanceOrderStatus(order.orderId)
        assertNotNull(step3)
        assertEquals(KitchenOrderStatus.DISPATCHED_TO_COURIER, step3!!.status)

        // 4. ADVANCE TO DELIVERED
        val step4 = engine.advanceOrderStatus(order.orderId)
        assertNotNull(step4)
        assertEquals(KitchenOrderStatus.DELIVERED, step4!!.status)
    }

    @Test
    fun testHardwareSensorDriverLiveStreamFrames() = runBlocking {
        val driver = HardwareSensorDriver()
        assertNotNull(driver.getKdsEngine())
        assertNotNull(driver.getLifecycleEngine())

        val firstFrame = driver.observePeripheralStream().first()
        assertEquals("Kitchen Display System (KDS) TCP/IP Print Server", firstFrame.peripheralName)
        assertEquals("CONNECTING", firstFrame.connectionState)
    }
}
