package mx.tec.familiasquesuman.data

import mx.tec.familiasquesuman.domain.Actividad
import mx.tec.familiasquesuman.domain.Aportacion
import mx.tec.familiasquesuman.domain.ArticuloMeta
import mx.tec.familiasquesuman.domain.Asociacion
import mx.tec.familiasquesuman.domain.Campana
import mx.tec.familiasquesuman.domain.CentroVisiteo
import mx.tec.familiasquesuman.domain.OpcionDonacion
import mx.tec.familiasquesuman.domain.PuntoEntrega
import mx.tec.familiasquesuman.domain.EstadoParticipacion
import mx.tec.familiasquesuman.domain.Familia
import mx.tec.familiasquesuman.domain.FormaDeApoyo
import mx.tec.familiasquesuman.domain.IconoApoyo
import mx.tec.familiasquesuman.domain.Impacto
import mx.tec.familiasquesuman.domain.Participacion
import mx.tec.familiasquesuman.domain.Proyecto
import mx.tec.familiasquesuman.domain.TemaActividad
import mx.tec.familiasquesuman.domain.Usuario

// Datos falsos, idénticos a los del Figma. `internal`: solo los repositorios de
// data/ los tocan. Ninguna pantalla ni ViewModel los importa directo; así, cuando
// llegue el backend, se cambia el repositorio y nadie más se entera.
// Teléfonos y correos ficticios.
internal object DatosDePrueba {

    val familia = Familia("f1", "Familia Rodríguez", "Monterrey", "ana.rodriguez@correo.com")

    val usuarioAdmin = Usuario(
        correo = "admin@correo.com",
        esAdmin = true,
        nombreFamilia = "Administrador General",
        ciudad = "Monterrey"
    )

    val impacto = Impacto(actividadesRealizadas = 12, horasDeServicio = 36, campanasApoyadas = 4)

    val asociaciones = mutableListOf(
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
            foto = "actividad_posada_sendero"),

        // Los tres casos de prueba de la parte 3 (vienen de RamaAndres): inscripción
        // exitosa, se acaban los lugares al confirmar y edad mínima de 15 años.
        Actividad("act9", "Preparar despensas de fin de mes", "a1",
            "sábado, 10 de octubre", "9:00 – 12:00", "Av. Rómulo Garza 240, Col. San Bernabé",
            edadMinima = 6,
            descripcion = "Vamos a armar 300 despensas para las familias de la colonia. Se forman equipos de cuatro. Lleven ropa cómoda y agua.",
            cupoTotal = 20, lugaresDisponibles = 8,
            municipio = "Monterrey",
            puntoDeEncuentro = "Comedor Comunitario San Bernabé",
            queHaremos = "Armar despensas en equipos de cuatro.",
            recomendaciones = "Ropa cómoda y agua."),
        Actividad("act10", "Tarde de lectura con abuelitos", "a2",
            "domingo, 11 de octubre", "16:00 – 18:00", "Calle Los Robles 118",
            edadMinima = 4,
            descripcion = "Cada familia lee un cuento con un abuelito y después compartimos un café.",
            cupoTotal = 12, lugaresDisponibles = 2,
            municipio = "Monterrey",
            puntoDeEncuentro = "Casa de Día Los Robles",
            queHaremos = "Leer un cuento con un abuelito y compartir un café."),
        Actividad("act11", "Clasificar ropa de invierno", "a3",
            "sábado, 17 de octubre", "10:00 – 13:00", "Av. Colón 905",
            edadMinima = 15,
            descripcion = "Separamos la ropa donada por talla y tipo antes de repartirla.",
            cupoTotal = 25, lugaresDisponibles = 15,
            municipio = "Monterrey",
            puntoDeEncuentro = "Albergue Nuevo Amanecer",
            queHaremos = "Separar la ropa donada por talla y tipo."),

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

    /**
     * Las campañas de familiasquesuman.com/donar, con sus textos tal cual los publica el sitio.
     * Solo Bibliotecas Infantiles tiene meta de artículos, así que es la única que se puede
     * apartar (RF-21). Sus 100 apartados son de ejemplo para la demostración: ponlos en 0 si
     * ya no se necesitan.
     */
    val campanas = mutableListOf(
        Campana("c1", "Destellos de Luz", "", "Salud",
            cierra = "", urgente = false,
            descripcion = "Destellos de Luz A.B.P. cuenta con más de 29 años de experiencia brindando " +
                "atención oftalmológica integral y programas educativos que impulsan la autonomía y " +
                "calidad de vida de personas con discapacidad visual.",
            unidadMeta = "", metaTotal = 0, completados = 0, articulos = emptyList(),
            imagen = "campana_destellos",
            telefono = "8180924504", whatsapp = "8180924504", contactoNombre = "Mariana Báez",
            descripcionLarga = "Destellos de Luz A.B.P. es una asociación con más de 29 años de " +
                "trayectoria, que cuenta con un área médica y un área educativa.\n\n" +
                "En el área Médica: brindamos atención oftalmológica integral, respaldada por un " +
                "cuerpo médico altruista especializado, desde la valoración médica hasta cirugías y " +
                "tratamientos especializados, para quienes más lo necesitan.\n\n" +
                "En el área Educativa: a través de 7 programas de educación fortalecemos las " +
                "habilidades de personas con discapacidad visual para favorecer su superación y " +
                "mejorar su calidad de vida.",
            comoAyudar = "Dona herramientas para personas con discapacidad visual las cuales les " +
                "facilitan su desplazamiento y aprendizaje.",
            opcionesDonacion = listOf(
                OpcionDonacion("Bastón para desplazamiento", "\$700"),
                OpcionDonacion("Computadora parlante", "\$21,000"),
                OpcionDonacion("Cirugía", "\$14,000")
            ),
            instagram = "https://www.instagram.com/destellosdeluzabp/"),
        Campana("c2", "Tapitas que Suman", "", "",
            cierra = "30 de diciembre", urgente = false,
            descripcion = "Recolección de tapitas de plástico para reciclaje",
            unidadMeta = "", metaTotal = 0, completados = 0, articulos = emptyList(),
            imagen = "campana_tapitas", textoBoton = "Quiero juntar",
            telefono = "8110809078", whatsapp = "8110809078", contactoNombre = "Familias que Suman",
            descripcionLarga = "Junta en casa todas las tapitas de plástico para reciclaje y ayuda a " +
                "la alimentación y el medio ambiente.",
            comoAyudar = "Ayudanos a difundir entre familiares y amigos y invitalos a juntar todas las " +
                "tapitas de botellas de plástico. Se pueden reciclar, se reutiliza ese plásticoy con " +
                "ese dinero recaudado podemos ayudar a niños con cáncer y a sus familias."),
        Campana("c3", "Suma a su Mesa", "", "Alimentos",
            cierra = "30 de octubre", urgente = false,
            descripcion = "Recoleccion de alimentos y productos de limpieza para el Asilo de Ancianos " +
                "Morada del Anciano Desvalido de Cadereyta. Se sirven 6,900 servicios de " +
                "alimentación al mes.",
            unidadMeta = "", metaTotal = 0, completados = 0, articulos = emptyList(),
            imagen = "campana_suma_mesa",
            telefono = "8114707350", whatsapp = "8114707350", contactoNombre = "Adriana Guerra",
            descripcionLarga = "Recolección de alimentos no perecederos y productos de limpieza para " +
                "el Asilo con Adultos Mayores en Cadereyta. Alimentos: aceite, arroz, frijoles, " +
                "lentejas, azúcar, avena, atún, productos enlatados, leche, té, café, galletas, " +
                "gelatina, pasta, saladitas, servilletas, papel de baño. Productos de limpieza: " +
                "escobas, trapeador, bolsas de basura, cloro, fabuloso, pinol, esponjas, jabón " +
                "liquido, desengrasante.",
            comoAyudar = "Recolecta entre familia y amigos alimentos y productos de limpieza. Existen " +
                "3 puntos de entrega. Reúne entre tu comunidad y hagan una gran donación.",
            puntosEntrega = listOf(
                PuntoEntrega("Calle Cóndor 1001", "Fraccionamiento Azhara"),
                PuntoEntrega("Río Amazonas 327", "Del Valle, 66220 San Pedro Garza García"),
                PuntoEntrega("Fuego 45", "Colonia Olinca")
            )),
        Campana("c4", "Bibliotecas Infantiles", "", "Útiles escolares",
            cierra = "", urgente = false,
            descripcion = "Recolección de cuentos infantiles para crear Bibliotecas",
            unidadMeta = "cuentos", metaTotal = 300, completados = 100,
            articulos = listOf(ArticuloMeta("c4-1", "Cuentos infantiles", 300, 100)),
            imagen = "campana_bibliotecas",
            telefono = "8120322281", contactoNombre = "Familias que Suman",
            metaTexto = "250-300 cuentos",
            descripcionLarga = "Reúne cuentos infantiles para crear Bibliotecas para niños. No " +
                "diccionarios, no libros de texto y no libros de colorear. Se necesitan entre 250 y " +
                "300 cuentos para juntar la Biblioteca.",
            comoAyudar = "Junta cuentos desde preescolar hasta secundaria en buen estado.")
    )

    /** Los proyectos activos de familiasquesuman.com/proyectos, con todo su detalle. */
    val proyectos = mutableListOf(
        Proyecto("pr1", "Trazo... Escribiendo una nueva historia",
            "Somos un grupo de mujeres voluntarias que realizamos visitas quincenales al Centro de " +
                "Reinserción Social de Escobedo para acompañar a mujeres privadas de la libertad " +
                "mediante pláticas de desarrollo humano, talleres de acuarela y actividades que " +
                "fortalecen su autoestima, creatividad y crecimiento personal.\n\n" +
                "En cada encuentro buscamos ofrecer un espacio de escucha, aprendizaje y esperanza, " +
                "recordándoles que siempre es posible comenzar de nuevo.",
            beneficiarios = "25 Mujeres", ciudad = "Monterrey", vigencia = null,
            logo = "proyecto_trazo",
            resumen = "Voluntariado que visita a mujeres en el penal de Escobedo.",
            acercaDe = "Trazo es un voluntariado que nace de la Asociación Renace, desde hace más de 2 " +
                "años lleva pláticas de desarrollo humano y cursos de acuarela y caligrafía a mujeres " +
                "privadas de la libertad quienes al final del semestre reciben un reconocimiento el " +
                "cual les ayuda en su expediente judicial.",
            formasDeApoyo = listOf(
                FormaDeApoyo("Buscamos personas que quieran compartir su tiempo y talento.",
                    "Voluntarias que imparten pláticas de desarrollo humano. • Talleres de acuarela y " +
                        "actividades creativas. • Otros talleres que promuevan el aprendizaje y el " +
                        "bienestar emocional.",
                    IconoApoyo.LIBRO),
                FormaDeApoyo("Aportación económica",
                    "Para comprar materiales: • Pinturas, pinceles, papel y otros insumos. " +
                        "• Aportaciones económicas para los alimentos que se entregan a las mujeres " +
                        "privadas de la libertad durante cada visita.",
                    IconoApoyo.DINERO)
            ),
            telefono = "8110771068", whatsapp = "8110771068"),
        Proyecto("pr2", "Voluntariado DIF Te Acompaña",
            "Acompañar a adultos mayores o jóvenes vulnerables, en soledad, con discapacidad o con " +
                "una red de apoyo reducida. Realizar visitas en familia mínimo una vez cada 15 días. " +
                "Se les puede asistir en compras de alimentos, medicamentos o solo una visita.",
            beneficiarios = "200 adultos mayores", ciudad = "San Pedro Garza García", vigencia = null,
            logo = "proyecto_dif",
            resumen = "Acompañar a personas vulnerables.",
            acercaDe = "El voluntariado existe desde hace varios años y se busca apoyar a personas " +
                "vulnerables con una visita o ayuda con alimentos, medicinas o acompañamiento.",
            comoAyudar = "Inscribir a su familia para que el Voluntariado les asigne una persona para " +
                "visitar o acompañar.",
            telefono = "8113008725", whatsapp = "8113008725"),
        Proyecto("pr3", "Cocinando de Corazón a Corazón",
            "Comedor comunitario diocesano que lleva alimento y esperanza a personas y familias en " +
                "situación vulnerable. A través de una red de parroquias, los alimentos preparados " +
                "se distribuyen en distintas comunidades, permitiendo llegar de manera cercana y " +
                "organizada a quienes más lo necesitan.\n\n" +
                "Esta obra se sostiene gracias a la generosidad de benefactores, voluntarios y, de " +
                "manera muy especial, de una gran red de amas de casa que regalan su tiempo y talento " +
                "preparando los alimentos con cariño desde sus hogares.",
            beneficiarios = null, ciudad = "Monterrey", vigencia = null,
            logo = "proyecto_cocinando",
            resumen = "Comedor comunitario de alimentos preparados por una red de amas de casa que " +
                "regalan su tiempo preparando alimentos desde sus hogares.",
            acercaDe = "Desde hace 6 años un conjunto de amas de casa comenzaron a apoyar cocinando " +
                "desde su casa para después repartir los alimentos en comunidades vulnerables. El " +
                "proyecto creció y actualmente se entregan hasta 1000 platillos diarios.",
            formasDeApoyo = listOf(
                FormaDeApoyo("Cocinando desde casa",
                    "Elige un día fijo a la semana y cocina un mínimo de 20 platillos."),
                FormaDeApoyo("Apoya al equipo del comedor",
                    "Elige un día fijo a la semana y apoya al comedor sirviendo y emplatando comidas."),
                FormaDeApoyo("Aporta alimentos",
                    "Aporta cualquier tipo de alimento para que otras familias puedan cocinar desde sus casas.")
            ),
            telefono = "8184596229", whatsapp = "8184596229",
            instagram = "https://www.instagram.com/cocinando_de_corazon/"),
        Proyecto("pr4", "Forma-T",
            "Clases impartidas por voluntarias en escuelas públicas enfocadas en la formación en " +
                "valores, escuela para padres y apoyo académico para niños y jóvenes.",
            beneficiarios = null, ciudad = "Monterrey", vigencia = null,
            logo = "proyecto_forma_t",
            resumen = "Asociación que busca provocar en la comunidad una formación de valores y " +
                "talentos para ponerlos al servicio de los demás.",
            acercaDe = "Desde el 2016 se ofrece apoyo académico impartido por voluntarias a alumnos de " +
                "escuelas públicas con rezago escolar. Escuela para padres y formación en valores " +
                "para familias y alumnos.",
            formasDeApoyo = listOf(
                FormaDeApoyo("Sesiones de 45 min en formación en valores.",
                    "Ofrecer clases de formación en valores entre dos voluntarias. Sesiones de 45 " +
                        "minutos una vez por semana.",
                    IconoApoyo.LIBRO),
                FormaDeApoyo("Sesiones de 50 min en apoyo académico.",
                    "Ofrecer apoyo a alumnos con rezago escolar una vez por semana. Se les asigna un " +
                        "alumno fijo a quien apoyarían durante el semestre en lecto-escritura o " +
                        "matemáticas según sea necesario.",
                    IconoApoyo.LIBRO)
            ),
            telefono = "8180201207", whatsapp = "8180201207",
            instagram = "https://www.instagram.com/format_mty"),
        Proyecto("pr5", "Materno Infantil - REGALANDO ESTRELLAS",
            "Visita al Hospital Materno Infantil el primer sábado al mes junto con más familias. Se " +
                "sirven aproximadamente 200 desayunos y se llevan juguetes o kits de higiene personal.",
            beneficiarios = "1500 Bebés, mamás, maternidad", ciudad = "Monterrey", vigencia = null,
            logo = "actividad_regalando_estrellas",
            resumen = "Visita una vez al mes el Hospital Materno Infantil.",
            acercaDe = "Desde hace ya 11 años se hacen visitas al Hospital Materno Infantil, se regalan " +
                "200 desayunos, se visita a los enfermos y se regalan kits de higiene personal a las " +
                "mujeres que se acaban de convertir en mamás.",
            formasDeApoyo = listOf(
                FormaDeApoyo("Armar kits de higiene personal para las mamás.",
                    "Armar kits de higiene personal para las mamás con toallas húmedas para bebés, " +
                        "chanclas, pañales, shampoo, cepillo de dientes, papel sanitario y agua."),
                FormaDeApoyo("Entrega de desayunos a familiares de pacientes.",
                    "Repartir aproximadamente 200 desayunos para los familiares de pacientes.")
            ),
            telefono = "8134021445", whatsapp = "8134021445",
            instagram = "https://www.instagram.com/regalandoestrellas.mx/"),
        Proyecto("pr6", "Bibliotecas Infantiles - Familia Viva",
            "Recolección de cuentos infantiles de preescolar, primaria y secundaria para crear una " +
                "biblioteca infantil que se entregará a instituciones con niños. Se pueden entregar en " +
                "albergues, casas hogar, centros comunitarios y escuelas.",
            beneficiarios = null, ciudad = "Monterrey", vigencia = null,
            logo = "proyecto_bibliotecas",
            resumen = "Bibliotecas para entregar en colegios o instituciones infantiles.",
            acercaDe = "Familia Viva detectó la necesidad de contar con más libros y espacios de lectura " +
                "en escuelas públicas. A partir de ahí nació una campaña para recolectar y entregar " +
                "libros y cuentos infantiles. En 2025, gracias al esfuerzo conjunto de Alas MX, " +
                "Familia Viva y muchas familias voluntarias, se han logrado entregar hasta hoy 38 " +
                "salitas de lectura en escuelas públicas. Es un proyecto sustentable porque tanto los " +
                "libros como los muebles son reutilizados.",
            comoAyudar = "Recolectar cuentos o libros desde preescolar, primaria y secundaria. Conseguir " +
                "un librero o comprar uno en alguna tienda tipo Home Depot. Una vez reunidos los 250 o " +
                "300 libros se clasifican y se arma la biblioteca. No diccionarios, no libros de texto " +
                "y no libros de colorear. Se pueden organizar con la encargada del proyecto para " +
                "clasificar libros e irla a entregar personalmente.",
            telefono = "8180296174", whatsapp = "8180296174",
            instagram = "https://www.instagram.com/familiaviva.mx/"),
        Proyecto("pr7", "Sendero - REGALANDO ESTRELLAS",
            "Pláticas y convivencia con mujeres en situación vulnerable donde les pueden enseñar, " +
                "convivir y compartir aprendizajes.",
            beneficiarios = null, ciudad = "Monterrey", vigencia = null,
            logo = "actividad_regalando_estrellas",
            resumen = "Pláticas y convivencia con mujeres de comunidad vulnerable.",
            acercaDe = "Solo mujeres. Pláticas, manualidades y enseñanza a mujeres vulnerables. Último " +
                "sábado del mes. 10:00 am a 12:00 pm.",
            comoAyudar = "Hacer una visita en grupo a una comunidad donde hay muchas mujeres con quienes " +
                "platicar, convivir y compartir aprendizajes.",
            telefono = "8134021445", whatsapp = "8134021445"),
        Proyecto("pr8", "Escucha Corazón",
            "Una visita al mes al colegio Mano Amiga Monterrey en donde darás acompañamiento a " +
                "alumnas de primaria y secundaria.",
            beneficiarios = null, ciudad = "Monterrey", vigencia = "Hasta 29 jun 2026",
            logo = "proyecto_escucha_corazon",
            resumen = "Acompañamiento a alumnas de primaria y secundaria.",
            acercaDe = "Mínimo 4 visitas en el ciclo escolar. Tiempo aprox. 25 minutos con cada alumna. " +
                "Pueden ser desde 2 a 4 alumnas en el ciclo escolar. Horario y días flexibles.",
            comoAyudar = "Dar acompañamiento ayuda a las alumnas a ser escuchadas.",
            telefono = "8110809089", whatsapp = "8110809078")
    )

    /** Los centros verificados de familiasquesuman.com/directorio, con todo su detalle. */
    val centros = mutableListOf(
        CentroVisiteo("ce1", "Morada del Anciano Desvalido Cadereyta", "Asilos",
            "Asilo de ancianos donde se atienden 24 horas a 46 adultos mayores.",
            "Atención a adultos mayores en abandono, soledad y falta de apoyo familiar. Se les " +
                "proporciona una vida digna. Se les ofrece refugio, alimentación, atención integral " +
                "y compañía.",
            listOf(
                "Alimentos como: azúcar, leche, aceite, té, gelatina, mole en lata, saladitas, " +
                    "servilletas, ensure, jugos y frutas",
                "Limpieza: trapeadores, cubetas, botes de basura, guantes, cloro, fabuloso, pino, " +
                    "jabón líquido, shampoo, desengrasantes, bolsas de basura"
            ),
            "Blvd José María González #1000, Cadereyta", logo = "centro_amad",
            comoAyudar = "Aportación en especie. Visita a los ancianos para convivir y platicar.",
            recomendaciones = "Hablar con anticipación para ver qué actividades se recomiendan y en qué horario.",
            telefono = "8282844311", whatsapp = "3318290852"),
        CentroVisiteo("ce2", "Casa de la Misericordia", "Casas hogar",
            "Casa Hogar que ofrece albergue, alimento y atención a jóvenes y adultos con " +
                "enfermedades irreversibles.",
            "Hogar de la Misericordia ofrece vida digna a personas con enfermedades irreversibles " +
                "y/o terminales no contagiosas, en completo desamparo y sin recursos económicos. Les " +
                "brindan albergue, vestido, alimentación, medicamentos, terapias físicas, asistencia " +
                "médica, dental y de enfermería.",
            listOf(
                "Pañales de adulto tamaño mediano y grande",
                "Alimentos como aceite, atún en lata, azúcar, sal, galletas maría, arroz, pasta, " +
                    "frijoles, leche deslactosada y de almendra",
                "Productos de limpieza como fabuloso, bolsas de basura jumbo, jabón líquido para " +
                    "manos, jabón para trastes, pinol y papel sanitario"
            ),
            "Monterrey", logo = "centro_misericordia", verificado = false,
            comoAyudar = "Visitas a los pacientes.\nAportación de alimentos, de productos de limpieza y " +
                "de higiene personal.\nAportación de ropa de jóvenes y adultos.",
            recomendaciones = "Se aceptan visitas de niños y adultos con previo aviso y agenda.\n" +
                "Se recomienda llevar alguna actividad planeada para convivir.\n" +
                "No más de 15 personas en cada visita.\n" +
                "Los pacientes disfrutan de actividades con colores, burbujas y música.",
            telefono = "8183368767"),
        CentroVisiteo("ce3", "La Gran Familia", "Casas hogar",
            "Casa Hogar de niños, niñas y adolescentes que fueron retirados de sus familias por " +
                "falta de cuidados parentales ubicados en el Municipio de Santiago, NL.",
            "La Gran Familia es una casa hogar que atiende a niños, niñas y adolescentes con " +
                "situación familiar vulnerable desde hace 44 años. Ofrecen vivienda, alimentación, " +
                "educación, salud mental y general.",
            listOf(
                "Visitas acompañadas de actividades de integración y socialización",
                "Alimentos, ropa, productos de limpieza e higiene personal, útiles escolares",
                "Tenis blancos y zapato escolar"
            ),
            "Villa de Santiago", logo = "centro_gran_familia",
            comoAyudar = "Visita a los niños, niñas y adolescentes con actividades que generen " +
                "convivencia y socialización. Se pueden organizar dinámicas, juegos, manualidades, " +
                "lectura de cuentos o talleres que ayuden a crear momentos de alegría, aprendizaje y " +
                "compañía.",
            recomendaciones = "Solo adultos para visitas internas.\n" +
                "Se requiere organización previa con la institución para confirmar calendario.\n" +
                "Se recomienda llevar una actividad para los niños y refrigerio para compartir.",
            whatsapp = "8126240294",
            instagram = "https://www.instagram.com/lagranfamiliaac"),
        CentroVisiteo("ce4", "Comedor Apadrina un Niño", "Comedores",
            "Comedor para apoyar a las familias de pacientes internados en la Clínica 25.",
            "Comedor que sirve aproximadamente 200 comidas de lunes a viernes de 1:00 a 3:00 pm.",
            listOf("Desechables", "Alimentos no perecederos"),
            "Cuautla #208, Colonia 5 de Mayo, Monterrey", logo = "centro_apadrina",
            comoAyudar = "Voluntarios bienvenidos a apoyar a servir comida todos los días de lunes a " +
                "viernes. Puede ser por día.",
            recomendaciones = "Adultos y niños bienvenidos.\nHablar antes para confirmar asistencia.",
            telefono = "8131117884", whatsapp = "8131117884",
            instagram = "https://www.instagram.com/apadrinaunnin/"),
        CentroVisiteo("ce5", "Apadrina un niño", "Casas hogar",
            "Albergue para niños con cáncer y comedor para sus familias.",
            "Apadrina un niño es un albergue donde pueden hospedarse niños que vengan a Monterrey a " +
                "algún tratamiento médico. La casa recibe a 40 niños más un adulto que los acompañe. " +
                "El albergue sirve alrededor de 200 comidas diarias de lunes a viernes a todos los " +
                "familiares que tengan algún paciente internado y hospeda los días que sean necesarios " +
                "sin costo alguno.",
            listOf(
                "Alimentos no perecederos",
                "Productos de higiene personal",
                "Ropa de niño y adulto",
                "Productos de limpieza",
                "Ropa de cama tamaño individual",
                "Pañales, toallas húmedas para bebé",
                "Juguetes",
                "Mobiliario en general",
                "Voluntarios para el comedor"
            ),
            "Cuautla 208, Col. 5 de Mayo, 64186 Monterrey, N.L.", logo = "centro_apadrina",
            comoAyudar = "Se buscan voluntarios de lunes a viernes para servir comida de 1:00 a 3:00. " +
                "Puede asistir por día.",
            recomendaciones = "No es necesario llevar aportaciones para visitar.\n" +
                "Se recomienda hablar antes de visitar para que sepan de su visita.",
            telefono = "8131117884", whatsapp = "8131117884",
            instagram = "https://www.instagram.com/apadrinaunnin/")
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
            estado = EstadoParticipacion.SIN_PENDIENTES
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
