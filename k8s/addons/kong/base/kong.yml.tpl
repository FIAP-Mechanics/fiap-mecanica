_format_version: "3.0"
_transform: true

# Template: __JWT_ISSUER__, __JWT_SECRET__ e __CORS_ORIGIN__ são substituídos pelo initContainer do Deployment
# a partir de JWT_ISSUER (ConfigMap atendimento-config), JWT_SECRET (Secret mecanica-shared-secrets) e CORS_ORIGIN.
# Mesma regra de acesso dos serviços: GET é público nos serviços de cadastro; escrita exige JWT.
# Regex usam regex_priority: rotas mais específicas vencem a rota protegida do mesmo prefixo (0).
# __LAMBDA_*__ configuram o plugin aws-lambda de /auth/cpf (RIE local ou AWS Lambda real).

consumers:
  - username: mecanica-servicos
    jwt_secrets:
      - key: "__JWT_ISSUER__"
        algorithm: HS256
        secret: "__JWT_SECRET__"

plugins:
  - name: correlation-id
    config:
      header_name: X-Request-ID
      generator: uuid
      echo_downstream: true
  - name: cors
    config:
      origins: ["__CORS_ORIGIN__"]
      methods: [GET, POST, PUT, PATCH, DELETE, OPTIONS]
      headers: [Authorization, Content-Type, X-Request-ID]
      exposed_headers: [X-Request-ID]
      max_age: 3600
  - name: rate-limiting
    config:
      minute: 600
      policy: local
      limit_by: ip
  - name: opentelemetry
    enabled: __OTEL_ENABLED__
    config:
      traces_endpoint: "__OTEL_TRACES_ENDPOINT__"
      sampling_rate: 1
      headers:
        api-key: "__OTEL_API_KEY__"
      resource_attributes:
        service.name: kong-gateway
        deployment.environment: "__DEPLOYMENT_ENVIRONMENT__"

services:
  # --- Serviços de cadastro: GET público, demais métodos com JWT ---
  - name: cliente
    url: http://cliente:8081
    routes:
      - name: cliente-documento
        paths: ["~/cliente/documento$"]
        regex_priority: 20
        strip_path: false
        plugins:
          - name: jwt
            config:
              key_claim_name: iss
              claims_to_verify: [exp]
              run_on_preflight: false
      - name: cliente-leitura
        paths: [/cliente]
        methods: [GET, HEAD, OPTIONS]
        strip_path: false
      - name: cliente-escrita
        paths: [/cliente]
        strip_path: false
        plugins:
          - name: jwt
            config:
              key_claim_name: iss
              claims_to_verify: [exp]
              run_on_preflight: false

  - name: veiculo
    url: http://veiculo:8082
    routes:
      - name: veiculo-leitura
        paths: [/veiculos]
        methods: [GET, HEAD, OPTIONS]
        strip_path: false
      - name: veiculo-escrita
        paths: [/veiculos]
        strip_path: false
        plugins:
          - name: jwt
            config:
              key_claim_name: iss
              claims_to_verify: [exp]
              run_on_preflight: false

  - name: funcionario
    url: http://funcionario:8083
    routes:
      - name: funcionario-leitura
        paths: [/funcionarios]
        methods: [GET, HEAD, OPTIONS]
        strip_path: false
      - name: funcionario-escrita
        paths: [/funcionarios]
        strip_path: false
        plugins:
          - name: jwt
            config:
              key_claim_name: iss
              claims_to_verify: [exp]
              run_on_preflight: false

  - name: servico
    url: http://servico:8084
    routes:
      - name: servico-leitura
        paths: [/servicos]
        methods: [GET, HEAD, OPTIONS]
        strip_path: false
      - name: servico-escrita
        paths: [/servicos]
        strip_path: false
        plugins:
          - name: jwt
            config:
              key_claim_name: iss
              claims_to_verify: [exp]
              run_on_preflight: false

  - name: estoque
    url: http://estoque:8085
    routes:
      - name: estoque-leitura
        paths: [/estoque]
        methods: [GET, HEAD, OPTIONS]
        strip_path: false
      - name: estoque-escrita
        paths: [/estoque]
        strip_path: false
        plugins:
          - name: jwt
            config:
              key_claim_name: iss
              claims_to_verify: [exp]
              run_on_preflight: false

  # --- Atendimento: só o login de funcionário é público; o restante exige JWT de funcionário ou de cliente (CPF) ---
  - name: auth-cpf
    url: http://localhost:8000
    routes:
      - name: auth-cpf-login
        paths: [/auth/cpf]
        methods: [POST, OPTIONS]
        strip_path: false
        plugins:
          - name: rate-limiting
            config:
              minute: 20
              policy: local
              limit_by: ip
          - name: aws-lambda
            config:
              host: "__LAMBDA_HOST__"
              port: __LAMBDA_PORT__
              disable_https: __LAMBDA_DISABLE_HTTPS__
              aws_region: "__LAMBDA_REGION__"
              function_name: "__LAMBDA_FUNCTION__"
              aws_key: "__LAMBDA_AWS_KEY__"
              aws_secret: "__LAMBDA_AWS_SECRET__"
              awsgateway_compatible: true
              forward_request_body: true
              forward_request_headers: true
              is_proxy_integration: true

  - name: atendimento
    url: http://atendimento:8086
    routes:
      - name: atendimento-login
        paths: [/auth/login]
        methods: [POST, OPTIONS]
        strip_path: false
      - name: atendimento-protegido
        paths: ["~/atendimento(/.*)?$", "~/templates(/.*)?$"]
        regex_priority: 0
        strip_path: false
        plugins:
          - name: jwt
            config:
              key_claim_name: iss
              claims_to_verify: [exp]
              run_on_preflight: false
