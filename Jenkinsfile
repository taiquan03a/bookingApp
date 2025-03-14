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
				sh '''
                docker run --rm \
                    -v $PWD:/app \
                    -w /app \
                    maven:3.8.6 mvn clean package -DskipTests
                '''
            }
        }

        stage('Build Docker Image') {
			steps {
				sh 'docker build -t $IMAGE_NAME .'
            }
        }

        stage('Run Docker Container') {
			steps {
				sh 'docker-compose down || true'  // Nếu container chưa chạy, bỏ qua lỗi
                sh 'docker-compose up -d'
            }
        }
    }
}
