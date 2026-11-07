package sa.factory.tableeyaexpressfoo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import sa.factory.tableeyaexpressfoo.domain.usecase.*
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressItem
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressOrder
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressTelemetry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TableeyaExpressUiState(
    val catalog: List<TableeyaExpressItem> = emptyList(),
    val filteredCatalog: List<TableeyaExpressItem> = emptyList(),
    val selectedCategory: String = "الكل",
    val cart: List<TableeyaExpressItem> = emptyList(),
    val telemetry: TableeyaExpressTelemetry? = null,
    val lastOrder: TableeyaExpressOrder? = null,
    val isCheckingOut: Boolean = false,
    val activeTab: String = "home"
)

class TableeyaExpressViewModel(
    private val getCatalogUseCase: GetCatalogUseCase,
    private val manageCartUseCase: ManageCartUseCase,
    private val processCheckoutUseCase: ProcessCheckoutUseCase,
    private val observeHardwareTelemetryUseCase: ObserveHardwareTelemetryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableeyaExpressUiState())
    val uiState: StateFlow<TableeyaExpressUiState> = _uiState.asStateFlow()

    init {
        // Observe Catalog
        getCatalogUseCase().onEach { list ->
            _uiState.update { it.copy(catalog = list, filteredCatalog = list) }
        }.launchIn(viewModelScope)

        // Observe Cart
        manageCartUseCase.cart.onEach { cartItems ->
            _uiState.update { it.copy(cart = cartItems) }
        }.launchIn(viewModelScope)

        // Observe Telemetry
        observeHardwareTelemetryUseCase().onEach { telem ->
            _uiState.update { it.copy(telemetry = telem) }
        }.launchIn(viewModelScope)
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            val filtered = if (category == "الكل") state.catalog else state.catalog.filter { it.category == category }
            state.copy(selectedCategory = category, filteredCatalog = filtered)
        }
    }

    fun switchTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun addToCart(item: TableeyaExpressItem) = manageCartUseCase.add(item)
    fun removeFromCart(item: TableeyaExpressItem) = manageCartUseCase.remove(item)

    fun checkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true) }
            val order = processCheckoutUseCase()
            _uiState.update { it.copy(isCheckingOut = false, lastOrder = order, activeTab = "checkout") }
        }
    }
}
