-- V1: schema inicial do sistema de Receitas

CREATE TABLE usuario (
    id       BIGSERIAL PRIMARY KEY,
    nome     VARCHAR(255),
    login    VARCHAR(255) NOT NULL UNIQUE,
    senha    VARCHAR(255) NOT NULL,
    situacao VARCHAR(50)
);

CREATE TABLE receita (
    id             BIGSERIAL PRIMARY KEY,
    nome           VARCHAR(255) NOT NULL,
    descricao      TEXT,
    data_registro  DATE,
    custo          NUMERIC(19,2),
    tipo_receita   VARCHAR(100),
    status         VARCHAR(20) DEFAULT 'ATIVO'
);

-- usuário inicial, só pra conseguir logar no sistema recém-criado
INSERT INTO usuario (nome, login, senha, situacao)
VALUES ('Administrador', 'admin', 'admin', 'ATIVO');
