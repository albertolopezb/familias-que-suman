package mx.tec.familiasquesuman.ui.screens.campanas

/**
 * Una asociación a la que se puede llevar lo que uno tiene para donar ("Tengo algo para donar").
 * Los datos salen de familiasquesuman.com/donar.
 *
 * Vive aquí, dentro de campanas, y no en domain/, para no tocar archivos compartidos mientras
 * el equipo hace el merge. Si más adelante vienen del panel de admin, se mueve a domain/.
 */
data class CentroRecepcion(
    val id: String,
    val nombre: String,
    val tipo: String,                    // "Albergue", "Casa Hogar"…
    val descripcion: String,
    val direccion: String,
    val verificado: Boolean,
    val tipos: List<String>,             // lo que reciben: "Ropa", "Juguetes"…
    val destinatarios: List<String>,
    val condiciones: List<String>,
    val metodosEntrega: List<String>,
    val condicionesRecepcion: String,
    val horario: String? = null,
    val telefono: String? = null,        // 10 dígitos
    val whatsapp: String? = null,        // 10 dígitos
    val instagram: String? = null,
    val imagen: String? = null           // nombre del archivo en drawable, sin extensión
)

/** Los tipos de donación, en el orden de la web. */
val TiposDeDonacion = listOf(
    "Juguetes", "Ropa", "Alimentos", "Higiene", "Sillas de ruedas", "Camas hospitalarias",
    "Mobiliario", "Artículos de cocina", "Electrónicos", "Útiles escolares", "Medicamentos",
    "Material didáctico"
)

private val Ambas = listOf("Nuevo", "Usado en buen estado")

val CentrosDeRecepcion = listOf(
    CentroRecepcion(
        id = "vifac",
        nombre = "VIFAC",
        tipo = "Albergue",
        descripcion = "Residencia donde atienden y capacitan a mujeres en estado vulnerable durante su embarazo. " +
            "Ofrecen casa y alimento durante su embarazo y unos meses posteriores a al nacimiento de su bebé.",
        direccion = "Gral Ignacio Zaragoza 625 Sur, La Pastora, 67140 Guadalupe, N.L., Guadalupe",
        verificado = false,
        tipos = listOf("Ropa", "Alimentos", "Higiene"),
        destinatarios = listOf("Adultos"),
        condiciones = Ambas,
        metodosEntrega = listOf("Entrega en el centro"),
        condicionesRecepcion = "Comunicarse para hacer la entrega de las donaciones.",
        telefono = "8181912040",
        imagen = "centro_vifac"
    ),
    CentroRecepcion(
        id = "amad",
        nombre = "Morada del Anciano Desvalido de Cadereyta",
        tipo = "Asilo de Ancianos",
        descripcion = "Asilo donde se atiende a 47 adultos indigentes o de escasos recursos. " +
            "Se les brinda casa, alimentación y atención.",
        direccion = "Blvd Jose Maria Gonzalez #1000, Cadereyta",
        verificado = true,
        tipos = listOf(
            "Alimentos", "Higiene", "Camas hospitalarias", "Sillas de ruedas", "Mobiliario",
            "Artículos de cocina", "Electrónicos", "Medicamentos"
        ),
        destinatarios = listOf("Adultos", "Adultos mayores"),
        condiciones = Ambas,
        metodosEntrega = listOf("Entrega en el centro"),
        condicionesRecepcion = "Hablar con anticipación para organizar la entrega.",
        telefono = "8282844311",
        whatsapp = "3318290852",
        imagen = "centro_amad"
    ),
    CentroRecepcion(
        id = "hogar-misericordia",
        nombre = "Hogar de la Misericordia",
        tipo = "Casa Hogar",
        descripcion = "Casa Hogar de adolescentes y adultos con paralisis cerebral y deterioro cognitivo. " +
            "Se les ofrece casa, alimentación y atención.",
        direccion = "Calle 20 de Noviembre #200, Col Idelfonso Vazquez, Santa Catarina",
        verificado = true,
        tipos = listOf("Ropa", "Alimentos", "Higiene", "Sillas de ruedas", "Artículos de cocina"),
        destinatarios = listOf("Adolescentes", "Adultos", "Adultos mayores"),
        condiciones = Ambas,
        metodosEntrega = listOf("Entrega en el centro"),
        condicionesRecepcion = "Contactar previo a la entrega. Se reciben visitas y donaciones con previo aviso " +
            "de Lunes a Sábado de 10:00 a 12:00 y de 4:00 a 5:30 pm.",
        horario = "Lunes a Sábado de 10:00 a 12:00 y de 4:00 a 5:30 pm.",
        telefono = "8183368767",
        imagen = "centro_hogar_misericordia"
    ),
    CentroRecepcion(
        id = "gran-familia",
        nombre = "La Gran Familia",
        tipo = "Casa Hogar",
        descripcion = "Casa Hogar de niños y adolescentes en estado vulnerable, se les ofrece casa, " +
            "alimento y educación.",
        direccion = "Carretera Nacional Km 255, Colonia Los Rodriguez, Santiago",
        verificado = true,
        tipos = listOf(
            "Ropa", "Alimentos", "Higiene", "Útiles escolares", "Electrónicos", "Mobiliario",
            "Juguetes", "Material didáctico"
        ),
        destinatarios = listOf("Niños", "Adolescentes"),
        condiciones = Ambas,
        metodosEntrega = listOf("Entrega en el centro", "Recogen donaciones", "Coordinar previamente"),
        condicionesRecepcion = "En caso de ser necesario, se puede coordinar una recolección en el domicilio. " +
            "Se reciben de Lunes a Domingo de 9:00 am a 6:00 pm en caseta.",
        horario = "9:00 a 6:00 pm Lunes a Domingo",
        whatsapp = "8126240294",
        instagram = "https://www.instagram.com/lagranfamiliaac",
        imagen = "centro_gran_familia"
    ),
    CentroRecepcion(
        id = "apadrina",
        nombre = "Apadrina un niño",
        tipo = "Albergue",
        descripcion = "Ofrece casa y alimento sin costo a niños en tratamiento de cáncer junto con un cuidador.",
        direccion = "Cuautla 208, Col. 5 de Mayo, 64186, Monterrey",
        verificado = true,
        tipos = listOf(
            "Juguetes", "Ropa", "Alimentos", "Higiene", "Sillas de ruedas", "Artículos de cocina",
            "Electrónicos", "Útiles escolares", "Medicamentos", "Material didáctico"
        ),
        destinatarios = listOf("Bebés", "Niños", "Adolescentes", "Adultos"),
        condiciones = listOf("Usado en buen estado", "Nuevo"),
        metodosEntrega = listOf("Entrega en el centro"),
        condicionesRecepcion = "Si tienes donaciones, contáctate directamente con el centro para coordinar " +
            "días y horarios de entrega.",
        telefono = "8131117884",
        whatsapp = "8131117884",
        instagram = "https://www.instagram.com/apadrinaunnin/",
        imagen = "centro_apadrina"
    )
)
