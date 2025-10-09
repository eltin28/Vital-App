package VitalApp.dto.paciente;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CrearPacienteDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Length(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        String nombre
) {}