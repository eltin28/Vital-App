package VitalApp.service.implement;

import VitalApp.dto.citaMedica.*;
import VitalApp.dto.medico.ItemHorarioDTO;
import VitalApp.exception.ResourceNotFoundException;
import VitalApp.model.documents.CitaMedica;
import VitalApp.model.documents.Medico;
import VitalApp.model.documents.Paciente;
import VitalApp.model.enums.EstadoCita;
import VitalApp.model.vo.HorarioMedico;
import VitalApp.model.vo.ResultadoMedico;
import VitalApp.repository.CitaMedicaRepository;
import VitalApp.repository.MedicoRepository;
import VitalApp.repository.PacienteRepository;
import VitalApp.service.service.CitaMedicaService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaMedicaServiceImpl implements CitaMedicaService {

    private final CitaMedicaRepository citaRepo;
    private final MedicoRepository medicoRepo;
    private final PacienteRepository pacienteRepo;

    @Override
    public String agendarCita(CrearCitaMedicaDTO dto) {
        // Validar IDs
        validarObjectId(dto.idPaciente(), "ID de paciente");
        validarObjectId(dto.idMedico(), "ID de médico");

        Medico medico = obtenerMedicoPorId(dto.idMedico());
        Paciente paciente = obtenerPacientePorId(dto.idPaciente());
        ItemHorarioDTO horarioDTO = dto.horario();

        // Validar que el horario sea futuro
        validarHorarioFuturo(horarioDTO);

        // Validar que el paciente no tenga otra cita en ese horario
        validarDisponibilidadPaciente(dto.idPaciente(), horarioDTO);

        // Buscar y reservar horario
        HorarioMedico horarioSeleccionado = buscarYReservarHorario(medico, horarioDTO);

        // Crear la cita
        CitaMedica cita = CitaMedica.builder()
                .idCliente(new ObjectId(paciente.getId()))
                .idMedico(new ObjectId(medico.getId()))
                .horario(horarioSeleccionado)
                .estado(EstadoCita.PENDIENTE)
                .build();

        return citaRepo.save(cita).getId();
    }

    @Override
    public String cancelarCita(String idCita) {
        CitaMedica cita = obtenerCitaPorId(idCita);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException("La cita ya fue cancelada");
        }

        if (cita.getEstado() == EstadoCita.VISTA) {
            throw new IllegalStateException("No se puede cancelar una cita que ya fue atendida");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        liberarHorarioCita(cita);

        return citaRepo.save(cita).getId();
    }

    @Override
    public List<ItemCitaMedicaDTO> listarCitasPorPaciente(String idPaciente) {
        validarObjectId(idPaciente, "ID de paciente");
        ObjectId pacienteId = new ObjectId(idPaciente);

        return citaRepo.findByIdCliente(pacienteId).stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemCitaMedicaDTO> listarCitasPorMedico(String idMedico) {
        validarObjectId(idMedico, "ID de médico");
        ObjectId medicoId = new ObjectId(idMedico);

        return citaRepo.findByIdMedico(medicoId).stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String agregarResultadoMedico(String idCita, CrearResultadoMedicoDTO dto) {
        CitaMedica cita = obtenerCitaPorId(idCita);

        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden agregar resultados a citas en estado PENDIENTE");
        }

        // Validar que la cita ya haya pasado
        LocalDateTime fechaCita = LocalDateTime.of(
                cita.getHorario().getFecha(),
                cita.getHorario().getHoraInicio()
        );

        if (fechaCita.isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("No se puede agregar resultado a una cita que aún no ha ocurrido");
        }

        ResultadoMedico resultado = ResultadoMedico.builder()
                .idCitaMedica(cita.getId())
                .descripcion(dto.descripcion())
                .diagnostico(dto.diagnostico())
                .recomendaciones(dto.recomendaciones())
                .fechaRegistro(dto.fechaRegistro() != null ? dto.fechaRegistro() : LocalDateTime.now())
                .build();

        cita.setResultado(resultado);
        cita.setEstado(EstadoCita.VISTA);

        return citaRepo.save(cita).getId();
    }

    @Override
    public InformacionResultadoMedicoDTO obtenerResultadoMedico(String idCita) {
        CitaMedica cita = obtenerCitaPorId(idCita);

        ResultadoMedico resultado = cita.getResultado();
        if (resultado == null) {
            throw new IllegalStateException("La cita no tiene resultado médico registrado");
        }

        return new InformacionResultadoMedicoDTO(
                cita.getId(),
                resultado.getDescripcion(),
                resultado.getDiagnostico(),
                resultado.getRecomendaciones(),
                resultado.getFechaRegistro()
        );
    }

    @Override
    public ItemCitaMedicaDTO obtenerCitaPorIdDTO(String id) {
        CitaMedica cita = obtenerCitaPorId(id);
        return convertToItemDTO(cita);
    }

    @Override
    public List<ItemCitaMedicaDTO> listarTodasLasCitas() {
        return citaRepo.findAll().stream()
                .map(this::convertToItemDTO)
                .sorted((c1, c2) -> {
                    // Ordenar por fecha y hora descendente (más recientes primero)
                    int compareFecha = c2.horario().getFecha().compareTo(c1.horario().getFecha());
                    if (compareFecha != 0) return compareFecha;
                    return c2.horario().getHoraInicio().compareTo(c1.horario().getHoraInicio());
                })
                .collect(Collectors.toList());
    }

    @Override
    public String actualizarResultadoMedico(String idCita, CrearResultadoMedicoDTO dto) {
        CitaMedica cita = obtenerCitaPorId(idCita);

        if (cita.getEstado() != EstadoCita.VISTA) {
            throw new IllegalStateException("Solo se pueden actualizar resultados de citas en estado VISTA");
        }

        if (cita.getResultado() == null) {
            throw new IllegalStateException("La cita no tiene un resultado médico para actualizar");
        }

        ResultadoMedico resultadoActualizado = ResultadoMedico.builder()
                .idCitaMedica(cita.getId())
                .descripcion(dto.descripcion())
                .diagnostico(dto.diagnostico())
                .recomendaciones(dto.recomendaciones())
                .fechaRegistro(cita.getResultado().getFechaRegistro()) // Mantener fecha original
                .build();

        cita.setResultado(resultadoActualizado);
        return citaRepo.save(cita).getId();
    }

    @Override
    public Map<String, Long> obtenerEstadisticas() {
        List<CitaMedica> todasLasCitas = citaRepo.findAll();

        Map<String, Long> estadisticas = new HashMap<>();

        // Contar por estado
        estadisticas.put("total", (long) todasLasCitas.size());
        estadisticas.put("pendientes", todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.PENDIENTE)
                .count());
        estadisticas.put("vistas", todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.VISTA)
                .count());
        estadisticas.put("canceladas", todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.CANCELADA)
                .count());

        // Citas de hoy
        LocalDate hoy = LocalDate.now();
        estadisticas.put("citasHoy", todasLasCitas.stream()
                .filter(c -> c.getHorario().getFecha().equals(hoy))
                .count());

        // Citas futuras pendientes
        LocalDateTime ahora = LocalDateTime.now();
        estadisticas.put("citasFuturasPendientes", todasLasCitas.stream()
                .filter(c -> c.getEstado() == EstadoCita.PENDIENTE)
                .filter(c -> {
                    LocalDateTime fechaCita = LocalDateTime.of(
                            c.getHorario().getFecha(),
                            c.getHorario().getHoraInicio()
                    );
                    return fechaCita.isAfter(ahora);
                })
                .count());

        return estadisticas;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private CitaMedica obtenerCitaPorId(String id) {
        validarObjectId(id, "ID de cita");
        return citaRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita médica no encontrada con ID: " + id));
    }

    private Medico obtenerMedicoPorId(String id) {
        return medicoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médico no encontrado con ID: " + id));
    }

    private Paciente obtenerPacientePorId(String id) {
        return pacienteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));
    }

    private HorarioMedico buscarYReservarHorario(Medico medico, ItemHorarioDTO horarioDTO) {
        HorarioMedico horarioSeleccionado = medico.getHorariosDisponibles().stream()
                .filter(h -> horarioCoincide(h, horarioDTO))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Horario no disponible para el médico seleccionado"));

        if (horarioSeleccionado.isReservado()) {
            throw new IllegalStateException("El horario ya está reservado");
        }

        horarioSeleccionado.setReservado(true);
        medicoRepo.save(medico);

        return horarioSeleccionado;
    }

    private void liberarHorarioCita(CitaMedica cita) {
        Medico medico = obtenerMedicoPorId(cita.getIdMedico().toHexString());

        medico.getHorariosDisponibles().stream()
                .filter(h -> horarioCoincide(h, cita.getHorario()))
                .findFirst()
                .ifPresent(h -> {
                    h.setReservado(false);
                    medicoRepo.save(medico);
                });
    }

    private boolean horarioCoincide(HorarioMedico h1, ItemHorarioDTO h2) {
        return h1.getFecha().equals(h2.fecha())
                && h1.getHoraInicio().equals(h2.horaInicio())
                && h1.getHoraFin().equals(h2.horaFin());
    }

    private boolean horarioCoincide(HorarioMedico h1, HorarioMedico h2) {
        return h1.getFecha().equals(h2.getFecha())
                && h1.getHoraInicio().equals(h2.getHoraInicio())
                && h1.getHoraFin().equals(h2.getHoraFin());
    }

    private void validarObjectId(String id, String nombreCampo) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(nombreCampo + " no puede estar vacío");
        }
        if (!ObjectId.isValid(id)) {
            throw new IllegalArgumentException(nombreCampo + " no es un ObjectId válido: " + id);
        }
    }

    private void validarHorarioFuturo(ItemHorarioDTO horario) {
        LocalDateTime fechaHoraInicio = LocalDateTime.of(horario.fecha(), horario.horaInicio());

        if (fechaHoraInicio.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se pueden agendar citas en horarios pasados");
        }
    }

    private void validarDisponibilidadPaciente(String idPaciente, ItemHorarioDTO horario) {
        ObjectId pacienteId = new ObjectId(idPaciente);
        List<CitaMedica> citasPaciente = citaRepo.findByIdCliente(pacienteId);

        boolean tieneConflicto = citasPaciente.stream()
                .filter(c -> c.getEstado() == EstadoCita.PENDIENTE || c.getEstado() == EstadoCita.VISTA)
                .anyMatch(c -> horariosSeSolapan(c.getHorario(), horario));

        if (tieneConflicto) {
            throw new IllegalStateException("El paciente ya tiene una cita agendada en ese horario");
        }
    }

    private boolean horariosSeSolapan(HorarioMedico h1, ItemHorarioDTO h2) {
        if (!h1.getFecha().equals(h2.fecha())) {
            return false;
        }

        // Verificar si los horarios se solapan
        return !h1.getHoraFin().isBefore(h2.horaInicio())
                && !h2.horaFin().isBefore(h1.getHoraInicio());
    }

    private ItemCitaMedicaDTO convertToItemDTO(CitaMedica cita) {
        return new ItemCitaMedicaDTO(
                cita.getId(),
                cita.getIdCliente(),
                cita.getIdMedico(),
                cita.getHorario(),
                cita.getEstado(),
                cita.getResultado()
        );
    }
}