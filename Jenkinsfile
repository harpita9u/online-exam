pipeline {
    agent any

    environment {
        FRONTEND_DIR = 'frontend'
        BACKEND_DIR = 'backend'
        TOMCAT_HOME = 'D:\\CICD\\apache-tomcat-9.0.108'
        FRONTEND_DEPLOY_DIR = "${TOMCAT_HOME}\\webapps\\frontend"
        BACKEND_DEPLOY_DIR = "${TOMCAT_HOME}\\webapps\\backend"
        BACKEND_JAR = 'backend.jar'
    }

    stages {

        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/harpita9u/online-exam.git', branch: 'main'
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
                bat """
                rmdir /S /Q "${FRONTEND_DEPLOY_DIR}"
                mkdir "${FRONTEND_DEPLOY_DIR}"
                xcopy /E /I /Y ${FRONTEND_DIR}\\dist\\* "${FRONTEND_DEPLOY_DIR}"
                """
            }
        }

        stage('Build Backend') {
            steps {
                dir("${BACKEND_DIR}") {
                    bat 'mvn clean install'
                }
            }
        }

        stage('Deploy Backend') {
            steps {
                bat """
                rmdir /S /Q "${BACKEND_DEPLOY_DIR}"
                mkdir "${BACKEND_DEPLOY_DIR}"
                xcopy /E /I /Y ${BACKEND_DIR}\\target\\*.war "${BACKEND_DEPLOY_DIR}"
                """
            }
        }

        stage('Restart Tomcat') {
            steps {
                bat """
                net stop Tomcat9
                net start Tomcat9
                """
            }
        }
    }

    post {
        success {
            echo 'Deployment completed successfully!'
        }
        failure {
            echo 'Deployment failed. Check console output for errors.'
        }
    }
}
