package mx.tec.familiasquesuman.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import mx.tec.familiasquesuman.data.red.RevisionTestimonioCuerpo
import mx.tec.familiasquesuman.data.red.TestimonioDto
import mx.tec.familiasquesuman.data.red.aDominio
import mx.tec.familiasquesuman.data.red.llamar
import mx.tec.familiasquesuman.domain.EstadoRevision
import mx.tec.familiasquesuman.domain.Testimonio
import mx.tec.familiasquesuman.domain.puedeEliminarlo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Los testimonios de las familias (RF-12). Se envían EN REVISIÓN y solo se publican cuando la
 * moderación los aprueba (HU-11); los elimina quien los escribió o la moderación. QUIÉN puede
 * revisar o eliminar lo decide el servidor: la app solo lo pide.
 */
interface TestimonioRepository {
    /** Los publicados (aprobados). */
    suspend fun getTodos(): List<Testimonio>
    /** La bandeja de la moderación: lo que espera revisión, lo más viejo primero. */
    suspend fun porRevisar(): List<Testimonio>
    /** Aprobar (se publica) o rechazar (no se publica). @return true si quedó. */
    suspend fun revisar(id: String, aprobar: Boolean): Boolean
    suspend fun getDeFamilia(correo: String): List<Testimonio>
    /** [nuevo].foto es la ruta de la foto en el teléfono; se sube y la API guarda su propia copia. */
    suspend fun publicar(nuevo: Testimonio)
    /** @return true si se eliminó. */
    suspend fun eliminar(id: String, correoDeQuienPide: String?, esAdmin: Boolean): Boolean
}

/** El de verdad: la API. */
class TestimonioRepositoryApi(private val carpetaFotos: File) : TestimonioRepository {

    private suspend fun conFotos(lista: List<TestimonioDto>, correo: String?): List<Testimonio> = coroutineScope {
        lista.map { t ->
            async(Dispatchers.IO) { t.aDominio(t.fotoUrl?.let { Fotos.descargar(it, carpetaFotos) }, correo) }
        }.awaitAll()
    }

    override suspend fun getTodos(): List<Testimonio> = conFotos(llamar { testimonios() }, null)

    override suspend fun getDeFamilia(correo: String): List<Testimonio> = conFotos(llamar { misTestimonios() }, correo)

    override suspend fun porRevisar(): List<Testimonio> = conFotos(llamar { testimoniosPorRevisar() }, null)

    override suspend fun revisar(id: String, aprobar: Boolean): Boolean = runCatching {
        llamar { revisarTestimonio(id, RevisionTestimonioCuerpo(if (aprobar) "aprobado" else "rechazado")) }
    }.isSuccess

    override suspend fun publicar(nuevo: Testimonio) {
        val ruta = requireNotNull(nuevo.foto) { "El testimonio necesita foto" }
        val bytes = withContext(Dispatchers.IO) { Fotos.prepararParaSubir(File(ruta).readBytes()) }
        val foto = MultipartBody.Part.createFormData("foto", "testimonio.jpg", bytes.toRequestBody("image/jpeg".toMediaType()))
        llamar { publicarTestimonio(nuevo.participacionId, nuevo.experiencia.toRequestBody("text/plain".toMediaType()), foto) }
    }

    override suspend fun eliminar(id: String, correoDeQuienPide: String?, esAdmin: Boolean): Boolean =
        runCatching { llamar { eliminarTestimonio(id) }.exigirExito() }.isSuccess
}

/** En memoria, para las pruebas unitarias (no necesitan servidor). Misma regla de quién elimina. */
class TestimonioRepositoryEnMemoria(iniciales: List<Testimonio> = emptyList()) : TestimonioRepository {
    private val testimonios = iniciales.toMutableList()

    override suspend fun getTodos(): List<Testimonio> = testimonios.filter { it.estado == EstadoRevision.APROBADO }

    override suspend fun porRevisar(): List<Testimonio> = testimonios.filter { it.estado == EstadoRevision.PENDIENTE }

    override suspend fun revisar(id: String, aprobar: Boolean): Boolean {
        val i = testimonios.indexOfFirst { it.id == id }
        if (i < 0) return false
        testimonios[i] = testimonios[i].copy(estado = if (aprobar) EstadoRevision.APROBADO else EstadoRevision.RECHAZADO)
        return true
    }

    override suspend fun getDeFamilia(correo: String): List<Testimonio> =
        testimonios.filter { it.correo.equals(correo, ignoreCase = true) }

    override suspend fun publicar(nuevo: Testimonio) {
        val i = testimonios.indexOfFirst {
            it.participacionId == nuevo.participacionId && it.correo.equals(nuevo.correo, ignoreCase = true)
        }
        // Igual que el servidor: lo nuevo (o lo editado) vuelve a revisión.
        val enRevision = nuevo.copy(estado = EstadoRevision.PENDIENTE)
        if (i >= 0) testimonios[i] = enRevision.copy(id = testimonios[i].id) else testimonios.add(0, enRevision)
    }

    override suspend fun eliminar(id: String, correoDeQuienPide: String?, esAdmin: Boolean): Boolean {
        val i = testimonios.indexOfFirst { it.id == id }
        if (i < 0 || !testimonios[i].puedeEliminarlo(correoDeQuienPide, esAdmin)) return false
        testimonios.removeAt(i)
        return true
    }
}
