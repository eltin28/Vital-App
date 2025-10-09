package VitalApp.service.service;

import VitalApp.dto.medico.*;

import java.util.List;

public interface MedicoService {
    String crearMedico(CrearMedicoDTO medico) throws Exception;
    String editarMedico(EditarMedicoDTO medico) throws Exception;
    String eliminarMedico(String id) throws Exception;
    InformacionMedicoDTO obtenerInformacionMedico(String id) throws Exception;
    List<ItemMedicoDTO> listarMedicos();
    String agregarHorario(String idMedico, CrearHorarioDTO horario) throws Exception;
    List<ItemHorarioDTO> listarHorarios(String idMedico) throws Exception;
}
