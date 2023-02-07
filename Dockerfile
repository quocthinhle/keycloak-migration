# syntax=docker/dockerfile:1

ARG KEYCLOAK_VERSION=19.0.3
ARG PROVIDERS_VERSION=1

FROM maven:3.6.0-jdk-11-slim AS providers

ARG KEYCLOAK_VERSION
ARG PROVIDERS_VERSION

RUN mkdir /home/.m2
WORKDIR /home/.m2

COPY pom.xml /home/app/pom.xml
COPY src /home/app/src

RUN --mount=type=cache,target=/root/.m2 \
    cd /home/app \
    && mvn versions:set -DnewVersion=$KEYCLOAK_VERSION.$PROVIDERS_VERSION -Dkeycloak.version=$KEYCLOAK_VERSION \
    && mvn clean package -Dkeycloak.version=$KEYCLOAK_VERSION

FROM quay.io/keycloak/keycloak:${KEYCLOAK_VERSION} AS e2e_test

ARG KEYCLOAK_VERSION
ARG PROVIDERS_VERSION
ARG KC_HOME_DIR=/opt/keycloak

COPY --from=providers \
    /home/app/target/migration-$KEYCLOAK_VERSION.$PROVIDERS_VERSION.jar \
    ${KC_HOME_DIR}/providers

RUN ${KC_HOME_DIR}/bin/kc.sh build