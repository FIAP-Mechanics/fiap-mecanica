_format_version: "3.0"
_transform: true

# Template: __JWT_ISSUER__, __JWT_SECRET__ e __CORS_ORIGIN__ são substituídos pelo initContainer do Deployment
# a partir de JWT_ISSUER (ConfigMap atendimento-config), JWT_SECRET (Secret mecanica-shared-secrets) e CORS_ORIGIN.
# Mesma regra de acesso dos serviços: GET é público nos serviços de cadastro; escrita exige JWT.
# Regex usam regex_priority: as rotas públicas (10) vencem a rota protegida do mesmo prefixo (0).

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

services:
  # --- Serviços de cadastro: GET público, demais métodos com JWT ---
  - name: cliente
    url: http://cliente:8081
    routes:
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

  # --- Atendimento: login e consulta/decisão do cliente públicos; o restante exige JWT ---
  - name: atendimento
    url: http://atendimento:8086
    routes:
      - name: atendimento-login
        paths: [/auth/login]
        methods: [POST, OPTIONS]
        strip_path: false
      - name: atendimento-consulta-publica
        paths:
          - "~/atendimento/[^/]+$"
          - "~/atendimento/[^/]+/status$"
        methods: [GET, HEAD, OPTIONS]
        regex_priority: 10
        strip_path: false
      # Mais específica que a consulta pública acima: /atendimento/abertos lista OS e exige JWT.
      - name: atendimento-abertos
        paths: ["~/atendimento/abertos$"]
        methods: [GET, HEAD]
        regex_priority: 20
        strip_path: false
        plugins:
          - name: jwt
            config:
              key_claim_name: iss
              claims_to_verify: [exp]
              run_on_preflight: false
      - name: atendimento-decisao-orcamento
        paths: ["~/atendimento/[^/]+/decisao-orcamento$"]
        methods: [POST, OPTIONS]
        regex_priority: 10
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
