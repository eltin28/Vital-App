package VitalApp.model.vo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ResultadoMedico {

    private String idCitaMedica;
    private String descripcion;
    private String diagnostico;
    private String recomendaciones;
    private LocalDateTime fechaRegistro;
}
