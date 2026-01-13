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

        stage('SAST - Semgrep Scan') {
            steps {
                sh 'pip3 install semgrep'
                sh 'semgrep ci'
            }
        }
        
        stage('Docker Build') {
            steps {
                sh 'docker build -t nithinragesh/webgoat:latest .'
            }
        }

        stage('Scan Image') {
            steps {
                sh '''
                    trivy image \
                    --format json \
                    --output trivy-report.json \
                    nithinragesh/webgoat:latest
                '''
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

        stage('DAST Scan - OWASP ZAP') {
            steps {
                sh '''
                    docker run --rm --network=host \
                    -u $(id -u):$(id -g) \
                    -v $(pwd):/zap/wrk \
                    zaproxy/zap-stable \
                    zap-baseline.py \
                    -t http://10.40.0.242:8040/WebGoat \
                    -x zap-report.xml \
                    -I \
                    --autooff
                '''
            }
        }

        stage('Upload DAST to DefectDojo') {
            steps {
                withCredentials([string(credentialsId: 'DEFECTDOJO_API_KEY', variable: 'DD_API_KEY')]) {
                    sh '''
                        curl -X POST "http://10.40.0.242:8081/api/v2/import-scan/" \
                        -H "Authorization: Token $DD_API_KEY" \
                        -F "scan_type=ZAP Scan" \
                        -F "file=@zap-report.xml" \
                        -F "engagement=1" \
                        -F "minimum_severity=Low"
                    '''
                }
            }
        }

    }
}

post {
    always {
        archiveArtifacts artifacts: '''
            zap-report.html,
            dependency-check-report/dependency-check-report.html,
            trivy-report.json
        ''', fingerprint: true
    }
}

