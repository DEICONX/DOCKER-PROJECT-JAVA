# CI/CD Workflow Documentation

## **Project Overview**
This document explains the complete CI/CD pipeline for a Java application using:
- **AWS Ubuntu Server**
- **Docker** (SonarQube, Nexus, Jenkins)
- **Maven**
- ##### Tomcat
- **GitHub Actions**

---

## **Prerequisites**
AWS Ubuntu Server (EC2)![](Screenshot%202025-11-14%20134206.png)

--------

GitHub Repository with Java Code

![](Screenshot%202025-11-16%20160704.png)

---------------

Docker Installed

SonarQube, Nexus, Jenkins Docker Images

Maven Installed on Host

Docker Hub Account

GitHub Secrets Configured

---

## **Steps**

### **1. Launch AWS Ubuntu Server**

- Provision EC2 instance.
- SSH into server:
```bash
ssh -i <pem-file> ubuntu@<public-ip>
```

------

### **2. Clone GitHub Repository**

`git clone <your-repo-url>`

### **3. Update System and Install Docker**

```bash
sudo apt-get update
sudo apt-get install docker.io -y
```

![](Screenshot%202025-11-14%20134240.png)

### **4. Pull Required Docker Images**(From DOCKER HUB)

```bash
docker pull sonarqube
docker pull sonatype/nexus3
docker pull jenkins/jenkins
```

![](Screenshot%202025-11-14%20134605.png)

### **5. Create Containers**

Example:
```bash
docker run -dt --name sonarqube -p 9000:9000 sonarqube
docker run -dt --name nexus -p 8081:8081 sonatype/nexus3
docker run -dt --name jenkins -p 8080:8080 jenkins/jenkins
```

![](Screenshot%202025-11-14%20135118.png)

### **6. Install Maven**

```bash
sudo apt-get install maven -y
```

### **7. SonarQube Analysis**

Run scanner:
```bash
sonar-scanner   -Dsonar.projectKey=my-java-app   -Dsonar.sources=.   -Dsonar.host.url=http://<sonarqube-ip>:9000   -Dsonar.login=<sonar-token>
```

![](Screenshot%202025-11-14%20150340.png)

-----

Output of Sonarweb page Success!!

![](Screenshot%202025-11-14%20150444.png)

### **8. Build Java Project**

```bash
mvn clean install
```

![](Screenshot%202025-11-14%20152138.png)

----------------

Artifact is Generated!!!!

![](Screenshot%202025-11-14%20152157.png)

### **9. Upload Artifact to Nexus**

Configure `distributionManagement` in `pom.xml`:
```xml
<distributionManagement>
  <repository>
    <id>nexus</id>
    <url>http://<nexus-ip>:8081/repository/maven-releases/</url>
  </repository>
</distributionManagement>
```
Deploy:
```bash
mvn deploy
```

![](Screenshot%202025-11-14%20152617.png)

---------

Artifact uploaded in nexus!!!!

![](Screenshot%202025-11-14%20152632.png)

### **10. Create Dockerfile**

Example:
```dockerfile
FROM tomcat:9-jdk11

# Install curl
RUN apt-get update && apt-get install -y curl

# Clean default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Download WAR from Nexus
ADD nexus-credentials.txt /tmp/nexus-credentials.txt

RUN export NEXUS_USER=$(sed -n '1p' /tmp/nexus-credentials.txt) && \
    export NEXUS_PASS=$(sed -n '2p' /tmp/nexus-credentials.txt) && \
    curl -u $NEXUS_USER:$NEXUS_PASS \
    -o /usr/local/tomcat/webapps/ROOT.war \
    "http://34.232.252.221:8081/repository/maven-releases/com/example/motivator-region/2.0/motivator-region-2.0.war"

EXPOSE 8080

CMD ["catalina.sh", "run"]
```

![](Screenshot%202025-11-14%20154343.png)

-------------

Also create nexus-credentials.txt file and give nexus username and password

![](Screenshot%202025-11-14%20154402.png)

### **11. Build & Push Image**

```bash
docker build -t myapp:latest .
```

![](Screenshot%202025-11-14%20154414.png)

------

The Image is successfully build

![](Screenshot%202025-11-14%20154430.png)

------------

#### DOCKER HUB CREATION

In your browser open docker hub and signup

![](Screenshot%202025-11-14%20154613.png)

------------------

Now in your terminal user

`docker login`  to connect CLI and GUI 

Here you will get URL as shown in below image

 open the the URL give and be noted that your docker hub page and this URL should be in same web browser page

Copy the code

![](Screenshot%202025-11-14%20154939.png)

---------------

Paste the code here and select continue

Your docker CLI and docker hub are connected go to repo

and there generate a token/password in settings and copy that store it some where

![](Screenshot%202025-11-14%20155017.png)

--------------------

`docker tag myapp:latest <dockerhub-user>/myapp:latest
docker push <dockerhub-user>/myapp:latest`

Use the above commands to push your image in to docker hub

![](Screenshot%202025-11-14%20155530.png)

-----------

Go to your dockerhub repo you will find your image

![](Screenshot%202025-11-14%20155609.png)

-------------------

Now i will manually create the container with my image using

`docker run -dt --name TOMCAT -p 8082:8080 <image id> `

![](Screenshot%202025-11-14%20155720.png)

--------------

open Browser and run http://<your-server ip>:8082

I see the out put and my application is working successfully!!!![](Screenshot%202025-11-14%20155742.png)

-------------

In the above steps i have done everything manually now i will use my Jenkins container to automate every thing

## **Jenkins Configuration**

- Add credentials: Nexus, SonarQube token, Docker Hub.

  ![](Screenshot%202025-11-14%20162456.png)

- Install plugins: GitHub, Maven, Nexus, Docker, SonarQube Scanner.

  In manage settings-->plugins

- Configure SonarQube in **System Configuration**.

  In manage settings-->system

- Add Maven & JDK in **Global Tool Configuration**.

  In manage settings-->tools

  ------

  Before creating a new item we must ensure 2 things

  Inside your Jenkins container 

  login as root

  `docker exec -it --user root JENKINS /bin/bash`

  Here install maven and docker (your jenkins should talk with your docker deamon)

  use:

   `apt update && apt install maven -y`  , `apt install docker.io`

---

## **Jenkins Pipeline Script**

Create a New Item 

![](Screenshot%202025-11-14%20162803.png)
----------

Write Your Pipeline script

```groovy
pipeline {
    agent any

    tools {
        jdk 'JDK' // Your JDK installation name in Jenkins
        maven 'MAVEN' // Optional: If you have Maven tool configured
    }

    environment {
        JAVA_HOME = tool name: 'JDK', type: 'jdk'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"

        // SonarQube credential ID in Jenkins
        SONARQUBE = 'SONAR'

        // Docker Hub credential (username/password stored in Jenkins)
        DOCKER_HUB = credentials('docker_hub')
        
        // Nexus credentials
        NEXUS_CREDENTIAL_ID = 'nexus-cred'
        NEXUS_SERVER_ID = 'maven-releases'
        NEXUS_URL = 'http://34.232.252.221:8081/repository/maven-releases/'
    }

    stages {

        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/DEICONX/DOCKER-PROJECT-JAVA.git'
            }
        }

        stage('Build with Maven') {
            steps {
                dir('java-webapp-region') {
                    sh 'mvn clean package'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('java-webapp-region') {
                    withSonarQubeEnv('SONAR') {
                        sh """
                            mvn sonar:sonar \\
                            -Dsonar.projectKey=java-webapp-region \\
                            -Dsonar.host.url=http://34.232.252.221:9000
                        """
                    }
                }
            }
        }

        stage('Deploy to Nexus') {
            steps {
                dir('java-webapp-region') {
                    // Create a temporary Maven settings.xml with Nexus credentials
                    withCredentials([usernamePassword(
                        credentialsId: env.NEXUS_CREDENTIAL_ID, 
                        usernameVariable: 'NEXUS_USERNAME', 
                        passwordVariable: 'NEXUS_PASSWORD'
                    )]) {
                        writeFile file: 'temp-settings.xml', text: """
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">
  <servers>
    <server>
      <id>${env.NEXUS_SERVER_ID}</id>
      <username>${NEXUS_USERNAME}</username>
      <password>${NEXUS_PASSWORD}</password>
    </server>
  </servers>
</settings>
                        """
                        sh 'mvn deploy -s temp-settings.xml'
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                    docker build -t java-image java-webapp-region
                    docker tag java-image deiconx/java-image:latest
                """
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker_hub',
                    usernameVariable: 'DOCKER_HUB_USR',
                    passwordVariable: 'DOCKER_HUB_PSW'
                )]) {
                    sh """
                        echo ${DOCKER_HUB_PSW} | docker login -u ${DOCKER_HUB_USR} --password-stdin
                        docker push deiconx/java-image:latest
                    """
                }
            }
        }

        stage('Deploy to Tomcat') {
            steps {
                sh """
                    docker stop TOMCAT || true
                    docker rm TOMCAT || true
                    docker run -d --name TOMCAT -p 8082:8080 deiconx/java-image:latest
                """
            }
        }
    }

    post {
        always {
            echo "Pipeline finished."
        }
        success {
            echo "Pipeline succeeded! All steps completed."
        }
        failure {
            echo "Pipeline failed! Check logs for details."
        }
    }
}
```

Save and BUILD 

![](Screenshot%202025-11-14%20163342.png)

------------------------

Pipeline is succesfully build and the below is the pipeline overview

![](Screenshot%202025-11-15%20012039.png)

------------------

WE WILL NOW SEE THE OUTPUTS OF OUR APPLICATION

SONARQUBE OUTPUT:

![](Screenshot%202025-11-15%20012115.png)

------

TOMCAT OUTPUT:

![](Screenshot%202025-11-15%20012129.png)

-----

## **GitHub Actions CI/CD**

Till now we have used JENKINS now we will see our application deployment with GITHUB ACTIONS

Give your credentials

repo settings-->secrets and variables-->actions-->new repository secret

![](Screenshot%202025-11-15%20154545.png)

-----------------

Create `.github/workflows/ci-cd.yml`:
```yaml
name: Java CI-CD Pipeline

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    # ---------------------------
    # Checkout Code
    # ---------------------------
    - name: Checkout repository
      uses: actions/checkout@v3

    # ---------------------------
    # Set up JDK
    # ---------------------------
    - name: Set up Java 17
      uses: actions/setup-java@v3
      with:
        java-version: "17"
        distribution: "temurin"

    # ---------------------------
    # Build with Maven
    # ---------------------------
    - name: Build Maven Project
      working-directory: java-webapp-region
      run: mvn clean package

    # ---------------------------
    # SonarQube Scan
    # ---------------------------
    - name: SonarQube Analysis
      working-directory: java-webapp-region
      env:
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
      run: |
        mvn sonar:sonar \
        -Dsonar.projectKey=java-webapp-region \
        -Dsonar.host.url=http://34.232.252.221:9000 \
        -Dsonar.login=$SONAR_TOKEN

    # ---------------------------
    # Deploy to Nexus
    # ---------------------------
    - name: Deploy Artifact to Nexus
      working-directory: java-webapp-region
      env:
        NEXUS_USERNAME: ${{ secrets.NEXUS_USERNAME }}
        NEXUS_PASSWORD: ${{ secrets.NEXUS_PASSWORD }}
      run: |
        cat > settings.xml <<EOF
        <settings>
          <servers>
            <server>
              <id>maven-releases</id>
              <username>${NEXUS_USERNAME}</username>
              <password>${NEXUS_PASSWORD}</password>
            </server>
          </servers>
        </settings>
        EOF
        mvn deploy -s settings.xml

    # ---------------------------
    # Build Docker Image
    # ---------------------------
    - name: Build Docker image
      run: |
        docker build -t java-image java-webapp-region
        docker tag java-image ${{ secrets.DOCKER_HUB_USERNAME }}/java-image:latest

    # ---------------------------
    # Push to Docker Hub
    # ---------------------------
    - name: Login and Push Docker Image
      run: |
        echo "${{ secrets.DOCKER_HUB_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_HUB_USERNAME }}" --password-stdin
        docker push ${{ secrets.DOCKER_HUB_USERNAME }}/java-image:latest

    # ---------------------------
    # Deploy to Tomcat (SSH into your server)
    # ---------------------------
    - name: Deploy on Server (Docker Run)
      uses: appleboy/ssh-action@v1.0.0
      with:
        host: 34.232.252.221
        username: ubuntu
        key: ${{ secrets.SSH_PRIVATE_KEY }}
        script: |
          docker stop TOMCAT || true
          docker rm TOMCAT || true
          docker pull ${{ secrets.DOCKER_HUB_USERNAME }}/java-image:latest
          docker run -d --name TOMCAT -p 8082:8080 ${{ secrets.DOCKER_HUB_USERNAME }}/java-image:latest

```

![](Screenshot%202025-11-15%20155120.png)

-----------------

Pipeline is build successfully!!!!!!!!!!

![](Screenshot%202025-11-15%20155626.png)

## **Architecture Diagram**
```
[GitHub] --> [AWS Ubuntu Server] --> [Docker Containers: SonarQube | Nexus | Jenkins]
    |--> [Maven Build] --> [Nexus Artifact] --> [Dockerfile -> Tomcat]
    |--> [Docker Hub Image]
    |--> [GitHub Actions CI/CD]
```

---

## **Final Results**
- Jenkins pipeline executed successfully.
- SonarQube shows code quality.
- Nexus stores artifacts.
- Tomcat deployed app.
- Docker Hub image uploaded.
- GitHub Actions workflow successful.

