pipeline {
    agent any

    environment {
        TAGS = "@ui or @api"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Run Tests in Docker') {
            steps {
                sh '''
                  docker compose down || true
                  docker compose build
                  TAGS="${TAGS}" docker compose up --abort-on-container-exit

                  echo "=== DEBUG: artifacts directory ==="
                  ls -lah artifacts || true
                '''
            }
        }
    }

    post {
        always {
            publishHTML([
                reportName: 'Cucumber HTML Report',
                reportDir: 'artifacts',
                reportFiles: 'cucumber.html',
                keepAll: true,
                alwaysLinkToLastBuild: true,
                allowMissing: true
            ])
        }
        cleanup {
            sh 'docker compose down || true'
        }
    }
}
