package test;

import VitalApp.dto.paciente.*;
import VitalApp.model.documents.Paciente;
import VitalApp.repository.PacienteRepository;
import VitalApp.service.service.PacienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = VitalApp.VitalAppApplication.class)
public class PacienteServiceImplTest {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private PacienteRepository pacienteRepo;

    @Test
    void crearPaciente_NombreValido_PersisteEnBD() throws Exception {
        // Arrange
        CrearPacienteDTO dto = new CrearPacienteDTO("Juan Pérez");

        // Act
        String pacienteId = pacienteService.crearPaciente(dto);

        // Assert - Verificar en BD
        Optional<Paciente> pacienteGuardado = pacienteRepo.findById(pacienteId);
        assertTrue(pacienteGuardado.isPresent());
        assertEquals("Juan Pérez", pacienteGuardado.get().getNombre());
        assertNotNull(pacienteGuardado.get().getId());
    }

    @Test
    void crearPaciente_NombreRepetido_LanzaExcepcion() throws Exception {
        // Arrange
        CrearPacienteDTO dto = new CrearPacienteDTO("Nombre Repetido");
        pacienteService.crearPaciente(dto); // Primer registro

        // Act & Assert
        assertThrows(Exception.class, () -> {
            pacienteService.crearPaciente(dto); // Segundo registro con mismo nombre
        });
    }

    @Test
    void editarPaciente_IdInexistente_LanzaExcepcion() {
        // Arrange
        EditarPacienteDTO dto = new EditarPacienteDTO(
                "id_inexistente",
                "Nombre Nuevo"
        );

        // Act & Assert
        assertThrows(Exception.class, () -> {
            pacienteService.editarPaciente(dto);
        });
    }

    @Test
    void editarPaciente_DatosValidos_ActualizaEnBD() throws Exception {
        // Arrange - Crear paciente inicial
        Paciente paciente = new Paciente();
        paciente.setNombre("Nombre Original");
        pacienteRepo.save(paciente);

        EditarPacienteDTO dto = new EditarPacienteDTO(
                paciente.getId(),
                "Nombre Actualizado"
        );

        // Act
        String resultado = pacienteService.editarPaciente(dto);

        // Assert - Verificar en BD
        Optional<Paciente> pacienteActualizado = pacienteRepo.findById(paciente.getId());
        assertTrue(pacienteActualizado.isPresent());
        assertEquals("Nombre Actualizado", pacienteActualizado.get().getNombre());
        assertEquals(paciente.getId(), resultado);
    }

    @Test
    void eliminarPaciente_Existente_EliminaDeBD() throws Exception {
        // Arrange - Crear paciente
        Paciente paciente = new Paciente();
        paciente.setNombre("Paciente a Eliminar");
        pacienteRepo.save(paciente);

        // Act
        String resultado = pacienteService.eliminarPaciente(paciente.getId());

        // Assert - Verificar en BD
        Optional<Paciente> pacienteEliminado = pacienteRepo.findById(paciente.getId());
        assertFalse(pacienteEliminado.isPresent());
        assertEquals(paciente.getId(), resultado);
    }

    @Test
    void listarPacientes_MultiplesRegistros_RetornaTodos() {
        // Mostrar pacientes existentes antes de la prueba
        System.out.println("\n=== PACIENTES EXISTENTES ANTES DE LA PRUEBA ===");
        pacienteRepo.findAll().forEach(p ->
                System.out.println("ID: " + p.getId() + " - Nombre: " + p.getNombre()));
        System.out.println("Total: " + pacienteRepo.count() + " pacientes\n");

        // Limpiar base de datos para prueba limpia
        pacienteRepo.deleteAll();

        // Crear pacientes de prueba
        Paciente p1 = new Paciente();
        p1.setNombre("Paciente 1");
        pacienteRepo.save(p1);

        Paciente p2 = new Paciente();
        p2.setNombre("Paciente 2");
        pacienteRepo.save(p2);

        // Mostrar pacientes después de creación
        System.out.println("=== PACIENTES CREADOS PARA PRUEBA ===");
        pacienteRepo.findAll().forEach(p ->
                System.out.println("ID: " + p.getId() + " - Nombre: " + p.getNombre()));

        // Act
        List<ItemPacienteDTO> resultado = pacienteService.listarPacientes();

        // Assert
        assertEquals(2, resultado.size(), "Deberían haberse creado exactamente 2 pacientes");

        // Mostrar resultado del servicio
        System.out.println("\n=== RESULTADO DEL SERVICIO ===");
        resultado.forEach(p ->
                System.out.println("ID: " + p.id() + " - Nombre: " + p.nombre()));
    }

    @Test
    void obtenerInformacionPaciente_Existente_RetornaDTO() throws Exception {
        // Arrange - Crear paciente
        Paciente paciente = new Paciente();
        paciente.setNombre("Paciente Info");
        pacienteRepo.save(paciente);

        // Act
        ItemPacienteDTO resultado = pacienteService.obtenerInformacionPaciente(paciente.getId());

        // Assert
        assertEquals(paciente.getId(), resultado.id());
        assertEquals("Paciente Info", resultado.nombre());
    }
}