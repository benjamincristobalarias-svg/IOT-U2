package cl.inacap.monitorestructural.model

// Revisa el correo y la contraseña antes de enviarlos a Firebase.
// Devuelve el mensaje de error, o null si están bien.
fun validarCredenciales(correo: String, clave: String, cuentaNueva: Boolean): String? = when {
    !CORREO_VALIDO.matches(correo.trim()) -> "Escribe un correo válido."
    clave.isEmpty() -> "Escribe la contraseña."
    cuentaNueva && clave.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
    else -> null
}

// Revisa los datos de la pantalla de Registro antes de crear la cuenta.
// Devuelve el mensaje de error, o null si están bien.
fun validarRegistro(nombre: String, correo: String, clave: String, confirmar: String): String? {
    val nombreLimpio = nombre.trim()
    return when {
        nombreLimpio.length < 3 -> "El nombre de usuario debe tener al menos 3 caracteres."
        nombreLimpio.length > 30 -> "El nombre de usuario puede tener hasta 30 caracteres."
        else -> validarCredenciales(correo, clave, cuentaNueva = true)
            ?: if (clave != confirmar) "Las contraseñas no coinciden." else null
    }
}

private val CORREO_VALIDO = Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")
