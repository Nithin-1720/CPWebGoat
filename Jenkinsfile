pipeline {
    agent any

    environment {
        SEMGREP_APP_TOKEN = credentials('SEMGREP_APP_TOKEN')
        SEMGREP_PR_ID = "${env.CHANGE_ID}"
        NVD_API_KEY = credentials('NVD_API_KEY')
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
                sh 'mvn clean test -DskipTests -P!start-server'
            }
        }

        stage('Maven Build') {
            steps {
                sh 'mvn clean install -DskipTests -P!start-server'
            }
        }

        stage('Semgrep Scan') {
            steps {
                sh 'pip3 install semgrep'
                sh 'semgrep ci'
            }
        }

        stage('SCA - OWASP Dependency Check') {
    steps {
        dependencyCheck additionalArguments: """
            --scan .
            --format HTML
            --out dependency-check-report
            --nvdApiKey ${NVD_API_KEY}
            --failOnCVSS 7
        """, odcInstallation: 'dependency-check'
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
                    withCredentials([
                        string(credentialsId: 'Docker-creds', variable: 'DOCKERHUB_TOKEN')
                    ]) {
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


