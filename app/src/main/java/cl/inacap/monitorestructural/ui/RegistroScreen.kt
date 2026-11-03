package cl.inacap.monitorestructural.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

// Pantalla de Registro: nombre de usuario, correo, contraseña y confirmar contraseña.
// El nombre se guarda en el perfil de Firebase y es el que se muestra en la app.
@Composable
fun RegistroScreen(
    state: LoginUiState,
    onCrearCuenta: (nombre: String, correo: String, clave: String, confirmar: String) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    // Las contraseñas solo viven en memoria mientras se escriben (no se guardan)
    var clave by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    val noCoinciden = confirmar.isNotEmpty() && confirmar != clave

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
            Text("Completa tus datos para usar el Monitor Estructural")
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de usuario") },
                supportingText = { Text("Así aparecerás en la app") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Contraseña") },
                supportingText = { Text("Mínimo 8 caracteres") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirmar,
                onValueChange = { confirmar = it },
                label = { Text("Confirmar contraseña") },
                isError = noCoinciden,
                supportingText = { if (noCoinciden) Text("Las contraseñas no coinciden") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            if (state.error != null) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
            }
            if (state.cargando) {
                CircularProgressIndicator()
            }
            Button(
                onClick = { onCrearCuenta(nombre, correo, clave, confirmar) },
                enabled = !state.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear cuenta")
            }
            TextButton(onClick = onVolver, enabled = !state.cargando) {
                Text("¿Ya tienes cuenta? Ingresa")
            }
        }
    }
}
