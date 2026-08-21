package es.afinavila.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.afinavila.feature.comunidad.domain.Comunidad
import es.afinavila.feature.comunidad.domain.ComunidadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val codigo: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false,
    val comunidadNombre: String = "",
)

class LoginViewModel(private val repository: ComunidadRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onCodigoChange(value: String) {
        _state.value = _state.value.copy(codigo = value, error = null)
    }

    fun login() {
        if (_state.value.loading) return
        val codigo = _state.value.codigo.trim()
        if (codigo.isEmpty()) {
            _state.value = _state.value.copy(error = "Introduzca su código de acceso")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(loading = true, error = null)
                }
                val result = repository.login(codigo)
                result.fold(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            _state.value = _state.value.copy(
                                loading = false,
                                loginSuccess = true,
                                comunidadNombre = it.nombre
                            )
                        }
                    },
                    onFailure = { failure ->
                        withContext(Dispatchers.Main) {
                            _state.value = _state.value.copy(
                                loading = false,
                                error = failure.message ?: "Error desconocido"
                            )
                        }
                    }
                )
            } catch (e: java.lang.Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(loading = false, error = "Error de conexión")
                }
            }
        }
    }
}
