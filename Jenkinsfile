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
            choices: ['ALL', 'CUSTOM', 'UI_REGRESSION', 'API_REGRESSION'],
            description: 'ALL=@ui or @api. CUSTOM=use TAGS. UI_REGRESSION=@ui and @regression (chrome+firefox). API_REGRESSION=@api and @regression (once).'
        )
        string(
            name: 'TAGS',
            defaultValue: '@ui or @api',
            description: 'Used when RUN_MODE=CUSTOM'
        )
        choice(
            name: 'BROWSERS',
            choices: ['chrome', 'firefox', 'both'],
            description: 'Used for UI runs (CUSTOM/UI_REGRESSION). API ignores this.'
        )
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
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

                  label_json() {
                    FILE="$1"
                    LABEL="$2"
                    if [ -f "$FILE" ]; then
                      python3 - <<'PY' "$FILE" "$LABEL"
import json, sys
path = sys.argv[1]
label = sys.argv[2]
with open(path, "r", encoding="utf-8") as f:
    data = json.load(f)

# Cucumber JSON is typically a list of "feature" objects with "name"
if isinstance(data, list):
    for feat in data:
        if isinstance(feat, dict) and "name" in feat and isinstance(feat["name"], str):
            feat["name"] = f"{feat['name']} [{label}]"

with open(path, "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False)
PY
                    fi
                  }

                  run_once() {
                    SUFFIX="$1"; TAGS="$2"; SELENIUM_IMAGE="$3"; BROWSER="$4"; LABEL="$5"

                    export COMPOSE_PROJECT_NAME="missionqa-${BUILD_NUMBER}-${SUFFIX}"

                    docker compose -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true

                    docker compose -p "$COMPOSE_PROJECT_NAME" build

                    TAGS="$TAGS" BROWSER="$BROWSER" SELENIUM_IMAGE="$SELENIUM_IMAGE" \
                      docker compose -p "$COMPOSE_PROJECT_NAME" up --abort-on-container-exit

                    mkdir -p "artifacts/$SUFFIX"

                    # Move report files into per-run folder
                    if [ -f artifacts/cucumber.html ]; then
                      mv artifacts/cucumber.html "artifacts/$SUFFIX/cucumber.html"
                    fi
                    if [ -f artifacts/cucumber.json ]; then
                      mv artifacts/cucumber.json "artifacts/$SUFFIX/cucumber.json"
                      label_json "artifacts/$SUFFIX/cucumber.json" "$LABEL"
                    fi

                    docker compose -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true
                  }

                  # 1) API regression once
                  run_once "api" "@api and @regression" "selenium/standalone-chrome:latest" "chromeheadless" "API"

                  # 2) UI regression chrome + firefox
                  run_once "ui-chrome" "@ui and @regression" "selenium/standalone-chrome:latest" "chromeheadless" "UI Chrome"
                  run_once "ui-firefox" "@ui and @regression" "selenium/standalone-firefox:latest" "firefoxheadless" "UI Firefox"
                '''
            }
        }

        stage('Manual') {
            when { not { triggeredBy 'TimerTrigger' } }
            steps {
                sh '''
                  set -e

                  label_json() {
                    FILE="$1"
                    LABEL="$2"
                    if [ -f "$FILE" ]; then
                      python3 - <<'PY' "$FILE" "$LABEL"
import json, sys
path = sys.argv[1]
label = sys.argv[2]
with open(path, "r", encoding="utf-8") as f:
    data = json.load(f)
if isinstance(data, list):
    for feat in data:
        if isinstance(feat, dict) and "name" in feat and isinstance(feat["name"], str):
            feat["name"] = f"{feat['name']} [{label}]"
with open(path, "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False)
PY
                    fi
                  }

                  run_once() {
                    SUFFIX="$1"; TAGS="$2"; SELENIUM_IMAGE="$3"; BROWSER="$4"; LABEL="$5"

                    export COMPOSE_PROJECT_NAME="missionqa-${BUILD_NUMBER}-${SUFFIX}"

                    docker compose -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true

                    docker compose -p "$COMPOSE_PROJECT_NAME" build

                    TAGS="$TAGS" BROWSER="$BROWSER" SELENIUM_IMAGE="$SELENIUM_IMAGE" \
                      docker compose -p "$COMPOSE_PROJECT_NAME" up --abort-on-container-exit

                    mkdir -p "artifacts/$SUFFIX"

                    if [ -f artifacts/cucumber.html ]; then
                      mv artifacts/cucumber.html "artifacts/$SUFFIX/cucumber.html"
                    fi
                    if [ -f artifacts/cucumber.json ]; then
                      mv artifacts/cucumber.json "artifacts/$SUFFIX/cucumber.json"
                      label_json "artifacts/$SUFFIX/cucumber.json" "$LABEL"
                    fi

                    docker compose -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true
                  }

                  MODE="${RUN_MODE}"
                  TAG_EXPR="${TAGS}"

                  if [ "$MODE" = "ALL" ]; then
                    TAG_EXPR='@ui or @api'
                  elif [ "$MODE" = "UI_REGRESSION" ]; then
                    TAG_EXPR='@ui and @regression'
                  elif [ "$MODE" = "API_REGRESSION" ]; then
                    TAG_EXPR='@api and @regression'
                  fi

                  # If API-only expression (no @ui)
                  if echo "$TAG_EXPR" | grep -q '@api' && ! echo "$TAG_EXPR" | grep -q '@ui'; then
                    run_once "manual-api" "$TAG_EXPR" "selenium/standalone-chrome:latest" "chromeheadless" "API"
                    exit 0
                  fi

                  # UI runs
                  if [ "${BROWSERS}" = "both" ]; then
                    run_once "manual-ui-chrome" "$TAG_EXPR" "selenium/standalone-chrome:latest" "chromeheadless" "UI Chrome"
                    run_once "manual-ui-firefox" "$TAG_EXPR" "selenium/standalone-firefox:latest" "firefoxheadless" "UI Firefox"
                  elif [ "${BROWSERS}" = "firefox" ]; then
                    run_once "manual-ui-firefox" "$TAG_EXPR" "selenium/standalone-firefox:latest" "firefoxheadless" "UI Firefox"
                  else
                    run_once "manual-ui-chrome" "$TAG_EXPR" "selenium/standalone-chrome:latest" "chromeheadless" "UI Chrome"
                  fi
                '''
            }
        }
    }

    post {
        always {
            sh 'echo "=== DEBUG artifacts ===" && find artifacts -maxdepth 3 -type f -name "cucumber.*" -print || true'

            // Aggregate ALL JSON files across run folders
            cucumber(fileIncludePattern: 'artifacts/**/cucumber.json')

            // Publish HTML links (allowMissing avoids failures when a suite wasn't run)
            publishHTML([reportName: 'API HTML',        reportDir: 'artifacts/api',        reportFiles: 'cucumber.html', keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true])
            publishHTML([reportName: 'UI Chrome HTML',  reportDir: 'artifacts/ui-chrome',  reportFiles: 'cucumber.html', keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true])
            publishHTML([reportName: 'UI Firefox HTML', reportDir: 'artifacts/ui-firefox', reportFiles: 'cucumber.html', keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true])
            publishHTML([reportName: 'Manual HTML',     reportDir: 'artifacts',            reportFiles: '**/cucumber.html', keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true])
        }
    }
}
