package VitalApp.model.documents;

import VitalApp.model.enums.EstadoCita;
import VitalApp.model.vo.HorarioMedico;
import VitalApp.model.vo.ResultadoMedico;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("citaMedica")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CitaMedica {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    private ObjectId idCliente;
    private ObjectId idMedico;
    private HorarioMedico horario;
    private EstadoCita estado;
    private ResultadoMedico resultado;
}
