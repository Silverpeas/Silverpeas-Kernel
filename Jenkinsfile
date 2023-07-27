import java.util.regex.Matcher

pipeline {
  environment {
    version = null
  }
  agent {
    docker {
      image 'silverpeas/silverbuild'
      args '''
        -v $HOME/.m2:/home/silverbuild/.m2 
        -v $HOME/.gitconfig:/home/silverbuild/.gitconfig 
        -v $HOME/.ssh:/home/silverbuild/.ssh 
        -v $HOME/.gnupg:/home/silverbuild/.gnupg
        '''
    }
  }
  stages {
    stage('Build') {
      steps {
        script {
          version = computeSnapshotVersion()
          sh """
            mvn -U versions:set -DgenerateBackupPoms=false -DnewVersion=${version}
            mvn clean install -Pdeployment -Djava.awt.headless=true -Dcontext=ci
            """
        }
      }
    }
    stage('Quality Analysis of the project') {
      // quality analyse with our SonarQube service is performed only for PR against our main
      // repository and for main branch
      when {
        expression {
          env.BRANCH_NAME == 'main' && env.GIT_URL.startsWith('https://github.com/Silverpeas')
        }
      }
      steps {
        script {
          withSonarQubeEnv {
            sh """
                mvn ${SONAR_MAVEN_GOAL} -Dsonar.projectKey=Silverpeas_Silverpeas-Kernel \\
                  -Dsonar.organization=silverpeas \\
                  -Dsonar.branch.name=${env.BRANCH_NAME} \\
                  -Dsonar.host.url=${SONAR_HOST_URL} \\
                  -Dsonar.login=${SONAR_AUTH_TOKEN}
                """
          }
          timeout(time: 30, unit: 'MINUTES') {
            // Just in case something goes wrong, pipeline will be killed after a timeout
            def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
            if (qg.status != 'OK' && qg.status != 'WARNING') {
              error "Pipeline aborted due to quality gate failure: ${qg.status}"
            }
          }
        }
      }
    }
    stage('Quality Analysis of the PR') {
      // quality analyse with our SonarQube service is performed only for PR against our main
      // repository and for main branch
      when {
        expression {
          env.BRANCH_NAME.startsWith('PR')  && env.CHANGE_URL?.startsWith('https://github.com/Silverpeas')
        }
      }
      steps {
        script {
          withSonarQubeEnv {
            sh """
                mvn ${SONAR_MAVEN_GOAL} -Dsonar.projectKey=Silverpeas_Silverpeas-Kernel \\
                  -Dsonar.organization=silverpeas \\
                  -Dsonar.pullrequest.branch=${env.BRANCH_NAME} \\
                  -Dsonar.pullrequest.key=${env.CHANGE_ID} \\
                  -Dsonar.pullrequest.base=main \\
                  -Dsonar.pullrequest.provider=github \\
                  -Dsonar.host.url=${SONAR_HOST_URL} \\
                  -Dsonar.login=${SONAR_AUTH_TOKEN}
                """
          }
          timeout(time: 30, unit: 'MINUTES') {
            // Just in case something goes wrong, pipeline will be killed after a timeout
            def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
            if (qg.status != 'OK' && qg.status != 'WARNING') {
              error "Pipeline aborted due to quality gate failure: ${qg.status}"
            }
          }
        }
      }
    }
  }
  post {
    always {
      step([$class                  : 'Mailer',
            notifyEveryUnstableBuild: true,
            recipients              : "miguel.moquillon@silverpeas.org, yohann.chastagnier@silverpeas.org",
            sendToIndividuals       : true])
    }
  }
}

def computeSnapshotVersion() {
  def pom = readMavenPom()
  final String version = pom.version
  final String defaultVersion = env.BRANCH_NAME == 'main' ? version :
      env.BRANCH_NAME.toLowerCase().replaceAll('[# -]', '')
  Matcher m = env.CHANGE_TITLE =~ /^(Bug #?\d+|Feature #?\d+).*$/
  String snapshot = m.matches()
      ? m.group(1).toLowerCase().replaceAll(' #?', '')
      : ''
  if (snapshot.isEmpty()) {
    m = env.CHANGE_TITLE =~ /^\[([^\[\]]+)].*$/
    snapshot = m.matches()
        ? m.group(1).toLowerCase().replaceAll('[/><|:&?!;,*%$=}{#~\'"\\\\°)(\\[\\]]', '').trim().replaceAll('[ @]', '-')
        : ''
  }
  return snapshot.isEmpty() ? defaultVersion : "${pom.properties['next.release']}-${snapshot}"
}

