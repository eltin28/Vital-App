package VitalApp.repository;

import VitalApp.model.documents.Medico;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicoRepository extends MongoRepository <Medico, String> {
    boolean existsByNombre(String nombre);
}