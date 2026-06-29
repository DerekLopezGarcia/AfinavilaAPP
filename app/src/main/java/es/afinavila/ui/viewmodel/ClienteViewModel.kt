package es.afinavila.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.afinavila.feature.comunidad.domain.Archivo
import es.afinavila.feature.comunidad.domain.ComunidadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class ClienteUiState(
    val comunidadNombre: String = "",
    val archivos: List<Archivo> = emptyList(),
    val tab: String = "Actas",
    val loading: Boolean = false,
    val error: String? = null,
    val pdfOpen: Archivo? = null,
)

class ClienteViewModel(
    private val repository: ComunidadRepository,
    private val app: Application
) : ViewModel() {

    private val _state = MutableStateFlow(ClienteUiState())
    val state: StateFlow<ClienteUiState> = _state.asStateFlow()

    private val _pdfFile = MutableStateFlow<File?>(null)
    val pdfFile: StateFlow<File?> = _pdfFile.asStateFlow()

    private val _pageCount = MutableStateFlow(0)
    val pageCount: StateFlow<Int> = _pageCount.asStateFlow()

    fun load() {
        if (_state.value.loading) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Estado loading inicial — se muestra durante la navegación
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(loading = true, error = null)
                }

                // Primera llamada: getMe
                val comunidadResult = repository.getMe()
                val nombre = comunidadResult.getOrNull()?.nombre ?: run {
                    val cached = repository.getCachedNombre()
                    withContext(Dispatchers.Main) {
                        _state.value = _state.value.copy(
                            comunidadNombre = cached ?: "",
                            error = if (cached != null) "Sesión expirada" else "Sesión expirada",
                            loading = false
                        )
                    }
                    return@launch
                }

                Log.d("Afinavila", "getMe nombre: $nombre")

                // Segunda llamada: archivos
                val archivosResult = repository.getArchivosSession()
                val archivos = archivosResult.getOrNull() ?: emptyList()
                val error = if (archivosResult.isFailure) "Error al cargar documentos" else null

                // ÚNICA actualización de estado con todos los datos
                // Así se evitan recomposiciones múltiples durante la animación
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        comunidadNombre = nombre,
                        archivos = archivos,
                        error = error,
                        loading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("Afinavila", "load error", e)
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(loading = false, error = "Error de conexión")
                }
            }
        }
    }

    fun setTab(tab: String) {
        _state.value = _state.value.copy(tab = tab)
    }

    fun openPdf(archivo: Archivo) {
        if (_state.value.pdfOpen != null) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(pdfOpen = archivo, error = null)
                }
                val result = repository.getPdfSession(archivo.id)
                result.fold(
                    onSuccess = { bytes ->
                        try {
                            val pdfDir = File(app.cacheDir, "pdfs").also { it.mkdirs() }
                            val file = File(pdfDir, "pdf_${archivo.id}.pdf")
                            file.writeBytes(bytes)
                            withContext(Dispatchers.Main) {
                                _pdfFile.value = file
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                _state.value = _state.value.copy(error = "Error al guardar PDF")
                            }
                        }
                    },
                    onFailure = {
                        withContext(Dispatchers.Main) {
                            _state.value = _state.value.copy(error = "Error al cargar PDF")
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(error = "Error de conexión")
                }
            }
        }
    }

    fun setPageCount(count: Int) {
        _pageCount.value = count
    }

    fun closePdf() {
        _pdfFile.value?.delete()
        _pdfFile.value = null
        _pageCount.value = 0
        _state.value = _state.value.copy(pdfOpen = null)
    }
}
