package VitalApp.service.implement;

import VitalApp.dto.paciente.CrearPacienteDTO;
import VitalApp.dto.paciente.EditarPacienteDTO;
import VitalApp.dto.paciente.ItemPacienteDTO;
import VitalApp.exception.ResourceNotFoundException;
import VitalApp.model.documents.CitaMedica;
import VitalApp.model.documents.Paciente;
import VitalApp.model.enums.EstadoCita;
import VitalApp.repository.CitaMedicaRepository;
import VitalApp.repository.PacienteRepository;
import VitalApp.service.service.PacienteService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepo;
    private final CitaMedicaRepository citaRepo;

    @Override
    public String crearPaciente(CrearPacienteDTO pacienteDTO) {
        // Validar nombre
        validarNombre(pacienteDTO.nombre());

        // Verificar si ya existe un paciente con ese nombre
        if (existeNombre(pacienteDTO.nombre())) {
            throw new IllegalStateException("Ya existe un paciente registrado con el nombre: " + pacienteDTO.nombre());
        }

        Paciente paciente = Paciente.builder()
                .nombre(pacienteDTO.nombre().trim())
                .build();

        return pacienteRepo.save(paciente).getId();
    }

    @Override
    public String editarPaciente(EditarPacienteDTO pacienteDTO) {
        Paciente paciente = obtenerPacientePorId(pacienteDTO.id());

        // Validar nombre
        validarNombre(pacienteDTO.nombre());

        // Verificar si el nuevo nombre ya existe en otro paciente
        if (existeNombre(pacienteDTO.nombre())) {
            Paciente pacienteConMismoNombre = pacienteRepo.findByNombre(pacienteDTO.nombre()).orElse(null);
            if (pacienteConMismoNombre != null && !pacienteConMismoNombre.getId().equals(pacienteDTO.id())) {
                throw new IllegalStateException("Ya existe otro paciente con el nombre: " + pacienteDTO.nombre());
            }
        }

        paciente.setNombre(pacienteDTO.nombre().trim());

        return pacienteRepo.save(paciente).getId();
    }

    @Override
    public String eliminarPaciente(String id) {
        Paciente paciente = obtenerPacientePorId(id);

        // Verificar que no tenga citas pendientes
        ObjectId pacienteObjectId = new ObjectId(id);
        List<CitaMedica> citasPendientes = citaRepo.findByIdClienteAndEstado(
                pacienteObjectId,
                EstadoCita.PENDIENTE
        );

        if (!citasPendientes.isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar el paciente porque tiene " + citasPendientes.size() +
                            " cita(s) pendiente(s). Debe cancelarlas primero."
            );
        }

        pacienteRepo.delete(paciente);
        return id;
    }

    @Override
    public ItemPacienteDTO obtenerInformacionPaciente(String id) {
        Paciente paciente = obtenerPacientePorId(id);
        return new ItemPacienteDTO(paciente.getId(), paciente.getNombre());
    }

    @Override
    public List<ItemPacienteDTO> listarPacientes() {
        return pacienteRepo.findAll()
                .stream()
                .map(p -> new ItemPacienteDTO(p.getId(), p.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemPacienteDTO> buscarPacientesPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de búsqueda no puede estar vacío");
        }

        return pacienteRepo.findByNombreContainingIgnoreCase(nombre.trim())
                .stream()
                .map(p -> new ItemPacienteDTO(p.getId(), p.getNombre()))
                .collect(Collectors.toList());
    }

    @Override
    public long contarPacientes() {
        return pacienteRepo.count();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Paciente obtenerPacientePorId(String id) {
        validarObjectId(id);
        return pacienteRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));
    }

    private boolean existeNombre(String nombre) {
        return pacienteRepo.existsByNombre(nombre.trim());
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del paciente no puede estar vacío");
        }
        if (nombre.trim().length() < 3) {
            throw new IllegalArgumentException("El nombre debe tener al menos 3 caracteres");
        }
        if (nombre.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede exceder los 100 caracteres");
        }
    }

    private void validarObjectId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException( " no puede estar vacío");
        }
        if (!ObjectId.isValid(id)) {
            throw new IllegalArgumentException(" no es válido: " + id);
        }
    }
}