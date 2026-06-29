package es.afinavila.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.afinavila.R
import es.afinavila.ui.theme.accent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Información Legal", color = androidx.compose.ui.graphics.Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = androidx.compose.ui.graphics.Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = accent)
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).navigationBarsPadding()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) { Text("Aviso Legal") }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) { Text("Privacidad") }
            }
            val text = if (selectedTab == 0)
                stringResource(R.string.texto_aviso_legal)
            else
                stringResource(R.string.texto_politica_privacidad)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                text.split("\n\n").forEach { paragraph ->
                    Text(paragraph, fontSize = 15.sp, modifier = Modifier.padding(bottom = 16.dp))
                }
            }
        }
    }
}
