CREATE TABLE public.proveedor
(
    id SERIAL,
    nombre VARCHAR(100) NOT NULL,
    contacto VARCHAR(100) NULL,
    telefono VARCHAR(30) NULL,
    direccion VARCHAR(100) NULL,
    imagen VARCHAR(100) NOT NULL,
	CONSTRAINT pk_proveedor PRIMARY KEY (id)
);

CREATE TABLE public.cliente
(
    id SERIAL,
    ci_ruc VARCHAR(20) NULL,
    nombre VARCHAR(50) NULL,
    email VARCHAR(100) NULL,
    telefono VARCHAR(20) NULL,
    apellido VARCHAR(50) NOT NULL,
    CONSTRAINT pk_cliente PRIMARY KEY (id)
);

CREATE TABLE public.unidad_medida
(
    id SERIAL NOT NULL,
    nombre VARCHAR(100) NOT null,
     CONSTRAINT pk_unidad_medida PRIMARY KEY (id),
     CONSTRAINT uq_unidad_medida_nombre UNIQUE (nombre)
);

CREATE TABLE public.articulo
(
    id SERIAL,
    nombre VARCHAR(100) NULL,
    descripcion TEXT NULL,
    precio DECIMAL(19,2) NULL,
    proveedor_id BIGINT NULL,
    unidad_medida_id BIGINT NOT NULL,
    CONSTRAINT pk_articulo PRIMARY KEY (id),
	CONSTRAINT fk_articulos_proveedor_id FOREIGN KEY(proveedor_id) 
		REFERENCES public.proveedor(id),
	CONSTRAINT fk_articulos_id_unidad_medida FOREIGN KEY(unidad_medida_id) 
		REFERENCES public.unidad_medida(id)
);

CREATE TABLE public.stock
(
    id serial,
    cantidad_minima numeric(19,2) NOT NULL,
    existencia numeric(19,2) NOT NULL,
    articulo_id bigint NOT NULL,
    CONSTRAINT pk_stock PRIMARY KEY (id),
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
    CONSTRAINT pk_venta PRIMARY KEY (id),
    CONSTRAINT fk_venta_cliente_id FOREIGN KEY(cliente_id) 
   		REFERENCES public.cliente(id),
    CONSTRAINT pk_venta UNIQUE (id, fecha),
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

CREATE TABLE public.parametros
(
    id INTEGER NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    codigo VARCHAR(255) NOT NULL,
    activo BOOL DEFAULT TRUE,
    valor VARCHAR(500) NOT null,
    CONSTRAINT pk_parametros PRIMARY KEY(id)
);

CREATE TABLE public.rol
(
    id serial,
    nombre character varying(100) NOT NULL,
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
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id)
        REFERENCES public.rol(id)
);