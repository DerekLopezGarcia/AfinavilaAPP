package es.afinavila

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import es.afinavila.ui.screens.*
import es.afinavila.ui.theme.AfinavilaAPPTheme
import es.afinavila.ui.theme.primary
import es.afinavila.ui.theme.primaryHover
import es.afinavila.ui.viewmodel.ClienteViewModel
import es.afinavila.ui.viewmodel.LoginViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by inject()
    private val clienteViewModel: ClienteViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AfinavilaAPPTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    Scaffold(
                        bottomBar = {
                            NavigationBar(containerColor = primary) {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                                    icon = { Icon(Icons.Default.Home, "Inicio") },
                                    label = { Text("Inicio") },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.White.copy(0.6f), indicatorColor = primaryHover)
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "clientes" || currentRoute == "cliente",
                                    onClick = { navController.navigate("clientes") { popUpTo("home") } },
                                    icon = { Icon(Icons.Default.Lock, "Clientes") },
                                    label = { Text("Clientes") },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.White.copy(0.6f), indicatorColor = primaryHover)
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "legal",
                                    onClick = { navController.navigate("legal") { popUpTo("home") } },
                                    icon = { Icon(Icons.Default.Info, "Legal") },
                                    label = { Text("Legal") },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, unselectedIconColor = Color.White.copy(0.6f), indicatorColor = primaryHover)
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(modifier = Modifier.padding(innerPadding), navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(onNavigateToLogin = { navController.navigate("clientes") })
                            }
                            composable("clientes") {
                                LoginScreen(
                                    viewModel = loginViewModel,
                                    onLoginSuccess = {
                                        clienteViewModel.load()
                                        navController.navigate("cliente") { popUpTo("clientes") { inclusive = true } }
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("cliente") {
                                ClienteScreen(
                                    viewModel = clienteViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("legal") {
                                LegalScreen(onBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}
