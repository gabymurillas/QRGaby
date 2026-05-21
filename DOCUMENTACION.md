# Control de Acceso Alcaraván — Documentación

> Aplicación móvil Android para control de acceso residencial con códigos QR e integración con Odoo ERP.
>
> **Versión:** 1.0 · **Plataforma:** Android · **Estado:** Listo para producción

---

## Índice

1. [¿Qué es esta app y por qué se hizo?](#1-qué-es-esta-app-y-por-qué-se-hizo)
2. [Cómo funciona (visión general)](#2-cómo-funciona-visión-general)
3. [Manual de usuario — Primer uso](#3-manual-de-usuario--primer-uso)
4. [Manual de usuario — Uso diario](#4-manual-de-usuario--uso-diario)
5. [Mapa de pantallas y botones](#5-mapa-de-pantallas-y-botones)
6. [Ajustes y opciones de seguridad](#6-ajustes-y-opciones-de-seguridad)
7. [Indicadores y estados visuales](#7-indicadores-y-estados-visuales)
8. [Preguntas frecuentes](#8-preguntas-frecuentes)
9. [Notas técnicas (para el equipo)](#9-notas-técnicas-para-el-equipo)
10. [Glosario rápido](#10-glosario-rápido)

---

## 1. ¿Qué es esta app y por qué se hizo?

**Control de Acceso Alcaraván** es la aplicación para residentes de un conjunto/edificio que les permite **abrir el portón de acceso desde su teléfono**, sin necesidad de control remoto físico, llaves ni guardias intermediando todos los ingresos.

### El problema que resuelve

- Los controles remotos físicos se pierden, se rompen y son costosos de reemplazar.
- Llamar al vigilante para cada ingreso es lento y depende de que esté disponible.
- No hay registro confiable de quién entra y a qué hora.
- Las visitas y vehículos autorizados son difíciles de administrar.

### La solución

Cada residente instala la app en su teléfono. La administración del conjunto le entrega un **código QR de activación** (una sola vez) que conecta la app con el servidor del conjunto (Odoo). A partir de ese momento, el residente puede:

- **Abrir el portón con un toque**, validándose con su huella o PIN.
- Mostrar un **QR personal** al administrador para verificar identidad y vehículos.
- Ver en tiempo real si el sistema está sincronizado y funcionando.

Detrás de escena, el servidor Odoo lleva el registro central de quién está autorizado, qué vehículos tiene cada residente y opera el relé físico (ESP32) que abre el portón.

### Para quién es

| Rol | Lo que hace con la app |
|---|---|
| **Residente / Conductor** | Registra sus datos, escanea el QR del administrador una vez y abre el portón cuando llega a casa. |
| **Administrador / Vigilancia** | Usa una app paralela para emitir el QR de activación y verificar el QR personal del residente. (Esta app no es para ellos.) |

---

## 2. Cómo funciona (visión general)

```
  ┌──────────────┐     QR de activación      ┌──────────────┐
  │ Administrador│ ─────────────────────────▶│ App Residente│
  │   (Odoo)     │                            │  (esta app)  │
  └──────┬───────┘                            └──────┬───────┘
         │                                            │
         │              "Abrir portón"                │
         │ ◀──────────────────────────────────────────┘
         │
         ▼
  ┌──────────────┐     señal eléctrica       ┌──────────────┐
  │  Servidor    │ ─────────────────────────▶│   ESP32      │
  │   Odoo v6    │                            │  + Relé      │
  └──────────────┘                            └──────┬───────┘
                                                      │
                                                      ▼
                                                  PORTÓN ABRE
```

### En palabras simples

1. **Una vez:** el administrador te muestra un QR con la dirección del servidor del conjunto. Lo escaneas y tu app queda "activada".
2. **Cada día:** abres la app, tocas un botón grande, confirmas con tu huella, y el portón se abre.
3. **El servidor decide:** la app no abre el portón sola, le pide al servidor Odoo que lo abra. Si tu cédula está autorizada, Odoo manda la señal al hardware.

---

## 3. Manual de usuario — Primer uso

### Lo que necesitas tener listo

- Tu teléfono Android (con huella o PIN del sistema configurado, idealmente).
- Tu **cédula** y las **placas** de los vehículos que vas a registrar.
- Estar cerca del administrador (o tener acceso al QR de activación que él te entregue).
- Conexión a la misma red Wi-Fi del conjunto (o cobertura de datos, según configure el administrador).

### Paso 1 — Pantalla de bienvenida (Splash)

Al abrir la app por primera vez verás un logo animado durante ~2 segundos. No tienes que hacer nada.

### Paso 2 — Registra tu perfil

Aparecerá un formulario con tres campos. Complétalos así:

| Campo | Cómo llenarlo | Ejemplo |
|---|---|---|
| **Nombre y Apellido** | Mínimo dos palabras. Solo letras (sin números ni acentos). | `Juan Perez` |
| **Cédula de Identidad** | Mínimo 6 dígitos, máximo 10. Solo números. | `12345678` |
| **Placa del vehículo** | Letras MAYÚSCULAS y números, hasta 8 caracteres. | `ABC1234` |

**Si tienes más vehículos:** toca **"Añadir otro vehículo"** para agregar otra placa (hasta 5). El botón con la **X** elimina una placa que ya añadiste.

Cuando todo esté correcto, toca **CONTINUAR**. Si algún campo está mal, aparecerá un mensaje en rojo indicándolo.

### Paso 3 — Activación con el QR del administrador

La app te lleva a la pantalla **Portal de Acceso**. Aquí pueden pasar dos cosas:

#### A) Mostrar tu QR personal al administrador

En el centro de la pantalla verás un **QR grande**. Este QR contiene tu nombre, cédula y placas (encriptado). El administrador lo escanea desde su app para registrarte en el sistema.

> **Botón "VER ID TÉCNICO"**: muestra un identificador único de tu teléfono durante 30 segundos. Solo úsalo si el administrador te lo pide expresamente.

#### B) Escanear el QR del administrador

Cuando el administrador te entregue su QR de configuración, toca el botón azul grande **"Escanear QR del Admin"**.

- Se abrirá la cámara (la primera vez te pedirá permiso, acéptalo).
- Apunta al QR del administrador.
- En cuanto la app lo lea, verás un mensaje verde: **"¡Configuración recibida! Acceso activado."**.
- A los 2 segundos pasa automáticamente a la pantalla principal.

Si el QR no es válido, verás un mensaje en rojo (token incorrecto, QR ilegible, etc.). Pide al administrador que vuelva a generarlo y reintenta.

### Paso 4 — Listo

Ya estás activado. De aquí en adelante, cada vez que abras la app irás directo a la pantalla principal.

---

## 4. Manual de usuario — Uso diario

### Abrir el portón (flujo normal)

1. Abre la app. Verás la pantalla principal con un **botón circular azul grande**.
2. En la parte superior izquierda hay un indicador de estado:
   - **Punto verde → "Sincronizado"**: todo bien, puedes abrir.
   - **Punto amarillo → "Sincronizando…"**: espera unos segundos.
   - **Punto rojo o gris**: revisa la sección [Indicadores y estados](#7-indicadores-y-estados-visuales).
3. Toca el botón circular azul ("**Toca para abrir**").
4. Confirma con tu **huella digital** (o tu PIN del app, si lo configuraste).
5. El botón se anima ("Abriendo…") y, en segundos, ves "**¡Acceso Concedido!**" con una pequeña vibración.
6. El portón abre. Listo.

### Si tu huella falla o no la tienes configurada

- Si tienes **PIN de la app** activado: te pedirá ingresarlo (4 dígitos).
- Si **no** tienes PIN: la app intentará abrir el portón directamente tras la confirmación biométrica (o si no hay biometría, directamente).

### Si olvidaste el PIN de la app

En el diálogo de PIN, toca **"¿Olvidó su PIN?"**. La app te pedirá verificar tu identidad con la **huella del teléfono** o el **PIN/patrón de bloqueo de tu celular**. Si lo confirmas, te permitirá crear un PIN nuevo.

---

## 5. Mapa de pantallas y botones

Esta sección describe cada pantalla y **qué hace cada botón**.

### 5.1 Splash (pantalla de bienvenida)

| Elemento | Función |
|---|---|
| Animación de logo | Solo decorativa. No hay botones. |
| Salida automática | Tras 2 segundos, va a **Registro** (si es primera vez) o a **Principal** (si ya estás activado). |

### 5.2 Registro (`RegistrationScreen`)

| Botón / Elemento | Acción |
|---|---|
| **Campo "Nombre y Apellido"** | Acepta solo letras y espacios. Muestra error si está vacío o tiene menos de 2 palabras. |
| **Campo "Cédula"** | Acepta solo dígitos. Mínimo 6, máximo 10. |
| **Campo "Placa"** | Acepta MAYÚSCULAS y dígitos, hasta 8 caracteres. |
| **"Añadir otro vehículo"** | Agrega una placa más (hasta 5 en total). |
| **Botón "X" en una placa** | Elimina esa placa (solo si hay más de una). |
| **CONTINUAR** | Valida todo, guarda los datos cifrados en el teléfono y pasa a **Portal de Acceso**. |

### 5.3 Portal de Acceso (`QrSyncScreen`)

| Botón / Elemento | Acción |
|---|---|
| **Flecha atrás (←)** | Vuelve a la pantalla de **Registro** para corregir datos. |
| **QR grande (centro)** | Tu QR personal. Lo escanea el administrador. |
| **VER ID TÉCNICO / OCULTAR ID** | Muestra/oculta tu identificador de teléfono encriptado durante 30 segundos. |
| **Badge de estado** | Muestra "ESPERANDO VALIDACIÓN" (azul, parpadeante) o "ACCESO ACTIVADO" (verde). |
| **"Escanear QR del Admin"** | Abre la cámara para leer el QR de activación que entrega el administrador. |
| **"Volver a editar perfil"** | Vuelve a **Registro**. |

### 5.4 Pantalla Principal (`MainScreen`)

| Botón / Elemento | Acción |
|---|---|
| **Indicador superior izquierdo** | Estado de sincronización con el servidor (ver §7). No es un botón. |
| **Engranaje (⚙) arriba derecha** | Abre el panel de **Ajustes**. |
| **"BIENVENIDO DE NUEVO" + tu nombre** | Saludo personalizado. No es un botón. |
| **Tarjeta "Estado del sistema"** | Muestra si estás verificado por el servidor. No es un botón. |
| **Botón circular azul (centro)** | **Abrir portón**. Pide huella / PIN y envía la orden al servidor. |
| **"Actualizar servidor"** | Abre un cuadro para re-escanear el QR del administrador (úsalo si cambió la IP del servidor del conjunto). |
| **Chip con la placa** | Solo informativo (muestra tu primera placa). |
| **"Tocar para salir"** (solo si no estás reconocido) | Cierra sesión y vuelve a **Registro**. |

### 5.5 Diálogos secundarios

**Diálogo de PIN — Ingreso:**
- Teclado numérico 0-9 + tecla borrar.
- 4 dígitos, valida automáticamente al cuarto.
- **Cancelar**: cierra el diálogo.
- **¿Olvidó su PIN?**: dispara la recuperación con huella/credencial del teléfono.

**Diálogo de PIN — Configuración:**
- Paso 1: ingresa PIN nuevo.
- Paso 2: repite el PIN. Si coinciden, queda guardado.

**Diálogo "Actualizar servidor":**
- **Escanear QR**: abre la cámara para leer el nuevo QR del administrador.
- **Cerrar**: cancela.

---

## 6. Ajustes y opciones de seguridad

Toca el **engranaje (⚙)** arriba a la derecha en la pantalla principal.

### Sección "Seguridad"

| Opción | Qué hace |
|---|---|
| **PIN de seguridad (switch)** | Actívalo para pedir un PIN adicional al abrir el portón. Desactívalo para abrir solo con huella. |
| **Cambiar PIN** | Cambia tu PIN actual por uno nuevo (solo aparece si el PIN está activo). |

> **Recomendación:** Activa el PIN si compartes el teléfono o si tu huella es poco confiable. Si tu teléfono ya tiene buena biometría, puedes prescindir del PIN.

### Sección "Apariencia"

| Opción | Qué hace |
|---|---|
| **Tema → Sistema** | Sigue la configuración de tu teléfono (claro/oscuro automático). |
| **Tema → Claro** | Fuerza modo claro. |
| **Tema → Oscuro** | Fuerza modo oscuro. |

El tema queda guardado aunque cierres sesión.

---

## 7. Indicadores y estados visuales

### Indicador de sincronización (esquina superior izquierda)

| Color | Texto | Significado |
|---|---|---|
| Verde | "Sincronizado" | Todo bien. Puedes abrir el portón. |
| Amarillo | "Sincronizando…" | La app está verificando con el servidor. Espera. |
| Rojo | "Usuario no encontrado" | Tu cédula no está autorizada en Odoo. Contacta al administrador. |
| Gris | "Sin conexión" | El teléfono no llega al servidor. Verifica Wi-Fi/datos. |

### Estados del botón de apertura

| Apariencia | Significado |
|---|---|
| Azul, "Toca para abrir" | Listo. Toca para iniciar. |
| Azul con animación, "Abriendo…" | La orden se envió al servidor; esperando confirmación. |
| Verde, "¡Acceso Concedido!" | El portón se abrió. (Vuelve a azul tras 1.5 s). |
| Rojo / mensaje de error | Algo falló. Lee el mensaje y reintenta. |
| Gris, "Tocar para salir" | No estás autorizado. Solo te permite cerrar sesión. |

---

## 8. Preguntas frecuentes

**¿Funciona sin internet?**
No. La app **necesita conexión** con el servidor del conjunto para que el portón abra. Sin red, el indicador queda en gris y el botón no responde.

**¿Y si pierdo el teléfono?**
Avisa de inmediato al administrador. Él puede dar de baja tu cédula en Odoo y nadie con tu teléfono podrá abrir el portón.

**¿Puedo usar la misma app en dos teléfonos?**
Cada teléfono se registra de forma única (con un identificador interno encriptado). El administrador decide si autoriza más de un dispositivo por cédula.

**¿El servidor cambió de IP, qué hago?**
En la pantalla principal toca **"Actualizar servidor"**, escanea el nuevo QR que te entregue el administrador, y listo. Tu perfil no se pierde.

**¿Mis datos están seguros?**
Sí. El identificador de tu teléfono se cifra con AES-256-GCM antes de enviarse. El QR del administrador solo se acepta con un token de seguridad compartido. El PIN nunca sale del teléfono.

**¿Por qué la app me pide la huella si ya entré con el PIN del celular?**
Son cosas distintas. El PIN del celular protege tu teléfono; la huella en la app protege específicamente la acción de abrir el portón. Es una segunda capa.

**¿Qué pasa si toco "Cerrar sesión"?**
Se borran tus datos del teléfono (nombre, cédula, placas, PIN, endpoint del servidor). La preferencia de tema oscuro/claro **sí se conserva**. Tendrás que volver a registrarte y escanear el QR del administrador.

---

## 9. Notas técnicas (para el equipo)

> Sección dirigida al equipo de desarrollo y soporte. Los usuarios finales pueden saltarla.

### Stack

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Arquitectura:** MVVM (un `ViewModel` por pantalla + `ThemeViewModel` global)
- **DI:** Hilt
- **Persistencia local:** Jetpack DataStore (Preferences)
- **Red:** Retrofit + OkHttp (interceptor de endpoint dinámico)
- **Escaneo QR:** CameraX + ML Kit Vision
- **Generación QR:** ZXing
- **Cifrado:** AES-256-GCM (`utils/CryptoManager.kt`)
- **Biometría:** AndroidX BiometricPrompt
- **Backend:** Odoo v6 vía JSON-RPC 2.0
- **Hardware:** ESP32 con relé, comandado desde Odoo

### Estructura de paquetes

```
com.example.qr_prueba_gaby/
├── app/                 # MainActivity, Application class
├── data/
│   ├── model/           # UserData, ThemeMode
│   ├── network/         # ApiService, EndpointProvider, Interceptor
│   └── pref/            # UserDataStore
├── di/                  # DataModule (Hilt)
├── presentation/ui/
│   ├── SplashScreen/
│   ├── RegistrationScreen/
│   ├── QrSyncScreen/
│   ├── MainScreen/
│   ├── theme/
│   └── common/          # States.kt (enums)
└── utils/               # CryptoManager, BiometricAuthManager
```

### Estados globales

- `is_registered` — el usuario completó el formulario inicial.
- `is_activated` — el dispositivo escaneó con éxito el QR del administrador.
- `theme_mode` — preferencia visual; **no se borra con logout**.

### Endpoints HTTP (Odoo)

Ambos son `POST` JSON-RPC 2.0 contra el endpoint dinámico recibido en el QR del administrador.

| Ruta | Propósito | Frecuencia |
|---|---|---|
| `/api/control_acceso` | Sondeo de estado (`{action:"read", cedula}`) | Cada 10 s en `MainScreen` |
| `/api/open_gate` | Solicita apertura del portón (`{cedula}`) | Bajo demanda |

El sondeo periódico **solo afecta el indicador visual**; nunca cierra sesión automáticamente.

### Endpoint dinámico

Retrofit usa `http://placeholder.invalid/` como baseUrl. El [DynamicEndpointInterceptor](app/src/main/java/com/example/qr_prueba_gaby/data/network/client/DynamicEndpointInterceptor.kt) reescribe `scheme + host + port` en cada request usando el valor guardado en DataStore. Si no hay endpoint provisionado, lanza `IOException` y la UI muestra estado offline.

### Cifrado

`CryptoManager.encrypt(text)` usa AES-256-GCM con IV aleatorio de 12 bytes. La llave proviene de `BuildConfig.SHARED_SECRET` (debe coincidir con la app del administrador). Formato almacenado: `IV_BASE64:CIPHERTEXT_BASE64`.

> **Para producción:** mover `SHARED_SECRET` a un sistema de gestión de secretos y rotarlo periódicamente. Hoy vive como `buildConfigField` en [app/build.gradle.kts](app/build.gradle.kts).

### Permisos

| Permiso | Cuándo se pide | Obligatorio |
|---|---|---|
| `INTERNET` | En instalación (auto-aceptado) | Sí |
| `CAMERA` | En runtime, la primera vez que se va a escanear un QR | Sí para activar/re-provisionar |
| Biometría | No requiere permiso explícito (la API la gestiona) | Opcional |

### Configuración relevante

- `usesCleartextTraffic="true"` está activo porque los servidores Odoo de conjunto suelen ser locales HTTP. **En producción real**: usar TLS y desactivar cleartext.
- `versionName = "1.0"`, `versionCode = 1` — recordar actualizar antes de cada release.
- `applicationId = "com.example.qr_prueba_gaby"` — **conviene renombrar** a algo como `com.alcaravan.acceso` antes de publicar en Play Store.

### Riesgos conocidos para producción

1. **`SHARED_SECRET` en `BuildConfig`** — accesible vía reverse engineering. Migrar a Keystore o backend.
2. **PIN almacenado en claro en DataStore** — proteger con EncryptedSharedPreferences o Keystore.
3. **Cleartext HTTP activo** — solo apto para LAN del conjunto. Forzar HTTPS para deploys públicos.
4. **`applicationId` y `namespace`** aún con prefijo `com.example` — bloquea publicación en Play Store.
5. **Sin telemetría/crash reporting** — considerar Firebase Crashlytics o similar antes de release.
6. **Token único `ALCARAVAN_2025`** hardcoded — rotar por instalación o por conjunto.

Ver memoria del proyecto: la cédula se envía sin prefijo `V-`; validar coincidencia con el esquema de Odoo en producción.

---

## 10. Glosario rápido

| Término | Significado |
|---|---|
| **Odoo** | El sistema ERP que el conjunto usa como cerebro central (gestiona residentes, vehículos, registros). |
| **Endpoint** | La dirección de red del servidor del conjunto (ej. `http://172.17.12.119:8059`). |
| **Provisionamiento** | El proceso único en el que tu app aprende cuál es el endpoint del conjunto (al escanear el QR del administrador). |
| **QR personal** | El código que la app te muestra para que el administrador verifique tu identidad. |
| **QR del administrador** | El código que el administrador te muestra una sola vez para activar tu app. |
| **ESP32** | El microcontrolador físico instalado en el portón que recibe la orden eléctrica de Odoo para abrirlo. |
| **Biometría** | Huella digital o reconocimiento facial. |
| **Cleartext** | Tráfico HTTP sin cifrar (no recomendado fuera de redes locales controladas). |
| **DataStore** | El almacén local cifrado donde la app guarda tus datos en el teléfono. |

---

*Documento mantenido por el equipo del proyecto. Última revisión correspondiente a la versión 1.0.*
