package VitalApp.repository;

import VitalApp.model.documents.Paciente;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends MongoRepository <Paciente, String> {
    boolean existsByNombre(String nombre);
    Optional<Paciente> findByNombre(String nombre);
    List<Paciente> findByNombreContainingIgnoreCase(String nombre);
}
