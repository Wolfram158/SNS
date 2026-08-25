FROM curlimages/curl:8.5.0

USER root
RUN apk add --no-cache jq

COPY debezium-postgres-connector.json /connectors/

COPY register-connectors.sh /register-connectors.sh

RUN chmod +x /register-connectors.sh

USER curl_user

ENTRYPOINT ["/register-connectors.sh"]