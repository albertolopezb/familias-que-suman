"""Mandar correos. Solo se usa para recuperar la contraseña (código y aviso de "tu contraseña cambió").

En desarrollo el servidor de correo es Mailpit (un contenedor que recibe los correos y los
muestra en http://localhost:8025, sin mandarlos a nadie). En producción se cambia SMTP_HOST por
un proveedor real; si lleva usuario y contraseña, va con STARTTLS (TLS sobre el puerto 587).

Se manda en segundo plano, DESPUÉS de responder: así la respuesta tarda lo mismo exista o no la
cuenta. Si el correo falla, se anota en la bitácora sin el destinatario ni el contenido.
"""

import logging
import smtplib
import ssl
from email.message import EmailMessage

from .config import ajustes

log = logging.getLogger("familias.correo")


def enviar(para: str, asunto: str, texto: str) -> None:
    mensaje = EmailMessage()   # rechaza saltos de línea en los encabezados: no hay inyección de encabezados
    mensaje["From"] = ajustes.correo_remitente
    mensaje["To"] = para
    mensaje["Subject"] = asunto
    mensaje.set_content(texto)
    try:
        with smtplib.SMTP(ajustes.smtp_host, ajustes.smtp_puerto, timeout=10) as servidor:
            if ajustes.smtp_tls:
                servidor.starttls(context=ssl.create_default_context())
            if ajustes.smtp_usuario:
                servidor.login(ajustes.smtp_usuario, ajustes.smtp_contrasena)
            servidor.send_message(mensaje)
    except (OSError, smtplib.SMTPException) as error:
        log.warning("no se pudo mandar un correo (%s)", type(error).__name__)
