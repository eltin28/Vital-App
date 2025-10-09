package VitalApp.service.implement;

import VitalApp.dto.medico.*;
import VitalApp.model.documents.Medico;
import VitalApp.model.vo.HorarioMedico;
import VitalApp.repository.MedicoRepository;
import VitalApp.service.service.MedicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepo;

    @Override
    public String crearMedico(CrearMedicoDTO medicoDTO) throws Exception {
        if (existeNombre(medicoDTO.nombre())) {
            throw new Exception("El médico con nombre " + medicoDTO.nombre() + " ya está registrado.");
        }

        if (medicoDTO.nombre() == null || medicoDTO.nombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        Medico nuevoMedico = Medico.builder()
                .nombre(medicoDTO.nombre())
                .especialidad(medicoDTO.especialidad())
                .horariosDisponibles(new ArrayList<>())
                .build();

        return medicoRepo.save(nuevoMedico).getId();
    }

    @Override
    public String editarMedico(EditarMedicoDTO medicoDTO) throws Exception {
        Medico medico = obtenerMedicoPorId(medicoDTO.id());

        medico.setNombre(medicoDTO.nombre());
        if (medicoDTO.nombre() == null || medicoDTO.nombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        medico.setEspecialidad(medicoDTO.especialidad());

        return medicoRepo.save(medico).getId();
    }

    @Override
    public String eliminarMedico(String id) throws Exception {
        Medico medico = obtenerMedicoPorId(id);
        medicoRepo.delete(medico);
        return id;
    }

    @Override
    public InformacionMedicoDTO obtenerInformacionMedico(String id) throws Exception {
        Medico medico = obtenerMedicoPorId(id);
        return new InformacionMedicoDTO(
                medico.getId(),
                medico.getNombre(),
                medico.getEspecialidad()
        );
    }

    @Override
    public List<ItemMedicoDTO> listarMedicos() {
        return medicoRepo.findAll()
                .stream()
                .map(m -> new ItemMedicoDTO(m.getId(), m.getNombre(), m.getEspecialidad()))
                .collect(Collectors.toList());
    }

    @Override
    public String agregarHorario(String idMedico, CrearHorarioDTO horarioDTO) throws Exception {
        Medico medico = obtenerMedicoPorId(idMedico);

        // Validar rango horario
        if (!horarioDTO.horaInicio().isBefore(horarioDTO.horaFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora fin");
        }

        HorarioMedico nuevoHorario = HorarioMedico.builder()
                .fecha(horarioDTO.fecha())
                .horaInicio(horarioDTO.horaInicio())
                .horaFin(horarioDTO.horaFin())
                .reservado(false)
                .build();

        // Validar solapamiento
        boolean horarioSolapado = medico.getHorariosDisponibles().stream()
                .anyMatch(h -> h.getFecha().equals(nuevoHorario.getFecha()) &&
                        !(h.getHoraFin().isBefore(nuevoHorario.getHoraInicio()) ||
                                h.getHoraInicio().isAfter(nuevoHorario.getHoraFin())));

        if (horarioSolapado) {
            throw new IllegalStateException("El nuevo horario se solapa con uno existente");
        }

        medico.getHorariosDisponibles().add(nuevoHorario);
        return medicoRepo.save(medico).getId();
    }

    @Override
    public List<ItemHorarioDTO> listarHorarios(String idMedico) throws Exception {
        return obtenerMedicoPorId(idMedico).getHorariosDisponibles()
                .stream()
                .map(this::convertToItemHorarioDTO)
                .collect(Collectors.toList());
    }

    private ItemHorarioDTO convertToItemHorarioDTO(HorarioMedico horario) {
        return new ItemHorarioDTO(
                horario.getFecha(),
                horario.getHoraInicio(),
                horario.getHoraFin(),
                horario.isReservado()
        );
    }

    private boolean existeNombre(String nombre) {
        return medicoRepo.existsByNombre(nombre);
    }

    private Medico obtenerMedicoPorId(String id) throws Exception {
        return medicoRepo.findById(id)
                .orElseThrow(() -> new Exception("Médico no encontrado con ID: " + id));
    }
}