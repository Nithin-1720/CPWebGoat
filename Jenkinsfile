pipeline {
    agent {
        docker {
            image 'maven:3.9.5-eclipse-temurin-21'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    tools {
        maven 'maven3'
    }

    stages {

        stage('Git Clone') {
            steps {
                git branch: 'main',
                credentialsId: 'github-creds',
                url: 'https://github.com/Nithin-1720/CPWebGoat.git'
            }
        }

        stage('Maven Test') {
            steps {
                sh "mvn clean test -DskipTests -P'!start-server'"
            }
        }

        stage('Maven Build') {
            steps {
                sh "mvn clean install -DskipTests -P'!start-server'"
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
    }
}

