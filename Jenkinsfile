pipeline{
    agent any
    tools{
        maven 'maven3'
    }
    stages{
        stage('git clone'){
            steps{
                git branch: 'main', credentialsId: 'github-creds', url: 'https://github.com/Nithin-1720/CPWebGoat.git'
            }
        }
        stage('mvn test'){
            steps{
                sh 'mvn clean test -P!start-server'
            }
        }
        stage('mvn build'){
            steps{
                sh 'mvn clean install -DskipTests -P!start-server'
            }
        }
        stage('docker build'){
            steps{
                sh 'docker build -t nithinragesh/webgoat:latest .'
            }
        }
        stage('docker push to hub'){
            steps{
                script{
                    withCredentials([string(credentialsId: 'Docker-creds', variable: 'Dockerhub')]) {
                        sh 'docker login -u nithinragesh -p ${Dockerhub}'   
                    }
                    sh 'docker push nithinragesh/webgoat:latest'
                }
            }
        }
        stage('docker container remove'){
            steps{
                sh 'docker rm -f webgoat'
            }
        }
        stage('docker run'){
            steps{
                sh 'docker run -d -p 8040:8080 --name=webgoat nithinragesh/webgoat'
            }
        }
    }
}
