
CREATE DATABASE IF NOT EXISTS pedidosdb
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pedidosdb;


DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS cliente;


CREATE TABLE cliente (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    nombre           VARCHAR(120)  NOT NULL,
    email            VARCHAR(150)  NOT NULL,
    documento        CHAR(12)      NOT NULL,
    fecha_nacimiento DATE          NULL,
    activo           BOOLEAN       NOT NULL DEFAULT TRUE,
    saldo            DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT pk_cliente       PRIMARY KEY (id),
    CONSTRAINT uq_cliente_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE pedido (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    cliente_id   BIGINT        NOT NULL,
    descripcion  VARCHAR(200)  NOT NULL,
    cantidad     INT           NOT NULL,
    valor_total  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    fecha_pedido DATETIME      NOT NULL,
    entregado    BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_pedido PRIMARY KEY (id),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_pedido_cliente   ON pedido (cliente_id);
CREATE INDEX idx_pedido_fecha     ON pedido (fecha_pedido);
CREATE INDEX idx_pedido_entregado ON pedido (entregado);


INSERT INTO cliente (nombre, email, documento, fecha_nacimiento, activo, saldo) VALUES
 ('Laura Mendoza',  'laura.mendoza@correo.com',  '1012345678', '1992-03-14', TRUE,  250000.00),
 ('Carlos Rivera',  'carlos.rivera@correo.com',  '1023456789', '1988-11-02', TRUE,  180500.50),
 ('Ana Sofia Pena', 'ana.pena@correo.com',       '1034567890', '1995-07-21', TRUE,   95000.00),
 ('Jorge Castillo', 'jorge.castillo@correo.com', '1045678901', '1979-01-30', FALSE,      0.00),
 ('Valentina Ruiz', 'valentina.ruiz@correo.com', '1056789012', '2000-05-09', TRUE,  410750.75);

GRANT ALL PRIVILEGES ON pedidosdb.* TO 'appuser'@'%';
FLUSH PRIVILEGES;

