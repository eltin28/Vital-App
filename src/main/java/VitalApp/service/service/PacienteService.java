package VitalApp.service.service;

import VitalApp.dto.paciente.CrearPacienteDTO;
import VitalApp.dto.paciente.EditarPacienteDTO;
import VitalApp.dto.paciente.ItemPacienteDTO;

import java.util.List;

public interface PacienteService {
    String crearPaciente(CrearPacienteDTO paciente) throws Exception;
    String editarPaciente(EditarPacienteDTO paciente) throws Exception;
    String eliminarPaciente(String id) throws Exception;
    ItemPacienteDTO obtenerInformacionPaciente(String id) throws Exception;
    List<ItemPacienteDTO> listarPacientes();
    List<ItemPacienteDTO> buscarPacientesPorNombre(String nombre);
    long contarPacientes();
}
