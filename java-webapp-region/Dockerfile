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
    "http://172.184.152.152:8081/repository/maven-releases/com/example/motivator-region/2.0/motivator-region-2.0.war"

EXPOSE 8080

CMD ["catalina.sh", "run"]
