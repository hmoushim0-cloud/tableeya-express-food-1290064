# تطبيق طبلية إكسبريس - إدارة المطابخ السحابية ونظام KDS (`tableeya-express-food-1290064`)
> منظومة food المتكاملة مع ربط Kitchen Display System (KDS) TCP/IP Print Server

- **Project ID**: `#1290064`
- **Commercial Archetype**: `food` (Cloud kitchen, restaurant orders, food delivery dispatcher)
- **Bound Hardware Peripheral**: `Kitchen Display System (KDS) TCP/IP Print Server`
- **Package Name**: `sa.factory.tableeyaexpressfoo`
- **Zero-Emoji Invariant**: Strictly Enforced (100% Vector Icons)

---

## [Sidekick Autonomous Update] Production Implementation Details

### 1. MultiSocketKdsProtocolEngine
- **Kitchen Display System Multi-Socket TCP/IP Protocol**: Dedicated communication channels across kitchen preparation stations (`GRILL` port 9101, `SALAD_APPETIZER` port 9102, `ASSEMBLY_EXPEDITE` port 9103, `BEVERAGE_DESSERT` port 9104).
- **Dynamic Station Router**: Automatically routes menu items based on culinary classification (steaks/burgers to grill, salads/appetizers to pantry, drinks/sweets to beverage bar).
- **ESC/POS Ticket Rendering**: Formats thermal print commands with printer initialization (`0x1B 0x40`), emphasized order headers, item quantity/notes, and automated paper cutting (`0x1D 0x56`).

### 2. CloudKitchenOrderLifecycleEngine
- **High-Throughput Order State Machine**: Seamless status transitions (`RECEIVED` -> `PREPARING` -> `READY_FOR_EXPEDITE` -> `DISPATCHED_TO_COURIER` -> `DELIVERED`).
- **Dynamic SLA Calculation**: Analyzes preparation complexity per item, station load, and packaging buffers to determine target preparation windows.
- **Courier Handoff Security**: Generates 4-digit verification OTPs for food delivery riders (Jahez, HungerStation, Ninja, Direct delivery).

### 3. HardwareSensorDriver Integration & MVVM StateFlow
- Dual-engine reactive telemetry stream feeding live KDS socket connection statuses, ticket counts, and preparation SLAs into ViewModel StateFlows.
- Clean Compose Material3 UI with zero single-quote template hazards and zero emoji unicode characters.

### 4. Unit Regression Test Suite
10 comprehensive unit tests covering:
- Repository checkout, 15% VAT calculation, and active cart clearing.
- KDS station TCP port mapping and network endpoints.
- Item category routing to kitchen preparation stations.
- ESC/POS thermal printer byte rendering and cutting commands.
- KDS ticket dispatch and socket state progression.
- Cloud kitchen order creation, multi-item pricing, and platform tracking.
- Dynamic SLA target timer calculation.
- 4-digit courier handoff OTP generation.
- Full 5-stage order lifecycle state progression.
- HardwareSensorDriver live reactive telemetry stream emission.

### 5. Build & Verification Commands
```bash
# Run unit regression test suite
./gradlew test

# Assemble debug APK for Pixel 6 emulator testing
./gradlew assembleDebug
```
