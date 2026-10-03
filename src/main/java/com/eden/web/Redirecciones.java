package com.eden.web;

/** Utilidades de redireccion segura. */
final class Redirecciones {

    private Redirecciones() {
    }

    /** Solo permite volver a rutas internas ("/algo"), nunca a otro sitio ("//sitio.com", "http://..."). */
    static String volverA(String destino, String porDefecto) {
        boolean seguro = destino != null && destino.startsWith("/") && !destino.startsWith("//") && !destino.contains("\\");
        return "redirect:" + (seguro ? destino : porDefecto);
    }
}
