package VitalApp.dto.citaMedica;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public record CrearResultadoMedicoDTO(
        @NotBlank(message = "El ID de la cita médica es obligatorio")
        @Size(min = 24, max = 24, message = "El ID de cita debe tener 24 caracteres")
        ObjectId idCitaMedica,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
        String descripcion,

        @NotBlank(message = "El diagnóstico es obligatorio")
        @Size(max = 500, message = "El diagnóstico no puede exceder los 500 caracteres")
        String diagnostico,

        @NotBlank(message = "Las recomendaciones son obligatorias")
        @Size(max = 1000, message = "Las recomendaciones no pueden exceder los 1000 caracteres")
        String recomendaciones,

        @NotNull(message = "La fecha de registro es obligatoria")
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime fechaRegistro
){}