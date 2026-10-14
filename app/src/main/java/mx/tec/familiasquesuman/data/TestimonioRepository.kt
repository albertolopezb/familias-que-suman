package mx.tec.familiasquesuman.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import mx.tec.familiasquesuman.data.red.TestimonioDto
import mx.tec.familiasquesuman.data.red.aDominio
import mx.tec.familiasquesuman.data.red.llamar
import mx.tec.familiasquesuman.domain.Testimonio
import mx.tec.familiasquesuman.domain.puedeEliminarlo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Los testimonios de las familias (RF-12). Se publican en cuanto se envían; los elimina quien
 * los escribió o la moderación. QUIÉN puede eliminar lo decide el servidor: la app solo lo pide.
 */
interface TestimonioRepository {
    suspend fun getTodos(): List<Testimonio>
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

    override suspend fun getTodos(): List<Testimonio> = testimonios.toList()

    override suspend fun getDeFamilia(correo: String): List<Testimonio> =
        testimonios.filter { it.correo.equals(correo, ignoreCase = true) }

    override suspend fun publicar(nuevo: Testimonio) {
        val i = testimonios.indexOfFirst {
            it.participacionId == nuevo.participacionId && it.correo.equals(nuevo.correo, ignoreCase = true)
        }
        if (i >= 0) testimonios[i] = nuevo.copy(id = testimonios[i].id) else testimonios.add(0, nuevo)
    }

    override suspend fun eliminar(id: String, correoDeQuienPide: String?, esAdmin: Boolean): Boolean {
        val i = testimonios.indexOfFirst { it.id == id }
        if (i < 0 || !testimonios[i].puedeEliminarlo(correoDeQuienPide, esAdmin)) return false
        testimonios.removeAt(i)
        return true
    }
}
