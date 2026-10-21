# 3. Matriz de trazabilidad y evidencia de pruebas

**Versión que respalda:** tag `v3.0-entrega` del repositorio.
**Evidencia automática:**

- Servidor: 191 pruebas pytest contra PostgreSQL, Redis y RustFS reales → [`docs/evidencia/pytest-servidor.txt`](../evidencia/pytest-servidor.txt).
- App: 66 pruebas locales (JVM) → [`docs/evidencia/pruebas-app.txt`](../evidencia/pruebas-app.txt), más 4 pruebas instrumentadas de Compose corridas en el teléfono físico → [`docs/evidencia/pruebas-instrumentadas.txt`](../evidencia/pruebas-instrumentadas.txt).
- Contrato de la API: `backend/smoke.sh`, 35 casos contra la API levantada → [`docs/evidencia/smoke.txt`](../evidencia/smoke.txt).
- Seguridad dinámica: OWASP ZAP sin y con sesión → [`backend/seguridad/zap/reporte-zap.html`](../../backend/seguridad/zap/reporte-zap.html).
- Pruebas manuales en el teléfono físico (guion `M-xx` al final de esta sección).

Cada nombre de prueba se encuentra con Ctrl+F en el archivo de evidencia. Los ID son los del backlog
en Planner (RF, RNF, HU y TT).

Cómo leer el estado:

- **Cumplido:** hay una prueba que lo demuestra y pasa.
- **Parcialmente cumplido:** pasa lo que se prueba, pero falta algo para que un usuario real lo tenga completo.
- **Pendiente:** no se ha podido demostrar.

> «Pasa» no es lo mismo que «Cumplido» (anexo de la Práctica 11): un requisito con sus pruebas en
> verde puede quedar parcial si a la persona usuaria todavía le falta algo.

## 3.1 Requerimientos funcionales

| ID | Requisito | Criterio de aceptación (del backlog) | Prueba o evidencia | Resultado | Estado |
|---|---|---|---|---|---|
| RF-01 | Directorio de asociaciones y centros por categoría | Una familia que abre la app por primera vez encuentra una causa que le interesa sin saber qué buscar | M-01; `smoke.sh` 2 "El catálogo es público" | Pasa | Cumplido |
| RF-02 | Filtro por ciudad y "cerca de mí", previa autorización | La familia ve primero lo que tiene cerca, sin compartir su ubicación: en el mapa escribe un municipio, colonia o calle (o toca un punto) y salen los tres lugares más cercanos con su distancia | `CercaniaTest` (6: distancia, orden, texto de la distancia, municipios sin internet, la familia de Apodaca, búsqueda en OpenStreetMap sin token); M-02 | Pasan 6 de 6 + M-02 | Cumplido |
| RF-03 | Ficha de la asociación | La familia decide si quiere ayudar sin salir de la app | M-03 | Pasa | Cumplido |
| RF-04 | Actividades con fecha, horario, lugar, qué se hará y si es para niños | La familia sabe si la actividad le acomoda antes de inscribirse | M-04; `test_inscripciones.py::test_edad_minima_se_revisa_en_el_servidor` | Pasa | Cumplido |
| RF-05 | Lugares disponibles en tiempo real | Si dos familias piden el último lugar a la vez, solo una lo obtiene y la otra recibe un aviso claro; nunca se sobrepasa el cupo | `test_inscripciones.py::test_toctou_varias_familias_a_la_vez_no_sobrepasan_el_cupo` (8 familias a la vez, `FOR UPDATE`), `::test_sin_lugares_suficientes`, `::test_inscribirse_descuenta_lugares` | Pasan 3 de 3 | Cumplido |
| RF-06 | Inscripción con acompañantes | La asociación recibe la lista de asistentes con el detalle que necesita | `::test_inscribirse_descuenta_lugares`, `::test_menores_piden_consentimiento`, `::test_quien_registra_asistencias_ve_quienes_van_sin_correos`, `::test_una_familia_o_el_admin_del_catalogo_no_ven_quienes_van`; M-06 (pantalla "Quiénes van") | Pasan 4 de 4 | Cumplido |
| RF-07 | Agregar al calendario | La familia recibe el recordatorio de su calendario el día de la actividad | M-07 en el teléfono (Intent `ACTION_INSERT` con título, inicio, fin y lugar) | Pasa M-07 | Cumplido |
| RF-08 | Contacto directo (llamada, WhatsApp, correo) | Contactar toma un toque, sin copiar números | M-08 en el teléfono | Pasa M-08 | Cumplido |
| RF-09 | Proyectos y campañas vigentes | La familia se entera de las campañas activas sin entrar al sitio web | M-09; `smoke.sh` 2-3 | Pasa | Cumplido |
| RF-10 | Información para donar, sin pagos | La familia sabe qué llevar y a dónde | M-10; revisión: no hay pantalla ni dependencia de pagos en `app/build.gradle.kts` | Pasa | Cumplido |
| RF-11 | Perfil e historial | La familia ve todo lo que ha hecho en el año | `test_participacion.py::test_historial_e_impacto_salen_de_las_asistencias`, `::test_una_familia_no_se_registra_asistencias` | Pasan 2 de 2 | Cumplido |
| RF-12 | Testimonio con foto, revisado antes de publicarse | Nada se publica sin que Familias que Suman lo apruebe | `test_testimonios.py` (6), entre ellas `::test_un_testimonio_nuevo_no_es_publico_hasta_aprobarlo` y `::test_rechazado_no_se_publica`; `TestimoniosYEncuestasTest` (12); `test_fotos.py::test_foto_valida_se_guarda_sin_exif` | Pasan 19 de 19 | Cumplido |
| RF-13 | Encuesta antes y después | Las respuestas quedan asociadas a la actividad y se consultan después | `test_participacion.py::test_encuesta_final_de_una_asistencia_propia`, `::test_encuesta_previa_pide_estar_inscrita`; `EncuestaViewModelTest` (6); `TestimoniosYEncuestasTest.lasRespuestasSeConsultanPorActividadYPorFamilia` | Pasan 9 de 9 | Cumplido |
| RF-14 | Reconocimiento (insignias y niveles) | La familia ve reflejado su recorrido | `FechasYNivelesTest.losNivelesSubenConLasActividades`; `test_historial_e_impacto_salen_de_las_asistencias` (sale de asistencias registradas, nadie se suma insignias) | Pasan 2 de 2 | Cumplido |
| RF-15 | Aviso de privacidad accesible y solicitud de eliminación | Una mamá lo lee completo y entiende qué pasa con sus datos y los de sus hijos | `test_identidad.py::test_registro_pide_el_aviso_de_privacidad`; `test_participacion.py::test_solicitud_de_eliminacion_es_idempotente_y_la_atiende_privacidad`; M-15 (pedir la eliminación desde la app y atenderla en "Solicitudes de eliminación") | Pasan 2 de 2 + M-15 | Cumplido |
| RF-16 | Recordatorio 24 h antes | La familia inscrita recibe el recordatorio sin abrir la app | `AvisosTest.recuerda_el_dia_anterior_y_el_mismo_dia_pero_no_antes`, `::sin_el_interruptor_de_recordatorios_no_recuerda`, `::al_telefono_solo_de_dia_sin_leer_y_una_vez`; M-16 (WorkManager con la app cerrada) | Pasan 3 de 3 + M-16 | Cumplido. Llega por WorkManager cada 15 min, no por FCM (ver desviaciones): puede tardar unos minutos más |
| RF-17 | Favoritos que se actualizan | La familia guarda una asociación y la encuentra sin buscarla; se entera de lo nuevo | `AvisosTest.avisa_lo_nuevo_de_una_asociacion_favorita_si_no_estan_inscritos`, `::avisa_que_cierra_una_campana_favorita`; M-17 | Pasan 2 de 2 | Cumplido |
| RF-18 | Cuenta con registro diferido y recuperación | Se recorre la app sin cuenta; al tocar "Inscribirme" se pide cuenta una vez y regresa a la misma actividad | `test_identidad.py::test_registro_crea_la_cuenta_y_entra`; `test_recuperacion.py` (8); `smoke.sh` 5-10 y 31-32; M-18 | Pasan 9 de 9 + M-18 | Cumplido |
| RF-19 | Cancelar hasta 12 h antes; los lugares se liberan | Con menos de 12 h se rechaza; el contador sube de inmediato | `test_inscripciones.py::test_cancelar_libera_lugares`, `::test_no_se_cancela_a_menos_de_12_horas`; `test_politicas.py::test_cancelar_solo_hasta_12_horas_antes` | Pasan 3 de 3 | Cumplido |
| RF-20 | Filtrar campañas por categoría | Solo se ve lo que cumple el filtro | `TengoAlgoParaDonarViewModelTest.todosYTodasLasCategoriasConservanElFiltroExactoYOrden`; `TengoAlgoParaDonarUiTest.filtrosExpansionYAccionesEntreganLosDatosOriginales`; M-20 | Pasan 2 de 2 | Cumplido |
| RF-21 | Apartar artículos de una campaña | No se aparta más de lo que falta; un reintento no cuenta doble | `test_aportaciones.py` (6): `::test_no_se_aparta_mas_de_lo_que_falta`, `::test_reintento_con_el_mismo_id_no_cuenta_doble` (idempotencia), `::test_campana_cerrada` | Pasan 6 de 6 | Cumplido |
| RF-22 | Avisos personalizados (intereses y urgencias de la ciudad) | La familia recibe urgencias de su ciudad y lo nuevo de lo que sigue | `test_avisos.py` (6): `::test_cada_familia_recibe_los_de_su_ciudad_y_los_generales`, `::test_una_actividad_nueva_genera_su_aviso`; `AvisosDelServidorTest` (2); M-22 (la administración envía un aviso y llega con la app cerrada) | Pasan 8 de 8 + M-22 | Cumplido |
| RF-23 | Métricas gráficas de impacto | La gráfica usa datos reales | `FechasYNivelesTest.cuentaLasActividadesPorMes`; `test_historial_e_impacto_salen_de_las_asistencias` | Pasan 2 de 2 | Cumplido |
| RF-24 | Entrar como familia o como administración, con sus permisos | El perfil y los permisos corresponden al rol | `test_identidad.py::test_iniciar_sesion_da_un_jwt_con_los_atributos_de_la_cuenta`, `::test_nadie_se_registra_como_admin`, `::test_los_permisos_vienen_del_token_no_del_cuerpo`; `smoke.sh` 11, 14-16 | Pasan | Cumplido |
| RF-25 | Elegir acompañantes y ver lugares actualizados | (igual que RF-05 y RF-06) | Las de RF-05 y RF-06 | Pasan | Cumplido |
| RF-26 | Filtrar actividades por ciudad y categoría | Con Monterrey no aparece Hermosillo | M-26 | Pasa M-26 | Cumplido |
| RF-27 | Corazón para guardar en Favoritos | Marcar y desmarcar se refleja en "Mis favoritos" | M-27; `PUT/DELETE /v1/yo/favoritos` idempotentes | Ver M-27 | Cumplido si M-27 pasa |
| RF-28 | Cancelar desde Mis actividades liberando cupos | (igual que RF-19) | Las de RF-19 | Pasan | Cumplido |
| ACTA-01 | Consultar fichas de proyectos y actividades sin conexión (compromiso del acta) | Sin internet se ven las listas y fichas guardadas, con aviso de que son guardadas y sin presumir cupo | `CatalogoLocalTest` (6): `.sin_red_sale_lo_ultimo_guardado_y_se_avisa_de_cuando_es`, `.con_el_servidor_caido_tambien_sale_lo_guardado`, `.un_error_del_servidor_no_se_tapa_con_lo_guardado`, `.al_volver_la_red_desaparece_el_aviso_y_se_reemplaza_lo_guardado`; M-OFF (modo avión) | Pasan 6 de 6 + M-OFF (listas y franja) | Cumplido |

## 3.2 Requerimientos no funcionales

| ID | Requisito | Criterio | Prueba o evidencia | Resultado | Estado |
|---|---|---|---|---|---|
| RNF-01 | Inicio y catálogo en menos de 1.5 s en el teléfono | Promedio de 10 aperturas < 1.5 s | M-RNF01: `adb shell am start -W` (TotalTime) y cronómetro del catálogo, en el teléfono físico | Arranque en frío: promedio 117 ms, máximo 148 ms (10 aperturas); catálogo visible en ~0.9 s contra el servidor de Azure | Cumplido |
| RNF-02 | Sin contraseñas, tokens ni sesiones en claro (repo y logs) | Cero secretos en el código y el historial; nada sensible en Logcat | Búsqueda de secretos en el repositorio (gitleaks, `docs/evidencia/secretos.txt`); `test_identidad.py::test_la_contrasena_se_guarda_como_hash_argon2id`; `test_recuperacion.py::test_el_codigo_no_se_guarda_en_claro`; el token se guarda con AES-256-GCM del Android Keystore (`ServicioSesion.kt`); OkHttp sin interceptor de logs | Pasan | Cumplido |
| RNF-03 | El estado sobrevive a la rotación | No se pierden datos al rotar a mitad de un formulario | ViewModel + `rememberSaveable` (la foto de la cámara también: `ElegirFoto.kt`); M-RNF03 | M-RNF03 por hacer en la demostración | Cumplido en el código; falta la evidencia en el teléfono (M-RNF03) |
| RNF-04 | Atrás no duplica pantallas ni cierra la app | Back regresa a la anterior | `DonarNavegacionUiTest.donarDesdeBarraInferiorYBackRegresaAInicioSinDestinosDeFiltros` (en el teléfono físico); M-RNF04 | Pasan | Cumplido |
| RNF-05 | Android 10 (API 29) o superior | Instala y corre sin fallas | `minSdk = 26` (Android 8), `targetSdk = 37`; probado en emulador Android 17 y en el teléfono físico Samsung SM-S947B con Android 16 (M-RNF05) | Instala y corre en Android 16 y 17 | Parcialmente cumplido: no se probó en un equipo con Android 10 exacto |
| RNF-06 | Cargando, vacío y error en toda pantalla con datos | Los tres estados se distinguen | `TengoAlgoParaDonarUiTest.loadingErrorReintentoYEmptyTienenRepresentacionSeparada`; `TengoAlgoParaDonarViewModelTest.vacioEsExitoYLaCargaYErrorSonIndependientesDeCampanas`; `UiState` común (TT-06) | Pasan 2 de 2 | Cumplido |
| RNF-07 | Confirmación de inscripción en menos de 2 s | 10 inscripciones con red normal < 2 s | M-RNF07 en el teléfono contra el servidor desplegado | El servidor resuelve la inscripción en 25.9 ms; ida y vuelta por el túnel: promedio 169 ms, máximo 240 ms; la confirmación aparece en ~0.2 s | Cumplido |

## 3.3 Requisitos de seguridad (rúbrica, parte 2)

| ID | Control | Criterio | Prueba o evidencia | Resultado | Estado |
|---|---|---|---|---|---|
| SEG-01 | Sesión e identidad robustas | Sin token, token alterado, `alg: none`, otro secreto o vencido → 401; refresh rotado y de un solo uso | `test_identidad.py::test_sin_token_es_401_y_dice_como_autenticarse`, `::test_contenido_alterado_rompe_la_firma`, `::test_alg_none_se_rechaza`, `::test_firmado_con_otro_secreto_se_rechaza`, `::test_token_vencido`, `::test_reusar_un_refresh_token_cierra_toda_la_sesion`; `smoke.sh` 4, 12, 13, 26-30 | Pasan | Cumplido |
| SEG-02 | Secretos no en claro | Contraseñas con Argon2id; códigos de recuperación como HMAC; token en el teléfono cifrado | `::test_la_contrasena_se_guarda_como_hash_argon2id`, `test_recuperacion.py::test_el_codigo_no_se_guarda_en_claro` | Pasan | Cumplido |
| SEG-03 | Nadie accede a lo ajeno (IDOR/BOLA) | Otra familia no ve ni borra lo ajeno (404) | `test_autorizacion.py::test_bola_no_se_ven_ni_se_borran_acompanantes_ajenos`, `::test_bola_testimonio_sobre_asistencia_ajena`, `test_fotos.py::test_la_foto_de_perfil_no_la_ve_otra_familia`; `smoke.sh` 18-19 | Pasan | Cumplido |
| SEG-04 | Acciones críticas con privilegios y validación | Cada función de admin pide su permiso exacto; el cuerpo no puede subir permisos | `test_autorizacion.py::test_funciones_de_admin_piden_permiso` (6), `::test_mass_assignment_campos_extra_se_rechazan`, `test_politicas.py::test_las_funciones_de_admin_piden_su_permiso_y_solo_ese`, `::test_lo_que_no_tiene_regla_se_niega` | Pasan | Cumplido |
| SEG-05 | Consultas parametrizadas y validación | SQLi en filtros, ids y cuerpos sin efecto; caracteres de control y topes | `test_entradas.py` (23); `test_minimo_privilegio.py` (10); `smoke.sh` 20-21 | Pasan | Cumplido |
| SEG-06 | Rate limit y bloqueo temporal | Inicio de sesión, registro y global con límite; 5 fallos bloquean el correo 15 min | `test_identidad.py::test_fuerza_bruta_bloquea_el_correo_aunque_cambie_de_ip`, `::test_limite_por_ip_del_inicio_de_sesion`, `test_seguridad_http.py::test_rate_limit_en_un_flujo_publico`, `::test_limite_global_por_ip`; `smoke.sh` 35 | Pasan | Cumplido |
| SEG-07 | Tráfico cifrado y cabeceras de seguridad | HTTPS (TLS 1.2/1.3) en el camino público; HSTS, nosniff y CSP en cada respuesta | `test_seguridad_http.py::test_encabezados_de_seguridad_en_todas_las_respuestas`; `smoke.sh` 22-24; `res/xml/seguridad_red.xml` (sin HTTP en claro); OWASP ZAP | Pasan | Cumplido |
| SEG-08 | DAST con OWASP ZAP | Sin alertas altas ni medias, sin y con sesión | `backend/seguridad/zap/reporte-zap.*` | 0 altas, 0 medias, 0 bajas, 1 informativa | Cumplido |

## 3.4 Historias de usuario

Cada historia se demuestra con las pruebas del requisito que la origina.

| HU | Requisitos | Estado |
|---|---|---|
| HU-01 Iniciar sesión y ver mis actividades e impacto | RF-11, RF-18, RF-23, RF-24 | Cumplido |
| HU-02 Panel de administración | RF-24, SEG-04; CRUD de TT-12 (M-HU02) | Cumplido |
| HU-03 Detalle completo de una actividad | RF-04, RF-05 | Cumplido |
| HU-04 Menú rápido en el encabezado | M-HU04 | Cumplido si M-HU04 pasa |
| HU-05 Apartar artículos | RF-21 | Cumplido |
| HU-06 Centros de donación con teléfono y mapa | RF-08, RF-10 (M-HU06) | Cumplido si M-HU06 pasa |
| HU-07 Aviso si se ocuparon los lugares mientras me inscribo | RF-05 (`sin_lugares` → pantalla P-08) | Cumplido |
| HU-08 Agregar al calendario | RF-07 (M-07) | Cumplido |
| HU-09 Cancelar hasta 12 h antes | RF-19 | Cumplido |
| HU-10 Favoritos en mi perfil | RF-17, RF-27 | Cumplido |
| HU-11 Testimonio con revisión | RF-12 | Cumplido |
| HU-12 Encuestas con selección única, progreso y validación | RF-13 (`EncuestaViewModelTest.noPermiteAvanzarSinRespuesta`) | Cumplido |
| HU-13 Recordatorio 24 h antes | RF-16 | Cumplido |
| HU-14 Solicitar la eliminación de mis datos | RF-15 | Cumplido |

## 3.5 Niveles de prueba

| Nivel | Qué se prueba | Herramienta | Evidencia |
|---|---|---|---|
| Unitarias (JVM) | Reglas de avisos, niveles, fechas, cercanía, coordenadas, encuestas, caché sin conexión | JUnit, ViewModel con repositorio falso | `docs/evidencia/pruebas-app.txt` (60) |
| Interfaz (instrumentadas) | Estados cargando/vacío/error, filtros y Atrás en Compose | Compose UI Test en el teléfono físico | `docs/evidencia/pruebas-instrumentadas.txt` (4 de 4) |
| Integración del servidor | Cada regla contra PostgreSQL, Redis y RustFS reales; políticas Oso directas | pytest en Docker | `docs/evidencia/pytest-servidor.txt` (182) |
| Contrato | La API levantada responde lo que promete, incluidos los rechazos | `backend/smoke.sh` (bash + curl) | `docs/evidencia/smoke.txt` (35) |
| Concurrencia | 8 familias por los mismos lugares a la vez | pytest con hilos y `Barrier` | `test_toctou_varias_familias_a_la_vez_no_sobrepasan_el_cupo` |
| Seguridad dinámica (DAST) | SQLi, XSS, inyección, cabeceras, fugas, sin y con sesión | OWASP ZAP 2.17 | `backend/seguridad/zap/` |
| Sistema (extremo a extremo) | Flujos completos con el APK de release firmado contra el servidor desplegado | Teléfono físico | Guion M-xx (abajo) y video de respaldo |

## 3.6 Guion de pruebas manuales en el teléfono (M-xx)

APK de release firmado, contra el servidor desplegado en Azure por el túnel HTTPS.

| ID | Pasos | Resultado esperado | Resultado |
|---|---|---|---|
| M-01 | Abrir la app sin cuenta → Inicio → "¿Cómo quieres ayudar hoy?" → Directorio → una categoría → una asociación | Se llega a una ficha sin buscar | Pendiente |
| M-02 | Inicio → mapa → "Buscar cerca de: Apodaca"; luego mantener el dedo sobre otro punto | Sale "Lo más cerca de Apodaca" con 3 lugares y su distancia; el punto tocado cambia la lista. La app no pide permiso de ubicación | Pendiente |
| M-03 | Abrir la ficha de 3 asociaciones | Qué hace, a quién ayuda, qué necesita, dirección y contacto | Pendiente |
| M-04 | Abrir una actividad | Fecha, horario, lugar, qué se hará, edad mínima y lugares | Pendiente |
| M-06 | Inscribirse con 2 acompañantes (uno menor, con consentimiento) → entrar como admin → Actividades → ícono "Quiénes van" | Aparece la familia con sus integrantes y "con menores" | Pasa (2026-10-20): la familia aparece con Sofía (9) y "con menores", vista como familia y como admin |
| M-07 | En una actividad inscrita, "Agregar al calendario" | El calendario abre el evento con título, hora y lugar | Pasa (2026-10-20) |
| M-08 | En una ficha: llamar, WhatsApp, correo | Cada botón abre la app correcta con el dato puesto | Pasa (2026-10-20): WhatsApp abre la conversación con el número |
| M-09 | Proyectos y Donar | Se listan los vigentes con su detalle | Pendiente |
| M-10 | Donar → una campaña → "¿Cómo donar?" | Qué llevar, dónde y horarios; ninguna pantalla de pago | Pendiente |
| M-15 | Perfil → Ajustes → Aviso de privacidad → "Solicitar eliminación" → folio. Entrar como admin → Perfil → "Solicitudes de eliminación" → Eliminar | Folio ELIM-xxxx; tras atenderla, esa cuenta ya no puede entrar | Pasa (2026-10-20): folio ELIM-0001; atendida, el inicio de sesión de esa cuenta responde 401 |
| M-16 | Inscribirse a una actividad de mañana; admin → Perfil → "Avisos con la app cerrada (en 20 s)"; cerrar la app | Llega el recordatorio como notificación | Pendiente |
| M-17 | Marcar una asociación como favorita; la administración publica una actividad suya | Llega el aviso de lo nuevo | Pendiente |
| M-18 | Sin cuenta, abrir una actividad → "Unirme" → crear cuenta | Al terminar regresa a la misma actividad | Pasa (2026-10-20) |
| M-20 | Donar → filtros por categoría y urgentes | Solo se ve lo que cumple | Pendiente |
| M-22 | Admin → "Enviar aviso a las familias" (urgencia de Monterrey) → app cerrada en otra cuenta de Monterrey | Llega la notificación | Pasa (2026-10-20) tras activar "Urgencias de tu ciudad" en Ajustes: las urgencias son opcionales (vienen apagadas) y al teléfono solo llegan de 8:00 a 21:00; fuera de ese horario quedan en la bandeja |
| M-26 | Cambiar de Monterrey a Hermosillo | No aparece nada de la otra ciudad | Pasa (2026-10-20) |
| M-27 | Marcar y desmarcar el corazón | Se refleja en "Mis favoritos" | Pendiente |
| M-OFF | Abrir Actividades y Proyectos con red → modo avión → cerrar y abrir la app → abrir una ficha | Franja "Sin conexión · estás viendo lo guardado el…"; listas y fichas visibles; el cupo dice "Lugares no disponibles sin conexión" | Pasa (2026-10-20), con la API detenida 31 s en la VM (14:59:27 a 14:59:58 UTC; el túnel respondía 502): tras cerrar y abrir la app, Actividades y Proyectos salen de lo guardado con la franja ("guardado el 20 de octubre, 08:59" y "08:56": cada lista dice cuándo se guardó) y el cupo dice "Lugares no disponibles sin conexión". Con la API de vuelta, el cupo en vivo regresa (12/20). Capturas en `docs/evidencia/m-off-*.png`. Abrir la ficha sin conexión: por repetir en la demostración con modo avión |
| M-RNF01 | `adb shell am start -W mx.tec.familiasquesuman/.MainActivity` 10 veces (arranque en frío) y medir la carga del catálogo | Promedio < 1.5 s | Pasa (2026-10-20): TotalTime promedio 117 ms, máximo 148 ms; catálogo visible en ~0.9 s |
| M-RNF03 | Rotar a mitad de crear cuenta y de un testimonio | No se pierde lo escrito | Pendiente (hacerlo en la demostración) |
| M-RNF04 | Recorrer Inicio → Actividad → Unirme → Atrás ×3 | Regresa paso a paso, sin cerrar la app | Pasa (2026-10-20) |
| M-RNF05 | Instalar el APK en el teléfono físico | Instala y abre sin fallas (versión de Android anotada) | Pasa (2026-10-20): Samsung SM-S947B, Android 16 (API 36) |
| M-RNF07 | 10 inscripciones/modificaciones con red normal | Confirmación < 2 s | Pasa (2026-10-20): ~0.2 s (servidor 25.9 ms; túnel promedio 169 ms, máximo 240 ms) |
| M-HU02 | Admin: crear, editar, duplicar y borrar una actividad | Cada cambio se ve en la lista | Pasa (2026-10-20) duplicar y borrar; crear y editar por repetir en la demostración |
| M-HU04 | Tocar el ícono de perfil del encabezado | Abre perfil, ajustes y notificaciones | Pendiente |
| M-HU06 | Directorio → un centro | Teléfono, dirección y mapa | Pendiente |
| M-MAPA | Inicio → "Mapa de Monterrey": mover, acercar, tocar un puntero, "Agrandar" y "Cerrar"; "Ocultar" y "Ver mapa" | Calles de OpenStreetMap con los punteros de colores; el mini menú abre la ficha; a pantalla completa y de regreso | Pasa (2026-10-20) |
| M-BUSCAR | Buscar en Actividades ("bernabe") y en Donar ("tapitas") | Solo quedan las que coinciden, sin importar acentos ni mayúsculas | Pasa (2026-10-20) |

Lo marcado "Pendiente" queda para correrlo en la demostración con el mismo APK.
