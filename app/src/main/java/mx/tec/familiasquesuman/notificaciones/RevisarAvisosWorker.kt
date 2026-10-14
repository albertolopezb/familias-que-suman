package mx.tec.familiasquesuman.notificaciones

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import mx.tec.familiasquesuman.FamiliasApplication
import mx.tec.familiasquesuman.data.AvisosStore
import mx.tec.familiasquesuman.data.CuentasStore
import mx.tec.familiasquesuman.data.FavoritosStore
import mx.tec.familiasquesuman.domain.QueAvisar
import mx.tec.familiasquesuman.domain.TipoAviso
import mx.tec.familiasquesuman.domain.avisosDelDia
import mx.tec.familiasquesuman.domain.juntarAvisos
import mx.tec.familiasquesuman.domain.porNotificar
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Corre cada pocas horas aunque la app esté cerrada: ve si a la cuenta con sesión le toca algún
 * aviso (recordatorio, favoritos, urgencias), lo deja en su bandeja y lo manda como notificación
 * del teléfono. Con la app abierta, CuentaViewModel hace lo mismo con lo que tiene en memoria.
 * Lee el catálogo de la API (público) y, de la cuenta, solo la copia local de a qué va.
 */
class RevisarAvisosWorker(contexto: Context, parametros: WorkerParameters) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        revisar(applicationContext)
        return Result.success()
    }

    companion object {
        private const val NOMBRE = "revisar-avisos"

        /** Lo programa una sola vez; llamarlo de nuevo no duplica nada. */
        fun programar(contexto: Context) {
            WorkManager.getInstance(contexto).enqueueUniquePeriodicWork(
                NOMBRE,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<RevisarAvisosWorker>(3, TimeUnit.HOURS).build()
            )
        }

        /**
         * Para probarlo: revisa una sola vez dentro de [segundos], aunque cierren la app mientras
         * tanto (la periódica ya corrió y no vuelve hasta dentro de 3 horas).
         */
        fun probarEn(contexto: Context, segundos: Long = 20) {
            WorkManager.getInstance(contexto).enqueueUniqueWork(
                "$NOMBRE-prueba",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<RevisarAvisosWorker>().setInitialDelay(segundos, TimeUnit.SECONDS).build()
            )
        }

        /** Lo que hace cada vez. Sin sesión abierta no hay a quién avisarle. */
        suspend fun revisar(contexto: Context, ahora: LocalDateTime = LocalDateTime.now()) {
            val container = (contexto.applicationContext as FamiliasApplication).container
            val cuentas = CuentasStore(contexto)
            val avisosStore = AvisosStore(contexto)
            val correo = cuentas.correoSesion() ?: return
            val prefs = container.preferenciasStore.preferencias.value
            // El catálogo es público (no hace falta el token); si no hay red, se intenta en la próxima vuelta.
            val actividades = runCatching { container.actividadRepository.getActividades() }.getOrNull() ?: return
            val campanas = runCatching { container.campanaRepository.getCampanas() }.getOrDefault(emptyList())
            val asociaciones = runCatching { container.actividadRepository.getTodasLasAsociaciones() }.getOrDefault(emptyList())

            val nuevos = avisosDelDia(
                ahora = ahora,
                actividades = actividades,
                campanas = campanas,
                asociaciones = asociaciones.associate { it.id to it.nombre },
                inscritas = cuentas.inscritas(correo),
                favoritas = FavoritosStore(contexto).cargar(correo).orEmpty(),
                lugares = { it.lugaresDisponibles },
                ciudad = prefs.ciudad,
                queAvisar = QueAvisar(prefs.recordatorioActividades, prefs.avisosFavoritos, prefs.urgenciasCiudad)
            )
            val (bandeja, llegaron) = juntarAvisos(avisosStore.cargar(correo), nuevos, reemplazar = false, System.currentTimeMillis())
            if (llegaron.isNotEmpty()) avisosStore.guardar(correo, bandeja)

            val enviados = porNotificar(bandeja, avisosStore.notificados(correo), ahora).filter { aviso ->
                Notificaciones.enviar(
                    contexto,
                    titulo = aviso.titulo,
                    texto = aviso.texto,
                    actividadId = aviso.actividadId,
                    campanaId = aviso.campanaId,
                    canal = when (aviso.tipo) {
                        TipoAviso.URGENCIA -> Notificaciones.CANAL_URGENCIAS
                        TipoAviso.FAVORITO, TipoAviso.CIERRE -> Notificaciones.CANAL_FAVORITOS
                        else -> Notificaciones.CANAL_RECORDATORIOS
                    }
                )
            }
            if (enviados.isNotEmpty()) avisosStore.marcarNotificados(correo, enviados.map { it.id })
        }
    }
}
