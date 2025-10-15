package VitalApp.controller;

import VitalApp.dto.Autentication.MensajeDTO;
import VitalApp.dto.citaMedica.InformacionResultadoMedicoDTO;
import VitalApp.dto.citaMedica.ItemCitaMedicaDTO;
import VitalApp.dto.paciente.CrearPacienteDTO;
import VitalApp.dto.paciente.EditarPacienteDTO;
import VitalApp.dto.paciente.ItemPacienteDTO;
import VitalApp.model.enums.EstadoCita;
import VitalApp.service.service.CitaMedicaService;
import VitalApp.service.service.PacienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "http://localhost:3000")
public class PacienteController {

    private final PacienteService pacienteService;
    private final CitaMedicaService citaMedicaService;

    @PostMapping
    public ResponseEntity<MensajeDTO<String>> crearPaciente(@Valid @RequestBody CrearPacienteDTO dto) throws Exception {
        String id = pacienteService.crearPaciente(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeDTO<>(false, "Paciente creado exitosamente con ID: " + id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeDTO<String>> editarPaciente(
            @PathVariable @NotBlank(message = "El ID del paciente es obligatorio") String id,
            @Valid @RequestBody EditarPacienteDTO dto) throws Exception {

        // Validar que el ID del path coincida con el del DTO
        if (!id.equals(dto.id())) {
            throw new IllegalArgumentException("El ID del path no coincide con el ID del paciente");
        }

        pacienteService.editarPaciente(dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Paciente editado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeDTO<String>> eliminarPaciente(
            @PathVariable @NotBlank(message = "El ID del paciente es obligatorio") String id) throws Exception {
        pacienteService.eliminarPaciente(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Paciente eliminado exitosamente"));
    }

    @GetMapping
    public ResponseEntity<MensajeDTO<List<ItemPacienteDTO>>> listarPacientes(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size) {

        List<ItemPacienteDTO> lista = pacienteService.listarPacientes();

        // Paginación simple (para implementación básica)
        int start = page * size;
        int end = Math.min(start + size, lista.size());

        if (start >= lista.size()) {
            lista = new ArrayList<>();
        } else {
            lista = lista.subList(start, end);
        }

        return ResponseEntity.ok(new MensajeDTO<>(false, lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeDTO<ItemPacienteDTO>> obtenerPaciente(
            @PathVariable @NotBlank(message = "El ID del paciente es obligatorio") String id) throws Exception {
        ItemPacienteDTO info = pacienteService.obtenerInformacionPaciente(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, info));
    }

    @GetMapping("/buscar")
    public ResponseEntity<MensajeDTO<List<ItemPacienteDTO>>> buscarPorNombre(
            @RequestParam @NotBlank(message = "El nombre es obligatorio") String nombre) {
        List<ItemPacienteDTO> lista = pacienteService.buscarPacientesPorNombre(nombre);
        return ResponseEntity.ok(new MensajeDTO<>(false, lista));
    }

    @GetMapping("/{id}/citas")
    public ResponseEntity<MensajeDTO<List<ItemCitaMedicaDTO>>> obtenerCitasPaciente(
            @PathVariable @NotBlank(message = "El ID del paciente es obligatorio") String id,
            @RequestParam(required = false) EstadoCita estado) {

        // Este método ya existe en CitaMedicaService, lo reutilizamos
        // Necesitarás inyectar CitaMedicaService aquí
        List<ItemCitaMedicaDTO> citas = citaMedicaService.listarCitasPorPaciente(id);

        if (estado != null) {
            citas = citas.stream()
                    .filter(c -> c.estado() == estado)
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(new MensajeDTO<>(false, citas));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<MensajeDTO<Map<String, Object>>> obtenerHistorialPaciente(
            @PathVariable @NotBlank(message = "El ID del paciente es obligatorio") String id) throws Exception {

        ItemPacienteDTO paciente = pacienteService.obtenerInformacionPaciente(id);
        List<ItemCitaMedicaDTO> citas = citaMedicaService.listarCitasPorPaciente(id);

        Map<String, Object> historial = new HashMap<>();
        historial.put("paciente", paciente);
        historial.put("totalCitas", citas.size());
        historial.put("citasPendientes", citas.stream()
                .filter(c -> c.estado() == EstadoCita.PENDIENTE)
                .count());
        historial.put("citasAtendidas", citas.stream()
                .filter(c -> c.estado() == EstadoCita.VISTA)
                .count());
        historial.put("citasCanceladas", citas.stream()
                .filter(c -> c.estado() == EstadoCita.CANCELADA)
                .count());
        historial.put("citas", citas);

        // Últimos resultados médicos
        List<InformacionResultadoMedicoDTO> resultados = citas.stream()
                .filter(c -> c.resultado() != null)
                .sorted((c1, c2) -> c2.horario().getFecha().compareTo(c1.horario().getFecha()))
                .limit(5)
                .map(c -> new InformacionResultadoMedicoDTO(
                        c.id(),
                        c.resultado().getDescripcion(),
                        c.resultado().getDiagnostico(),
                        c.resultado().getRecomendaciones(),
                        c.resultado().getFechaRegistro()
                ))
                .collect(Collectors.toList());

        historial.put("ultimosResultados", resultados);

        return ResponseEntity.ok(new MensajeDTO<>(false, historial));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<MensajeDTO<Map<String, Object>>> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        long totalPacientes = pacienteService.contarPacientes();
        estadisticas.put("totalPacientes", totalPacientes);

        return ResponseEntity.ok(new MensajeDTO<>(false, estadisticas));
    }

    @GetMapping("/{id}/proximas-citas")
    public ResponseEntity<MensajeDTO<List<ItemCitaMedicaDTO>>> obtenerProximasCitas(
            @PathVariable @NotBlank(message = "El ID del paciente es obligatorio") String id) {

        List<ItemCitaMedicaDTO> citas = citaMedicaService.listarCitasPorPaciente(id);
        LocalDateTime ahora = LocalDateTime.now();

        List<ItemCitaMedicaDTO> proximasCitas = citas.stream()
                .filter(c -> c.estado() == EstadoCita.PENDIENTE)
                .filter(c -> {
                    LocalDateTime fechaCita = LocalDateTime.of(
                            c.horario().getFecha(),
                            c.horario().getHoraInicio()
                    );
                    return fechaCita.isAfter(ahora);
                })
                .sorted((c1, c2) -> {
                    int compareFecha = c1.horario().getFecha().compareTo(c2.horario().getFecha());
                    if (compareFecha != 0) return compareFecha;
                    return c1.horario().getHoraInicio().compareTo(c2.horario().getHoraInicio());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new MensajeDTO<>(false, proximasCitas));
    }
}