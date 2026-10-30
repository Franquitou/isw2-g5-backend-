package isw2_G5.liga_amateur.security.auth;

import isw2_G5.liga_amateur.users.entity.Rol;

/** Respuesta RF-01; nunca incluye contraseña ni el hash almacenado. */
public record LoginResponse(String token, String nombreCompleto, Rol rol) {
}
