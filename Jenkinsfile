pipeline {
    agent any

    environment {
        FRONTEND_DIR = 'quizWeb'          // Angular frontend folder
        BACKEND_DIR = 'quizServer'        // Spring Boot backend folder
        TOMCAT_HOME = 'D:\\CICD\\apache-tomcat-9.0.108'
        FRONTEND_DEPLOY_DIR = "${TOMCAT_HOME}\\webapps\\quizWeb"
        BACKEND_DEPLOY_DIR = "${TOMCAT_HOME}\\webapps\\quizServer"
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/harpita9u/online-exam.git'  // your GitHub repo URL
            }
        }

        stage('Build Frontend') {
            steps {
                dir("${FRONTEND_DIR}") {
                    bat 'npm install'
                    bat 'npm run build'
                }
            }
        }

        stage('Deploy Frontend') {
            steps {
                bat "rmdir /S /Q \"${FRONTEND_DEPLOY_DIR}\" || exit 0"
                bat "mkdir \"${FRONTEND_DEPLOY_DIR}\""
                bat "xcopy /E /I /Y ${FRONTEND_DIR}\\dist\\* \"${FRONTEND_DEPLOY_DIR}\""
            }
        }

        stage('Build Backend') {
            steps {
                dir("${BACKEND_DIR}") {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Deploy Backend') {
            steps {
                bat "rmdir /S /Q \"${BACKEND_DEPLOY_DIR}\" || exit 0"
                bat "mkdir \"${BACKEND_DEPLOY_DIR}\""
                bat "xcopy /E /I /Y ${BACKEND_DIR}\\target\\*.jar \"${BACKEND_DEPLOY_DIR}\""
            }
        }

        stage('Restart Tomcat') {
            steps {
                bat "net stop Tomcat9"
                bat "net start Tomcat9"
            }
        }
    }

    post {
        success {
            echo 'Deployment succeeded!'
        }
        failure {
            echo 'Deployment failed. Check console output for errors.'
        }
    }
}
