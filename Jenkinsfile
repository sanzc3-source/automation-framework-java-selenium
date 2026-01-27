pipeline {
    agent any

    triggers {
        cron('* * * * *')
    }

    parameters {
        string(name: 'TAGS', defaultValue: '@ui or @api', description: 'Cucumber tag expression (ex: @ui, @api, @smoke, @regression, @ui and not @wip)')
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
            // Jenkins "Cucumber Test Result" (reads cucumber.json)
            cucumber(
                fileIncludePattern: 'artifacts/cucumber.json'
            )

            // Keep the HTML file link too
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
