DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'auth_cpf_ro') THEN
        CREATE ROLE auth_cpf_ro LOGIN;
    END IF;
END
$$;

ALTER ROLE auth_cpf_ro PASSWORD :'ro_password';

CREATE OR REPLACE VIEW v_cliente_autenticacao AS
SELECT id,
       regexp_replace(documento, '\D', '', 'g') AS documento,
       ativo
FROM cliente;

REVOKE ALL ON SCHEMA public FROM auth_cpf_ro;
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM auth_cpf_ro;
GRANT CONNECT ON DATABASE mecanica_cliente TO auth_cpf_ro;
GRANT USAGE ON SCHEMA public TO auth_cpf_ro;
GRANT SELECT ON v_cliente_autenticacao TO auth_cpf_ro;
