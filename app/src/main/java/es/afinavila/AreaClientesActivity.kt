package es.afinavila

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import es.afinavila.feature.comunidad.domain.GetIdComunidadByCodAccesUseCase
import es.afinavila.feature.comunidad.presentation.ComunidadActivity
import es.afinavila.ui.theme.AfinavilaAPPTheme
import es.afinavila.ui.theme.inverseSurfaceLight
import es.afinavila.ui.theme.onErrorContainerLight
import es.afinavila.ui.theme.primaryLight
import es.afinavila.ui.theme.scrimLight
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AreaClientesActivity : ComponentActivity() {
    internal val getIdComunidadByCodAccesUseCase: GetIdComunidadByCodAccesUseCase by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AfinavilaAPPTheme {
                AreaClientesScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaClientesScreen() {
    val context = LocalContext.current
    var codAcceso by remember { mutableStateOf("") }
    val activity = (context as? AreaClientesActivity)
    var errorTexto: Boolean = false

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(R.string.clientsArea), fontSize = 24.sp, color = scrimLight)
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = codAcceso,
                onValueChange = { codAcceso = it },
                label = {
                    Text(
                        if (errorTexto) stringResource(R.string.codigo_incorrecto_mensaje)
                        else stringResource(R.string.codigo),
                        color = if (errorTexto) onErrorContainerLight else scrimLight
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(
                        width = 1.dp,
                        color = if (errorTexto) onErrorContainerLight else primaryLight.copy(alpha = 0.5f)
                    )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                activity?.lifecycleScope?.launch {
                    val comunidadId = activity.getIdComunidadByCodAccesUseCase.invoke(codAcceso)
                    if (comunidadId != null) {
                        errorTexto = false
                        codAcceso = ""
                        navigateToComunidad(context, comunidadId)
                    } else {
                        errorTexto = true
                        codAcceso = ""
                    }
                }
            }) {
                Text(stringResource(R.string.acces), color = scrimLight)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.afinavila),
                fontSize = 24.sp,
                color = scrimLight,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

fun gotoHome(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    context.startActivity(intent)
}

fun navigateToComunidad(context: Context, comunidadId: Int) {
    val intent = Intent(context, ComunidadActivity::class.java).apply {
        putExtra("comunidadId", comunidadId)
    }
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun AreaClientesScreenPreview() {
    AfinavilaAPPTheme {
        AreaClientesScreen()
    }
}
