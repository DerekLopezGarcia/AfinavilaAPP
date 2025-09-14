package es.afinavila.feature.comunidad.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import es.afinavila.feature.comunidad.data.remote.ComunidadApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class WebViewActivity : ComponentActivity() {
    private val comunidadApi: ComunidadApi by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val archivoId = intent.getIntExtra("archivoId", -1)
        if (archivoId != -1) {
            abrirUrlDeArchivo(archivoId)
        } else {
            Toast.makeText(this, "Archivo ID no encontrado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun abrirUrlDeArchivo(archivoId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = comunidadApi.service.getArchivoById(archivoId)
                if (response.isSuccessful) {
                    val url = response.body()?.url
                    if (!url.isNullOrEmpty()) {
                        runOnUiThread {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@WebViewActivity, "URL no encontrada", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@WebViewActivity, "Error al obtener la URL", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@WebViewActivity, "Error al abrir el archivo", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
