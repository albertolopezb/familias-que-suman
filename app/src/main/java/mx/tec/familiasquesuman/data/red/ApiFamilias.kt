package mx.tec.familiasquesuman.data.red

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Todas las rutas de la API (backend/api). Retrofit arma cada petición a partir de estas firmas.
 * El token (JWT) NO se pasa aquí: lo agrega ClienteApi a las que lo necesitan.
 */
interface ApiFamilias {

    // ─── Catálogo (público) ───
    @GET("v1/ciudades") suspend fun ciudades(): List<CiudadDto>
    @GET("v1/asociaciones") suspend fun asociaciones(@Query("todas") todas: Boolean = false): List<AsociacionDto>
    @GET("v1/asociaciones/{id}") suspend fun asociacion(@Path("id") id: String): AsociacionDto
    @GET("v1/actividades") suspend fun actividades(@Query("ciudad") ciudad: String? = null): List<ActividadDto>
    @GET("v1/actividades/{id}") suspend fun actividad(@Path("id") id: String): ActividadDto
    @GET("v1/actividades/{id}/enlace") suspend fun enlace(@Path("id") id: String): EnlaceDto
    @GET("v1/campanas") suspend fun campanas(): List<CampanaDto>
    @GET("v1/campanas/{id}") suspend fun campana(@Path("id") id: String): CampanaDto
    @GET("v1/proyectos") suspend fun proyectos(): List<ProyectoDto>
    @GET("v1/centros") suspend fun centros(): List<CentroDto>
    @GET("v1/testimonios") suspend fun testimonios(): List<TestimonioDto>
    @GET suspend fun descargar(@Url ruta: String): ResponseBody
    @POST("v1/sugerencias") suspend fun sugerir(@Body cuerpo: SugerenciaCuerpo): ConteoDto

    // ─── Sesión: correo y contraseña → JWT ───
    @POST("v1/sesion/iniciar") suspend fun iniciarSesion(@Body cuerpo: InicioSesionCuerpo): TokenDto
    @POST("v1/sesion/registro") suspend fun registrarse(@Body cuerpo: RegistroCuerpo): TokenDto
    @POST("v1/sesion/recuperar") suspend fun recuperar(@Body cuerpo: CorreoCuerpo): Response<Unit>
    @POST("v1/sesion/restablecer") suspend fun restablecer(@Body cuerpo: RestablecerCuerpo): Response<Unit>
    @POST("v1/sesion/cerrar") suspend fun cerrarSesion(@Body cuerpo: CerrarSesionCuerpo): Response<Unit>
    // La renovación (v1/sesion/renovar) NO va aquí: la hace ServicioSesion con su propio cliente,
    // sin interceptor ni Authenticator, para que nunca se llame a sí misma en ciclo.

    // ─── Mi cuenta ───
    @GET("v1/yo") suspend fun yo(): YoDto
    @PATCH("v1/yo") suspend fun editarYo(@Body cambios: YoCambiosDto): YoDto
    @GET("v1/yo/acompanantes") suspend fun acompanantes(): List<AcompananteDto>
    @POST("v1/yo/acompanantes") suspend fun agregarAcompanante(@Body persona: PersonaDto): AcompananteDto
    @DELETE("v1/yo/acompanantes/{id}") suspend fun quitarAcompanante(@Path("id") id: String): Response<Unit>
    @GET("v1/yo/favoritos") suspend fun favoritos(): List<FavoritoDto>
    @PUT("v1/yo/favoritos/{id}") suspend fun marcarFavorito(@Path("id") id: String): FavoritoDto
    @DELETE("v1/yo/favoritos/{id}") suspend fun quitarFavorito(@Path("id") id: String): Response<Unit>
    @GET("v1/yo/preferencias") suspend fun preferencias(): PreferenciasDto
    @PUT("v1/yo/preferencias") suspend fun guardarPreferencias(@Body p: PreferenciasDto): PreferenciasDto
    @GET("v1/yo/foto") suspend fun miFoto(): ResponseBody
    @Multipart @PUT("v1/yo/foto") suspend fun cambiarFoto(@Part foto: MultipartBody.Part): YoDto
    @DELETE("v1/yo/foto") suspend fun quitarFoto(): Response<Unit>
    @POST("v1/yo/invitaciones") suspend fun registrarInvitacion(@Body cuerpo: InvitacionDto): ConteoDto
    @GET("v1/yo/eliminacion") suspend fun miSolicitudEliminacion(): ResponseBody   // JSON: la solicitud o null
    @POST("v1/yo/eliminacion") suspend fun solicitarEliminacion(): SolicitudEliminacionDto

    // ─── Inscripciones ───
    @GET("v1/yo/inscripciones") suspend fun inscripciones(): List<InscripcionDto>
    @PUT("v1/yo/inscripciones/{actividad}") suspend fun inscribirse(@Path("actividad") actividadId: String, @Body cuerpo: InscripcionCuerpo): InscripcionResultadoDto
    @DELETE("v1/yo/inscripciones/{actividad}") suspend fun cancelar(@Path("actividad") actividadId: String): CancelacionDto

    // ─── Participación ───
    @GET("v1/yo/historial") suspend fun historial(): List<ParticipacionDto>
    @GET("v1/yo/testimonios") suspend fun misTestimonios(): List<TestimonioDto>
    @Multipart @PUT("v1/yo/participaciones/{id}/testimonio")
    suspend fun publicarTestimonio(@Path("id") participacionId: String, @Part("experiencia") experiencia: RequestBody, @Part foto: MultipartBody.Part): TestimonioDto
    @DELETE("v1/testimonios/{id}") suspend fun eliminarTestimonio(@Path("id") id: String): Response<Unit>
    @GET("v1/yo/encuestas") suspend fun misEncuestas(): List<EncuestaDto>
    @PUT("v1/yo/actividades/{id}/encuesta-previa") suspend fun encuestaPrevia(@Path("id") actividadId: String, @Body c: EncuestaCuerpo): EncuestaDto
    @PUT("v1/yo/participaciones/{id}/encuesta-final") suspend fun encuestaFinal(@Path("id") participacionId: String, @Body c: EncuestaCuerpo): EncuestaDto

    // ─── Campañas ───
    @POST("v1/campanas/articulos/{id}/aportaciones") suspend fun apartar(@Path("id") articuloId: String, @Body c: AportacionCuerpo): AportacionDto

    // ─── Administración (el servidor revisa el permiso; la app solo enseña los botones) ───
    @GET("v1/admin/actividades/{id}/inscritos") suspend fun inscritos(@Path("id") id: String): List<InscritoDto>
    @GET("v1/admin/solicitudes-eliminacion") suspend fun solicitudesEliminacion(): List<SolicitudEliminacionDto>
    @GET("v1/admin/resumen") suspend fun resumen(): ResumenDto
    @POST("v1/admin/solicitudes-eliminacion/{id}/atender") suspend fun atenderEliminacion(@Path("id") id: String): SolicitudEliminacionDto
    @GET("v1/admin/encuestas") suspend fun todasLasEncuestas(@Query("actividad_id") actividadId: String? = null): List<EncuestaDto>
    @GET("v1/admin/sugerencias") suspend fun bandejaSugerencias(): List<SugerenciaDto>
    @PATCH("v1/admin/sugerencias/{id}") suspend fun revisarSugerencia(@Path("id") id: String, @Body c: EstadoSugerenciaCuerpo): SugerenciaDto
    @GET("v1/avisos") suspend fun avisos(@Query("desde") desde: Long): List<AvisoDto>
    @POST("v1/admin/avisos") suspend fun crearAviso(@Body c: AvisoCuerpo): AvisoDto
    @GET("v1/admin/testimonios") suspend fun testimoniosPorRevisar(@Query("estado") estado: String = "pendiente"): List<TestimonioDto>
    @PATCH("v1/admin/testimonios/{id}") suspend fun revisarTestimonio(@Path("id") id: String, @Body c: RevisionTestimonioCuerpo): TestimonioDto
    @POST("v1/admin/asociaciones") suspend fun crearAsociacion(@Body c: AsociacionCuerpo): AsociacionDto
    @PUT("v1/admin/asociaciones/{id}") suspend fun editarAsociacion(@Path("id") id: String, @Body c: AsociacionCuerpo): AsociacionDto
    @DELETE("v1/admin/asociaciones/{id}") suspend fun borrarAsociacion(@Path("id") id: String): Response<Unit>
    @POST("v1/admin/actividades") suspend fun crearActividad(@Body c: CuerpoLibre): ActividadDto
    @PUT("v1/admin/actividades/{id}") suspend fun editarActividad(@Path("id") id: String, @Body c: CuerpoLibre): ActividadDto
    @POST("v1/admin/actividades/{id}/duplicar") suspend fun duplicarActividad(@Path("id") id: String): ActividadDto
    @DELETE("v1/admin/actividades/{id}") suspend fun borrarActividad(@Path("id") id: String): Response<Unit>
    @POST("v1/admin/campanas") suspend fun crearCampana(@Body c: CuerpoLibre): CampanaDto
    @PUT("v1/admin/campanas/{id}") suspend fun editarCampana(@Path("id") id: String, @Body c: CuerpoLibre): CampanaDto
    @DELETE("v1/admin/campanas/{id}") suspend fun borrarCampana(@Path("id") id: String): Response<Unit>
    @POST("v1/admin/proyectos") suspend fun crearProyecto(@Body c: CuerpoLibre): ProyectoDto
    @PUT("v1/admin/proyectos/{id}") suspend fun editarProyecto(@Path("id") id: String, @Body c: CuerpoLibre): ProyectoDto
    @DELETE("v1/admin/proyectos/{id}") suspend fun borrarProyecto(@Path("id") id: String): Response<Unit>
    @POST("v1/admin/centros") suspend fun crearCentro(@Body c: CuerpoLibre): CentroDto
    @PUT("v1/admin/centros/{id}") suspend fun editarCentro(@Path("id") id: String, @Body c: CuerpoLibre): CentroDto
    @DELETE("v1/admin/centros/{id}") suspend fun borrarCentro(@Path("id") id: String): Response<Unit>
}
