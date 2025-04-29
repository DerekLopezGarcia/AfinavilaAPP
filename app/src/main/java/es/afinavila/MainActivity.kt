package es.afinavila

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.afinavila.ui.theme.AfinavilaAPPTheme
import es.afinavila.ui.theme.primaryLight
import androidx.core.net.toUri
import es.afinavila.ui.theme.inverseSurfaceLight
import es.afinavila.ui.theme.scrimLight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AfinavilaAPPTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    Scaffold(
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
                            painterResource(id = R.drawable.ic_dashboard_black_24dp), 
                            contentDescription = "Área Clientes",
                            tint = scrimLight
                        ) 
                    },
                    label = { 
                        Text(
                            text = stringResource(id = R.string.clientsArea),
                            color = scrimLight
                        ) 
                    },
                    selected = false,
                    onClick = {
                        navigateToAreaClientes(context)
                    },
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            painterResource(id = R.drawable.baseline_public_24), 
                            contentDescription = "Home",
                            tint = scrimLight
                        ) 
                    },
                    label = { 
                        Text(
                            text = stringResource(id = R.string.web),
                            color = scrimLight
                        ) 
                    },
                    selected = false,
                    onClick = {
                        val webIntent = Intent(Intent.ACTION_VIEW,
                            "https://www.afinavila.es".toUri())
                        context.startActivity(webIntent)
                    },
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            painterResource(id = R.drawable.baseline_info_24), 
                            contentDescription = "Info",
                            tint = scrimLight
                        ) 
                    },
                    label = { 
                        Text(
                            text = stringResource(id = R.string.info),
                            color = scrimLight
                        ) 
                    },
                    selected = false,
                    onClick = {
                        navigateToInfo(context)
                    },
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(inverseSurfaceLight),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.homepage),
                    contentDescription = "homepage",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                Text(
                    text = stringResource(id = R.string.slogan),
                    fontSize = 24.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
fun navigateToAreaClientes(context: Context) {
    val intent = Intent(context, AreaClientesActivity::class.java)
    context.startActivity(intent)
}
fun navigateToInfo(context: Context) {
    val intent = Intent(context, InfoActivity::class.java)
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AfinavilaAPPTheme {
        MainScreen()
    }
}

