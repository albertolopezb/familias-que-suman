package mx.tec.familiasquesuman.ui.screens.perfil

/** Las preguntas de antes de la actividad (RF-13): de dónde parte la familia. */
val EncuestaPrevia = DefinicionEncuesta(3, listOf(
    PreguntaEncuesta(
        numero = 1,
        texto = "¿Tus hijos han participado antes en una actividad así?",
        opciones = listOf("Es la primera vez", "Una o dos veces", "Van seguido")
    ),
    PreguntaEncuesta(
        numero = 2,
        texto = "¿Qué esperan de esta actividad?",
        opciones = listOf(
            "Convivir en familia",
            "Ayudar a quienes lo necesitan",
            "Que mis hijos aprendan valores",
            "Conocer a la asociación"
        )
    ),
    PreguntaEncuesta(
        numero = 3,
        texto = "¿Ya hablaron con tus hijos de lo que van a hacer?",
        opciones = listOf("Sí, ya les expliqué", "Un poco", "Todavía no")
    )
))

/** Las de al terminar (RF-13): cómo les fue y qué se llevan. Comparadas con las de antes miden el aprendizaje. */
val EncuestaFinal = DefinicionEncuesta(4, listOf(
    PreguntaEncuesta(
        numero = 1,
        texto = "¿Cómo se sintieron en la actividad?",
        opciones = listOf(
            "Muy bien, volveríamos",
            "Bien",
            "Regular",
            "No fue lo que esperábamos"
        )
    ),
    PreguntaEncuesta(
        numero = 2,
        texto = "¿Qué aprendieron tus hijos en esta actividad?",
        opciones = listOf(
            "Entendieron mejor cómo viven otras familias",
            "Aprendieron a trabajar en equipo",
            "Se divirtieron, pero no hablamos del tema",
            "Todavía no lo comentamos"
        )
    ),
    PreguntaEncuesta(
        numero = 3,
        texto = "¿Qué tan clara fue la organización del día?",
        opciones = listOf("Muy clara", "Clara", "Poco clara", "Confusa")
    ),
    PreguntaEncuesta(
        numero = 4,
        texto = "¿Volverían a participar en otra actividad?",
        opciones = listOf("Sí, pronto", "Sí, más adelante", "No estoy seguro")
    )
))
