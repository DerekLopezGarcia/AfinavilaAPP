package es.afinavila.feature.comunidad.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import es.afinavila.R
import es.afinavila.feature.comunidad.domain.Archivo
import es.afinavila.feature.comunidad.domain.GetArchivosByComunidadIdUseCase
import es.afinavila.feature.comunidad.domain.GetComunidadNameByIdUseCase
import es.afinavila.gotoHome
import es.afinavila.ui.theme.AfinavilaAPPTheme
import es.afinavila.ui.theme.inverseSurfaceLight
import es.afinavila.ui.theme.primaryLight
import es.afinavila.ui.theme.scrimLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class ComunidadActivity : ComponentActivity() {
    internal val getComunidadNameByIdUseCase: GetComunidadNameByIdUseCase by inject()
    internal val getArchivosByComunidadIdUseCase: GetArchivosByComunidadIdUseCase by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val comunidadId = intent.getIntExtra("comunidadId", -1)
        setContent {
            AfinavilaAPPTheme {
                ClientScreen(
                    comunidadId = comunidadId
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(comunidadId: Int) {
    val filtro = listOf("Todos", "Actas", "Evoluciones", "Extractos", "Otros")
    val context = LocalContext.current
    val activity = (context as? ComunidadActivity)
    var comunidadName by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf(filtro[0]) }
    var archivos by remember { mutableStateOf(listOf<Archivo>()) }
    var filteredArchivos by remember { mutableStateOf(listOf<Archivo>()) }

    LaunchedEffect(comunidadId) {
        comunidadName = withContext(Dispatchers.IO) {
            activity?.getComunidadNameByIdUseCase?.invoke(comunidadId)
        }
        archivos = withContext(Dispatchers.IO) {
            activity?.getArchivosByComunidadIdUseCase?.invoke(comunidadId) ?: emptyList()
        }
        filteredArchivos = archivos // Inicialmente mostrar todos los archivos
    }

    LaunchedEffect(selectedFilter) {
        filteredArchivos = when (selectedFilter) {
            "Todos" -> archivos
            "Actas" -> archivos.filter { it.descripcion.contains("Acta", ignoreCase = true) }
            "Evoluciones" -> archivos.filter {
                it.descripcion.contains(
                    "Evolución",
                    ignoreCase = true
                )
            }

            "Extractos" -> archivos.filter {
                it.descripcion.contains(
                    "Extracto",
                    ignoreCase = true
                )
            }

            "Otros" -> archivos.filter {
                !it.descripcion.contains("Acta", ignoreCase = true) &&
                        !it.descripcion.contains("Evolución", ignoreCase = true) &&
                        !it.descripcion.contains("Extracto", ignoreCase = true)
            }

            else -> archivos
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = comunidadName ?: "Cargando...", fontSize = 20.sp)
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
            .background(inverseSurfaceLight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Filtro como pestañas
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                items(filtro.size) { index ->
                    val item = filtro[index]
                    Text(
                        text = item,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable { selectedFilter = item }
                            .background(
                                color = if (selectedFilter == item) primaryLight else Color.LightGray,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (selectedFilter == item) Color.White else Color.Black,
                        fontWeight = if (selectedFilter == item) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Cuadrícula de archivos
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(filteredArchivos.size) { index ->
                    val archivo = filteredArchivos[index]
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                activity?.lifecycleScope?.launch(Dispatchers.IO) {
                                    withContext(Dispatchers.Main) {
                                        if (true) {
                                            val intent =
                                                Intent(context, WebViewActivity::class.java).apply {
                                                    putExtra("archivoId", archivo.id)
                                                }
                                            context.startActivity(intent)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "No se pudo cargar el archivo",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = archivo.descripcion,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
