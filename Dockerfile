# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY . .

RUN --mount=type=cache,target=/root/.m2 chmod +x mvnw \
    && ./mvnw \
        -pl services/cliente,services/veiculo,services/funcionario,services/servico,services/estoque,services/atendimento \
        -am package -DskipTests

FROM eclipse-temurin:21-jre-jammy

ARG SERVICE

RUN useradd --system --create-home --uid 10001 app

WORKDIR /app

COPY --from=build --chown=app:app /workspace/services/${SERVICE}/target/*.jar /app/app.jar

USER app

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
