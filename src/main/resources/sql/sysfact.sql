----------------------------------------
-- Creación de la Estructura de la BD --
----------------------------------------
CREATE TABLE public.rol
(
    id serial,
    nombre character varying(100) NOT NULL,
    permisos character varying(100) NULL,
    CONSTRAINT pk_rol PRIMARY KEY (id)
);

CREATE TABLE public.usuario
(
    id serial,
    nombres character varying(100) NOT NULL,
    apellidos character varying(100) NOT NULL,
    correo character varying(100) NOT NULL,
    rol_id bigint NOT NULL,
    contrasena character varying(200) NOT NULL,
    password_token text NULL,
    password_token_vence timestamp with time zone NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id)
        REFERENCES public.rol(id)
);

CREATE TABLE public.parametros
(
    id serial NOT NULL,
    descripcion varchar(255) NOT NULL,
    codigo varchar(255) NOT NULL,
    activo bool DEFAULT TRUE,
    valor varchar(500) NOT NULL,
    CONSTRAINT parametros_pkey PRIMARY KEY (id)
);

CREATE TABLE public.proveedor
(
    id SERIAL,
    nombre VARCHAR(100) NOT NULL,
    contacto VARCHAR(100) NULL,
    telefono VARCHAR(30) NULL,
    direccion VARCHAR(100) null,
    CONSTRAINT pk_proveedor PRIMARY KEY (id)
);

CREATE TABLE public.cliente
(
    id SERIAL,
    ci_ruc VARCHAR(20) NOT NULL,
    nombres VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    email VARCHAR(100) NULL,
    telefono VARCHAR(20) null,
    CONSTRAINT pk_cliente PRIMARY KEY (id)
);

CREATE TABLE public.unidad_medida
(
    id SERIAL NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    plural VARCHAR(100) NOT NULL,
    CONSTRAINT pk_unidad_medida PRIMARY KEY (id),
    CONSTRAINT uq_unidad_medida_nombre UNIQUE (nombre)
);

CREATE TABLE public.articulo
(
    id SERIAL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT NULL,
    precio DECIMAL(19,2) NULL,
    unidad_medida_id BIGINT NOT NULL,
    proveedor_id BIGINT NOT NULL,
    nombre_imagen VARCHAR(100) NULL,
    tasa_iva SMALLINT NOT NULL DEFAULT 10,
    CONSTRAINT pk_articulo PRIMARY KEY (id),
    CONSTRAINT ck_articulo_tasa_iva CHECK (tasa_iva IN (0, 5, 10)),
    CONSTRAINT fk_articulos_proveedor_id FOREIGN KEY(proveedor_id)
        REFERENCES public.proveedor(id),
    CONSTRAINT fk_articulos_unidad_medida_id FOREIGN KEY(unidad_medida_id)
        REFERENCES public.unidad_medida(id)
);

CREATE TABLE public.stock
(
    id serial,
    articulo_id BIGINT NOT NULL,
    existencia numeric(19,2) NOT NULL,
    stock_minimo BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_stock PRIMARY KEY (id),
    CONSTRAINT ck_stock_minimo CHECK (stock_minimo >= 0),
    CONSTRAINT uq_stock_articulo_id UNIQUE (articulo_id),
    CONSTRAINT fk_stock_articulo_id FOREIGN KEY (articulo_id)
        REFERENCES public.articulo(id)
);

CREATE TABLE public.venta
(
    id SERIAL,
    condicion_venta CHAR(3) NOT NULL,
    fecha timestamp without time zone NOT NULL
        DEFAULT CURRENT_TIMESTAMP,
    total_iva5 DECIMAL(19,2) NOT NULL,
    total_iva10 DECIMAL(19,2) NOT NULL,
    total_exenta DECIMAL(19,2) NOT NULL,
    total_descuento DECIMAL(19,2) NOT NULL,
    total_factura DECIMAL(19,2) NULL,
    cliente_id BIGINT null,
    usuario_id BIGINT NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'EMITIDA',
    -- How a cash sale was paid (see migracion_caja.sql); empty for credit sales.
    forma_pago VARCHAR(15) NULL,
    anulacion_fecha timestamp without time zone NULL,
    anulacion_usuario_id BIGINT NULL,
    anulacion_motivo VARCHAR(250) NULL,
    CONSTRAINT pk_venta PRIMARY KEY (id),
    CONSTRAINT ck_venta_forma_pago CHECK (
        (condicion_venta = 'CRE' AND forma_pago IS NULL)
        OR (condicion_venta = 'CON'
            AND forma_pago IN ('EFECTIVO', 'TRANSFERENCIA', 'TARJETA', 'CHEQUE', 'OTRO'))),
    CONSTRAINT fk_venta_anulacion_usuario_id FOREIGN KEY (anulacion_usuario_id)
        REFERENCES public.usuario (id),
    CONSTRAINT venta_estado_check CHECK (estado IN ('EMITIDA', 'ANULADA')),
    CONSTRAINT fk_venta_cliente_id FOREIGN KEY(cliente_id)
        REFERENCES public.cliente(id),
    CONSTRAINT fk_venta_usuario_id FOREIGN KEY (usuario_id)
        REFERENCES public.usuario (id),
    CONSTRAINT uq_venta UNIQUE (id, cliente_id, fecha),
    CONSTRAINT venta_condicion_venta_check
        CHECK (condicion_venta = ANY (ARRAY['CON'::bpchar, 'CRE'::bpchar]))
);

CREATE TABLE public.detalle_venta
(
    id SERIAL,
    cantidad BIGINT NULL,
    descuento DECIMAL(19,2) NOT NULL,
    exenta DECIMAL(19,2) NULL,
    iva10 DECIMAL(19,2) NOT NULL,
    iva5 DECIMAL(19,2) NOT NULL,
    precio_venta DECIMAL(19,2) NULL,
    venta_id BIGINT NULL,
    articulo_id BIGINT null,
    CONSTRAINT pk_detalle_venta PRIMARY KEY (id),
    CONSTRAINT fk_detalle_venta_venta_id FOREIGN KEY (venta_id)
        REFERENCES public.venta(id),
    CONSTRAINT fk_detalle_venta_articulo_id FOREIGN KEY(articulo_id)
        REFERENCES public.articulo(id)
);

-- Audit log (see migracion_venta_anulacion_auditoria.sql). The backend role only gets SELECT and
-- INSERT on it, so entries cannot be changed or deleted from the application.
CREATE TABLE public.auditoria
(
    id BIGSERIAL,
    fecha timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NULL,
    usuario_nombre VARCHAR(201) NOT NULL,
    usuario_rol VARCHAR(50) NULL,
    accion VARCHAR(20) NOT NULL,
    entidad VARCHAR(30) NOT NULL,
    entidad_id BIGINT NULL,
    detalle VARCHAR(500) NULL,
    datos_antes JSONB NULL,
    datos_despues JSONB NULL,
    CONSTRAINT pk_auditoria PRIMARY KEY (id)
);

CREATE INDEX ix_auditoria_entidad ON public.auditoria (entidad, entidad_id);
CREATE INDEX ix_auditoria_fecha ON public.auditoria (fecha DESC);

-- Stock movements (see migracion_stock_movimientos.sql): goods received, adjustments, sales and
-- cancellations. Append-only like the audit log; existencia is the stock after each movement.
CREATE TABLE public.movimiento_stock
(
    id BIGSERIAL,
    fecha timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    articulo_id BIGINT NOT NULL,
    tipo VARCHAR(15) NOT NULL,
    cantidad BIGINT NOT NULL,
    existencia BIGINT NOT NULL,
    usuario_id BIGINT NULL,
    usuario_nombre VARCHAR(201) NOT NULL,
    venta_id BIGINT NULL,
    proveedor_id BIGINT NULL,
    motivo VARCHAR(250) NULL,
    CONSTRAINT pk_movimiento_stock PRIMARY KEY (id),
    CONSTRAINT ck_movimiento_stock_tipo
        CHECK (tipo IN ('INICIAL', 'INGRESO', 'AJUSTE', 'VENTA', 'DEVOLUCION')),
    CONSTRAINT fk_movimiento_stock_articulo_id FOREIGN KEY (articulo_id)
        REFERENCES public.articulo (id) ON DELETE CASCADE,
    CONSTRAINT fk_movimiento_stock_venta_id FOREIGN KEY (venta_id)
        REFERENCES public.venta (id),
    CONSTRAINT fk_movimiento_stock_proveedor_id FOREIGN KEY (proveedor_id)
        REFERENCES public.proveedor (id) ON DELETE SET NULL
);

CREATE INDEX ix_movimiento_stock_articulo ON public.movimiento_stock (articulo_id, id DESC);

-- Payments received for credit sales (see migracion_cobros.sql). They are cancelled, never deleted.
CREATE TABLE public.cobro
(
    id BIGSERIAL,
    fecha timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    venta_id BIGINT NOT NULL,
    monto DECIMAL(19,2) NOT NULL,
    forma_pago VARCHAR(15) NOT NULL,
    referencia VARCHAR(100) NULL,
    usuario_id BIGINT NULL,
    usuario_nombre VARCHAR(201) NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'VIGENTE',
    anulacion_fecha timestamp without time zone NULL,
    anulacion_usuario_id BIGINT NULL,
    anulacion_usuario_nombre VARCHAR(201) NULL,
    anulacion_motivo VARCHAR(250) NULL,
    CONSTRAINT pk_cobro PRIMARY KEY (id),
    CONSTRAINT ck_cobro_monto CHECK (monto > 0),
    CONSTRAINT ck_cobro_forma_pago
        CHECK (forma_pago IN ('EFECTIVO', 'TRANSFERENCIA', 'TARJETA', 'CHEQUE', 'OTRO')),
    CONSTRAINT ck_cobro_estado CHECK (estado IN ('VIGENTE', 'ANULADO')),
    CONSTRAINT fk_cobro_venta_id FOREIGN KEY (venta_id) REFERENCES public.venta (id)
);

CREATE INDEX ix_cobro_venta ON public.cobro (venta_id);
CREATE INDEX ix_cobro_fecha ON public.cobro (fecha);
CREATE INDEX ix_venta_fecha ON public.venta (fecha);

-- Daily cash closings (see migracion_caja.sql). Cancelled, never deleted; one valid per day.
CREATE TABLE public.caja_cierre
(
    id BIGSERIAL,
    fecha DATE NOT NULL,
    fondo_inicial DECIMAL(19,2) NOT NULL DEFAULT 0,
    total_ventas DECIMAL(19,2) NOT NULL,
    total_cobros DECIMAL(19,2) NOT NULL,
    efectivo_esperado DECIMAL(19,2) NOT NULL,
    efectivo_contado DECIMAL(19,2) NOT NULL,
    diferencia DECIMAL(19,2) NOT NULL,
    observacion VARCHAR(250) NULL,
    creado timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NULL,
    usuario_nombre VARCHAR(201) NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'VIGENTE',
    anulacion_fecha timestamp without time zone NULL,
    anulacion_usuario_id BIGINT NULL,
    anulacion_usuario_nombre VARCHAR(201) NULL,
    anulacion_motivo VARCHAR(250) NULL,
    CONSTRAINT pk_caja_cierre PRIMARY KEY (id),
    CONSTRAINT ck_caja_cierre_montos CHECK (fondo_inicial >= 0 AND efectivo_contado >= 0),
    CONSTRAINT ck_caja_cierre_estado CHECK (estado IN ('VIGENTE', 'ANULADO'))
);

CREATE UNIQUE INDEX ux_caja_cierre_fecha_vigente ON public.caja_cierre (fecha) WHERE estado = 'VIGENTE';

---------------------
-- Datos de Prueba --
---------------------
INSERT INTO public.proveedor (id, nombre) VALUES(1, 'Proveedor 1');
INSERT INTO public.proveedor (id, nombre) VALUES(2, 'Proveedor 2');
INSERT INTO public.proveedor (id, nombre) VALUES(3, 'Proveedor 3');
SELECT setval('proveedor_id_seq', 3);

-----

INSERT INTO public.unidad_medida (id, nombre, plural) VALUES(1, 'Maceta','Macetas');
INSERT INTO public.unidad_medida (id, nombre, plural) VALUES(2, 'Bolsa','Bolsas');
INSERT INTO public.unidad_medida (id, nombre, plural) VALUES(3, 'Recipiente','Recipientes');
INSERT INTO public.unidad_medida (id, nombre, plural) VALUES(4, 'Unidad','Unidades');
SELECT setval('unidad_medida_id_seq', 4);

-----

INSERT INTO public.articulo (id, nombre, precio, proveedor_id, unidad_medida_id) VALUES(1, 'Planta Pequeña de Limón', 15000, 1, 1);
INSERT INTO public.articulo (id, nombre, precio, proveedor_id, unidad_medida_id) VALUES(2, 'Planta Pequeña de Palmera', 20000, 1, 1);
INSERT INTO public.articulo (id, nombre, precio, proveedor_id, unidad_medida_id) VALUES(3, 'Planta Pequeña de Santa Rita', 20000, 1, 1);
INSERT INTO public.articulo (id, nombre, precio, proveedor_id, unidad_medida_id) VALUES(4, 'Plantera pequeña de Cerámica', 15000, 1, 4);
INSERT INTO public.articulo (id, nombre, precio, proveedor_id, unidad_medida_id) VALUES(5, 'Abono Pequeño', 5000, 2, 2);
INSERT INTO public.articulo (id, nombre, precio, proveedor_id, unidad_medida_id) VALUES(6, 'Abono Mediano', 10000, 2, 2);
INSERT INTO public.articulo (id, nombre, precio, proveedor_id, unidad_medida_id) VALUES(7, 'Abono Grande', 20000, 2, 2);
SELECT setval('articulo_id_seq', 7);

-----

INSERT INTO public.stock (id, articulo_id, existencia) VALUES(1, 1, 2);
INSERT INTO public.stock (id, articulo_id, existencia) VALUES(2, 2, 1);
INSERT INTO public.stock (id, articulo_id, existencia) VALUES(3, 3, 3);
INSERT INTO public.stock (id, articulo_id, existencia) VALUES(4, 4, 5);
INSERT INTO public.stock (id, articulo_id, existencia) VALUES(5, 5, 3);
INSERT INTO public.stock (id, articulo_id, existencia) VALUES(6, 6, 2);
INSERT INTO public.stock (id, articulo_id, existencia) VALUES(7, 7, 1);
SELECT setval('stock_id_seq', 7);

-----

INSERT INTO public.cliente (id, apellidos, nombres, ci_ruc, email, telefono) VALUES (1, 'Benitez', 'Antonio Abelino', '5444777-5', 'antabelbenitez@gmail.com', '0985 752 343');
INSERT INTO public.cliente (id, apellidos, nombres, ci_ruc, email, telefono) VALUES (2, 'Cáceres', 'Maria Teresa', '4558334-9', 'mariaterecacerez@hotmail.com', '0982 888 444');
INSERT INTO public.cliente (id, apellidos, nombres, ci_ruc, email, telefono) VALUES (3, 'Martinez', 'Juan José', '2203855-2', 'jjmartinez@gmail.com', '0993 555 888');
INSERT INTO public.cliente (id, apellidos, nombres, ci_ruc, email, telefono) VALUES (4, 'González', 'María Antonia', '2551887-2', 'mariaantoniagonz@yahoo.com', '0991 733 555');
INSERT INTO public.cliente (id, apellidos, nombres, ci_ruc, email, telefono) VALUES (5, 'Cabrera', 'Alberto Damián', '7551584-8', 'adamiancabrera@gmail.com', '0971 665 999');
INSERT INTO public.cliente (id, apellidos, nombres, ci_ruc, email, telefono) VALUES (6, 'Dominguez', 'Sebastián ', '3995554-5', 'sebasdom@outlook.com', '0981 777 333');
INSERT INTO public.cliente (id, apellidos, nombres, ci_ruc, email, telefono) VALUES (7, 'Caballero ', 'Maria Josefina', '5321456-7', 'majosefinacab@hotmail.com', '0961 123 456');
SELECT setval('cliente_id_seq', 7);

-----

INSERT INTO public.rol (id, nombre, permisos) VALUES(1, 'Administrador', '["/"]');
INSERT INTO public.rol (id, nombre, permisos) VALUES(2, 'Cargador', '["/articulos", "/unidad-de-medida", "/stock"]');
INSERT INTO public.rol (id, nombre, permisos) VALUES(3, 'Cajero', '["/ventas"]');
SELECT setval('rol_id_seq', 3);

-----

INSERT INTO public.usuario (id, nombres, apellidos, correo, rol_id, contrasena, password_token)
VALUES(1,'Usuario','Administrador','alcides.abel.benitez@gmail.com',1,'$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRkgVduWkyXMWDwo.jfO7hXLTh6',NULL);
INSERT INTO public.usuario (id, nombres, apellidos, correo, rol_id, contrasena, password_token)
VALUES(2, 'Usuario', 'Cargador', '', 2, '$2a$10$sg3OFOWCyh.5jzVtT.ayle3veJmGFENCiQvO0GRE8ayh9WQ6vR9jy', NULL);
INSERT INTO public.usuario (id, nombres, apellidos, correo, rol_id, contrasena, password_token)
VALUES(3, 'Usuario', 'Cajero', '', 3, '$2a$10$sg3OFOWCyh.5jzVtT.ayle3veJmGFENCiQvO0GRE8ayh9WQ6vR9jy', NULL);
SELECT setval('rol_id_seq', 3);

-----

INSERT INTO public.parametros (id, descripcion, codigo, activo, valor) VALUES(1, 'Usuario de correo admin', 'email.user', true, 'admin@maritheplantasyceramicas.com');
INSERT INTO public.parametros (id, descripcion, codigo, activo, valor) VALUES(2, 'URL para cambio de contraseña', 'url.change.password', true, 'http://127.0.0.1:5500/cambiar-contrasena?token=');
INSERT INTO public.parametros (id, descripcion, codigo, activo, valor) VALUES(3, 'Contraseña de usuario de correo', 'email.password', true, 'Cambiar54321.');
INSERT INTO public.parametros (id, descripcion, codigo, activo, valor) VALUES(4, 'Puerto de servidor de correo', 'smtp.port', true, '465');
INSERT INTO public.parametros (id, descripcion, codigo, activo, valor) VALUES(5, 'Servidor stmp', 'smtp.host', true, 'mail.maritheplantasyceramicas.com');
INSERT INTO public.parametros (id, descripcion, codigo, activo, valor) VALUES(6, 'Directorio de reportes', 'reports.path', true, '\home\marithe\sysfact\src\main\resources\reportes');
INSERT INTO public.parametros (id, descripcion, codigo, activo, valor) VALUES(7, 'Directorio de subida de archivos', 'uploads.path', true, '\home\marithe\sysfact\uploads');
INSERT INTO public.parametros (id, descripcion, codigo, activo, valor) VALUES(8, 'Plazo de las ventas a crédito (días)', 'credito.plazo.dias', true, '30');
SELECT setval('parametros_id_seq', 8);

-----