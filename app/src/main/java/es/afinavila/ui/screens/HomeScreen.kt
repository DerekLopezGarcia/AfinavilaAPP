package es.afinavila.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.afinavila.ui.theme.SkylineFontFamily
import es.afinavila.ui.theme.accent
import es.afinavila.ui.theme.primary
import es.afinavila.ui.theme.primaryLight
import es.afinavila.ui.theme.surface

@Composable
fun HomeScreen(onNavigateToLogin: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().verticalScroll(rememberScrollState())) {
        // Hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
                .background(accent),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text("AFINAVILA", fontSize = 90.sp, fontWeight = FontWeight.Bold, color = primaryLight, letterSpacing = 6.sp, fontFamily = SkylineFontFamily)
                Spacer(Modifier.height(8.dp))
                Text("Administración", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("de Fincas", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryLight)
                Spacer(Modifier.height(16.dp))
                Text("Su comunidad en buenas manos", fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                    modifier = Modifier.height(50.dp)
                ) { Text("Área de Clientes", fontSize = 16.sp) }
            }
        }

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth().background(surface).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("2013", "Desde")
            StatItem("+50", "Comunidades")
            StatItem("+2.000", "Vecinos")
            StatItem("24/7", "Acceso")
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = primary)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
