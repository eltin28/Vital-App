package VitalApp.model.documents;

import VitalApp.model.vo.HorarioMedico;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("medico")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Medico {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    private String nombre;
    private String especialidad;
    private List<HorarioMedico> horariosDisponibles;

}
