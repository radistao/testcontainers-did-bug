pipeline {
    agent {
        docker {
            image 'gradle:5.5.1-jdk11'
            args '-v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock -v $PWD:$PWD -w $PWD -u root --privileged'
        }
    }

    stages {
        stage('Install Tools') {
            steps {
                sh "cat /etc/os-release"
                sh "uname -a"
                sh "whoami"

                sh "java --version"
                sh "docker --version"

                sh "docker pull alpine:3.5"
                sh "docker images"
            }
        }
        stage('Test') {
            steps {
                sh './gradlew testClasses' // low logging level
                // high logging level only for this task
                sh './gradlew test --info'
            }
        }
    }
}
