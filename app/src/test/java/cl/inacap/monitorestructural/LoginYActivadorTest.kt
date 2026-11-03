package cl.inacap.monitorestructural

import cl.inacap.monitorestructural.model.ConnectionStatus
import cl.inacap.monitorestructural.model.armarEstructura
import cl.inacap.monitorestructural.model.validarCredenciales
import cl.inacap.monitorestructural.model.validarRegistro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Pruebas del Login y Registro (revisión de datos) y del Activador (lectura que se envía)
class LoginYActivadorTest {

    @Test
    fun login_correoInvalido_muestraError() {
        assertNotNull(validarCredenciales("benja.gmail.com", "clave1234", cuentaNueva = false))
    }

    @Test
    fun crearCuenta_contrasenaCorta_muestraError() {
        assertEquals(
            "La contraseña debe tener al menos 8 caracteres.",
            validarCredenciales("benja@gmail.com", "1234", cuentaNueva = true)
        )
    }

    @Test
    fun login_datosCorrectos_sinError() {
        assertNull(validarCredenciales("benja@gmail.com", "clave1234", cuentaNueva = false))
    }

    @Test
    fun registro_datosCorrectos_sinError() {
        assertNull(validarRegistro("Benja", "benja@gmail.com", "clave1234", "clave1234"))
    }

    @Test
    fun registro_contrasenasDistintas_muestraError() {
        assertEquals(
            "Las contraseñas no coinciden.",
            validarRegistro("Benja", "benja@gmail.com", "clave1234", "clave9999")
        )
    }

    @Test
    fun registro_nombreVacio_muestraError() {
        assertEquals(
            "El nombre de usuario debe tener al menos 3 caracteres.",
            validarRegistro("   ", "benja@gmail.com", "clave1234", "clave1234")
        )
    }

    @Test
    fun registro_contrasenaCorta_muestraError() {
        assertEquals(
            "La contraseña debe tener al menos 8 caracteres.",
            validarRegistro("Benja", "benja@gmail.com", "1234", "1234")
        )
    }

    @Test
    fun activador_frecuenciaBaja_esDano() {
        val estructura = armarEstructura("NODO-02", 30.0, 140_000.0, conectado = true, nowMillis = 1L)!!
        assertTrue(estructura.hasCurrentAlert)
        assertEquals(35.0, estructura.frecuenciaBaseHz, 0.0)   // la línea base no cambia desde la app
    }

    @Test
    fun activador_desconectado_noEnviaLecturaNiAlerta() {
        val estructura = armarEstructura("NODO-01", 20.0, 300_000.0, conectado = false, nowMillis = 1L)!!
        assertEquals(ConnectionStatus.DISCONNECTED, estructura.connection)
        assertNull(estructura.lectura)
        assertFalse(estructura.hasCurrentAlert)
    }

    @Test
    fun activador_estructuraQueNoExiste_noSeEnvia() {
        assertNull(armarEstructura("NODO-99", 30.0, 0.0, conectado = true, nowMillis = 1L))
    }
}
