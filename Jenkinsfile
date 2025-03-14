pipeline {
	agent any

    environment {
		IMAGE_NAME = 'bookingapp'
        DOCKER_HUB_USER = 'taiquan03a'
        DOCKER_HUB_REPO = "${DOCKER_HUB_USER}/${IMAGE_NAME}"
        DOCKER_TAG = 'latest'
        AZURE_WEBAPP_NAME = 'bookingapp'  // Tên Azure App Service
        RESOURCE_GROUP = 'bookingapp_group'
        DOCKER_HUB_CREDENTIALS = '7ea86a63-930b-4446-bdc5-439d002f1221' // Jenkins Credentials ID
        DOCKER_PASS = 'Taiquan123@'
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
				sh "docker build -t $DOCKER_HUB_REPO:$DOCKER_TAG ."
            }
        }

        stage('Push Docker Image to Docker Hub') {
			steps {
				withCredentials([usernamePassword(credentialsId: '7ea86a63-930b-4446-bdc5-439d002f1221', usernameVariable: $DOCKER_HUB_USER, passwordVariable: $DOCKER_PASS)]) {
					sh '''
                    echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                    docker push $DOCKER_HUB_REPO:$DOCKER_TAG
                    '''
                }
            }
        }

        stage('Deploy to Azure Web App') {
			steps {
				sh '''
                az webapp config container set --name $AZURE_WEBAPP_NAME --resource-group $RESOURCE_GROUP \
                --docker-custom-image-name $DOCKER_HUB_REPO:$DOCKER_TAG
                az webapp restart --name $AZURE_WEBAPP_NAME --resource-group $RESOURCE_GROUP
                '''
            }
        }
    }

    post {
		success {
			echo "Deployment successful!"
        }
        failure {
			echo "Deployment failed!"
        }
    }
}
