pipeline {
	agent any

    environment {
		AZURE_REGISTRY = "bookingspring.azurecr.io"
        IMAGE_NAME = "bookingspringboot"
        IMAGE_TAG = "development"
        AZURE_WEB_APP = "api-booking-app"
        RESOURCE_GROUP = "booking"
    }

    stages {
		stage('Clone Code') {
			steps {
				git branch: 'main', url: 'https://github.com/taiquan03a/bookingApp.git'
            }
        }
        stage('Login to Azure Container Registry') {
			steps {
				withCredentials([usernamePassword(credentialsId: 'fc2f7088-cccb-4ab5-aaeb-f47511b32693', usernameVariable: 'ACR_USER', passwordVariable: 'ACR_PASS')]) {
					sh "docker login ${AZURE_REGISTRY} -u ${ACR_USER} -p ${ACR_PASS}"
                }
            }
        }
		stage('Build & Push Docker Image') {
			steps {
				sh """
                    docker build -t ${AZURE_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} .
                    docker push ${AZURE_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                """
            }
        }

        stage('Deploy to Azure App Service') {
			steps {
				withCredentials([usernamePassword(credentialsId: '630d5656-9bfd-434e-85e4-c33a08815d6e', usernameVariable: 'AZURE_CLIENT_ID', passwordVariable: 'AZURE_CLIENT_SECRET')]) {
					sh """
                        az login --username ${AZURE_CLIENT_ID} --password ${AZURE_CLIENT_SECRET}
                        az webapp config container set --name ${AZURE_WEB_APP} --resource-group ${RESOURCE_GROUP} --docker-custom-image-name ${AZURE_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                        az webapp restart --name ${AZURE_WEB_APP} --resource-group ${RESOURCE_GROUP}
                    """
                }
            }
        }
    }

    post {
		success {
			echo 'Deployment thành công trên Azure!'
        }
        failure {
			echo 'Deployment thất bại!'
        }
    }
}
