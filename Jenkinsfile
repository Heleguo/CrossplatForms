pipeline {
    agent any
    tools {
        jdk 'Jdk17'
        gradle 'gradle'
    }
    stages {
        stage('Build') {
            steps {
                echo 'Building..'
                sh './gradlew clean build'
            }
        }
        stage('Post') {
            steps {
                archiveArtifacts 'target/CrossplatForms.jar'
                discordSend description: "**Build:** [${currentBuild.id}](${env.BUILD_URL})\n**Status:** [${currentBuild.currentResult}]" , footer: 'ProjectG', link: env.BUILD_URL, result: currentBuild.currentResult, title: "ProjectG-Plugins/CrossplatForms/${env.BRANCH_NAME}", webhookURL: "https://discord.com/api/webhooks/829602972098887720/kscr0LGNfA6cyYEtg0Gkfzu0gD4jmun6x-p3xPW2_xhH3BmOQD6ytc7jFx1j6cuTqlRq"

                  }

                }
        }
}
