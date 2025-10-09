package VitalApp.repository;

import VitalApp.model.documents.Paciente;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends MongoRepository <Paciente, String> {
    boolean existsByNombre(String nombre);
}
