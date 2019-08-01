pipeline {
    agent {
        docker {
            image 'adoptopenjdk/openjdk11:x86_64-debian-jdk-11.0.4_11'
            args '-v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock -u root --privileged'
        }
    }

    stages {
        stage('Verify Environment') {
            steps {
//                sh "cat /etc/os-release"
                sh "uname -a"
                sh "whoami"

                sh "java --version"
                sh "docker version"

                sh "docker pull alpine:3.5"
                sh "docker images"
            }
        }
        stage('Pre build') {
            steps {
                sh "ip route|awk '/default/ { print \$3 }'"
                sh 'docker run --rm alpine:3.5 sh -c "ip route|awk \'/default/ { print \\$3 }\'"'

                sh 'docker run -t --rm -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock ' +
                        'gradle:5.5.1-jdk11 ' +
                        'sh -c "docker run -t --rm alpine:3.5 sh -c \\"ip route | awk \'/default/ { print \\\\\\$3 }\'\\""'

            }
        }
        stage('Test') {
            steps {
                // low logging level
                sh './gradlew testClasses --quiet -Dorg.gradle.internal.launcher.welcomeMessageEnabled=false'

                // high logging level only for this task
                sh './gradlew test --info'
            }
        }
    }
}
