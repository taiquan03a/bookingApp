pipeline {
	agent any

    environment {
		IMAGE_NAME = 'springboot-app'
        CONTAINER_NAME = 'springboot-container'
    }

    stages {
		stage('Clone Repository') {
			steps {
				git branch: 'main', url: 'https://github.com/taiquan03a/bookingApp.git'
            }
        }

        stage('Build Application') {
			steps {
				sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
			steps {
				sh 'docker build -t $IMAGE_NAME .'
            }
        }

        stage('Run Docker Container') {
			steps {
				sh 'docker-compose down'
                sh 'docker-compose up -d'
            }
        }
    }
}
