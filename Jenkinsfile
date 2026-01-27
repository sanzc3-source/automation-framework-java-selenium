pipeline {
    agent any

    triggers {
        cron('1 0 * * *')  // 12:01 AM daily
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        choice(
            name: 'RUN_MODE',
            choices: ['UI_REGRESSION', 'API_REGRESSION', 'CUSTOM', 'ALL'],
            description: 'UI_REGRESSION=@ui and @regression (chrome+firefox nightly; manual uses BROWSERS). ' +
                         'API_REGRESSION=@api and @regression (once). ' +
                         'CUSTOM=run TAGS (must be UI-only OR API-only). ' +
                         'ALL=run UI (@ui) + API (@api).'
        )
        string(
            name: 'TAGS',
            defaultValue: '@ui and @regression',
            description: 'Only used when RUN_MODE=CUSTOM. Must be UI-only (contains @ui) OR API-only (contains @api).'
        )
        choice(
            name: 'BROWSERS',
            choices: ['both', 'chrome', 'firefox'],
            description: 'Used for UI runs (UI_REGRESSION or CUSTOM UI-only). API ignores this.'
        )
    }

    stages {

        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Prep Workspace (clean artifacts)') {
            steps {
                sh '''
                  set -e
                  # HARD RESET so reports never leak across builds
                  rm -rf artifacts
                  mkdir -p artifacts
                '''
            }
        }

        stage('Nightly (cron)') {
            when { triggeredBy 'TimerTrigger' }
            steps {
                sh '''
                  set -e

                  run_once() {
                    SUFFIX="$1"; TAGS="$2"; SELENIUM_IMAGE="$3"; BROWSER="$4"

                    export COMPOSE_PROJECT_NAME="missionqa-${BUILD_NUMBER}-${SUFFIX}"

                    # Clean any leftovers for this compose project
                    docker compose -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true

                    # Clean runner outputs (compose mounts ./artifacts -> /app/artifacts)
                    rm -f artifacts/cucumber.html artifacts/cucumber.json || true

                    docker compose -p "$COMPOSE_PROJECT_NAME" build

                    TAGS="$TAGS" BROWSER="$BROWSER" SELENIUM_IMAGE="$SELENIUM_IMAGE" \
                      docker compose -p "$COMPOSE_PROJECT_NAME" up --abort-on-container-exit

                    # Standardized output folders (always same names)
                    mkdir -p "artifacts/$SUFFIX"
                    mv artifacts/cucumber.html "artifacts/$SUFFIX/cucumber.html" || true
                    mv artifacts/cucumber.json "artifacts/$SUFFIX/cucumber.json" || true

                    docker compose -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true
                  }

                  # API regression once
                  run_once "api" "@api and @regression" "selenium/standalone-chrome:latest" "chromeheadless"

                  # UI regression both browsers
                  run_once "ui-chrome"  "@ui and @regression" "selenium/standalone-chrome:latest"  "chromeheadless"
                  run_once "ui-firefox" "@ui and @regression" "selenium/standalone-firefox:latest" "firefoxheadless"
                '''
            }
        }

        stage('Manual (Build with Parameters)') {
            when { not { triggeredBy 'TimerTrigger' } }
            steps {
                sh '''
                  set -e

                  run_once() {
                    SUFFIX="$1"; TAGS="$2"; SELENIUM_IMAGE="$3"; BROWSER="$4"

                    export COMPOSE_PROJECT_NAME="missionqa-${BUILD_NUMBER}-${SUFFIX}"

                    docker compose -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true
                    rm -f artifacts/cucumber.html artifacts/cucumber.json || true

                    docker compose -p "$COMPOSE_PROJECT_NAME" build

                    TAGS="$TAGS" BROWSER="$BROWSER" SELENIUM_IMAGE="$SELENIUM_IMAGE" \
                      docker compose -p "$COMPOSE_PROJECT_NAME" up --abort-on-container-exit

                    mkdir -p "artifacts/$SUFFIX"
                    mv artifacts/cucumber.html "artifacts/$SUFFIX/cucumber.html" || true
                    mv artifacts/cucumber.json "artifacts/$SUFFIX/cucumber.json" || true

                    docker compose -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true
                  }

                  MODE="${RUN_MODE}"

                  # ---- Decide what to run ----
                  if [ "$MODE" = "UI_REGRESSION" ]; then
                    UI_TAGS='@ui and @regression'
                    if [ "${BROWSERS}" = "both" ]; then
                      run_once "ui-chrome"  "$UI_TAGS" "selenium/standalone-chrome:latest"  "chromeheadless"
                      run_once "ui-firefox" "$UI_TAGS" "selenium/standalone-firefox:latest" "firefoxheadless"
                    elif [ "${BROWSERS}" = "firefox" ]; then
                      run_once "ui-firefox" "$UI_TAGS" "selenium/standalone-firefox:latest" "firefoxheadless"
                    else
                      run_once "ui-chrome" "$UI_TAGS" "selenium/standalone-chrome:latest" "chromeheadless"
                    fi
                    exit 0
                  fi

                  if [ "$MODE" = "API_REGRESSION" ]; then
                    run_once "api" "@api and @regression" "selenium/standalone-chrome:latest" "chromeheadless"
                    exit 0
                  fi

                  if [ "$MODE" = "ALL" ]; then
                    # Run UI (@ui) and API (@api) as two separate runs (clean separation, clean reports)
                    if [ "${BROWSERS}" = "both" ]; then
                      run_once "ui-chrome"  "@ui" "selenium/standalone-chrome:latest"  "chromeheadless"
                      run_once "ui-firefox" "@ui" "selenium/standalone-firefox:latest" "firefoxheadless"
                    elif [ "${BROWSERS}" = "firefox" ]; then
                      run_once "ui-firefox" "@ui" "selenium/standalone-firefox:latest" "firefoxheadless"
                    else
                      run_once "ui-chrome" "@ui" "selenium/standalone-chrome:latest" "chromeheadless"
                    fi

                    run_once "api" "@api" "selenium/standalone-chrome:latest" "chromeheadless"
                    exit 0
                  fi

                  # CUSTOM mode: must be UI-only OR API-only (no mixed expressions)
                  TAG_EXPR="${TAGS}"

                  HAS_UI=false
                  HAS_API=false
                  echo "$TAG_EXPR" | grep -q '@ui'  && HAS_UI=true || true
                  echo "$TAG_EXPR" | grep -q '@api' && HAS_API=true || true

                  if [ "$HAS_UI" = "true" ] && [ "$HAS_API" = "true" ]; then
                    echo "ERROR: CUSTOM TAGS cannot include both @ui and @api. Use RUN_MODE=ALL instead."
                    exit 2
                  fi

                  if [ "$HAS_API" = "true" ]; then
                    run_once "api" "$TAG_EXPR" "selenium/standalone-chrome:latest" "chromeheadless"
                    exit 0
                  fi

                  if [ "$HAS_UI" = "true" ]; then
                    if [ "${BROWSERS}" = "both" ]; then
                      run_once "ui-chrome"  "$TAG_EXPR" "selenium/standalone-chrome:latest"  "chromeheadless"
                      run_once "ui-firefox" "$TAG_EXPR" "selenium/standalone-firefox:latest" "firefoxheadless"
                    elif [ "${BROWSERS}" = "firefox" ]; then
                      run_once "ui-firefox" "$TAG_EXPR" "selenium/standalone-firefox:latest" "firefoxheadless"
                    else
                      run_once "ui-chrome" "$TAG_EXPR" "selenium/standalone-chrome:latest" "chromeheadless"
                    fi
                    exit 0
                  fi

                  echo "ERROR: CUSTOM TAGS must include @ui or @api."
                  exit 3
                '''
            }
        }
    }

    post {
        always {
            sh 'echo "=== DEBUG artifacts ===" && find artifacts -maxdepth 3 -type f -name "cucumber.*" -print || true'

            // Aggregate JSON files that exist in THIS build only (no leak because we wipe artifacts every build)
            cucumber(fileIncludePattern: 'artifacts/**/cucumber.json')

            // Clean, obvious HTML links (only show what exists)
            publishHTML([reportName: 'API HTML',        reportDir: 'artifacts/api',        reportFiles: 'cucumber.html', keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true])
            publishHTML([reportName: 'UI Chrome HTML',  reportDir: 'artifacts/ui-chrome',  reportFiles: 'cucumber.html', keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true])
            publishHTML([reportName: 'UI Firefox HTML', reportDir: 'artifacts/ui-firefox', reportFiles: 'cucumber.html', keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true])
        }
    }
}
