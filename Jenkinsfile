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

        // stage('Secrets Scan - TruffleHog (Repo)') {
        //     steps {
        //         sh '''
        //         docker run --rm \
        //           -v "$(pwd):/repo" \
        //           trufflesecurity/trufflehog:latest \
        //           filesystem /repo \
        //           --fail
        //         '''
        //     }
        // }


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

        // stage('SCA - OWASP Dependency Check') {
        //     steps {
        //         dependencyCheck additionalArguments: """
        //         --scan .
        //         --format HTML
        //         --out dependency-check-report
        //         --nvdApiKey ${NVD_API_KEY}
        //         --failOnCVSS 5
        //         """, odcInstallation: 'dependency-check'
        //     }
        // }

        // stage('SAST - Semgrep Scan') {
        //     steps {
        //         sh 'pip3 install semgrep'
        //         sh ' semgrep scan --config auto --severity ERROR'
        //     }
        // }
        
        stage('Docker Build') {
            steps {
                sh 'docker build -t nithinragesh/webgoat:latest .'
            }
        }

        // stage('Scan Image') {
        //     steps {
        //         sh '''
        //             trivy image \
        //             --severity CRITICAL,HIGH \
        //             --exit-code 1 \
        //             --format json \
        //             --output trivy-report.json \
        //             nithinragesh/webgoat:latest
        //         '''
        //     }
        // }

        // stage('Secrets Scan - TruffleHog (Docker Image)') {
        //     steps {
        //         sh '''
        //         docker run --rm \
        //           -v /var/run/docker.sock:/var/run/docker.sock \
        //           trufflesecurity/trufflehog:latest \
        //           docker nithinragesh/webgoat:latest \
        //           --fail
        //         '''
        //     }
        // }


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

        // stage('DAST Scan - OWASP ZAP') {
        //     steps {
        //         sh '''
        //             echo "Waiting for WebGoat to be ready..."
        //             for i in {1..30}; do
        //               if curl -s http://localhost:8040/WebGoat > /dev/null; then
        //                 echo "WebGoat is UP"
        //                 break
        //               fi
        //               echo "WebGoat not ready yet... retrying"
        //               sleep 5
        //             done
            
        //             docker run --rm --network=host \
        //               zaproxy/zap-stable \
        //               zap-baseline.py \
        //               -t http://localhost:8040/WebGoat \
        //               -r zap-report.html \
        //               -I
        //         '''
        //     }
        // }


    }
}

post {
    always {
        archiveArtifacts artifacts: '''
            dependency-check-report/dependency-check-report.html,
            trivy-report.json
        ''', fingerprint: true
    }
}


