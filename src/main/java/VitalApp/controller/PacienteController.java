package VitalApp.controller;

import VitalApp.dto.Autentication.MensajeDTO;
import VitalApp.dto.paciente.CrearPacienteDTO;
import VitalApp.dto.paciente.EditarPacienteDTO;
import VitalApp.dto.paciente.ItemPacienteDTO;
import VitalApp.service.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @PostMapping("/crear")
    public ResponseEntity<MensajeDTO<String>> crearPaciente(@Valid @RequestBody CrearPacienteDTO dto) throws Exception {
        pacienteService.crearPaciente(dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Paciente creado exitosamente"));
    }

    @PutMapping("/editar")
    public ResponseEntity<MensajeDTO<String>> editarPaciente(@Valid @RequestBody EditarPacienteDTO dto) throws Exception {
        pacienteService.editarPaciente(dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Paciente editado exitosamente"));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<MensajeDTO<String>> eliminarPaciente(@PathVariable String id) throws Exception {
        pacienteService.eliminarPaciente(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Paciente eliminado exitosamente"));
    }

    @GetMapping("/listar-todo")
    public ResponseEntity<MensajeDTO<List<ItemPacienteDTO>>> listarPacientes() {
        List<ItemPacienteDTO> lista = pacienteService.listarPacientes();
        return ResponseEntity.ok(new MensajeDTO<>(false, lista));
    }

    @GetMapping("/obtener/{id}")
    public ResponseEntity<MensajeDTO<ItemPacienteDTO>> obtenerPaciente(@PathVariable String id) throws Exception {
        ItemPacienteDTO info = pacienteService.obtenerInformacionPaciente(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, info));
    }
}