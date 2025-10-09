package test;

import VitalApp.dto.medico.*;
import VitalApp.service.service.MedicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = VitalApp.VitalAppApplication.class)
public class MedicoServiceImplTest {

    @Autowired
    private MedicoService medicoService;

    @Test
    void crearMedico_DatosValidos_RetornaId() throws Exception {
        // Arrange
        CrearMedicoDTO dto = new CrearMedicoDTO(
                "Dr. Test",
                "Cardiología",
                List.of() // Lista vacía de horarios iniciales
        );

        // Act
        String id = medicoService.crearMedico(dto);

        // Assert
        assertNotNull(id);
        assertFalse(id.isEmpty());
    }

    @Test
    void editarMedico_DatosValidos_ActualizaCorrectamente() throws Exception {
        // Arrange
        CrearMedicoDTO crearDto = new CrearMedicoDTO(
                "Dr. Original",
                "Pediatría",
                List.of()
        );
        String id = medicoService.crearMedico(crearDto);

        EditarMedicoDTO editarDto = new EditarMedicoDTO(
                id,
                "Dr. Editado",
                "Neurología",
                List.of()
        );

        // Act
        String resultado = medicoService.editarMedico(editarDto);

        // Assert
        assertEquals(id, resultado);
        InformacionMedicoDTO actualizado = medicoService.obtenerInformacionMedico(id);
        assertEquals("Dr. Editado", actualizado.nombre());
        assertEquals("Neurología", actualizado.especialidad());
    }

    @Test
    void agregarHorario_HorarioValido_AgregaCorrectamente() throws Exception {
        // Arrange
        CrearMedicoDTO medicoDto = new CrearMedicoDTO(
                "Dr. Horario",
                "Dermatología",
                List.of()
        );
        String id = medicoService.crearMedico(medicoDto);

        CrearHorarioDTO horarioDto = new CrearHorarioDTO(
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0)
        );

        // Act
        String resultado = medicoService.agregarHorario(id, horarioDto);

        // Assert
        assertEquals(id, resultado);
        List<ItemHorarioDTO> horarios = medicoService.listarHorarios(id);
        assertEquals(1, horarios.size());
    }

    @Test
    void listarMedicos_ConDatos_RetornaLista() {
        // Act
        List<ItemMedicoDTO> medicos = medicoService.listarMedicos();

        // Assert
        assertNotNull(medicos);
    }

    @Test
    void eliminarMedico_Existente_EliminaCorrectamente() throws Exception {
        // Arrange
        CrearMedicoDTO dto = new CrearMedicoDTO(
                "Dr. Eliminar",
                "Oftalmología",
                List.of()
        );
        String id = medicoService.crearMedico(dto);

        // Act
        String resultado = medicoService.eliminarMedico(id);

        // Assert
        assertEquals(id, resultado);
        assertThrows(Exception.class, () -> medicoService.obtenerInformacionMedico(id));
    }
}