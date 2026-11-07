package sa.factory.tableeyaexpressfoo.di

import sa.factory.tableeyaexpressfoo.data.repository.TableeyaExpressRepositoryImpl
import sa.factory.tableeyaexpressfoo.domain.repository.TableeyaExpressRepository
import sa.factory.tableeyaexpressfoo.domain.usecase.*
import sa.factory.tableeyaexpressfoo.hardware.HardwareSensorDriver
import sa.factory.tableeyaexpressfoo.ui.viewmodel.TableeyaExpressViewModel

class DiContainer {
    val sensorDriver = HardwareSensorDriver()
    val repository: TableeyaExpressRepository = TableeyaExpressRepositoryImpl(sensorDriver)

    val getCatalogUseCase = GetCatalogUseCase(repository)
    val manageCartUseCase = ManageCartUseCase(repository)
    val processCheckoutUseCase = ProcessCheckoutUseCase(repository)
    val observeHardwareTelemetryUseCase = ObserveHardwareTelemetryUseCase(repository)

    fun provideViewModel(): TableeyaExpressViewModel = TableeyaExpressViewModel(
        getCatalogUseCase,
        manageCartUseCase,
        processCheckoutUseCase,
        observeHardwareTelemetryUseCase
    )
}
