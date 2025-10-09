db = connect( 'mongodb://localhost:27017/VitalApp' );


// 2. Insertar Pacientes
db.paciente.insertMany([
  {
    _id: ObjectId("66a2a9aaa8620e3c1c543701"),
    nombre: "María González López",
    _class: "VitalApp.model.documents.Paciente"
  },
  {
    _id: ObjectId("66a2a9aaa8620e3c1c543702"),
    nombre: "Carlos Ruiz Mendoza",
    _class: "VitalApp.model.documents.Paciente"
  },
  {
    _id: ObjectId("66a2a9aaa8620e3c1c543703"),
    nombre: "Ana Torres Sánchez",
    _class: "VitalApp.model.documents.Paciente"
  }
]);

// 3. Insertar Médicos
db.medico.insertMany([
  {
    _id: ObjectId("66a2a9aaa8620e3c1c543711"),
    nombre: "Dr. Javier Méndez",
    especialidad: "Cardiología",
    horariosDisponibles: [
      {
        fecha: ISODate("2024-08-10T00:00:00Z"),
        horaInicio: "09:00",
        horaFin: "13:00",
        reservado: false
      },
      {
        fecha: ISODate("2024-08-11T00:00:00Z"),
        horaInicio: "14:00",
        horaFin: "18:00",
        reservado: false
      }
    ],
    _class: "VitalApp.model.documents.Medico"
  },
  {
    _id: ObjectId("66a2a9aaa8620e3c1c543712"),
    nombre: "Dra. Laura Jiménez",
    especialidad: "Pediatría",
    horariosDisponibles: [
      {
        fecha: ISODate("2024-08-10T00:00:00Z"),
        horaInicio: "08:00",
        horaFin: "12:00",
        reservado: false
      }
    ],
    _class: "VitalApp.model.documents.Medico"
  }
]);

// 4. Insertar Citas Médicas
db.citaMedica.insertMany([
  {
    _id: ObjectId("66a2a9aaa8620e3c1c543721"),
    idCliente: ObjectId("66a2a9aaa8620e3c1c543701"), // María González
    idMedico: ObjectId("66a2a9aaa8620e3c1c543711"), // Dr. Javier Méndez
    horario: {
      fecha: ISODate("2024-08-10T00:00:00Z"),
      horaInicio: "09:00",
      horaFin: "10:00",
      reservado: true
    },
    estado: "PENDIENTE",
    _class: "VitalApp.model.documents.CitaMedica"
  },
  {
    _id: ObjectId("66a2a9aaa8620e3c1c543722"),
    idCliente: ObjectId("66a2a9aaa8620e3c1c543702"), // Carlos Ruiz
    idMedico: ObjectId("66a2a9aaa8620e3c1c543712"), // Dra. Laura Jiménez
    horario: {
      fecha: ISODate("2024-08-10T00:00:00Z"),
      horaInicio: "08:00",
      horaFin: "09:00",
      reservado: true
    },
    estado: "VISTA",
    resultado: {
      idCitaMedica: ObjectId("66a2a9aaa8620e3c1c543722"),
      descripcion: "Control de crecimiento normal",
      diagnostico: "Desarrollo físico y mental acorde a edad",
      recomendaciones: "Continuar alimentación balanceada",
      fechaRegistro: ISODate("2024-08-10T09:30:00Z")
    },
    _class: "VitalApp.model.documents.CitaMedica"
  }
]);

// 5. Insertar Resultados Médicos (para citas existentes)
db.citaMedica.updateOne(
  { _id: ObjectId("66a2a9aaa8620e3c1c543721") },
  {
    $set: {
      estado: "VISTA",
      resultado: {
        idCitaMedica: ObjectId("66a2a9aaa8620e3c1c543721"),
        descripcion: "Paciente presenta presión arterial elevada",
        diagnostico: "Hipertensión grado I",
        recomendaciones: "Reducir consumo de sal y control semanal",
        fechaRegistro: ISODate("2024-08-10T10:05:00Z")
      }
    }
  }
);