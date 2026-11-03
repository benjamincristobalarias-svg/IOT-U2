package cl.inacap.monitorestructural.data

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest

// Inicio de sesión con Firebase Authentication (correo y contraseña).
// La app no guarda la contraseña: Firebase la guarda cifrada (hash) en sus servidores
// y la envía por una conexión cifrada (HTTPS/TLS).
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    // Nombre del usuario con sesión (si no tiene nombre, su correo), o null si nadie ha iniciado sesión
    fun usuarioActual(): String? = auth.currentUser?.let { it.displayName?.takeIf(String::isNotBlank) ?: it.email }

    fun ingresar(correo: String, clave: String, alTerminar: (usuario: String?, error: String?) -> Unit) {
        auth.signInWithEmailAndPassword(correo, clave)
            .addOnCompleteListener { tarea -> responder(tarea, alTerminar) }
    }

    // Crea la cuenta y guarda el nombre de usuario en el perfil de Firebase
    fun crearCuenta(
        nombre: String,
        correo: String,
        clave: String,
        alTerminar: (usuario: String?, error: String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(correo, clave)
            .addOnCompleteListener { tarea ->
                // tarea.result solo se lee si salió bien (si falló, lanza una excepción)
                val usuario = if (tarea.isSuccessful) tarea.result?.user else null
                if (usuario == null) {
                    responder(tarea, alTerminar)
                    return@addOnCompleteListener
                }
                val perfil = UserProfileChangeRequest.Builder().setDisplayName(nombre).build()
                usuario.updateProfile(perfil).addOnCompleteListener { guardado ->
                    // Si el nombre no se pudo guardar, la cuenta igual queda creada y se usa el correo
                    alTerminar(if (guardado.isSuccessful) nombre else usuario.email, null)
                }
            }
    }

    fun salir() = auth.signOut()

    private fun responder(tarea: Task<AuthResult>, alTerminar: (String?, String?) -> Unit) {
        if (tarea.isSuccessful) {
            alTerminar(usuarioActual(), null)
        } else {
            alTerminar(null, mensajeError(tarea.exception))
        }
    }

    // Si falla el correo o la contraseña se muestra el mismo mensaje,
    // así no se revela si el correo tiene cuenta (buena práctica de seguridad)
    private fun mensajeError(error: Exception?): String = when (error) {
        is FirebaseNetworkException -> "Sin conexión a internet. Intenta de nuevo."
        is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil."
        is FirebaseAuthUserCollisionException -> "Ese correo ya tiene una cuenta."
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> "Correo o contraseña incorrectos."
        else -> "No se pudo conectar con Firebase. Intenta de nuevo."
    }
}
