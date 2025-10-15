package VitalApp.controller;

import VitalApp.dto.Autentication.MensajeDTO;
import VitalApp.dto.medico.*;
import VitalApp.service.service.MedicoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "http://localhost:3000")
public class MedicoController {

    private final MedicoService medicoService;

    @PostMapping
    public ResponseEntity<MensajeDTO<String>> crearMedico(@Valid @RequestBody CrearMedicoDTO dto) throws Exception {
        String id = medicoService.crearMedico(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeDTO<>(false, "Médico creado exitosamente con ID: " + id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeDTO<String>> editarMedico(
            @PathVariable @NotBlank(message = "El ID del médico es obligatorio") String id,
            @Valid @RequestBody EditarMedicoDTO dto) throws Exception {

        if (!id.equals(dto.id())) {
            throw new IllegalArgumentException("El ID del path no coincide con el ID del médico");
        }

        medicoService.editarMedico(dto);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Médico editado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeDTO<String>> eliminarMedico(
            @PathVariable @NotBlank(message = "El ID del médico es obligatorio") String id) throws Exception {
        medicoService.eliminarMedico(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Médico eliminado exitosamente"));
    }

    @GetMapping
    public ResponseEntity<MensajeDTO<List<ItemMedicoDTO>>> listarMedicos() {
        List<ItemMedicoDTO> lista = medicoService.listarMedicos();
        return ResponseEntity.ok(new MensajeDTO<>(false, lista));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeDTO<InformacionMedicoDTO>> obtenerMedico(
            @PathVariable @NotBlank(message = "El ID del médico es obligatorio") String id) throws Exception {
        InformacionMedicoDTO info = medicoService.obtenerInformacionMedico(id);
        return ResponseEntity.ok(new MensajeDTO<>(false, info));
    }

    @GetMapping("/buscar")
    public ResponseEntity<MensajeDTO<List<ItemMedicoDTO>>> buscarPorEspecialidad(
            @RequestParam @NotBlank(message = "La especialidad es obligatoria") String especialidad) {
        List<ItemMedicoDTO> lista = medicoService.buscarMedicosPorEspecialidad(especialidad);
        return ResponseEntity.ok(new MensajeDTO<>(false, lista));
    }

    // ==================== GESTIÓN DE HORARIOS ====================

    @PostMapping("/{idMedico}/horarios")
    public ResponseEntity<MensajeDTO<String>> agregarHorario(
            @PathVariable @NotBlank(message = "El ID del médico es obligatorio") String idMedico,
            @Valid @RequestBody CrearHorarioDTO dto) throws Exception {
        medicoService.agregarHorario(idMedico, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeDTO<>(false, "Horario agregado exitosamente"));
    }

    @PostMapping("/{idMedico}/horarios/batch")
    public ResponseEntity<MensajeDTO<String>> agregarMultiplesHorarios(
            @PathVariable @NotBlank(message = "El ID del médico es obligatorio") String idMedico,
            @Valid @RequestBody List<CrearHorarioDTO> horarios) {

        if (horarios == null || horarios.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar al menos un horario");
        }

        int agregados = 0;
        for (CrearHorarioDTO dto : horarios) {
            try {
                medicoService.agregarHorario(idMedico, dto);
                agregados++;
            } catch (Exception e) {
                // Continuar con los demás horarios
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeDTO<>(false, agregados + " horario(s) agregado(s) exitosamente"));
    }

    @GetMapping("/{idMedico}/horarios")
    public ResponseEntity<MensajeDTO<List<ItemHorarioDTO>>> listarHorarios(
            @PathVariable @NotBlank(message = "El ID del médico es obligatorio") String idMedico,
            @RequestParam(required = false) Boolean soloDisponibles) throws Exception {

        List<ItemHorarioDTO> lista;

        if (soloDisponibles != null && soloDisponibles) {
            lista = medicoService.listarHorariosDisponibles(idMedico);
        } else {
            lista = medicoService.listarHorarios(idMedico);
        }

        return ResponseEntity.ok(new MensajeDTO<>(false, lista));
    }

    @GetMapping("/{idMedico}/horarios/disponibles")
    public ResponseEntity<MensajeDTO<List<ItemHorarioDTO>>> listarHorariosDisponibles(
            @PathVariable @NotBlank(message = "El ID del médico es obligatorio") String idMedico,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fecha) {

        List<ItemHorarioDTO> lista = medicoService.listarHorariosDisponibles(idMedico);

        // Filtrar por fecha si se proporciona
        if (fecha != null) {
            lista = lista.stream()
                    .filter(h -> h.fecha().equals(fecha))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(new MensajeDTO<>(false, lista));
    }

    @DeleteMapping("/{idMedico}/horarios")
    public ResponseEntity<MensajeDTO<String>> eliminarHorario(
            @PathVariable @NotBlank(message = "El ID del médico es obligatorio") String idMedico,
            @RequestParam @NotNull @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate fecha,
            @RequestParam @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicio) {

        medicoService.eliminarHorario(idMedico, fecha, horaInicio);
        return ResponseEntity.ok(new MensajeDTO<>(false, "Horario eliminado exitosamente"));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<MensajeDTO<Map<String, Object>>> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();

        List<ItemMedicoDTO> medicos = medicoService.listarMedicos();
        estadisticas.put("totalMedicos", medicos.size());

        // Contar por especialidad
        Map<String, Long> porEspecialidad = medicos.stream()
                .collect(Collectors.groupingBy(
                        ItemMedicoDTO::especialidad,
                        Collectors.counting()
                ));
        estadisticas.put("porEspecialidad", porEspecialidad);

        return ResponseEntity.ok(new MensajeDTO<>(false, estadisticas));
    }

    @GetMapping("/especialidades")
    public ResponseEntity<MensajeDTO<List<String>>> listarEspecialidades() {
        List<String> especialidades = medicoService.listarMedicos().stream()
                .map(ItemMedicoDTO::especialidad)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return ResponseEntity.ok(new MensajeDTO<>(false, especialidades));
    }
}