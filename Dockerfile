# Build stage
FROM gradle:latest AS build
WORKDIR /home/gradle/src

# Copiar código fuente (no necesitas instalar JDK manualmente, la imagen ya lo incluye)
COPY --chown=gradle:gradle . .

# Construir la aplicación (combinamos los comandos para reducir capas)
RUN gradle clean bootJar

# Fase de ejecución (Package stage)
FROM openjdk:17-jdk-slim
WORKDIR /app

# Variable para el archivo JAR (ajustado para capturar el nombre correcto)
ARG JAR_FILE=VitalApp-*.jar

# Copiar el JAR generado desde la fase de construcción
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# Puerto expuesto (definido por Render)
EXPOSE ${PORT}

# Configuración de perfil para pruebas
CMD ["java", "-jar", "/app/app.jar", "--spring.profiles.active=test"]