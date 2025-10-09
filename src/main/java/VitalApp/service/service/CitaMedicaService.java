package VitalApp.service.service;

import VitalApp.dto.citaMedica.CrearCitaMedicaDTO;
import VitalApp.dto.citaMedica.InformacionResultadoMedicoDTO;
import VitalApp.dto.citaMedica.ItemCitaMedicaDTO;
import VitalApp.dto.citaMedica.CrearResultadoMedicoDTO;

import java.util.List;

public interface CitaMedicaService {
    String agendarCita(CrearCitaMedicaDTO cita) throws Exception;
    String cancelarCita(String idCita) throws Exception;
    List<ItemCitaMedicaDTO> listarCitasPorPaciente(String idPaciente);
    List<ItemCitaMedicaDTO> listarCitasPorMedico(String idMedico);
    String agregarResultadoMedico(String idCita, CrearResultadoMedicoDTO resultado) throws Exception;
    InformacionResultadoMedicoDTO obtenerResultadoMedico(String idCita) throws Exception;
}
