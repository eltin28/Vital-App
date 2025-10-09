package VitalApp.service.implement;

import VitalApp.dto.paciente.CrearPacienteDTO;
import VitalApp.dto.paciente.EditarPacienteDTO;
import VitalApp.dto.paciente.ItemPacienteDTO;
import VitalApp.model.documents.Paciente;
import VitalApp.repository.PacienteRepository;
import VitalApp.service.service.PacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepo;

    @Override
    public String crearPaciente(CrearPacienteDTO pacienteDTO) throws Exception {
        if (pacienteDTO.nombre() == null || pacienteDTO.nombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del paciente no puede estar vacío.");
        }

        if (existeNombre(pacienteDTO.nombre())) {
            throw new Exception("Ya existe un paciente con el nombre: " + pacienteDTO.nombre());
        }

        Paciente paciente = Paciente.builder()
                .nombre(pacienteDTO.nombre().trim())
                .build();

        return pacienteRepo.save(paciente).getId();
    }

    @Override
    public String editarPaciente(EditarPacienteDTO pacienteDTO) throws Exception {
        Paciente paciente = obtenerPacientePorId(pacienteDTO.id());

        if (pacienteDTO.nombre() == null || pacienteDTO.nombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del paciente no puede estar vacío.");
        }

        paciente.setNombre(pacienteDTO.nombre().trim());

        return pacienteRepo.save(paciente).getId();
    }

    @Override
    public String eliminarPaciente(String id) throws Exception {
        Paciente paciente = obtenerPacientePorId(id);
        pacienteRepo.delete(paciente);
        return id;
    }

    @Override
    public ItemPacienteDTO obtenerInformacionPaciente(String id) throws Exception {
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

    private boolean existeNombre(String nombre) {
        return pacienteRepo.existsByNombre(nombre.trim());
    }

    private Paciente obtenerPacientePorId(String id) throws Exception {
        return pacienteRepo.findById(id)
                .orElseThrow(() -> new Exception("Paciente no encontrado con ID: " + id));
    }
}