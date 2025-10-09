package VitalApp.dto.Autentication;

public record MensajeDTO<T>(
        boolean error,
        T respuesta
) {
}