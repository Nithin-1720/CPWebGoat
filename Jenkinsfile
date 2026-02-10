pipeline {
    agent any

    environment {
    SNYK_PROJECT = "webgoat"
    SNYK_ORG = "Cyberpwn NFR - Shared"
    }

    stages {

        stage('Git Clone') {
            steps {
                git branch: 'main',
                credentialsId: 'github-creds',
                url: 'https://github.com/Nithin-1720/CPWebGoat.git'
            }
        }

        stage('Maven Test (in Java 21 container)') {
            steps {
                sh '''
                docker run --rm \
                  -v "$(pwd):/app" \
                  -w /app \
                  maven:3.9.5-eclipse-temurin-21 \
                  mvn clean test -DskipTests -P'!start-server'
                '''
            }
        }

        stage('Maven Build (in Java 21 container)') {
            steps {
                sh '''
                docker run --rm \
                  -v "$(pwd):/app" \
                  -w /app \
                  maven:3.9.5-eclipse-temurin-21 \
                  mvn clean install -DskipTests -P'!start-server'
                '''
            }
        }

        stage('Snyk open source monitor') {
            steps {
                sh 'snyk monitor --org="$SNYK_ORG" --project-name-prefix=webgoat'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t nithinragesh/webgoat:latest .'
            }
        }

        stage('Docker Push to Hub') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'Docker-creds', variable: 'DOCKERHUB_TOKEN')]) {
                        sh 'docker login -u nithinragesh -p $DOCKERHUB_TOKEN'
                    }
                }
                sh 'docker push nithinragesh/webgoat:latest'
                sh 'docker rm -f webgoat || true'
            }
        }

        stage('Docker Run') {
            steps {
                sh 'docker run -d -p 8040:8080 --name=webgoat nithinragesh/webgoat:latest'
            }
        }

        stage('Snyk docker monitor') {
            steps {
                sh 'sleep 15'
                sh 'snyk container monitor nithinragesh/webgoat:latest'
            }
        }

        stage('Snyk code test') {
            steps {
                sh 'snyk code test --allprojects --report --project-name=webgoat-code --severity-threshold=high'
            }
        }
    }
}
