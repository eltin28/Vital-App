package VitalApp.model.vo;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HorarioMedico {

    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean reservado;
}
