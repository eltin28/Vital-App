package VitalApp.controller;

import VitalApp.dto.Autentication.MensajeDTO;
import VitalApp.dto.medico.*;
import VitalApp.service.service.MedicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService medicoService;

    @PostMapping("/crear")
    public ResponseEntity<MensajeDTO<String>> crearMedico(@Valid @RequestBody CrearMedicoDTO dto) throws Exception {
        medicoService.crearMedico(dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Médico creado exitosamente"));
    }

    @PutMapping("/editar")
    public ResponseEntity<MensajeDTO<String>> editarMedico(@Valid @RequestBody EditarMedicoDTO dto) throws Exception {
        medicoService.editarMedico(dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Médico editado exitosamente"));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<MensajeDTO<String>> eliminarMedico(@PathVariable String id) throws Exception {
        medicoService.eliminarMedico(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Médico eliminado exitosamente"));
    }

    @GetMapping("/listar-todo")
    public ResponseEntity<MensajeDTO<List<ItemMedicoDTO>>> listarMedicos() {
        List<ItemMedicoDTO> lista = medicoService.listarMedicos();
        return ResponseEntity.ok(new MensajeDTO<>(false, lista));
    }

    @GetMapping("/obtener/{id}")
    public ResponseEntity<MensajeDTO<InformacionMedicoDTO>> obtenerMedico(@PathVariable String id) throws Exception {
        InformacionMedicoDTO info = medicoService.obtenerInformacionMedico(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, info));
    }

    // Gestión de horarios

    @PostMapping("/horario/agregar/{idMedico}")
    public ResponseEntity<MensajeDTO<String>> agregarHorario(@PathVariable String idMedico, @Valid @RequestBody CrearHorarioDTO dto) throws Exception {
        medicoService.agregarHorario(idMedico, dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Horario agregado exitosamente"));
    }

    @GetMapping("/horarios/{idMedico}")
    public ResponseEntity<MensajeDTO<List<ItemHorarioDTO>>> listarHorarios(@PathVariable String idMedico) throws Exception {
        List<ItemHorarioDTO> lista = medicoService.listarHorarios(idMedico);
        return ResponseEntity.ok(new MensajeDTO<>(false, lista));
    }
}