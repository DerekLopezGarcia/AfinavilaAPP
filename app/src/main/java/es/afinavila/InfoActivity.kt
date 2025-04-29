package es.afinavila

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.afinavila.ui.theme.AfinavilaAPPTheme
import es.afinavila.ui.theme.primaryLight
import es.afinavila.ui.theme.scrimLight
import es.afinavila.ui.theme.inverseSurfaceLight

class InfoActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AfinavilaAPPTheme {
                InfoScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = inverseSurfaceLight,
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.afinavila_logo),
                        contentDescription = "Logo Afinavila",
                        modifier = Modifier.fillMaxSize()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryLight
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = primaryLight
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_home_black_24dp),
                            contentDescription = "Inicio",
                            tint = scrimLight
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.homes),
                            color = scrimLight
                        )
                    },
                    selected = false,
                    onClick = {
                        gotoHome(context)
                    },
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = primaryLight
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.aviso_legal), color = scrimLight) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.politica_privacidad), color = scrimLight) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            val paragraphs = if (selectedTab == 0) {
                stringResource(R.string.texto_aviso_legal).split("\n\n")
            } else {
                stringResource(R.string.texto_politica_privacidad).split("\n\n")
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(paragraphs) { paragraph ->
                    Text(
                        text = paragraph,
                        fontSize = 16.sp,
                        color = scrimLight,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.Start)
                    )
                }
            }
        }
    }
}
