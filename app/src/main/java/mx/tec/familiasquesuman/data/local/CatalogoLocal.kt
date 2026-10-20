package mx.tec.familiasquesuman.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import mx.tec.familiasquesuman.data.red.ApiExcepcion
import mx.tec.familiasquesuman.data.red.ClienteApi

/**
 * El catálogo guardado en el teléfono: lo último que respondió la API de actividades, asociaciones,
 * proyectos, campañas y centros, para consultar las fichas SIN CONEXIÓN (acta de acuerdos).
 *
 * Es Room, como en la Práctica 5, con una sola tabla: la respuesta de cada ruta pública tal como
 * llegó (JSON) y cuándo llegó. Así no hay que duplicar el modelo del servidor en entidades.
 *
 * Privacidad: SOLO se guarda lo público (lo mismo que cualquiera ve sin cuenta). Nada de /v1/yo,
 * ni inscripciones, ni datos de la familia: eso vive en el servidor y se pide con la sesión.
 *
 * Sincronización: el catálogo es de solo lectura en el teléfono, así que no hay cambios locales
 * que subir ni conflictos. Cada vez que hay red, la respuesta nueva reemplaza a la guardada.
 * Lo que sí cambia la familia (inscribirse, apartar) se manda al servidor en el momento y nunca
 * desde lo guardado: el cupo de lo guardado puede estar viejo.
 */
@Entity(tableName = "respuestas")
data class RespuestaGuardada(
    @PrimaryKey val clave: String,
    val json: String,
    val guardadaEn: Long
)

@Dao
interface RespuestasDao {
    @Query("SELECT * FROM respuestas WHERE clave = :clave")
    suspend fun leer(clave: String): RespuestaGuardada?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(respuesta: RespuestaGuardada)
}

@Database(entities = [RespuestaGuardada::class], version = 1, exportSchema = false)
abstract class CatalogoDb : RoomDatabase() {
    abstract fun respuestas(): RespuestasDao
}

object CatalogoLocal {

    @Volatile private var dao: RespuestasDao? = null

    private val _mostrandoGuardado = MutableStateFlow<Long?>(null)

    /**
     * null = lo que se ve llegó de la API. Un número = no hubo conexión y se está enseñando lo
     * guardado en ese momento (milisegundos). La app lo usa para avisar y para no presumir cupo.
     */
    val mostrandoGuardado: StateFlow<Long?> = _mostrandoGuardado.asStateFlow()

    /** Para las pruebas: una tabla falsa en memoria en lugar de la base del teléfono. */
    internal fun usar(tabla: RespuestasDao) {
        dao = tabla
        _mostrandoGuardado.value = null
    }

    fun iniciar(contexto: Context) {
        if (dao != null) return
        dao = Room.databaseBuilder(contexto.applicationContext, CatalogoDb::class.java, "catalogo.db")
            .fallbackToDestructiveMigration(dropAllTables = true)   // es una caché: se puede rehacer
            .build()
            .respuestas()
    }

    /**
     * Pide [red] a la API y guarda la respuesta bajo [clave]. Si no hay conexión, regresa lo último
     * guardado (y lo avisa en [mostrandoGuardado]); si nunca se guardó, deja pasar el error. Lo
     * mismo si el servidor está caído (5xx). Las respuestas de "no" del servidor (404, 422…) no se
     * tapan con lo guardado: se ven tal cual.
     */
    suspend fun <T> conRespaldo(clave: String, serializador: KSerializer<T>, red: suspend () -> T): T {
        val tabla = dao
        return try {
            red().also { valor ->
                _mostrandoGuardado.value = null
                tabla?.runCatching {
                    guardar(RespuestaGuardada(clave, ClienteApi.json.encodeToString(serializador, valor), System.currentTimeMillis()))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiExcepcion) {
            // Sin internet, o el servidor no responde (502/503/504 del túnel cuando la API está
            // caída): se enseña lo guardado. Un 4xx (no existe, datos inválidos) se ve tal cual.
            if (!e.sinServidor) throw e
            leer(clave, serializador) ?: throw e
        }
    }

    /** Lo guardado bajo [clave], o null. Marca que se está enseñando algo guardado. */
    suspend fun <T> leer(clave: String, serializador: KSerializer<T>): T? {
        val guardada = dao?.leer(clave) ?: return null
        val valor = runCatching { ClienteApi.json.decodeFromString(serializador, guardada.json) }.getOrNull() ?: return null
        _mostrandoGuardado.value = guardada.guardadaEn
        return valor
    }
}
