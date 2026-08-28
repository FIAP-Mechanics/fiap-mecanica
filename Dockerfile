# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-jammy AS build

ARG SERVICE

WORKDIR /workspace

COPY . .

RUN --mount=type=cache,target=/root/.m2 case "${SERVICE}" in \
        cliente|veiculo|funcionario|servico|estoque|atendimento) ;; \
        *) echo "SERVICE inválido: ${SERVICE}" >&2; exit 1 ;; \
    esac \
    && chmod +x mvnw \
    && ./mvnw \
        -pl "services/${SERVICE}" \
        -am package -DskipTests

FROM eclipse-temurin:21-jre-jammy

ARG SERVICE

RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home --uid 10001 app

WORKDIR /app

COPY --from=build --chown=app:app /workspace/services/${SERVICE}/target/*.jar /app/app.jar

USER app

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
