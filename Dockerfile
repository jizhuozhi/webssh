FROM node:latest
WORKDIR /workspace/webssh/
COPY src/main/resources/webssh/package.json package.json
RUN npm install
COPY src/main/resources/webssh/ .
RUN npm run build

FROM maven:latest
WORKDIR /workspace/
COPY pom.xml pom.xml
RUN mvn dependency:resolve --batch-mode
COPY src src
COPY --from=0 /workspace/static/ src/main/resources/static
RUN mvn install --batch-mode
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM openjdk:latest
WORKDIR /app/
ARG DEPENDENCY=/workspace/target/dependency
COPY --from=1 ${DEPENDENCY}/BOOT-INF/lib ./lib
COPY --from=1 ${DEPENDENCY}/META-INF ./META-INF
COPY --from=1 ${DEPENDENCY}/BOOT-INF/classes .
COPY docker/entrypoint.sh .
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "entrypoint.sh"]
