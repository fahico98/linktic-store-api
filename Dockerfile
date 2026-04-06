FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# ARG JAR_FILE=./target/linktic-store-0.0.1.jar
# COPY ${JAR_FILE} app-linktic-store.jar

# Al estar el contexto en ./backend, Docker busca mvnw en esa carpeta
COPY mvnw ./
COPY .mvn/ .mvn/
COPY pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src ./src

EXPOSE 8080
CMD ["./mvnw", "spring-boot:run"]