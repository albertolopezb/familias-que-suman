# Autorización ABAC de Familias que Suman, escrita en Polar (el lenguaje de Oso), como el
# `policy.polar` de la guía de clase (s23 · "Declarative ABAC Rules in Polar").
#
# Cómo se lee cada política:
#   allow(sujeto, "acción", recurso) if not motivo(sujeto, "acción", recurso, _);
#       → la acción se permite si NINGUNA regla `motivo` la niega…
#   motivo(sujeto, "acción", recurso, "codigo") if <condición>;
#       → …y cada `motivo` es una razón concreta para negarla (la app recibe ese código).
# Lo que no tiene su `allow` aquí se niega: Oso niega por omisión (falla cerrado).
#
# Los atributos que se usan:
#   · Sujeto  (sale del JWT ya verificado): s.id, s.rol, s.puede("permiso").
#   · Recurso (sale de la base, ver politicas.py): dueño, fechas, cupo, edades, meta, …
#   · Entorno: r.ahora / r.hoy — la hora del SERVIDOR, nunca la del teléfono (que se puede cambiar).
# Las fechas llegan como segundos (o días) para compararlas como números.
#
# Lo que es de otra cuenta se niega con "no_encontrado" (404, no 403): así ni se confirma que
# existe (OWASP API1 · BOLA/IDOR).


# ─── Funciones de administración ───────────────────────────────────────────
# RBAC como un caso particular de ABAC: el permiso es un atributo más del sujeto (claim `permisos`).

allow(s: Sujeto, "usar", f: Funcion) if not motivo(s, "usar", f, _);

motivo(s: Sujeto, "usar", f: Funcion, "sin_permiso") if not s.puede(f.permiso);


# ─── Propiedad: cada quien lo suyo ─────────────────────────────────────────

allow(s: Sujeto, "ver", r: Recurso) if not motivo(s, "ver", r, _);

motivo(s: Sujeto, "ver", r: Recurso, "no_encontrado") if r.dueno != s.id;


# ─── Inscripciones ─────────────────────────────────────────────────────────

allow(s: Sujeto, "inscribirse", r: Inscripcion) if not motivo(s, "inscribirse", r, _);

motivo(_: Sujeto, "inscribirse", r: Inscripcion, "actividad_iniciada") if
    r.ahora >= r.inicia;

motivo(_: Sujeto, "inscribirse", r: Inscripcion, "edad_minima") if
    r.edad_minima != nil and
    i in r.integrantes and i.edad != nil and i.edad < r.edad_minima;

motivo(_: Sujeto, "inscribirse", r: Inscripcion, "falta_consentimiento") if
    r.consentimiento = false and
    i in r.integrantes and i.edad != nil and i.edad < 18;

# Los lugares que la familia YA tenía cuentan: modificar quién va no le quita su lugar.
motivo(_: Sujeto, "inscribirse", r: Inscripcion, "sin_lugares") if
    r.tiene_cupo = true and
    r.personas > r.lugares_libres + r.lugares_propios;


allow(s: Sujeto, "cancelar", r: Cancelacion) if not motivo(s, "cancelar", r, _);

motivo(s: Sujeto, "cancelar", r: Cancelacion, "no_encontrado") if r.dueno != s.id;

motivo(_: Sujeto, "cancelar", r: Cancelacion, "actividad_iniciada") if r.ahora >= r.inicia;


# ─── Participación: testimonios y encuestas ────────────────────────────────
# La asistencia la registra la asociación (permiso registrar:asistencias), nunca la familia.

allow(s: Sujeto, "publicar_testimonio", p: Participacion) if not motivo(s, "publicar_testimonio", p, _);

motivo(s: Sujeto, "publicar_testimonio", p: Participacion, "no_encontrado") if p.dueno != s.id;

motivo(_: Sujeto, "publicar_testimonio", p: Participacion, "actividad_no_realizada") if p.ahora < p.inicia;


allow(s: Sujeto, "eliminar", t: Testimonio) if not motivo(s, "eliminar", t, _);

motivo(s: Sujeto, "eliminar", t: Testimonio, "no_encontrado") if
    t.autor != s.id and not s.puede("moderar:contenido");


allow(s: Sujeto, "contestar_previa", e: EncuestaPrevia) if not motivo(s, "contestar_previa", e, _);

motivo(_: Sujeto, "contestar_previa", e: EncuestaPrevia, "sin_inscripcion") if e.inscrita = false;

motivo(_: Sujeto, "contestar_previa", e: EncuestaPrevia, "actividad_iniciada") if e.ahora >= e.inicia;


allow(s: Sujeto, "contestar_final", p: Participacion) if not motivo(s, "contestar_final", p, _);

motivo(s: Sujeto, "contestar_final", p: Participacion, "no_encontrado") if p.dueno != s.id;


allow(s: Sujeto, "ver_todas", "encuestas") if not motivo(s, "ver_todas", "encuestas", _);

motivo(s: Sujeto, "ver_todas", "encuestas", "sin_permiso") if not s.puede("moderar:contenido");


# ─── Campañas ──────────────────────────────────────────────────────────────
# Nadie puede "comprometer" 10,000 cuentos para inflar la barra.

allow(s: Sujeto, "aportar", a: Aportacion) if not motivo(s, "aportar", a, _);

motivo(_: Sujeto, "aportar", a: Aportacion, "campana_cerrada") if
    a.cierra_en != nil and a.hoy > a.cierra_en;

motivo(_: Sujeto, "aportar", a: Aportacion, "meta_completa") if a.faltan <= 0;

motivo(_: Sujeto, "aportar", a: Aportacion, "ya_no_alcanza") if
    a.faltan > 0 and a.cantidad > a.faltan;


# ─── Fotos ─────────────────────────────────────────────────────────────────

allow(s: Sujeto, "ver", f: FotoPerfil) if not motivo(s, "ver", f, _);

motivo(s: Sujeto, "ver", f: FotoPerfil, "no_encontrado") if f.dueno != s.id;
