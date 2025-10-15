pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'juanjothin/vitalapp'
        DOCKER_TAG = 'latest'
        REGISTRY_CREDENTIALS = 'Token-docker' // ID de las credenciales en Jenkins
    }

    stages {

        stage('Checkout') {
            steps {
                // Limpiar workspace antes de clonar
                deleteDir()

                // Clonar solo un repositorio
                checkout([$class: 'GitSCM',
                          branches: [[name: 'Staging']],
                          doGenerateSubmoduleConfigurations: false,
                          extensions: [],
                          userRemoteConfigs: [[url: 'https://github.com/eltin28/Vital-App.git']]])
            }
        }

        stage('Compilar (Gradle)') {
            steps {
                sh 'chmod +x ./gradlew'
                sh "rm -rf ${env.HOME}/.gradle"
                sh './gradlew clean'
                sh './gradlew clean build -x test'
            }
        }

        stage('Análisis SonarQube') {
            steps {
                withSonarQubeEnv('sonarqube-local') {
                    script {
                        echo "=== DEBUG: Variables de entorno Sonar ==="
                        sh 'echo "SONAR_HOST_URL = $SONAR_HOST_URL"'
                        sh 'echo "SONAR_AUTH_TOKEN = ${SONAR_AUTH_TOKEN:+***}"'
                        sh 'echo "Ejecutando análisis con Gradle..."'
                        sh './gradlew sonarqube -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_AUTH_TOKEN'
                    }
                }
            }
        }

        stage('Tests (JUnit + Postman)') {
            steps {
                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                    sh '''
                        # Limpiamos build previo
                        ./gradlew clean build -x test

                        echo "Levantando servidor Spring Boot..."
                        nohup java -jar build/libs/*.jar --spring.profiles.active=test > app.log 2>&1 &
                        sleep 15

                        echo "Ejecutando tests Newman..."
                        newman run tests/postman_collection.json --insecure || true

                        echo "Deteniendo servidor..."
                        pkill -f 'java -jar' || true
                    '''
                }
                junit 'build/test-results/test/*.xml'
            }
        }


        stage('Construir imagen Docker') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE:$DOCKER_TAG .'
            }
        }

        stage('Test en Docker') {
            steps {
                sh 'docker run --rm -d -p 8081:8080 --name test-vitalapp $DOCKER_IMAGE:$DOCKER_TAG'
                sh 'sleep 15'
                sh 'curl -f http://localhost:8081/actuator/health'
                sh 'docker stop test-vitalapp'
            }
        }

        stage('Push a DockerHub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "$REGISTRY_CREDENTIALS",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    sh 'docker push $DOCKER_IMAGE:$DOCKER_TAG'
                }
            }
        }

        stage('Desplegar a Staging') {
            steps {
                sh 'docker-compose down || true'
                sh 'docker-compose pull'
                sh 'docker-compose up -d'
            }
        }
    }
}
