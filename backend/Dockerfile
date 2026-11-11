# Two stages: the running image gets the jar and a JRE, not Maven, the source or a compiler.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Dependencies get their own layer so a source edit does not re-download everything.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S -g 1000 sitescribe && adduser -S -u 1000 -G sitescribe sitescribe

# Local storage mode writes voice notes here. On a free host this disk is wiped on every
# restart, which is why the live config points STORAGE_MODE at Supabase instead.
RUN mkdir -p /app/uploads && chown sitescribe:sitescribe /app/uploads
USER 1000

WORKDIR /app
COPY --from=build --chown=sitescribe:sitescribe /build/target/sitescribe-backend-*.jar app.jar

EXPOSE 8090

# Size the heap from the container's memory, not the host's, so the platform does not
# kill the process without saying why.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENV UPLOADS_DIR=/app/uploads

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar \"$@\"", "--"]
