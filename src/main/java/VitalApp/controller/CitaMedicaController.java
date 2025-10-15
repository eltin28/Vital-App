package VitalApp.controller;

import VitalApp.dto.Autentication.MensajeDTO;
import VitalApp.dto.citaMedica.CrearCitaMedicaDTO;
import VitalApp.dto.citaMedica.CrearResultadoMedicoDTO;
import VitalApp.dto.citaMedica.InformacionResultadoMedicoDTO;
import VitalApp.dto.citaMedica.ItemCitaMedicaDTO;
import VitalApp.model.enums.EstadoCita;
import VitalApp.service.service.CitaMedicaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/citas")
@Validated
@CrossOrigin(origins = "http://localhost:3000")
public class CitaMedicaController {

    private final CitaMedicaService citaMedicaService;

    @PostMapping("/agendar")
    public ResponseEntity<MensajeDTO<String>> agendarCita(@Valid @RequestBody CrearCitaMedicaDTO dto) throws Exception {
        String id = citaMedicaService.agendarCita(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeDTO<>(false, "Cita agendada exitosamente con ID: " + id));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<MensajeDTO<String>> cancelarCita(
            @PathVariable @NotBlank(message = "El ID de la cita es obligatorio") String id) throws Exception {
        citaMedicaService.cancelarCita(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Cita cancelada exitosamente"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeDTO<ItemCitaMedicaDTO>> obtenerCita(
            @PathVariable @NotBlank(message = "El ID de la cita es obligatorio") String id) {
        ItemCitaMedicaDTO cita = citaMedicaService.obtenerCitaPorIdDTO(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, cita));
    }

    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<MensajeDTO<List<ItemCitaMedicaDTO>>> listarPorPaciente(
            @PathVariable @NotBlank(message = "El ID del paciente es obligatorio") String idPaciente,
            @RequestParam(required = false) EstadoCita estado) {
        List<ItemCitaMedicaDTO> citas = citaMedicaService.listarCitasPorPaciente(idPaciente);

        // Filtrar por estado si se proporciona
        if (estado != null) {
            citas = citas.stream()
                    .filter(c -> c.estado() == estado)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(new MensajeDTO<>(false, citas));
    }

    @GetMapping("/medico/{idMedico}")
    public ResponseEntity<MensajeDTO<List<ItemCitaMedicaDTO>>> listarPorMedico(
            @PathVariable @NotBlank(message = "El ID del médico es obligatorio") String idMedico,
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fecha) {
        List<ItemCitaMedicaDTO> citas = citaMedicaService.listarCitasPorMedico(idMedico);

        // Filtrar por estado si se proporciona
        if (estado != null) {
            citas = citas.stream()
                    .filter(c -> c.estado() == estado)
                    .collect(Collectors.toList());
        }

        // Filtrar por fecha si se proporciona
        if (fecha != null) {
            citas = citas.stream()
                    .filter(c -> c.horario().getFecha().equals(fecha))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(new MensajeDTO<>(false, citas));
    }

    @GetMapping
    public ResponseEntity<MensajeDTO<List<ItemCitaMedicaDTO>>> listarTodasLasCitas(
            @RequestParam(required = false) EstadoCita estado) {
        // Necesitarás agregar este método en el servicio
        List<ItemCitaMedicaDTO> citas = citaMedicaService.listarTodasLasCitas();

        if (estado != null) {
            citas = citas.stream()
                    .filter(c -> c.estado() == estado)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(new MensajeDTO<>(false, citas));
    }

    @PostMapping("/{idCita}/resultado")
    public ResponseEntity<MensajeDTO<String>> agregarResultado(
            @PathVariable @NotBlank(message = "El ID de la cita es obligatorio") String idCita,
            @Valid @RequestBody CrearResultadoMedicoDTO dto) throws Exception {
        citaMedicaService.agregarResultadoMedico(idCita, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeDTO<>(false, "Resultado médico agregado exitosamente"));
    }

    @GetMapping("/{idCita}/resultado")
    public ResponseEntity<MensajeDTO<InformacionResultadoMedicoDTO>> obtenerResultado(
            @PathVariable @NotBlank(message = "El ID de la cita es obligatorio") String idCita) throws Exception {
        InformacionResultadoMedicoDTO resultado = citaMedicaService.obtenerResultadoMedico(idCita);
        return ResponseEntity.ok(new MensajeDTO<>(false, resultado));
    }

    @PutMapping("/{idCita}/resultado")
    public ResponseEntity<MensajeDTO<String>> actualizarResultado(
            @PathVariable @NotBlank(message = "El ID de la cita es obligatorio") String idCita,
            @Valid @RequestBody CrearResultadoMedicoDTO dto) {
        // Necesitarás agregar este método en el servicio
        citaMedicaService.actualizarResultadoMedico(idCita, dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Resultado médico actualizado exitosamente"));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<MensajeDTO<Map<String, Long>>> obtenerEstadisticas() {
        // Necesitarás agregar este método en el servicio
        Map<String, Long> estadisticas = citaMedicaService.obtenerEstadisticas();
        return ResponseEntity.ok(new MensajeDTO<>(false, estadisticas));
    }
}