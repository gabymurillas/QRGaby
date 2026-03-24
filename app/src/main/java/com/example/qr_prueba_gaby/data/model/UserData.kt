package com.example.qr_prueba_gaby.data.model

/**
 * Datos del usuario para el sistema de acceso residencial.
 * Los nombres de campo son cortos para generar un QR más compacto y fácil de escanear.
 *
 * @param u  Nombre completo del usuario
 * @param c  Cédula de identidad
 * @param p  Lista de placas de vehículos registradas
 * @param aid ANDROID_ID encriptado en Base64 (IV + ciphertext)
 */
data class UserData(
    val u: String,   // nombre
    val c: String,   // cedula
    val t: String,   // telefono
    val p: List<String>, // placas
    val aid: String  // android id encriptado
) {
    /**
     * Serializa a JSON compacto.
     * Ejemplo: {"u":"Juan P","c":"12345678","p":["ABC-123","XYZ-456"],"aid":"..."}
     */
    fun toJson(): String {
        val placasJson = p.joinToString(",") { "\"$it\"" }
        return "{\"u\":\"$u\",\"c\":\"$c\",\"t\":\"$t\",\"p\":[$placasJson],\"aid\":\"$aid\"}"
    }

    companion object {
        /**
         * Parsea el JSON compacto de vuelta a UserData.
         * Implementación simple sin dependencia de librerías externas.
         */
        fun fromJson(json: String): UserData? {
            return try {
                val u = extractValue(json, "u")
                val c = extractValue(json, "c")
                val t = extractValue(json, "t")
                val aid = extractValue(json, "aid")
                val p = extractList(json, "p")
                UserData(u, c, t, p, aid)
            } catch (e: Exception) { null }
        }

        private fun extractValue(json: String, key: String): String {
            val pattern = "\"$key\":\"([^\"]+)\"".toRegex()
            return pattern.find(json)?.groupValues?.get(1) ?: ""
        }

        private fun extractList(json: String, key: String): List<String> {
            val pattern = "\"$key\":\\[([^]]*)]".toRegex()
            val content = pattern.find(json)?.groupValues?.get(1) ?: return emptyList()
            if (content.isBlank()) return emptyList()
            return content.split(",").map { it.trim().removeSurrounding("\"") }
        }
    }
}
