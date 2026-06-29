package es.afinavila.feature.comunidad.domain

data class Archivo(
    val id: Int,
    val nombre: String,
    val nombreMostrar: String?,
    val descripcion: String,
    val comunidadId: Int,
    val categoria: String?,
    val fecha: String?,
)
