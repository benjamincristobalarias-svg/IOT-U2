package cl.inacap.monitorestructural

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.inacap.monitorestructural.data.AuthRepository
import cl.inacap.monitorestructural.data.EstructurasRepository
import cl.inacap.monitorestructural.data.FakeEstructurasRepository
import cl.inacap.monitorestructural.data.FirebaseEstructurasRepository
import cl.inacap.monitorestructural.ui.ActivadorScreen
import cl.inacap.monitorestructural.ui.ActivadorViewModel
import cl.inacap.monitorestructural.ui.EstructurasViewModel
import cl.inacap.monitorestructural.ui.LoginScreen
import cl.inacap.monitorestructural.ui.LoginViewModel
import cl.inacap.monitorestructural.ui.MonitoreoScreen
import cl.inacap.monitorestructural.ui.PrincipalScreen
import cl.inacap.monitorestructural.ui.RegistroScreen
import cl.inacap.monitorestructural.ui.theme.MonitorEstructuralTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Fuentes de datos:
        // - AuthRepository: inicio de sesión (Firebase Authentication)
        // - FirebaseEstructurasRepository: base de datos (Realtime Database)
        // - FakeEstructurasRepository: simulador para el envío automático
        val auth = AuthRepository()
        val firebase = FirebaseEstructurasRepository()
        val simulador: EstructurasRepository = FakeEstructurasRepository()

        val factory = viewModelFactory {
            initializer { LoginViewModel(auth) }
            initializer { EstructurasViewModel(firebase) }
            initializer { ActivadorViewModel(firebase, simulador) }
        }
        val provider = ViewModelProvider(this, factory)
        val loginViewModel = provider.get(LoginViewModel::class.java)
        val monitoreoViewModel = provider.get(EstructurasViewModel::class.java)
        val activadorViewModel = provider.get(ActivadorViewModel::class.java)

        setContent {
            MonitorEstructuralTheme {
                val sesion by loginViewModel.uiState.collectAsStateWithLifecycle()
                val usuario = sesion.usuario
                // true = se muestra Registro; false = Login
                var registrando by rememberSaveable { mutableStateOf(false) }
                // Cuando entra a la app se olvida el Registro: al cerrar sesión vuelve al Login
                LaunchedEffect(usuario) { if (usuario != null) registrando = false }
                if (usuario == null) {
                    // Sin sesión solo se ven Login y Registro
                    if (registrando) {
                        RegistroScreen(
                            state = sesion,
                            onCrearCuenta = loginViewModel::crearCuenta,
                            onVolver = {
                                loginViewModel.limpiarError()
                                registrando = false
                            }
                        )
                    } else {
                        LoginScreen(
                            state = sesion,
                            onIngresar = loginViewModel::ingresar,
                            onIrARegistro = {
                                loginViewModel.limpiarError()
                                registrando = true
                            }
                        )
                    }
                } else {
                    val conectado by monitoreoViewModel.conectado.collectAsStateWithLifecycle()
                    PrincipalScreen(
                        usuario = usuario,
                        conectado = conectado,
                        onSalir = {
                            activadorViewModel.cambiarAutomatico(false)
                            loginViewModel.salir()
                        },
                        monitoreo = { modifier ->
                            val state by monitoreoViewModel.uiState.collectAsStateWithLifecycle()
                            MonitoreoScreen(state, modifier)
                        },
                        activador = { modifier ->
                            val state by activadorViewModel.uiState.collectAsStateWithLifecycle()
                            ActivadorScreen(
                                state = state,
                                onEnviar = activadorViewModel::enviarManual,
                                onAutomatico = activadorViewModel::cambiarAutomatico,
                                modifier = modifier
                            )
                        }
                    )
                }
            }
        }
    }
}
