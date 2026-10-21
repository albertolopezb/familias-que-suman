# 9. Consideraciones éticas y legales

## 9.0 Ética, moral y ley en el desarrollo de la app

Usamos las distinciones del módulo de Sentido Humano:

- **Moral:** el conjunto de costumbres y normas que una comunidad considera buenas para regular el
  comportamiento.
- **Ética:** la disciplina que estudia la moral y justifica las decisiones, sobre todo cuando la ley
  no alcanza o hay valores en conflicto.
- **Ley:** la norma jurídica general y exigible, dentro de la jerarquía Constitución → códigos y
  leyes → reglamentos → normas.

| Ámbito | Qué significa en este proyecto | Cómo se convirtió en algo del sistema |
|---|---|---|
| **Ley** (exigible) | LFPDPPP (DOF 20-03-2025, última reforma DOF 14-11-2025): aviso de privacidad, consentimiento, seguridad, derechos ARCO. Ley General de los Derechos de Niñas, Niños y Adolescentes, art. 76 (vida privada y datos personales de menores). Código Penal Federal, arts. 211 bis 1 a 211 bis 7 (acceso ilícito a sistemas) | Aviso integral y simplificado; aceptación del aviso para crear la cuenta; consentimiento del tutor para inscribir menores; eliminación de datos con folio y plazos de ley; controles contra el acceso no autorizado |
| **Moral** (lo que la comunidad de Familias que Suman considera correcto) | Cuidar a las familias y a los menores, no exponer a nadie, no lucrar con la ayuda | Testimonios revisados antes de publicarse; sin mensajería entre familias; sin listas públicas de quién ayuda; la donación es informativa, sin cobros |
| **Ética** (justificar cuando hay conflicto) | Utilidad de la app frente a privacidad, autonomía y sostenibilidad | Los dilemas de §9.4: ubicación, testimonios con menores, notificaciones sin terceros, qué guardar sin conexión, qué conservar al eliminar una cuenta |

**Sostenibilidad (Agenda 2030).** La app atiende el ODS 10 (reducir desigualdades) y el ODS 11
(comunidades sostenibles) al conectar a familias con causas locales. También el ODS 17 (alianzas):
le da visibilidad a organizaciones pequeñas que no tienen cómo publicar sus necesidades, como
registramos en la guía de observación de la OSF. Del lado técnico se cuida el consumo:

- las fotos se reducen a 1600 px antes de subirse;
- los avisos se revisan en un solo trabajo cada 15 minutos que Android agrupa;
- no hay servicios de terceros duplicados;
- el servidor es una sola VM pequeña (2 núcleos, 4 GB) que se apaga cuando no se usa.

**Delitos cibernéticos.** Los riesgos del módulo que aplican a esta app y lo que hace cada control:

| Riesgo | Control |
|---|---|
| Acceso ilícito a datos (art. 211 bis del CPF) | Autorización por recurso, fuerza bruta bloqueada, auditoría (§4) |
| *Doxing* o difusión de datos de una familia | Lo público nunca incluye correos ni teléfonos de las familias; los testimonios salen con el nombre de la familia |
| *Grooming* | No hay mensajería entre usuarios, y la lista de asistentes solo la ve quien tiene el permiso de registrar asistencias |
| Suplantación | Nadie se registra como administración; los permisos van firmados en el token |

## 9.1 Criterios éticos y legales convertidos en requisitos verificables

| ID del backlog | Criterio (ámbito) | Requisito verificable | Criterio de aceptación | Prueba o evidencia | Referencia en la matriz |
|---|---|---|---|---|---|
| RF-15 | Informar antes de tratar los datos (Ley: LFPDPPP arts. 14, 15 y 16-II) | La app muestra el aviso integral (art. 15, fr. I a VI) en Ajustes y el simplificado al crear la cuenta; sin aceptarlo no se crea | `POST /v1/sesion/registro` sin `acepta_aviso` responde 422 `aviso_requerido` | `test_identidad.py::test_registro_pide_el_aviso_de_privacidad`; pantalla `AvisoPrivacidadScreen` | §3.1 RF-15 |
| HU-14, TT-14 | Derecho de cancelación (Ley: LFPDPPP arts. 24 y 31) | La familia pide la eliminación desde la app y recibe un folio; la administración la atiende y se borran cuenta, acompañantes, inscripciones, encuestas, testimonios y fotos | Tras atenderla, la cuenta no existe, su token deja de servir y queda solo la constancia (folio, correo, fecha) | `test_participacion.py::test_solicitud_de_eliminacion_es_idempotente_y_la_atiende_privacidad`; M-15 | §3.1 RF-15, §3.4 HU-14 |
| RF-06 | Datos de menores con consentimiento de su tutor (Ley: LFPDPPP art. 7; LGDNNA art. 76) | Inscribir a un menor exige el consentimiento de quien lo inscribe, y se guarda cuándo se dio | Sin consentimiento: 422 `falta_consentimiento`; con él: 200 y `consentimiento_menores_en` | `test_inscripciones.py::test_menores_piden_consentimiento` | §3.1 RF-06 |
| RF-06 | Minimización (Ley: LFPDPPP art. 12; Ética) | La asociación ve quiénes van solo con nombre y edad, y solo quien registra asistencias | La lista no contiene ningún correo; una familia o el admin del catálogo reciben 403 | `::test_quien_registra_asistencias_ve_quienes_van_sin_correos`, `::test_una_familia_o_el_admin_del_catalogo_no_ven_quienes_van` | §3.1 RF-06 |
| RF-12, HU-11 | Cuidado del otro: no exponer a menores (Moral) | Ningún testimonio ni su foto es público hasta que la OSF lo aprueba; editarlo lo regresa a revisión | Un testimonio nuevo no aparece en `GET /v1/testimonios`; su foto solo la ven su autora y la moderación | `test_testimonios.py::test_un_testimonio_nuevo_no_es_publico_hasta_aprobarlo`, `::test_la_foto_en_revision_solo_la_ven_su_autora_y_la_moderacion` | §3.1 RF-12 |
| RF-12 | Las fotos no delatan dónde viven (Ética; Ley: art. 18) | Toda foto se re-codifica sin EXIF (GPS incluido), en el teléfono y otra vez en el servidor | La foto guardada no tiene EXIF | `test_fotos.py::test_foto_valida_se_guarda_sin_exif`; `Fotos.prepararParaSubir` | §3.1 RF-12 |
| RF-02 | Ubicación mínima y opcional (Ética; Ley: art. 12) | La app no pide la ubicación: "Buscar cerca de" usa un lugar que la familia escribe o toca en el mapa | Ninguna ruta de la API recibe ubicación de la familia; el manifiesto no pide permisos de ubicación; a OpenStreetMap solo va el texto escrito, sin token | `CercaniaTest` (6); M-02; `AndroidManifest.xml` | §3.1 RF-02 |
| RNF-02, SEG-02 | Seguridad de los datos (Ley: LFPDPPP art. 18; CPF art. 211 bis) | Contraseñas con Argon2id, códigos como HMAC, sesión del teléfono cifrada, ningún secreto en el repositorio | La base no contiene la contraseña; la búsqueda de secretos no encuentra nada | `::test_la_contrasena_se_guarda_como_hash_argon2id`, `test_recuperacion.py::test_el_codigo_no_se_guarda_en_claro`; `docs/evidencia/secretos.txt` | §3.2 RNF-02, §3.3 SEG-02 |
| SEG-03 | Nadie ve lo ajeno (Ley: art. 18; Moral) | Una familia no puede leer ni borrar datos de otra | 404 en cualquier recurso ajeno | `test_autorizacion.py::test_bola_no_se_ven_ni_se_borran_acompanantes_ajenos` | §3.3 SEG-03 |
| RF-18 | No revelar quién tiene cuenta (Ética) | Iniciar sesión y recuperar responden igual exista o no el correo | Mismo código y mensaje en los dos casos | `::test_mismo_mensaje_si_el_correo_no_existe_o_la_contrasena_esta_mal`, `test_recuperacion.py::test_responde_lo_mismo_exista_o_no_la_cuenta` | §3.1 RF-18 |
| RF-10 | No lucrar con la ayuda (Moral) | La app no procesa pagos ni pide datos financieros | No hay pantalla de pago ni dependencia de pagos | Revisión de `app/build.gradle.kts` y de las pantallas de Donar; M-10 | §3.1 RF-10 |
| ACTA-01 | Lo que se guarda en el teléfono es solo lo público (Ética; Ley: art. 12) | Sin conexión solo se consulta el catálogo público, nunca datos de la familia | La tabla `respuestas` de Room solo guarda rutas públicas | `CatalogoLocalTest`; `CatalogoLocal.kt` (lista de claves) | §3.1 ACTA-01 |

## 9.2 Aviso de privacidad

**Fundamento:** Ley Federal de Protección de Datos Personales en Posesión de los Particulares (DOF
20-03-2025):

- **art. 15:** contenido mínimo del aviso, fracciones I a VI;
- **art. 16, fracción II:** aviso simplificado cuando los datos se obtienen por medios electrónicos;
- **art. 14:** obligación de informar;
- **art. 18:** medidas de seguridad;
- **arts. 21 a 33:** derechos ARCO; el art. 31 fija los plazos.

Es el mismo texto que la app muestra en Perfil → Ajustes → Aviso de privacidad. El simplificado
(puntos 1 a 4) aparece al crear la cuenta.

> **Aviso de privacidad integral · App Familias que Suman** (actualizado el 20 de octubre de 2026)
>
> **1. Quién es responsable** *(art. 15-I)*. Familias que Suman, asociación civil, con domicilio en
> Monterrey, Nuevo León, México. Para cualquier tema de tus datos: hola@familiasquesuman.com. Este aviso
> es el de la app; el de las actividades y el sitio está en familiasquesuman.com/aviso-de-privacidad.
>
> **2. Qué datos guardamos** *(art. 15-II)*.
>
> - **De la cuenta:** tu correo; tu contraseña, guardada como huella Argon2id (nadie puede leerla);
>   el nombre de tu familia; tu nombre, fecha de nacimiento y sexo; tu ciudad; y, si la subes, tu
>   foto de perfil.
> - **De tu familia:** nombre, fecha de nacimiento o edad y sexo de quienes registras como
>   acompañantes. Si son menores de edad, guardamos que tú, como madre, padre o tutor, diste tu
>   consentimiento.
> - **De tu participación:** inscripciones, asistencias, favoritos, ajustes de avisos, encuestas,
>   testimonios con foto y sugerencias.
>
> No pedimos datos sensibles: salud, religión, origen, preferencias, datos financieros ni biométricos.
>
> **3. Para qué los usamos** *(art. 15-III)*.
>
> - **Necesarias para el servicio:**
>   - crear tu cuenta e identificarte;
>   - mandarte a tu correo el código para cambiar tu contraseña cuando lo pides;
>   - inscribirte a actividades y avisar a quien organiza cuántas personas van y de qué edades;
>   - registrar tu asistencia y armar tu historial;
>   - proteger la app contra abusos.
> - **Voluntarias (solo si tú las usas):** publicar tu testimonio, contestar encuestas para mejorar
>   las actividades, recibir recordatorios y avisos de tu ciudad, y mandar sugerencias.
>
> No usamos tus datos para publicidad.
>
> **4. Cómo limitar su uso** *(art. 15-IV)*. En Ajustes apagas los recordatorios y avisos.
> Testimonios, encuestas, sugerencias y foto de perfil son opcionales y puedes borrarlos. La app no
> pide tu ubicación. Y puedes pedir que borremos todo.
>
> **Dónde se guardan.** En nuestro servidor, una máquina de Microsoft Azure en Estados Unidos (región
> North Central US): la base de datos PostgreSQL y un almacén propio para las fotos. Microsoft solo
> presta la máquina: no usa ni consulta tus datos. En tu teléfono solo quedan:
>
> - tu sesión, cifrada con una llave del propio teléfono;
> - tus ajustes;
> - el catálogo público (actividades, asociaciones, proyectos y campañas), para consultarlo sin
>   internet.
>
> **Quién más los ve.**
>
> - El equipo de Familias que Suman que lo necesita para su trabajo; por ejemplo, quien registra la
>   asistencia.
> - Solo para coordinar una actividad, la institución que la organiza: recibe lo indispensable, como
>   los nombres y edades de quienes van.
> - Un servicio de correo, para mandar el código de tu contraseña: solo recibe tu dirección y ese
>   mensaje.
> - Cloudflare, para que la app llegue al servidor de forma cifrada.
> - Microsoft Azure, que es donde está el servidor (en Estados Unidos).
>
> Los servicios técnicos actúan como encargados y no usan tus datos para nada más. No vendemos tus
> datos. Solo los entregaríamos a una autoridad que los pida conforme a la ley.
>
> **Tu ubicación.** La app no pide permiso de ubicación: no sabemos dónde estás. En el mapa puedes
> "Buscar cerca de" un municipio, una colonia o una calle que tú escribes (o un punto que tocas): los
> municipios se encuentran en el teléfono y lo demás se busca en OpenStreetMap, que solo recibe ese
> texto, sin tu cuenta ni tus datos. No lo guardamos.
> El mapa del Inicio dibuja las calles con OpenStreetMap: tu teléfono les pide las imágenes de la
> zona que estás viendo, sin tu ubicación ni tus datos.
>
> **Fotos y testimonios.** Antes de publicarse, el equipo los revisa; mientras tanto solo los ves tú.
> Al publicarse aparecen con el nombre de tu familia, nunca con tu correo. Antes de subirse, a las
> fotos se les quita la ubicación y otros metadatos. Puedes eliminarlos cuando quieras.
>
> **Cómo los cuidamos** *(art. 18)*. Todo viaja por HTTPS. En el servidor, cada cuenta solo puede ver lo
> suyo, los intentos de adivinar contraseñas se bloquean y queda una bitácora de las acciones sensibles
> (con una huella de la IP, no la IP).
>
> **Cuánto tiempo los guardamos.** Mientras tengas tu cuenta. La sesión del teléfono vence a los 30
> días sin uso. Al eliminar tu cuenta se borran tus datos, los de tus acompañantes, tus inscripciones,
> encuestas, testimonios y fotos. Se conserva:
>
> - la constancia de tu solicitud (folio, correo y fecha), como bloqueo conforme al art. 24;
> - la bitácora de seguridad, que no tiene tu nombre.
>
> Tus sugerencias se quedan sin tu nombre ni tu contacto.
>
> **Tus derechos ARCO** *(art. 15-V; arts. 21 a 33)*.
>
> - **Acceso y rectificación:** tus datos están en Perfil, y ahí los corriges.
> - **Cancelación:** el botón **Solicitar eliminación de mis datos** genera un folio.
> - **Oposición, o cualquier duda:** escribe a hola@familiasquesuman.com con tu folio o el correo de
>   tu cuenta.
>
> Te respondemos en un máximo de 20 días y, si procede, se hace efectivo en los 15 días siguientes
> (art. 31). Si no te respondemos, puedes acudir a la Secretaría Anticorrupción y Buen Gobierno.
>
> **Si este aviso cambia** *(art. 15-VI)*. Publicaremos la nueva versión en esta pantalla, con su fecha,
> y lo anunciaremos en la app antes de que aplique.

**Cómo se comprueba que el aviso dice la verdad** (la rúbrica pide que corresponda con el sistema):

| Lo que promete | Dónde se ve en el sistema |
|---|---|
| Contraseña como huella Argon2id | `identidad.py`; `test_la_contrasena_se_guarda_como_hash_argon2id` |
| Consentimiento del tutor | `inscripciones.consentimiento_menores_en`; `test_menores_piden_consentimiento` |
| Sin ubicación de la familia | El manifiesto no pide permisos de ubicación; `BuscadorDeLugares.kt` manda solo el texto, sin token (`CercaniaTest`) |
| Testimonios revisados y sin correo | `test_testimonios.py`; `test_los_testimonios_publicos_no_exponen_correos` |
| La institución ve nombres y edades | `GET /v1/admin/actividades/{id}/inscritos` (sin correos); pantalla "Quiénes van" |
| Eliminación con folio y plazos | Botón en la app → `ELIM-xxxx`; pantalla "Solicitudes de eliminación"; `admin.py::atender` borra en cascada y las fotos del almacén |
| Fotos sin metadatos | `Fotos.prepararParaSubir` y `imagenes.py`; `test_foto_valida_se_guarda_sin_exif` |
| En el teléfono solo lo público | `CatalogoLocal.kt` |
| Bitácora con huella de la IP | `auditoria.ip_hmac` (HMAC-SHA256) |

**Pendiente de la OSF:** publicar su razón social y domicilio completos. El sitio todavía no los
muestra, y el art. 15-I los pide. Se cambian en un solo lugar (`Responsable`, en
`AvisoPrivacidadScreen.kt`).

## 9.3 Clasificación de los datos (campo por campo)

**Niveles:**

| Nivel | Qué incluye |
|---|---|
| **Crítico** | Credenciales y secretos: dan acceso a la cuenta o al sistema |
| **Alto** | Datos personales de menores, o que permiten localizar o identificar a una familia |
| **Medio** | Datos personales de adultos y de su participación |
| **Bajo** | Seudónimos y preferencias |
| **Público** | Lo que cualquiera ve sin cuenta |

**Protección común a todo el servidor:**

- TLS en el camino público y redes de Docker internas, sin acceso desde internet;
- PostgreSQL con un usuario de la API de mínimo privilegio;
- autorización por recurso (ABAC) y respaldos.

Columnas: dato · nivel · ubicación · protección específica · tiempo de conservación.

### Cuenta (tabla `cuentas`)

| Dato | Nivel | Ubicación | Protección específica | Conservación |
|---|---|---|---|---|
| `correo` | Medio | Servidor, `cuentas.correo` | Solo lo ve su dueña y quien atiende la privacidad; nunca sale en respuestas públicas | Mientras exista la cuenta |
| `contrasena_hash` | Crítico | `cuentas.contrasena_hash` | Huella Argon2id con sal; la contraseña nunca se guarda ni se registra | Mientras exista la cuenta |
| `nombre_familia` | Medio | `cuentas.nombre_familia` | Sale en testimonios publicados (aprobados) | Mientras exista la cuenta |
| `titular_nombre` | Medio | `cuentas.titular_nombre` | Solo su dueña y la lista de "quiénes van" | Mientras exista la cuenta |
| `titular_nacimiento` | Medio | `cuentas.titular_nacimiento` | Se usa para calcular la edad mínima; no se muestra a terceros | Mientras exista la cuenta |
| `titular_sexo` | Medio | `cuentas.titular_sexo` | Opcional; solo su dueña | Mientras exista la cuenta |
| `ciudad` | Bajo | `cuentas.ciudad` y `PreferenciasStore` en el teléfono | — | Mientras exista la cuenta |
| `foto_clave` (foto de perfil) | Alto | Almacén RustFS; copia en el teléfono (`filesDir/perfil`) | Re-codificada sin EXIF; solo la ve su dueña | Hasta que la quite o elimine la cuenta |
| `rol`, `permisos` | Crítico (autorización) | `cuentas`; firmados dentro del JWT | Solo se cambian en el servidor (`herramientas.py`); cambiarlos invalida los tokens viejos | Mientras exista la cuenta |
| `aviso_aceptado_en` | Bajo | `cuentas` | Prueba del consentimiento | Mientras exista la cuenta |
| `ultimo_acceso_en`, `creada_en` | Bajo | `cuentas` | — | Mientras exista la cuenta |
| `tokens_validos_desde` | Bajo | `cuentas` | Corta todos los tokens anteriores | Mientras exista la cuenta |

### Familia e inscripciones

| Dato | Nivel | Ubicación | Protección específica | Conservación |
|---|---|---|---|---|
| `acompanantes.nombre` | Alto (pueden ser menores) | `acompanantes` | Solo su dueña (BOLA → 404) | Hasta que lo quite o elimine la cuenta (cascada) |
| `acompanantes.fecha_nacimiento` / `edad` | Alto | `acompanantes` | Solo su dueña; se usa para la edad mínima | Igual |
| `acompanantes.sexo` | Alto | `acompanantes` | Opcional; solo su dueña | Igual |
| `inscripcion_personas.nombre`, `edad`, `fecha_nacimiento`, `sexo` | Alto | `inscripcion_personas` | Lo ve su dueña y quien tiene `registrar:asistencias` (nombre y edad, sin contacto; consulta auditada) | Mientras exista la cuenta (cascada) |
| `inscripciones.consentimiento_menores_en` | Medio | `inscripciones` | Prueba del consentimiento del tutor | Mientras exista la cuenta |
| `inscripciones.personas`, `creada_en`, `cancelada_en` | Bajo | `inscripciones` | — | Mientras exista la cuenta |
| `asistencias` (quién fue a qué) | Medio | `asistencias` | Solo la registra quien tiene `registrar:asistencias` | Mientras exista la cuenta |
| `favoritos` | Bajo | `favoritos`; copia en `FavoritosStore` del teléfono | — | Hasta que los quite o elimine la cuenta |
| `preferencias` (recordatorios, urgencias) | Bajo | `preferencias`; copia en `PreferenciasStore` | — | Mientras exista la cuenta |
| `aportaciones` (qué se comprometió a donar) | Medio | `aportaciones` | Idempotentes; no se muestra quién aportó | Mientras exista la cuenta |
| `invitaciones` | Bajo | `invitaciones` | Solo un conteo | Mientras exista la cuenta |

### Participación y contenido

| Dato | Nivel | Ubicación | Protección específica | Conservación |
|---|---|---|---|---|
| `testimonios.experiencia` | Medio | `testimonios` | No es público hasta aprobarse; sale con el nombre de la familia, sin correo | Hasta que lo borre o elimine la cuenta |
| `testimonios.foto_clave` | Alto (puede mostrar menores) | Almacén RustFS; caché del teléfono | Sin EXIF; en revisión solo la ven su autora y la moderación | Igual; se borra del almacén al eliminar la cuenta |
| `respuestas_encuesta.respuestas` | Medio | `respuestas_encuesta` | Solo su dueña y la moderación | Mientras exista la cuenta |
| `sugerencias.sugerida_por`, `contacto` | Medio | `sugerencias` | Solo la administración | Al eliminar la cuenta se borran (queda la sugerencia anónima) |
| `sugerencias.descripcion`, `organizacion` | Bajo | `sugerencias` | Solo la administración | Indefinida (son sobre causas, no sobre la familia) |

### Sesión, recuperación y seguridad

| Dato | Nivel | Ubicación | Protección específica | Conservación |
|---|---|---|---|---|
| JWT de acceso | Crítico | Teléfono (`sesion_jwt`) | AES-256-GCM con llave del Android Keystore; firmado HS256 | 15 minutos |
| Refresh token | Crítico | Teléfono (cifrado); en el servidor solo su huella (`sesiones.huella`) | Rotación y un solo uso; reusarlo cierra la sesión | 30 días sin uso, o hasta cerrar sesión |
| Tokens revocados | Bajo | Redis | Contraseña de Redis; red interna | Hasta que vence el token |
| `recuperaciones.huella` (código de 6 dígitos) | Crítico | `recuperaciones` | Se guarda como HMAC, no el código; 5 intentos; un solo uso | Vence a los 15 min; la fila se borra con la cuenta |
| Intentos fallidos por correo | Bajo | Redis | La clave es el SHA-256 del correo, no el correo | 15 minutos |
| `auditoria.ip_hmac` | Bajo (seudónimo) | `auditoria` | HMAC-SHA256 de la IP: sin la llave no se recupera la IP | Indefinida. Pendiente: definir un plazo (propuesta: 12 meses) |
| `auditoria.actor_id`, `accion`, `detalle` | Medio | `auditoria` | La API solo puede agregar renglones: no editarlos ni borrarlos (`test_minimo_privilegio.py`); sin nombres | Indefinida (misma propuesta) |
| `solicitudes_eliminacion.correo`, `folio` | Medio | `solicitudes_eliminacion` | Solo quien atiende la privacidad | Indefinida, como constancia y bloqueo (art. 24) |

### Solo en el teléfono

| Dato | Nivel | Ubicación | Protección específica | Conservación |
|---|---|---|---|---|
| Lugar de "Buscar cerca de" (lo que se escribe o se toca) | Bajo | Memoria de la app, nunca en disco | No es la ubicación del teléfono; a OpenStreetMap va solo el texto, sin token ni correo | Lo que dura la pantalla |
| Correo de la sesión e inscripciones (copia para el worker) | Medio | `CuentasStore` (SharedPreferences privadas) | Sandbox de la app; `allowBackup=false` | Hasta cerrar sesión |
| Bandeja de avisos | Bajo | `AvisosStore` | Sandbox | Hasta cerrar sesión o desinstalar |
| Catálogo público | Público | Room `catalogo.db` | — | Se reemplaza en cada consulta con red |
| Foto de la cámara antes de subirla | Alto | Caché `camara/` | Compartida con la app de cámara solo por FileProvider; se borra en la siguiente foto | Hasta la siguiente foto |

### Catálogo (público)

Nombre, descripción, dirección, teléfono y correo **de las asociaciones, proyectos, campañas y
centros** (no de familias). Nivel **Público**. Se guardan en el servidor y en la caché del teléfono.
Solo la administración del catálogo los modifica, y se conservan mientras estén vigentes.

## 9.4 Dilemas éticos y decisiones de diseño

### Dilema 1 · Ubicación: utilidad frente a privacidad

- **El dilema.** RF-02 pide "lo que está cerca de mí". Para ordenar por cercanía hace falta saber
  dónde está la familia, pero la ubicación de una familia con hijos es de los datos más delicados que
  puede tener una app.
- **Alternativas:**
  - (a) GPS preciso enviado al servidor, que ordena y puede sugerir;
  - (b) solo elegir la ciudad a mano;
  - (c) ubicación aproximada, calculada solo en el teléfono y solo cuando se pide;
  - (d) no pedir la ubicación: la familia escribe o toca el lugar desde el que quiere buscar.
- **Argumentos:**
  - (a) da el mejor orden y permite avisos por cercanía, pero crea un registro de dónde está cada
    familia, que se puede filtrar o pedir;
  - (b) no expone nada, pero no cumple el criterio de RF-02;
  - (c) cumple el criterio con el mínimo dato (LFPDPPP art. 12), aunque el orden es menos fino y no
    hay avisos por cercanía;
  - (d) cumple el criterio sin ningún dato de ubicación, y además sirve para buscar cerca del trabajo
    o de la escuela, no solo de donde está el teléfono; a cambio, la familia escribe un lugar.
- **Postura del equipo:** primero fue (c); en la revisión final (20 oct.) pasamos a (d). "Un dato que
  no se recibe no se puede filtrar", y uno que ni siquiera se pide, menos.
- **Decisión concreta:**
  - el manifiesto no pide ningún permiso de ubicación;
  - "Buscar cerca de" vive en el mapa del Inicio: los municipios de las dos ciudades se resuelven en
    el teléfono; una colonia o calle se busca en OpenStreetMap (`BuscadorDeLugares.kt`), que solo
    recibe el texto y la ciudad, sin token ni correo;
  - la distancia se calcula en el teléfono (`porCercania`, haversine) y ninguna ruta de la API recibe
    coordenadas de la familia.
- **Dónde se ve:** el campo "Buscar cerca de" del mapa; `AndroidManifest.xml`; `CercaniaTest`; aviso
  de privacidad, "Tu ubicación".

### Dilema 2 · Testimonios con fotos de niñas y niños: participación frente a protección

- **El dilema.** Los testimonios con foto motivan a otras familias y son lo que la OSF quiere
  mostrar. Pero muchas fotos tienen menores, y publicarlas sin filtro puede exponerlos (LGDNNA art.
  76; riesgo de *grooming* y *doxing*). Además, la foto de un teléfono lleva las coordenadas de dónde
  se tomó.
- **Alternativas:**
  - (a) publicar al enviar y moderar después;
  - (b) prohibir las fotos;
  - (c) revisión previa por la OSF, foto sin metadatos y sin datos de contacto.
- **Argumentos:**
  - (a) es inmediato y menos trabajo para la OSF, pero lo dañino ya se vio;
  - (b) protege del todo, pero quita lo que más valor tiene para la socia;
  - (c) conserva el valor y protege, a costa de trabajo de moderación y de que la familia espere.
- **Postura del equipo:** (c). El daño a un menor no se deshace al borrar la foto. La decisión
  original ("se publican al enviarse") se **reemplazó** el 16 de octubre (`docs/bitacora.md`).
- **Decisión concreta:**
  - estados `pendiente` → `aprobado`/`rechazado`, y editar un testimonio lo regresa a revisión;
  - solo `moderar:contenido` revisa;
  - la foto en revisión solo la ven su autora y la moderación;
  - toda foto se re-codifica sin EXIF en el teléfono y en el servidor;
  - lo publicado sale con el nombre de la familia, nunca con su correo.
- **Dónde se ve:** Perfil → Testimonios (admin); `test_testimonios.py`;
  `test_foto_valida_se_guarda_sin_exif`; aviso de privacidad, "Las fotos y testimonios".

### Dilema 3 · Notificaciones: inmediatez frente a no depender de terceros

- **El dilema.** Las urgencias ("se necesitan 5 voluntarios") valen más si llegan al instante. La
  forma estándar es Firebase Cloud Messaging, que hace pasar cada aviso por Google.
- **Alternativas:**
  - (a) FCM;
  - (b) WorkManager que pregunta al servidor propio cada 15 minutos.
- **Argumentos:**
  - (a) entrega al instante con la app cerrada, pero pide cuenta de Google, Play Services y otra llave
    privada en el servidor, y el aviso de privacidad tendría que declarar a Google como encargado;
  - (b) no comparte nada con terceros y funciona en teléfonos sin Play Services, pero puede tardar
    ~15 minutos o más (Doze).
- **Postura del equipo:** (b), como en la Práctica 8. Para avisos de voluntariado, unos minutos de
  retraso no ponen a nadie en riesgo; compartir con un tercero quién recibe qué aviso sí cambia lo que
  la familia aceptó.
- **Decisión concreta:**
  - `RevisarAvisosWorker` cada 15 minutos;
  - `GET /v1/avisos?desde=` con la sesión;
  - recordatorios calculados en el teléfono con el catálogo público;
  - el riesgo queda aceptado y escrito en la guía (§15).
- **Dónde se ve:** `RevisarAvisosWorker.kt`; Ajustes (interruptores); `test_avisos.py`; aviso de
  privacidad, "Quién más los ve" (no aparece Google).

### Dilema 4 · Qué conservar cuando una familia pide borrar sus datos

- **El dilema.** La familia tiene derecho a que se borren sus datos (art. 24). La OSF quiere conservar
  las sugerencias de nuevas causas que mandó, y necesita una constancia de que atendió la solicitud.
- **Alternativas:**
  - (a) borrar todo, incluidas sugerencias y constancia;
  - (b) borrar la cuenta pero conservar todo lo demás con su nombre;
  - (c) borrar todo lo personal, conservar la sugerencia sin nombre ni contacto, y una constancia
    mínima.
- **Argumentos:**
  - (a) es lo más radical, pero pierde información útil para la comunidad y la prueba de cumplimiento;
  - (b) conserva datos personales contra la voluntad de la titular;
  - (c) respeta el derecho y conserva lo que no es de la persona.
- **Postura del equipo:** (c).
- **Decisión concreta:**
  - `atender` borra la cuenta en cascada y sus fotos del almacén;
  - las sugerencias quedan como "Familia que eliminó su cuenta", sin contacto;
  - la solicitud se queda con folio, correo y fecha (bloqueo, art. 24).
- **Dónde se ve:** pantalla "Solicitudes de eliminación"; `admin.py::atender`; aviso de privacidad,
  "Cuánto tiempo los guardamos".

### Dilema 5 · Sin conexión: comodidad frente a datos en el teléfono

- **El dilema.** El acta pide consultar las fichas sin conexión. Lo más cómodo sería guardar todo en
  el teléfono, incluidas inscripciones y perfil. Pero un teléfono se pierde o se presta, y ahí
  quedarían nombres y edades de los hijos.
- **Alternativas:**
  - (a) guardar todo;
  - (b) no guardar nada;
  - (c) guardar solo el catálogo público.
- **Postura del equipo:** (c). Cumple el compromiso sin crear una copia de datos personales fuera del
  servidor. Además, el cupo guardado no se presenta como real.
- **Decisión concreta:** Room con una sola tabla de respuestas de rutas públicas (`CatalogoLocal.kt`);
  franja "Sin conexión · estás viendo lo guardado el…"; "Lugares no disponibles sin conexión".
- **Dónde se ve:** modo avión en la app (M-OFF); `CatalogoLocalTest`; aviso de privacidad, "Dónde se
  guardan".
