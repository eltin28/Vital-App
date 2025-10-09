package VitalApp.dto.medico;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CrearHorarioDTO(
        @NotNull
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate fecha,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaInicio,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaFin
) {}