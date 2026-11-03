# Monitor Estructural: app Android (Unidad 2)

**TI3042 · Aplicaciones Móviles para IoT (INACAP)**

App que monitorea el estado de estructuras (edificios, puentes) a partir de su frecuencia de vibración. Si una estructura se daña, pierde rigidez y su frecuencia **baja**. La app compara esa frecuencia con la línea base de la estructura sana y avisa con colores.

Dos celulares se comunican por **WiFi o datos móviles a través de Firebase**: uno hace de sensor (**Activador**) y el otro ve los datos en vivo (**Monitoreo**).

**Hecha con:** Android Studio, Kotlin, Jetpack Compose, ViewModel, Firebase Authentication y Firebase Realtime Database.

## Pantallas

| Pantalla | Qué hace |
|---|---|
| **Login** | Ingreso con correo y contraseña (Firebase Authentication) |
| **Registro** | Nombre de usuario, correo, contraseña (mínimo 8 caracteres) y confirmar contraseña. La app saluda con "Hola, *nombre*" |
| **Activador** | Simula el sensor: elige la estructura, la frecuencia, la amplitud y si el sensor está conectado, y envía la lectura a Firebase. Tiene envío automático cada 3 s |
| **Monitoreo** | Muestra en vivo una tarjeta por estructura con su frecuencia, línea base, caída y estado |

## Estados

| Color | Estado | Cuándo |
|---|---|---|
| Rojo | Daño | La frecuencia bajó más de 5 % respecto de la línea base |
| Amarillo | Sismo | Sin daño, pero la amplitud supera 230.000 |
| Verde | Normal | Frecuencia cerca de la línea base |
| — | Sin evaluación actual | Sensor desconectado: su última lectura se muestra como "no actual" |

## Por qué Firebase (WiFi / datos) y no Bluetooth

- Funciona a cualquier distancia; Bluetooth llega a pocos metros.
- Datos en tiempo real, con historial guardado en la nube.
- Conexión estable: sin internet, Firebase guarda los envíos y los manda al volver.

## Seguridad (ISO 27400)

| Medida | Cómo está en la app |
|---|---|
| Control de acceso | Sin sesión iniciada no se puede leer ni escribir (reglas `auth != null`) |
| Autenticación | Firebase Authentication; contraseña de mínimo 8 caracteres y confirmada al registrarse; mismo mensaje si falla el correo o la contraseña, para no revelar qué cuentas existen |
| Credenciales | La app no guarda la contraseña; Firebase la guarda cifrada (hash) |
| Cifrado en tránsito | Solo HTTPS/TLS (`network_security_config.xml` bloquea HTTP) |
| Datos en el celular | `allowBackup="false"`: la sesión no se copia a respaldos ni a otro celular |
| Integridad | Las reglas validan tipos y rangos (0–100 Hz) y rechazan campos desconocidos |
| Registro de eventos | `historial` guarda cada lectura con el usuario que la envió; no se puede modificar ni borrar |
| Mínimo privilegio | Solo los permisos `INTERNET` y `ACCESS_NETWORK_STATE` |

Las reglas de la base están en [`firebase/database.rules.json`](firebase/database.rules.json).

## Datos en Firebase

```
estructuras/NODO-01/   nombre, ubicacion, frecuenciaBaseHz, conectada, lectura { frecuenciaHz, amplitud, measuredAtMillis }
historial/NODO-01/     {clave}: { frecuenciaHz, amplitud, measuredAtMillis, usuario }   <- uno por envío
```

## Código

```
app/src/main/java/cl/inacap/monitorestructural/
    MainActivity.kt                        muestra Login/Registro o la app según la sesión
    model/Estructura.kt                    estructuras, lecturas y reglas de daño y sismo
    model/Credenciales.kt                  revisa los datos del login y del registro
    data/AuthRepository.kt                 login, crear cuenta y salir (Firebase Authentication)
    data/FirebaseEstructurasRepository.kt  envía y escucha los datos (Realtime Database)
    data/FakeEstructurasRepository.kt      simulador del envío automático
    ui/LoginScreen.kt, RegistroScreen.kt, LoginViewModel.kt
    ui/ActivadorScreen.kt, ActivadorViewModel.kt
    ui/MonitoreoScreen.kt, EstructurasViewModel.kt
    ui/PrincipalScreen.kt                  barra superior y pestañas Monitoreo / Activador
```

## Pruebas

15 pruebas automáticas en `app/src/test` (reglas de daño y sismo, simulador, login, registro y Activador). Todas pasan.

## Cómo abrirla

1. Descargar `google-services.json` desde la consola de Firebase (proyecto `monitor-estructural`) y copiarlo en `app/`. No se sube al repo.
2. Android Studio → **Open** → esta carpeta → esperar el Gradle Sync.
3. **▶ Run** en un emulador o celular con internet.
