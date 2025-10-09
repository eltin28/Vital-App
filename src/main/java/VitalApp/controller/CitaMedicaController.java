package VitalApp.controller;

import VitalApp.dto.Autentication.MensajeDTO;
import VitalApp.dto.citaMedica.CrearCitaMedicaDTO;
import VitalApp.dto.citaMedica.CrearResultadoMedicoDTO;
import VitalApp.dto.citaMedica.InformacionResultadoMedicoDTO;
import VitalApp.dto.citaMedica.ItemCitaMedicaDTO;
import VitalApp.service.service.CitaMedicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/citaMedica")

public class CitaMedicaController {

    private final CitaMedicaService citaMedicaService;

    @PostMapping("/agendar")
    public ResponseEntity<MensajeDTO<String>> agendarCita(@Valid @RequestBody CrearCitaMedicaDTO dto) throws Exception {
        String id = citaMedicaService.agendarCita(dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Cita agendada exitosamente con ID: " + id));
    }

    @PutMapping("/cancelar/{id}")
    public ResponseEntity<MensajeDTO<String>> cancelarCita(@PathVariable String id) throws Exception {
        citaMedicaService.cancelarCita(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Cita cancelada exitosamente"));
    }

    @GetMapping("/listar/paciente/{idPaciente}")
    public ResponseEntity<MensajeDTO<List<ItemCitaMedicaDTO>>> listarPorPaciente(@PathVariable String idPaciente) {
        List<ItemCitaMedicaDTO> citas = citaMedicaService.listarCitasPorPaciente(idPaciente);
        return ResponseEntity.ok(new MensajeDTO<>(false, citas));
    }

    @GetMapping("/listar/medico/{idMedico}")
    public ResponseEntity<MensajeDTO<List<ItemCitaMedicaDTO>>> listarPorMedico(@PathVariable String idMedico) {
        List<ItemCitaMedicaDTO> citas = citaMedicaService.listarCitasPorMedico(idMedico);
        return ResponseEntity.ok(new MensajeDTO<>(false, citas));
    }

    @PostMapping("/resultado/{idCita}")
    public ResponseEntity<MensajeDTO<String>> agregarResultado(@PathVariable String idCita, @Valid @RequestBody CrearResultadoMedicoDTO dto) throws Exception {
        citaMedicaService.agregarResultadoMedico(idCita, dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Resultado médico agregado exitosamente"));
    }

    @GetMapping("/resultado/{idCita}")
    public ResponseEntity<MensajeDTO<InformacionResultadoMedicoDTO>> obtenerResultado(@PathVariable String idCita) throws Exception {
        InformacionResultadoMedicoDTO resultado = citaMedicaService.obtenerResultadoMedico(idCita);
        return ResponseEntity.ok(new MensajeDTO<>(false, resultado));
    }
}
