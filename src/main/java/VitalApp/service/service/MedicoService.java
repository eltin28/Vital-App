package VitalApp.service.service;

import VitalApp.dto.medico.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface MedicoService {
    String crearMedico(CrearMedicoDTO medico) throws Exception;
    String editarMedico(EditarMedicoDTO medico) throws Exception;
    String eliminarMedico(String id) throws Exception;
    InformacionMedicoDTO obtenerInformacionMedico(String id) throws Exception;
    List<ItemMedicoDTO> listarMedicos();
    List<ItemMedicoDTO> buscarMedicosPorEspecialidad(String especialidad);
    String agregarHorario(String idMedico, CrearHorarioDTO horario) throws Exception;
    String eliminarHorario(String idMedico, LocalDate fecha, LocalTime horaInicio);
    List<ItemHorarioDTO> listarHorarios(String idMedico) throws Exception;
    List<ItemHorarioDTO> listarHorariosDisponibles(String idMedico);
}
