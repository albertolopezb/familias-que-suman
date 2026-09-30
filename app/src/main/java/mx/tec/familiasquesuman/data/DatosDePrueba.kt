package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Aportacion
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.domain.CentroVisiteo
import mx.tec.familiasquesuman.domain.EstadoParticipacion
import mx.tec.familiasquesuman.domain.Familia
import mx.tec.familiasquesuman.domain.Impacto
import mx.tec.familiasquesuman.domain.Participacion
import mx.tec.familiasquesuman.domain.Proyecto
import mx.tec.familiasquesuman.domain.TemaActividad

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

    /**
     * Quienes organizan las actividades del sitio. Van aparte de `asociaciones`
     * para no meterlos al directorio de la Parte 1 sin que lo decida el equipo.
     */
    val organizadores = listOf(
        Asociacion("o1", "Familias que Suman", "Voluntariado familiar",
            "Conectamos familias con oportunidades de voluntariado en su ciudad.",
            "Monterrey, N.L.",
            "8100000005", "5218100000005", "contacto@familiasquesuman.org"),
        Asociacion("o2", "Regalando Estrellas", "Salud",
            "Desde hace 11 años visitamos el Hospital Materno Infantil.",
            "Guadalupe, N.L.",
            "8100000006", "5218100000006", "contacto@regalandoestrellas.org"),
        Asociacion("o3", "Cíclica", "Medio ambiente",
            "Organizamos la Mega Limpieza con familias de la ciudad.",
            "Monterrey, N.L.",
            "8100000007", "5218100000007", "contacto@ciclica.org")
    )

    // Las actividades de familiasquesuman.com/actividades, con sus textos.
    // Primero las próximas y luego las que ya pasaron, como en el sitio.
    private const val REGALANDO_DESCRIPCION =
        "Entrega de aproximadamente 200 desayunos. Entrega de regalos, juguetes y kits de higiene personal."
    private const val REGALANDO_ACERCA =
        "Desde hace ya 11 años se hacen visitas al Hospital Materno Infantil, se regalan 200 " +
            "desayunos, se visita a los enfermos y se regalan kits de higiene personal a las " +
            "mujeres que se acaban de convertir en mamás."
    private const val REGALANDO_HAREMOS =
        "Entrega de aproximadamente 200 desayunos.\nEntrega de regalos, juguetes y kits de higiene personal."
    private const val REGALANDO_LLEVAR =
        "Es necesario comunicarse con la encargada de Regalando Estrellas para organizar tu aportación."
    private const val REGALANDO_RECOMENDACIONES_7 =
        "Pueden asistir familias con niños mayores de 7 años.\n" +
            "Llevar ropa cómoda, blusa blanca, sencilla, tenis.\n" +
            "Contáctate con Regalando Estrellas si tienes más dudas.\n" +
            "No olvides comentar que eres una Familia que Suma+"
    private const val REGALANDO_RECOMENDACIONES_TODOS =
        "Pueden asistir familias completas.\n" +
            "Llevar ropa cómoda, sencilla, tenis.\n" +
            "Contáctate con Regalando Estrellas si tienes más dudas.\n" +
            "No olvides comentar que eres una Familia que Suma+"
    private const val REGALANDO_APORTACION =
        "Se solicita: plátanos, manzanas, mandarinas, sándwiches, galletas o paletas heladas. " +
            "Es necesario ver en esta visita qué es lo que hace falta. Cada familia voluntaria " +
            "llega con una aportación."
    private const val REGALANDO_DIRECCION = "Av. San Rafael No. 450"
    private const val REGALANDO_ENCUENTRO = "Explanada del Hospital Materno Infantil"
    private const val REGALANDO_TITULO = "Regalando Estrellas Visita al Materno Infantil"

    val actividades = listOf(
        Actividad("act1", REGALANDO_TITULO, "o1",
            "sábado, 3 de octubre", "09:45 – 11:30", REGALANDO_DIRECCION,
            edadMinima = 7,
            descripcion = REGALANDO_DESCRIPCION,
            cupoTotal = 5, lugaresDisponibles = 0,
            municipio = "Guadalupe",
            tema = TemaActividad.SALUD,
            aportacion = Aportacion.EnEspecie(REGALANDO_APORTACION),
            puntoDeEncuentro = REGALANDO_ENCUENTRO,
            acercaDelProyecto = REGALANDO_ACERCA,
            queHaremos = REGALANDO_HAREMOS,
            queLlevar = REGALANDO_LLEVAR,
            recomendaciones = REGALANDO_RECOMENDACIONES_7,
            foto = "actividad_regalando_estrellas"),
        Actividad("act2", "Posada Sendero", "o1",
            "sábado, 28 de noviembre", "09:30 – 12:00",
            "San Ildefonso SN, Colonia Sendero, Santa Catarina",
            edadMinima = null,
            descripcion = "Posada navideña para las familias del Arroyo en la Colonia Sendero.",
            cupoTotal = 15, lugaresDisponibles = 8,
            municipio = "Monterrey",
            tema = TemaActividad.CELEBRACION,
            aportacion = Aportacion.EnEspecie(),
            puntoDeEncuentro = "Centro Comunitario",
            acercaDelProyecto = "Llevamos una mañana de regalos y juegos a las familias del Arroyo en la Colonia Sendero.",
            queHaremos = "Juegos, regalos, merienda y convivencia.",
            queLlevar = "Se junta una cantidad previa para hacer pagos de flautas, juguetes, pasteles, etc.",
            recomendaciones = "Ropa cómoda y sencilla.",
            // El sitio usa el logo de Regalando Estrellas también aquí.
            foto = "actividad_regalando_estrellas"),

        // Ya pasaron
        Actividad("act3", "Festejo a los papás de Apadrina un niño", "o1",
            "sábado, 13 de junio", "10:00 – 12:00", "",
            edadMinima = null,
            descripcion = "5 familias completas. Celebra a los papás de Apadrina un niño. Juegos y convivencia.",
            cupoTotal = 0, lugaresDisponibles = 0,
            municipio = "Monterrey",
            tema = TemaActividad.CELEBRACION,
            aportacion = Aportacion.Monetaria("\$500",
                "Contactarse para hacer el pago a través de Moneypool."),
            acercaDelProyecto = "Convivencia de 10 familias para festejar a los papás que luchan todos los días por la salud de sus hijos.",
            queHaremos = "Jugar lotería y convivir.",
            queIncluye = "Con la aportación compraremos algo para compartir de alimentos y bebidas.",
            yaPaso = true),
        Actividad("act4", REGALANDO_TITULO, "o2",
            "sábado, 4 de julio", "09:45 – 11:30", REGALANDO_DIRECCION,
            edadMinima = null,
            descripcion = REGALANDO_DESCRIPCION,
            cupoTotal = 0, lugaresDisponibles = 0,
            municipio = "Guadalupe",
            tema = TemaActividad.SALUD,
            puntoDeEncuentro = REGALANDO_ENCUENTRO,
            acercaDelProyecto = REGALANDO_ACERCA,
            queHaremos = REGALANDO_HAREMOS,
            queLlevar = "Si tienes oportunidad, aporta algo de frutas como manzanas, plátanos o naranjas.",
            recomendaciones = REGALANDO_RECOMENDACIONES_TODOS,
            foto = "actividad_regalando_estrellas",
            yaPaso = true),
        Actividad("act5", "Mega Limpieza", "o3",
            "sábado, 4 de julio", "08:00 – 10:30", "",
            edadMinima = null,
            descripcion = "Actividad familiar para ayudar al medio ambiente.",
            cupoTotal = 0, lugaresDisponibles = 0,
            municipio = "Monterrey",
            tema = TemaActividad.MEDIO_AMBIENTE,
            acercaDelProyecto = "Actividad familiar para ayudar a hacer limpieza de áreas.",
            queHaremos = "Limpieza de áreas.",
            queIncluye = "Guantes, chalecos, herramientas e hidratación.",
            recomendaciones = "Llevar termo con agua, jeans, gorra, protector solar y actitud.\n" +
                "Visitar la página www.megalimpieza.org",
            foto = "actividad_mega_limpieza",
            yaPaso = true),
        Actividad("act6", REGALANDO_TITULO, "o2",
            "sábado, 1 de agosto", "09:45 – 11:30", REGALANDO_DIRECCION,
            edadMinima = null,
            descripcion = REGALANDO_DESCRIPCION,
            cupoTotal = 20, lugaresDisponibles = 17,
            municipio = "Guadalupe",
            tema = TemaActividad.SALUD,
            puntoDeEncuentro = REGALANDO_ENCUENTRO,
            acercaDelProyecto = REGALANDO_ACERCA,
            queHaremos = REGALANDO_HAREMOS,
            queLlevar = REGALANDO_LLEVAR,
            recomendaciones = REGALANDO_RECOMENDACIONES_TODOS,
            foto = "actividad_regalando_estrellas",
            yaPaso = true),
        Actividad("act7", REGALANDO_TITULO, "o1",
            "sábado, 5 de septiembre", "09:45 – 11:30", REGALANDO_DIRECCION,
            edadMinima = 7,
            descripcion = REGALANDO_DESCRIPCION,
            cupoTotal = 9, lugaresDisponibles = 0,
            municipio = "Guadalupe",
            tema = TemaActividad.SALUD,
            aportacion = Aportacion.EnEspecie(REGALANDO_APORTACION),
            puntoDeEncuentro = REGALANDO_ENCUENTRO,
            acercaDelProyecto = REGALANDO_ACERCA,
            queHaremos = REGALANDO_HAREMOS,
            queLlevar = REGALANDO_LLEVAR,
            recomendaciones = REGALANDO_RECOMENDACIONES_7,
            foto = "actividad_regalando_estrellas",
            yaPaso = true),
        Actividad("act8", "Mega Limpieza San Pedro", "o3",
            "sábado, 26 de septiembre", "08:00 – 10:30",
            "Humberto Lobo esq. con Av. Ignacio Morones Prieto",
            edadMinima = null,
            descripcion = "Actividad familiar para ayudar haciendo limpieza al medio ambiente.",
            cupoTotal = 0, lugaresDisponibles = 0,
            municipio = "Monterrey",
            tema = TemaActividad.MEDIO_AMBIENTE,
            puntoDeEncuentro = "Auditorio San Pedro",
            acercaDelProyecto = "Actividad familiar para ayudar a hacer limpieza de áreas.",
            queHaremos = "Limpieza de áreas.",
            queIncluye = "Guantes, chalecos, herramientas e hidratación.",
            queLlevar = "Bloqueador, zapato cerrado, gorra, termo de agua.",
            recomendaciones = "Llevar termo con agua, jeans, gorra, protector solar y actitud.\n" +
                "Visitar la página www.megalimpieza.org",
            foto = "actividad_mega_limpieza",
            yaPaso = true)
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

    /** Los proyectos activos de familiasquesuman.com/proyectos. */
    val proyectos = listOf(
        Proyecto("pr1", "Trazo... Escribiendo una nueva historia",
            "Somos un grupo de mujeres voluntarias que realizamos visitas quincenales al Centro de " +
                "Reinserción Social de Escobedo para acompañar a mujeres privadas de la libertad " +
                "mediante pláticas de desarrollo humano, talleres de acuarela y actividades que " +
                "fortalecen su autoestima, creatividad y crecimiento personal.",
            beneficiarios = "25 Mujeres", ciudad = "Monterrey", vigencia = null,
            logo = "proyecto_trazo"),
        Proyecto("pr2", "Voluntariado DIF Te Acompaña",
            "Acompañar a adultos mayores o jóvenes vulnerables, en soledad, con discapacidad o con " +
                "una red de apoyo reducida. Realizar visitas en familia mínimo una vez cada 15 días.",
            beneficiarios = "200 adultos mayores", ciudad = "San Pedro Garza García", vigencia = null,
            logo = "proyecto_dif"),
        Proyecto("pr3", "Cocinando de Corazón a Corazón",
            "Comedor comunitario diocesano que lleva alimento y esperanza a personas y familias en " +
                "situación vulnerable. A través de una red de parroquias, los alimentos preparados " +
                "se distribuyen en distintas comunidades.",
            beneficiarios = null, ciudad = "Monterrey", vigencia = null,
            logo = "proyecto_cocinando"),
        Proyecto("pr4", "Forma-T",
            "Clases impartidas por voluntarias en escuelas públicas enfocadas en la formación en " +
                "valores, escuela para padres y apoyo académico para niños y jóvenes.",
            beneficiarios = null, ciudad = "Monterrey", vigencia = null,
            logo = "proyecto_forma_t"),
        Proyecto("pr5", "Materno Infantil - REGALANDO ESTRELLAS",
            "Visita al Hospital Materno Infantil el primer sábado al mes junto con más familias. Se " +
                "sirven aproximadamente 200 desayunos y se llevan juguetes o kits de higiene personal.",
            beneficiarios = "1500 Bebés, mamás, maternidad", ciudad = "Monterrey", vigencia = null,
            logo = "actividad_regalando_estrellas"),
        Proyecto("pr6", "Bibliotecas Infantiles - Familia Viva",
            "Recolección de cuentos infantiles de preescolar, primaria y secundaria para crear una " +
                "biblioteca infantil que se donará a instituciones con niños.",
            beneficiarios = null, ciudad = "Monterrey", vigencia = null,
            logo = "proyecto_bibliotecas"),
        Proyecto("pr7", "Sendero - REGALANDO ESTRELLAS",
            "Pláticas y convivencia con mujeres en situación vulnerable donde les pueden enseñar, " +
                "convivir y compartir aprendizajes.",
            beneficiarios = null, ciudad = "Monterrey", vigencia = null,
            logo = "actividad_regalando_estrellas"),
        Proyecto("pr8", "Escucha Corazón",
            "Una visita al mes al colegio Mano Amiga Monterrey en donde darás acompañamiento a " +
                "alumnas de primaria y secundaria.",
            beneficiarios = null, ciudad = "Monterrey", vigencia = "Hasta 29 jun 2026",
            logo = "proyecto_escucha_corazon")
    )

    /** Los centros verificados de familiasquesuman.com/directorio. */
    val centros = listOf(
        CentroVisiteo("ce1", "Morada del Anciano Desvalido Cadereyta", "Asilos",
            "Asilo de ancianos donde se atienden 24 horas a 46 adultos mayores.",
            "Atención a adultos mayores en abandono, soledad y falta de apoyo familiar. Se les " +
                "ofrece refugio, alimentación, atención integral y compañía.",
            listOf(
                "Alimentos como: azúcar, leche, aceite, té, gelatina, mole en lata, saladitas, " +
                    "servilletas, ensure, jugos y frutas",
                "Limpieza: trapeadores, cubetas, botes de basura, guantes, cloro, fabuloso, pino, " +
                    "jabón líquido, shampoo, desengrasantes, bolsas de basura"
            ),
            "Blvd José María González #1000, Cadereyta", logo = "centro_amad"),
        CentroVisiteo("ce2", "Casa de la Misericordia", "Casas hogar",
            "Casa Hogar que ofrece albergue, alimento y atención a jóvenes y adultos con " +
                "enfermedades irreversibles.",
            "Hogar de la Misericordia ofrece vida digna a personas con enfermedades irreversibles " +
                "y/o terminales no contagiosas, en completo desamparo y sin recursos económicos.",
            listOf(
                "Pañales de adulto tamaño mediano y grande",
                "Alimentos como aceite, atún en lata, azúcar, sal, galletas maría, arroz, pasta, " +
                    "frijoles, leche deslactosada y de almendra",
                "Productos de limpieza como fabuloso, bolsas de basura jumbo, jabón líquido para " +
                    "manos, jabón para trastes, pinol y papel sanitario"
            ),
            "Monterrey", logo = "centro_misericordia"),
        CentroVisiteo("ce3", "La Gran Familia", "Casas hogar",
            "Casa Hogar de niños, niñas y adolescentes que fueron retirados de sus familias por " +
                "falta de cuidados parentales.",
            "La Gran Familia es una casa hogar que atiende a niños, niñas y adolescentes con " +
                "situación familiar vulnerable desde hace 44 años. Ofrecen vivienda, alimentación, " +
                "educación, salud mental y general.",
            listOf(
                "Visitas acompañadas de actividades de integración y socialización",
                "Alimentos, ropa, productos de limpieza e higiene personal, útiles escolares",
                "Tenis blancos y zapato escolar"
            ),
            "Villa de Santiago", logo = "centro_gran_familia"),
        CentroVisiteo("ce4", "Comedor Apadrina un Niño", "Comedores",
            "Comedor para apoyar a las familias de pacientes internados en la Clínica 25.",
            "Comedor que sirve aproximadamente 200 comidas de lunes a viernes de 1:00 a 3:00 pm.",
            listOf("Desechables", "Alimentos no perecederos"),
            "Cuautla #208, Colonia 5 de Mayo, Monterrey", logo = "centro_apadrina"),
        CentroVisiteo("ce5", "Apadrina un niño", "Casas hogar",
            "Albergue para niños con cáncer y comedor para sus familias.",
            "Apadrina un niño es un albergue donde pueden hospedarse niños que vengan a Monterrey a " +
                "algún tratamiento médico. La casa recibe a 40 niños más un adulto que los acompañe.",
            listOf(
                "Alimentos no perecederos",
                "Productos de higiene personal",
                "Ropa de niño y adulto",
                "Productos de limpieza"
            ),
            "Cuautla 208, Col. 5 de Mayo, Monterrey", logo = "centro_apadrina")
    )

    val proximasDeLaFamilia = listOf("act2")
    val favoritas = listOf("a1", "a2", "a3")

    /**
     * Historial de participación de la familia (RF-11), de lo más reciente a lo
     * más viejo. Son las actividades pasadas del sitio.
     */
    val historial = listOf(
        Participacion(
            id = "p1",
            tituloActividad = "Mega Limpieza San Pedro",
            nombreAsociacion = "Cíclica",
            fecha = "sábado, 26 de septiembre",
            mes = "SEPTIEMBRE",
            estado = EstadoParticipacion.ENCUESTA_PENDIENTE
        ),
        Participacion(
            id = "p2",
            tituloActividad = REGALANDO_TITULO,
            nombreAsociacion = "Familias que Suman",
            fecha = "sábado, 5 de septiembre",
            mes = "SEPTIEMBRE",
            estado = EstadoParticipacion.TESTIMONIO_EN_REVISION
        ),
        Participacion(
            id = "p3",
            tituloActividad = REGALANDO_TITULO,
            nombreAsociacion = "Regalando Estrellas",
            fecha = "sábado, 1 de agosto",
            mes = "AGOSTO",
            estado = EstadoParticipacion.TESTIMONIO_PUBLICADO
        ),
        Participacion(
            id = "p4",
            tituloActividad = "Mega Limpieza",
            nombreAsociacion = "Cíclica",
            fecha = "sábado, 4 de julio",
            mes = "JULIO",
            estado = EstadoParticipacion.SIN_PENDIENTES
        )
    )
}
