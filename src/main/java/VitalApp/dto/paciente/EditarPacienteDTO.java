package VitalApp.dto.paciente;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record EditarPacienteDTO(
        @NotBlank(message = "El ID es obligatorio")
        @Length(max = 36, message = "El ID no puede exceder los 36 caracteres")
        String id,

        @NotBlank(message = "El nombre es obligatorio")
        @Length(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        String nombre
) {}