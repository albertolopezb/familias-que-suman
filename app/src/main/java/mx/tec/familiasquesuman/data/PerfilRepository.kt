package mx.tec.familiasquesuman.data

import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mx.tec.familiasquesuman.data.red.ClienteApi
import mx.tec.familiasquesuman.data.red.aDominio
import mx.tec.familiasquesuman.data.red.llamar
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.Familia
import mx.tec.familiasquesuman.domain.Impacto
import mx.tec.familiasquesuman.domain.Participacion

/**
 * Lo de la familia con sesión: perfil, impacto, historial, favoritas y la solicitud para borrar
 * sus datos. Todo sale de /v1/yo/…: el servidor sabe quién es por el token, así que aquí NUNCA
 * se manda el correo ni el id de la cuenta (el parámetro `correo` que queda en algunas firmas es
 * solo para saber si hay sesión).
 */
class PerfilRepository {

    suspend fun getFamilia(): Familia {
        val yo = llamar { yo() }
        return Familia(yo.id, yo.nombreFamilia, yo.ciudad, yo.correo)
    }

    suspend fun getImpacto(correo: String?): Impacto {
        if (correo == null) return Impacto(0, 0, 0)
        val i = llamar { yo() }.impacto
        return Impacto(i.actividadesRealizadas, i.horasDeServicio, i.campanasApoyadas)
    }

    /** Lo que la familia ya hizo, de lo más reciente a lo más viejo (RF-11). */
    suspend fun getHistorial(correo: String?): List<Participacion> =
        if (correo == null) emptyList() else llamar { historial() }.map { it.aDominio() }

    /** Las asociaciones marcadas con el corazón. */
    suspend fun getFavoritas(): List<Asociacion> {
        val marcadas = llamar { favoritos() }.filter { it.tipo == "asociacion" }.map { it.objetoId }.toSet()
        if (marcadas.isEmpty()) return emptyList()
        return llamar { asociaciones(todas = true) }.filter { it.id in marcadas }.map { it.aDominio() }
    }

    /** El folio de la solicitud de eliminación de datos pendiente, si ya la mandó. */
    suspend fun getSolicitudEliminacion(correo: String): String? {
        val texto = llamar { miSolicitudEliminacion() }.string().trim()
        if (texto == "null" || texto.isEmpty()) return null
        return ClienteApi.json.parseToJsonElement(texto).jsonObject["folio"]?.jsonPrimitive?.content
    }

    /** Registra la solicitud de eliminación (HU-14). Si ya había una, regresa la misma. */
    suspend fun solicitarEliminacion(correo: String): String = llamar { solicitarEliminacion() }.folio
}
