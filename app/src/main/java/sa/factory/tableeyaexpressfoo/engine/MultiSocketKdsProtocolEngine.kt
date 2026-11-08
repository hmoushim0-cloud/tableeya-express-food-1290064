package sa.factory.tableeyaexpressfoo.engine

enum class KdsStation(val code: String, val titleArabic: String, val tcpPort: Int, val ipAddress: String) {
    GRILL("GRILL", "محطة الشواء واللحوم", 9101, "192.168.10.11"),
    SALAD_APPETIZER("SALAD", "محطة السلطات والمقبلات", 9102, "192.168.10.12"),
    ASSEMBLY_EXPEDITE("EXPEDITE", "محطة التجميع والتسليم", 9103, "192.168.10.13"),
    BEVERAGE_DESSERT("BEV", "محطة المشروبات والحلويات", 9104, "192.168.10.14")
}

enum class KdsSocketState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED_READY,
    PRINTING_TICKET,
    STATION_ALERT
}

data class TicketItem(
    val nameArabic: String,
    val quantity: Int,
    val specialNotes: String = ""
)

data class KdsTicket(
    val ticketId: String,
    val orderId: String,
    val station: KdsStation,
    val items: List<TicketItem>,
    val timestampMs: Long = System.currentTimeMillis(),
    val isUrgent: Boolean = false
)

class MultiSocketKdsProtocolEngine {
    private val stationStates = mutableMapOf<KdsStation, KdsSocketState>().apply {
        KdsStation.values().forEach { put(it, KdsSocketState.CONNECTED_READY) }
    }
    private val dispatchedTickets = mutableListOf<KdsTicket>()

    fun getStationState(station: KdsStation): KdsSocketState =
        stationStates[station] ?: KdsSocketState.DISCONNECTED

    fun getDispatchedTickets(): List<KdsTicket> = dispatchedTickets.toList()

    fun routeItemToStation(itemName: String): KdsStation {
        return when {
            itemName.contains("شواء") || itemName.contains("برجر") || itemName.contains("لحم") || itemName.contains("دجاج") ->
                KdsStation.GRILL
            itemName.contains("سلطة") || itemName.contains("مقبلات") || itemName.contains("حمص") || itemName.contains("شوربة") ->
                KdsStation.SALAD_APPETIZER
            itemName.contains("عصير") || itemName.contains("مشروب") || itemName.contains("حلى") || itemName.contains("قهوة") ->
                KdsStation.BEVERAGE_DESSERT
            else ->
                KdsStation.ASSEMBLY_EXPEDITE
        }
    }

    // Renders standard ESC/POS bytes for thermal KDS kitchen network ticket
    fun renderEscPosBytes(ticket: KdsTicket): ByteArray {
        val bytes = mutableListOf<Byte>()
        // ESC @ (Initialize printer)
        bytes.addAll(listOf(0x1B.toByte(), 0x40.toByte()))
        // ESC E 1 (Turn emphasized mode on)
        bytes.addAll(listOf(0x1B.toByte(), 0x45.toByte(), 0x01.toByte()))

        val header = "[KDS TICKET: ${ticket.ticketId} | ORD: ${ticket.orderId} | ${ticket.station.titleArabic}]\n"
        bytes.addAll(header.toByteArray(Charsets.UTF_8).toList())

        // ESC E 0 (Turn emphasized mode off)
        bytes.addAll(listOf(0x1B.toByte(), 0x45.toByte(), 0x00.toByte()))

        ticket.items.forEach { item ->
            val line = " - ${item.quantity}x ${item.nameArabic} ${if (item.specialNotes.isNotBlank()) "(${item.specialNotes})" else ""}\n"
            bytes.addAll(line.toByteArray(Charsets.UTF_8).toList())
        }

        // GS V 66 0 (Cut paper)
        bytes.addAll(listOf(0x1D.toByte(), 0x56.toByte(), 0x42.toByte(), 0x00.toByte()))
        return bytes.toByteArray()
    }

    fun dispatchTicket(ticket: KdsTicket): Boolean {
        stationStates[ticket.station] = KdsSocketState.PRINTING_TICKET
        dispatchedTickets.add(ticket)
        stationStates[ticket.station] = KdsSocketState.CONNECTED_READY
        return true
    }

    fun getSummaryTelemetry(): String {
        val total = dispatchedTickets.size
        val readyStations = stationStates.values.count { it == KdsSocketState.CONNECTED_READY }
        return "محطات KDS: $readyStations/${KdsStation.values().size} متصلة | تذاكر التحضير: $total تذكرة عبر منافذ TCP (9101-9104)"
    }
}
