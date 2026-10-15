# syntax=docker/dockerfile:1

# ---------- 1º estágio: compila e roda os testes ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

# "package" compila, roda os testes (com H2 em memória, sem precisar do Postgres) e gera o jar.
# Se algum teste falhar, o build da imagem para aqui.
# O cache do ~/.m2 é reaproveitado entre builds, então as dependências só baixam na 1ª vez.
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -B package && cp target/*.jar app.jar

# ---------- 2º estágio: só o necessário para rodar ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Não roda como root dentro do container
RUN addgroup -S app && adduser -S -G app app
USER app

# Do 1º estágio vem só o jar: nada de JDK, Maven, código-fonte ou dependências de teste
COPY --from=build /app/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
