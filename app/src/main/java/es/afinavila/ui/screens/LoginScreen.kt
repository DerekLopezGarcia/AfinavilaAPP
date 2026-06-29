package es.afinavila.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Context
import es.afinavila.ui.theme.*
import es.afinavila.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) {
            ctx.getSharedPreferences("afinavila_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("comunidad_nombre", state.comunidadNombre)
                .apply()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(surface).navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Área de Clientes", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accent)
                Spacer(Modifier.height(4.dp))
                Text("Introduzca su código de acceso", fontSize = 14.sp, color = textGray)
                Spacer(Modifier.height(24.dp))

                var passwordVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = state.codigo,
                    onValueChange = viewModel::onCodigoChange,
                    label = { Text("Código de Acceso") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    singleLine = true,
                    isError = state.error != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (state.error != null) {
                    Text(state.error!!, color = errorColor, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = viewModel::login,
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Acceder", fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Si no conoce su código, contacte con la administración.",
                    fontSize = 12.sp,
                    color = textGrayLight
                )
            }
        }
    }
}
