package VitalApp.dto.citaMedica;

import java.time.LocalDateTime;

public record InformacionResultadoMedicoDTO(
        String idCitaMedica,
        String descripcion,
        String diagnostico,
        String recomendaciones,
        LocalDateTime fechaRegistro
) {}