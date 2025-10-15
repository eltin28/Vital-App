package VitalApp.dto.citaMedica;

import VitalApp.model.enums.EstadoCita;
import VitalApp.model.vo.HorarioMedico;
import VitalApp.model.vo.ResultadoMedico;
import org.bson.types.ObjectId;

public record ItemCitaMedicaDTO(
        String id,
        ObjectId idPaciente,
        ObjectId idMedico,
        HorarioMedico horario,
        EstadoCita estado,
        ResultadoMedico resultado
) {}