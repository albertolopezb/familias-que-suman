package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.domain.EstadoParticipacion
import mx.tec.familiasquesuman.domain.Familia
import mx.tec.familiasquesuman.domain.Impacto
import mx.tec.familiasquesuman.domain.Participacion

// Datos falsos, idénticos a los del Figma. `internal`: solo los repositorios de
// data/ los tocan. Ninguna pantalla ni ViewModel los importa directo; así, cuando
// llegue el backend, se cambia el repositorio y nadie más se entera.
// Teléfonos y correos ficticios.
internal object DatosDePrueba {

    val familia = Familia("f1", "Familia Rodríguez", "Monterrey", "ana.rodriguez@correo.com")

    val impacto = Impacto(actividadesRealizadas = 12, horasDeServicio = 36, campanasApoyadas = 4)

    val asociaciones = listOf(
        Asociacion("a1", "Comedor Comunitario San Bernabé", "Alimentación",
            "Damos comida caliente a 180 familias de la colonia y armamos despensas de fin de mes.",
            "Av. Rómulo Garza 240, Col. San Bernabé, Monterrey",
            "8100000001", "5218100000001", "contacto@comedorsanbernabe.org"),
        Asociacion("a2", "Casa de Día Los Robles", "Adultos mayores",
            "Acompañamos a 40 abuelitos con actividades dos veces por semana.",
            "Calle Los Robles 118, Monterrey",
            "8100000002", "5218100000002", "contacto@casalosrobles.org"),
        Asociacion("a3", "Albergue Nuevo Amanecer", "Ropa y abrigo",
            "Recibimos ropa de invierno todo el año para personas en situación de calle.",
            "Av. Colón 905, Monterrey",
            "8100000003", "5218100000003", "contacto@nuevoamanecer.org"),
        Asociacion("a4", "Parroquia San Bernabé", "Niñez",
            "Organizamos la primera comunión de los niños de la colonia.",
            "Av. Aztlán 1500, Monterrey",
            "8100000004", "5218100000004", "contacto@parroquiasanbernabe.org")
    )

    val actividades = listOf(
        Actividad("act1", "Preparar despensas de fin de mes", "a1",
            "Sábado 12 de septiembre", "9:00 a 12:00", "Av. Rómulo Garza 240, Col. San Bernabé",
            edadMinima = 6,
            descripcion = "Vamos a armar 300 despensas para las familias de la colonia. Se forman equipos de cuatro. Lleven ropa cómoda y agua.",
            cupoTotal = 20, lugaresDisponibles = 8),
        Actividad("act2", "Tarde de lectura con abuelitos", "a2",
            "Domingo 13 de septiembre", "16:00 a 18:00", "Calle Los Robles 118",
            edadMinima = 4,
            descripcion = "Cada familia lee un cuento con un abuelito y después compartimos un café.",
            cupoTotal = 12, lugaresDisponibles = 2),
        Actividad("act3", "Clasificar ropa de invierno", "a3",
            "Sábado 19 de septiembre", "10:00 a 13:00", "Av. Colón 905",
            edadMinima = 15,
            descripcion = "Separamos la ropa donada por talla y tipo antes de repartirla.",
            cupoTotal = 25, lugaresDisponibles = 15)
    )

    val campanas = listOf(
        Campana("c1", "Kits de primera comunión para San Bernabé", "a4", "Útiles escolares",
            cierra = "20 de septiembre", urgente = true,
            descripcion = "Cuarenta y cinco niños hacen su primera comunión el 4 de octubre. Aparta lo que vayas a llevar para que no se junten cosas repetidas.",
            unidadMeta = "kits", metaTotal = 45, completados = 18,
            articulos = listOf(
                ArticuloMeta("c1-1", "Rosario blanco", 45, 12),
                ArticuloMeta("c1-2", "Biblia infantil", 45, 30),
                ArticuloMeta("c1-3", "Vela decorada", 45, 45),
                ArticuloMeta("c1-4", "Mochila con útiles", 45, 8)
            )),
        Campana("c2", "Despensas de fin de mes", "a1", "Alimentos",
            cierra = "30 de septiembre", urgente = false,
            descripcion = "Arroz, frijol y aceite para 300 familias de la colonia.",
            unidadMeta = "despensas", metaTotal = 300, completados = 120,
            articulos = listOf(
                ArticuloMeta("c2-1", "Arroz, bolsa de 1 kg", 300, 140),
                ArticuloMeta("c2-2", "Frijol, bolsa de 1 kg", 300, 120),
                ArticuloMeta("c2-3", "Aceite, 1 litro", 300, 95)
            )),
        Campana("c3", "Ropa de invierno para el albergue", "a3", "Ropa",
            cierra = "15 de octubre", urgente = false,
            descripcion = "Cobijas, chamarras y calcetines para la temporada de frío.",
            unidadMeta = "prendas", metaTotal = 200, completados = 40,
            articulos = listOf(
                ArticuloMeta("c3-1", "Cobijas", 80, 20),
                ArticuloMeta("c3-2", "Chamarras", 70, 12),
                ArticuloMeta("c3-3", "Calcetines, par", 50, 8)
            ))
    )

    val proximasDeLaFamilia = listOf("act1")
    val favoritas = listOf("a1", "a2", "a3")

    /** Historial de participación de la familia (RF-11), de lo más reciente a lo más viejo. */
    val historial = listOf(
        Participacion(
            id = "p1",
            tituloActividad = "Tarde de lectura con abuelitos",
            nombreAsociacion = "Casa de Día Los Robles",
            fecha = "Domingo 6 de septiembre",
            mes = "SEPTIEMBRE",
            estado = EstadoParticipacion.TESTIMONIO_PUBLICADO
        ),
        Participacion(
            id = "p2",
            tituloActividad = "Clasificar ropa de invierno",
            nombreAsociacion = "Albergue Nuevo Amanecer",
            fecha = "Sábado 30 de agosto",
            mes = "AGOSTO",
            estado = EstadoParticipacion.ENCUESTA_PENDIENTE
        ),
        Participacion(
            id = "p3",
            tituloActividad = "Armar kits escolares",
            nombreAsociacion = "Parroquia San Bernabé",
            fecha = "Sábado 16 de agosto",
            mes = "AGOSTO",
            estado = EstadoParticipacion.TESTIMONIO_EN_REVISION
        ),
        Participacion(
            id = "p4",
            tituloActividad = "Pintar el comedor",
            nombreAsociacion = "Comedor Comunitario San Bernabé",
            fecha = "Domingo 3 de agosto",
            mes = "AGOSTO",
            estado = EstadoParticipacion.SIN_PENDIENTES
        )
    )
}
