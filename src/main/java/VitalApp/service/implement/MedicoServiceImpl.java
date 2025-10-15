package VitalApp.service.implement;

import VitalApp.dto.medico.*;
import VitalApp.exception.ResourceNotFoundException;
import VitalApp.model.documents.CitaMedica;
import VitalApp.model.documents.Medico;
import VitalApp.model.enums.EstadoCita;
import VitalApp.model.vo.HorarioMedico;
import VitalApp.repository.CitaMedicaRepository;
import VitalApp.repository.MedicoRepository;
import VitalApp.service.service.MedicoService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepo;
    private final CitaMedicaRepository citaRepo;

    @Override
    public String crearMedico(CrearMedicoDTO medicoDTO) {
        // Validar nombre no vacío (aunque ya viene validado por @NotBlank)
        validarNombre(medicoDTO.nombre());

        // Verificar si ya existe un médico con ese nombre
        if (existeNombre(medicoDTO.nombre())) {
            throw new IllegalStateException("Ya existe un médico registrado con el nombre: " + medicoDTO.nombre());
        }

        Medico nuevoMedico = Medico.builder()
                .nombre(medicoDTO.nombre().trim())
                .especialidad(medicoDTO.especialidad().trim())
                .horariosDisponibles(new ArrayList<>())
                .build();

        return medicoRepo.save(nuevoMedico).getId();
    }

    @Override
    public String editarMedico(EditarMedicoDTO medicoDTO) {
        Medico medico = obtenerMedicoPorId(medicoDTO.id());

        // Validar nombre
        validarNombre(medicoDTO.nombre());

        // Verificar si el nuevo nombre ya existe en otro médico
        if (existeNombre(medicoDTO.nombre())) {
            Medico medicoConMismoNombre = medicoRepo.findByNombre(medicoDTO.nombre()).orElse(null);
            if (medicoConMismoNombre != null && !medicoConMismoNombre.getId().equals(medicoDTO.id())) {
                throw new IllegalStateException("Ya existe otro médico con el nombre: " + medicoDTO.nombre());
            }
        }

        medico.setNombre(medicoDTO.nombre().trim());
        medico.setEspecialidad(medicoDTO.especialidad().trim());

        return medicoRepo.save(medico).getId();
    }

    @Override
    public String eliminarMedico(String id) {
        Medico medico = obtenerMedicoPorId(id);

        // Verificar que no tenga citas pendientes
        ObjectId medicoObjectId = new ObjectId(id);
        List<CitaMedica> citasPendientes = citaRepo.findByIdMedicoAndEstado(
                medicoObjectId,
                EstadoCita.PENDIENTE
        );

        if (!citasPendientes.isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar el médico porque tiene " + citasPendientes.size() +
                            " cita(s) pendiente(s). Debe cancelarlas primero."
            );
        }

        medicoRepo.delete(medico);
        return id;
    }

    @Override
    public InformacionMedicoDTO obtenerInformacionMedico(String id) {
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
                .map(m -> new ItemMedicoDTO(
                        m.getId(),
                        m.getNombre(),
                        m.getEspecialidad()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemMedicoDTO> buscarMedicosPorEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.trim().isEmpty()) {
            throw new IllegalArgumentException("La especialidad no puede estar vacía");
        }

        return medicoRepo.findByEspecialidadContainingIgnoreCase(especialidad.trim())
                .stream()
                .map(m -> new ItemMedicoDTO(
                        m.getId(),
                        m.getNombre(),
                        m.getEspecialidad()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public String agregarHorario(String idMedico, CrearHorarioDTO horarioDTO) {
        Medico medico = obtenerMedicoPorId(idMedico);

        // Validar que el horario sea futuro
        validarHorarioFuturo(horarioDTO);

        // Validar rango horario
        if (!horarioDTO.horaInicio().isBefore(horarioDTO.horaFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora fin");
        }

        // Validar horarios razonables (ej: no más de 12 horas continuas)
        long horas = Duration.between(horarioDTO.horaInicio(), horarioDTO.horaFin()).toHours();
        if (horas > 12) {
            throw new IllegalArgumentException("Un horario no puede durar más de 12 horas continuas");
        }

        HorarioMedico nuevoHorario = HorarioMedico.builder()
                .fecha(horarioDTO.fecha())
                .horaInicio(horarioDTO.horaInicio())
                .horaFin(horarioDTO.horaFin())
                .reservado(false)
                .build();

        // Validar solapamiento con horarios existentes
        validarSolapamientoHorarios(medico, nuevoHorario);

        medico.getHorariosDisponibles().add(nuevoHorario);
        return medicoRepo.save(medico).getId();
    }

    @Override
    public String eliminarHorario(String idMedico, LocalDate fecha, LocalTime horaInicio) {
        Medico medico = obtenerMedicoPorId(idMedico);

        HorarioMedico horarioAEliminar = medico.getHorariosDisponibles().stream()
                .filter(h -> h.getFecha().equals(fecha) && h.getHoraInicio().equals(horaInicio))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Horario no encontrado"));

        if (horarioAEliminar.isReservado()) {
            throw new IllegalStateException("No se puede eliminar un horario que ya está reservado");
        }

        medico.getHorariosDisponibles().remove(horarioAEliminar);
        return medicoRepo.save(medico).getId();
    }

    @Override
    public List<ItemHorarioDTO> listarHorarios(String idMedico) {
        return obtenerMedicoPorId(idMedico).getHorariosDisponibles()
                .stream()
                .map(this::convertToItemHorarioDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemHorarioDTO> listarHorariosDisponibles(String idMedico) {
        Medico medico = obtenerMedicoPorId(idMedico);
        LocalDateTime ahora = LocalDateTime.now();

        return medico.getHorariosDisponibles()
                .stream()
                .filter(h -> !h.isReservado())
                .filter(h -> {
                    LocalDateTime fechaHorario = LocalDateTime.of(h.getFecha(), h.getHoraInicio());
                    return fechaHorario.isAfter(ahora);
                })
                .sorted((h1, h2) -> {
                    int compareFecha = h1.getFecha().compareTo(h2.getFecha());
                    if (compareFecha != 0) return compareFecha;
                    return h1.getHoraInicio().compareTo(h2.getHoraInicio());
                })
                .map(this::convertToItemHorarioDTO)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Medico obtenerMedicoPorId(String id) {
        validarObjectId(id);
        return medicoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado con ID: " + id));
    }

    private boolean existeNombre(String nombre) {
        return medicoRepo.existsByNombre(nombre);
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (nombre.trim().length() < 3) {
            throw new IllegalArgumentException("El nombre debe tener al menos 3 caracteres");
        }
    }

    private void validarObjectId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(" no puede estar vacío");
        }
        if (!ObjectId.isValid(id)) {
            throw new IllegalArgumentException(" no es válido: " + id);
        }
    }

    private void validarHorarioFuturo(CrearHorarioDTO horario) {
        LocalDateTime fechaHoraInicio = LocalDateTime.of(horario.fecha(), horario.horaInicio());

        if (fechaHoraInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se pueden agregar horarios en fechas pasadas");
        }
    }

    private void validarSolapamientoHorarios(Medico medico, HorarioMedico nuevoHorario) {
        boolean horarioSolapado = medico.getHorariosDisponibles().stream()
                .anyMatch(h -> h.getFecha().equals(nuevoHorario.getFecha()) &&
                        horariosSeSolapan(h, nuevoHorario));

        if (horarioSolapado) {
            throw new IllegalStateException(
                    "El horario se solapa con un horario existente en la fecha " +
                            nuevoHorario.getFecha()
            );
        }
    }

    private boolean horariosSeSolapan(HorarioMedico h1, HorarioMedico h2) {
        // Dos horarios se solapan si:
        // - Inicio de h1 es antes del fin de h2 Y
        // - Fin de h1 es después del inicio de h2
        return !h1.getHoraFin().isBefore(h2.getHoraInicio())
                && !h2.getHoraFin().isBefore(h1.getHoraInicio());
    }

    private ItemHorarioDTO convertToItemHorarioDTO(HorarioMedico horario) {
        return new ItemHorarioDTO(
                horario.getFecha(),
                horario.getHoraInicio(),
                horario.getHoraFin(),
                horario.isReservado()
        );
    }
}