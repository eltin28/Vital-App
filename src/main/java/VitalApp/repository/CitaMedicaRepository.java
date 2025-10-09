package VitalApp.repository;

import VitalApp.model.documents.CitaMedica;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaMedicaRepository extends MongoRepository <CitaMedica, String> {

    List<CitaMedica> findByIdCliente(ObjectId idCliente);

    List<CitaMedica> findByIdMedico(ObjectId idMedico);
}
