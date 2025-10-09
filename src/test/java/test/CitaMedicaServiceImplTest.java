package test;

import VitalApp.dto.citaMedica.*;
import VitalApp.dto.medico.ItemHorarioDTO;
import VitalApp.model.documents.CitaMedica;
import VitalApp.model.documents.Medico;
import VitalApp.model.documents.Paciente;
import VitalApp.model.enums.EstadoCita;
import VitalApp.model.vo.HorarioMedico;
import VitalApp.repository.CitaMedicaRepository;
import VitalApp.repository.MedicoRepository;
import VitalApp.repository.PacienteRepository;
import VitalApp.service.service.CitaMedicaService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = VitalApp.VitalAppApplication.class)
public class CitaMedicaServiceImplTest {

    @Autowired
    private CitaMedicaService citaService;

    @Autowired
    private CitaMedicaRepository citaRepo;

    @Autowired
    private MedicoRepository medicoRepo;

    @Autowired
    private PacienteRepository pacienteRepo;

    @Test
    void agendarCita_HorarioDisponible() throws Exception {
        // Arrange - Crear y guardar médico y paciente reales
        Medico medico = new Medico();
        medico.setNombre("Dr. Prueba");
        medico.setHorariosDisponibles(new ArrayList<>());
        HorarioMedico horario = new HorarioMedico(
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                false
        );
        medico.getHorariosDisponibles().add(horario);
        medicoRepo.save(medico);

        Paciente paciente = new Paciente();
        paciente.setNombre("Paciente Prueba");
        pacienteRepo.save(paciente);

        CrearCitaMedicaDTO dto = new CrearCitaMedicaDTO(
                paciente.getId(),
                medico.getId(),
                new ItemHorarioDTO(
                        horario.getFecha(),
                        horario.getHoraInicio(),
                        horario.getHoraFin(),
                        false
                )
        );

        // Act
        String citaId = citaService.agendarCita(dto);

        // Assert
        assertNotNull(citaId);

        // Verificar en BD real
        Optional<CitaMedica> citaGuardada = citaRepo.findById(citaId);
        assertTrue(citaGuardada.isPresent());
        assertEquals(EstadoCita.PENDIENTE, citaGuardada.get().getEstado());

        // Verificar que el médico se actualizó
        Optional<Medico> medicoActualizado = medicoRepo.findById(medico.getId());
        assertTrue(medicoActualizado.get().getHorariosDisponibles().get(0).isReservado());
    }

    @Test
    void cancelarCita_CitaPendiente_CanceladaCorrectamente() throws Exception {
        // Arrange - Crear y guardar médico
        Medico medico = new Medico();
        medico.setNombre("Dr. Test Cancelar");
        HorarioMedico horario = new HorarioMedico(
                LocalDate.now(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                true
        );
        medico.setHorariosDisponibles(new ArrayList<>(List.of(horario)));
        medicoRepo.save(medico);

        // Crear y guardar paciente
        Paciente paciente = new Paciente();
        paciente.setNombre("Paciente Test Cancelar");
        pacienteRepo.save(paciente);

        // Crear y guardar cita pendiente
        CitaMedica cita = new CitaMedica();
        cita.setIdCliente(new ObjectId(paciente.getId()));
        cita.setIdMedico(new ObjectId(medico.getId()));
        cita.setHorario(horario);
        cita.setEstado(EstadoCita.PENDIENTE);
        citaRepo.save(cita);

        // Act
        String resultado = citaService.cancelarCita(cita.getId());

        // Assert
        // Verificar en BD
        Optional<CitaMedica> citaCancelada = citaRepo.findById(cita.getId());
        assertTrue(citaCancelada.isPresent());
        assertEquals(EstadoCita.CANCELADA, citaCancelada.get().getEstado());

        // Verificar horario liberado
        Optional<Medico> medicoActualizado = medicoRepo.findById(medico.getId());
        assertTrue(medicoActualizado.isPresent());
        assertFalse(medicoActualizado.get().getHorariosDisponibles().get(0).isReservado());

        // Verificar retorno correcto
        assertEquals(cita.getId(), resultado);
    }

    @Test
    void agregarResultadoMedico_CitaPendiente_ResultadoAgregado() throws Exception {
        // Arrange - Crear y guardar médico
        Medico medico = new Medico();
        medico.setNombre("Dr. Test Resultado");
        medicoRepo.save(medico);

        // Crear y guardar paciente
        Paciente paciente = new Paciente();
        paciente.setNombre("Paciente Test Resultado");
        pacienteRepo.save(paciente);

        // Crear y guardar cita pendiente
        CitaMedica cita = new CitaMedica();
        cita.setIdCliente(new ObjectId(paciente.getId()));
        cita.setIdMedico(new ObjectId(medico.getId()));
        cita.setEstado(EstadoCita.PENDIENTE);
        citaRepo.save(cita);

        LocalDateTime fechaRegistro = LocalDateTime.now().withNano(0);
        CrearResultadoMedicoDTO resultadoDTO = new CrearResultadoMedicoDTO(
                new ObjectId(cita.getId()),
                "Descripción detallada",
                "Diagnóstico completo",
                "Recomendaciones específicas",
                fechaRegistro
        );

        // Act
        String resultado = citaService.agregarResultadoMedico(cita.getId(), resultadoDTO);

        // Assert
        // Verificar en BD
        Optional<CitaMedica> citaActualizada = citaRepo.findById(cita.getId());
        assertTrue(citaActualizada.isPresent());

        // Verificar estado cambiado
        assertEquals(EstadoCita.VISTA, citaActualizada.get().getEstado());

        // Verificar resultado
        assertNotNull(citaActualizada.get().getResultado());
        assertEquals("Descripción detallada", citaActualizada.get().getResultado().getDescripcion());
        assertEquals(fechaRegistro, citaActualizada.get().getResultado().getFechaRegistro().withNano(0));

        // Verificar retorno correcto
        assertEquals(cita.getId(), resultado);
    }
}