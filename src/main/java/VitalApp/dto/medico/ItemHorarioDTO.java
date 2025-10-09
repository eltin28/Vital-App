package VitalApp.dto.medico;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ItemHorarioDTO(
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        boolean reservado
) {}