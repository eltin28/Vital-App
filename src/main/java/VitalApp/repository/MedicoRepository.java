package VitalApp.repository;

import VitalApp.model.documents.Medico;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MedicoRepository extends MongoRepository <Medico, String> {
    boolean existsByNombre(String nombre);

    Optional<Medico> findByNombre(String nombre);  // ← Debe retornar Optional<Medico>
    List<Medico> findByEspecialidadContainingIgnoreCase(String especialidad);
}