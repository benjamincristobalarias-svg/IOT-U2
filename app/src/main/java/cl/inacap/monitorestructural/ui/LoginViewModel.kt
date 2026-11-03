package cl.inacap.monitorestructural.ui

import androidx.lifecycle.ViewModel
import cl.inacap.monitorestructural.data.AuthRepository
import cl.inacap.monitorestructural.model.validarCredenciales
import cl.inacap.monitorestructural.model.validarRegistro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Estado de las pantallas de Login y Registro
data class LoginUiState(
    val usuario: String? = null,   // nombre (o correo) del usuario con sesión; null = sin sesión
    val cargando: Boolean = false,
    val error: String? = null
)

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {

    // Firebase recuerda la sesión: si ya había entrado antes, parte con su nombre
    private val _uiState = MutableStateFlow(LoginUiState(usuario = auth.usuarioActual()))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val alTerminar = { usuario: String?, mensaje: String? ->
        _uiState.value = LoginUiState(usuario = usuario, error = mensaje)
    }

    fun ingresar(correo: String, clave: String) {
        // Primero se revisan los datos en el celular (no se envía nada mal escrito)
        val error = validarCredenciales(correo, clave, cuentaNueva = false)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        _uiState.update { it.copy(cargando = true, error = null) }
        auth.ingresar(correo.trim(), clave, alTerminar)
    }

    fun crearCuenta(nombre: String, correo: String, clave: String, confirmar: String) {
        val error = validarRegistro(nombre, correo, clave, confirmar)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        _uiState.update { it.copy(cargando = true, error = null) }
        auth.crearCuenta(nombre.trim(), correo.trim(), clave, alTerminar)
    }

    // Al cambiar entre Login y Registro se borra el mensaje de error anterior
    fun limpiarError() = _uiState.update { it.copy(error = null) }

    fun salir() {
        auth.salir()
        _uiState.value = LoginUiState()
    }
}
