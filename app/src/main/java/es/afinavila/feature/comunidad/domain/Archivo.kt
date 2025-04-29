package es.afinavila.feature.comunidad.domain

data class Archivo(
    val id : Int,
    val nombre : String,
    val descripcion : String,
    val comuidadId : Int,
)
