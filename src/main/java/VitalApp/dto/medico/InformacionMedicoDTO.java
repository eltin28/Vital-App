package VitalApp.dto.medico;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record InformacionMedicoDTO(
        String id,
        String nombre,
        String especialidad
) {}