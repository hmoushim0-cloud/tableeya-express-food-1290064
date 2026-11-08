package sa.factory.tableeyaexpressfoo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sa.factory.tableeyaexpressfoo.model.TableeyaExpressItem
import sa.factory.tableeyaexpressfoo.ui.theme.*
import sa.factory.tableeyaexpressfoo.ui.viewmodel.TableeyaExpressUiState
import sa.factory.tableeyaexpressfoo.ui.viewmodel.TableeyaExpressViewModel

@Composable
fun HomeScreen(
    state: TableeyaExpressUiState,
    viewModel: TableeyaExpressViewModel,
    onItemClick: (TableeyaExpressItem) -> Unit
) {
    val categories = listOf("الكل", "الخدمات الرئيسية", "المراقبة الحية", "التقارير الضريبية", "الإعدادات")
    val defaultPeripheral = "Kitchen Display System (KDS) TCP/IP Print Server"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Column {
            Text("تطبيق طبلية إكسبريس - إدارة المطابخ السحابية", fontSize = 22.sp, fontWeight = FontWeight.Black, color = BrandTextPrimary)
            Text("منظومة food المتكاملة مع ربط Kitchen Display System (KDS) TCP/IP Print Server", fontSize = 12.sp, color = BrandTextSecondary)
        }

        // Hardware Status Capsule
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandAccentGlow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("العتاد الحي: ${state.telemetry?.peripheralName ?: defaultPeripheral}", fontWeight = FontWeight.Bold, color = BrandPrimary, fontSize = 13.sp)
                    Text(state.telemetry?.liveReading ?: "Kitchen Display System (KDS) TCP/IP Print Server: ONLINE | READY", fontSize = 11.sp, color = BrandTextSecondary)
                }
                Badge(containerColor = BrandPrimary) {
                    Text(state.telemetry?.status ?: "ONLINE", color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                }
            }
        }

        // Category Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                val isSelected = state.selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) BrandPrimary else BrandSurface,
                    modifier = Modifier.clickable { viewModel.selectCategory(cat) }
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.Black else BrandTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Service Catalog List
        val filteredList = if (state.selectedCategory == "الكل") {
            state.catalog
        } else {
            state.catalog.filter { it.category == state.selectedCategory }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(item) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandTextPrimary)
                            Text(item.description, fontSize = 12.sp, color = BrandTextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Badge(containerColor = BrandCard) {
                                Text(item.badge, fontSize = 10.sp, color = BrandTextSecondary, modifier = Modifier.padding(2.dp))
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${item.priceSar} ر.س", fontWeight = FontWeight.Black, color = BrandSecondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { onItemClick(item) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("إضافة", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
