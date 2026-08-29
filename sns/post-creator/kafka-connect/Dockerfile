FROM alpine:3.19

RUN sed -i 's|dl-cdn.alpinelinux.org|mirrors.edge.kernel.org|g' /etc/apk/repositories && \
    apk add --no-cache curl jq

COPY debezium-postgres-connector.json /connectors/
COPY register-connectors.sh /register-connectors.sh
RUN chmod +x /register-connectors.sh

CMD ["/register-connectors.sh"]