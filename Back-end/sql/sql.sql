-- migrations/001_create_users_table.sql
CREATE TABLE IF NOT EXISTS users (
  id SERIAL,
  email TEXT NOT NULL,
  password TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),

  CONSTRAINT PK_USERS PRIMARY KEY (id),
  CONSTRAINT UQ_USERS_EMAIL UNIQUE (email)
);

-- migrations/002_create_contacts_table.sql
CREATE TABLE IF NOT EXISTS contacts (
  id SERIAL,
  user_id INTEGER NOT NULL,
  name TEXT NOT NULL,
  phone TEXT NOT NULL,

  CONSTRAINT PK_CONTACTS PRIMARY KEY (id),
  CONSTRAINT FK_CONTACTS_USER_ID FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE CONTACTS (
  user_id   INTEGER NOT NULL,
  contact_user_id  INTEGER NOT NULL,
  nombre        TEXT,  -- opcional: nombre/apodo que asigne el dueño

  CONSTRAINT pk_usuario_contacto
    PRIMARY KEY (user_id, contact_user_id),

  CONSTRAINT fk_usuario_contactousuario
    FOREIGN KEY (user_id)
    REFERENCES USERS (Id)
    ON DELETE CASCADE,

  CONSTRAINT fk_usuario_contactocontacto
    FOREIGN KEY (user_id)
    REFERENCES USERS (Id)
    ON DELETE CASCADE,

  CONSTRAINT chk_no_self_contact
    CHECK (user_id <> contact_user_id)
);



-- migrations/003_create_categoria_table.sql
CREATE TABLE IF NOT EXISTS categoria (
  id_categoria SERIAL,
  img_categoria TEXT,
  nombre TEXT,

  CONSTRAINT PK_CATEGORIA PRIMARY KEY (id_categoria),
  CONSTRAINT NN_CATEGORIA_NOMBRE CHECK (nombre IS NOT NULL)
);


INSERT INTO categoria (img_categoria, nombre) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/juridicos.png','Juridicos'); 
INSERT INTO categoria (img_categoria, nombre) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/laboral.png','Laboral y formación'); 
INSERT INTO categoria (img_categoria, nombre) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/economicos.png','Economicos'); 
INSERT INTO categoria (img_categoria, nombre) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/emergencias.png','Emergencias'); 
INSERT INTO categoria (img_categoria, nombre) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/informacion.png','Información y orientación'); 
INSERT INTO categoria (img_categoria, nombre) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/especializada.png','Especializados'); 
INSERT INTO categoria (img_categoria, nombre) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/programas.png','Programas'); 
INSERT INTO categoria (img_categoria, nombre) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/emergencias.png','Discapacidad'); 
-- + Gratuitos x +Problemas auditivos x +Todos x +Entidades

-- Tabla de entidades
CREATE TABLE IF NOT EXISTS entidad (
    id_entidad SERIAL,
    imagen TEXT,
    email TEXT,
    telefono TEXT,
    pagina_web TEXT,
    direccion TEXT,
    horario TEXT,

    CONSTRAINT PK_ENTIDAD PRIMARY KEY (id_entidad),
    CONSTRAINT UQ_ENTIDAD_EMAIL UNIQUE (email),
    CONSTRAINT UQ_ENTIDAD_TELEFONO UNIQUE (telefono),
    CONSTRAINT NN_ENTIDAD_EMAIL CHECK (email IS NOT NULL),
    CONSTRAINT NN_ENTIDAD_TELEFONO CHECK (telefono IS NOT NULL),
    CONSTRAINT NN_ENTIDAD_DIRECCION CHECK (direccion IS NOT NULL)
);


INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/CasaDeLaMujer.png', 'casamujer@zaragoza.es', '976 726 040', 'www.zaragoza.es/sede/portal/servicios-sociales/mujer/conocenos/', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'Lunes a viernes de 9 a 14 h, lunes y miércoles de 16:30 a 19h.
En verano (del 1 de julio al 31 de agosto) de Lunes a Viernes de 9 a 14 h.');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/ServidoAdvoPoliticasSociales.png', 'ayudasvivienda@zaragoza.es', '976 721812', 'https://administracion.gob.es/', 'Pza. San Carlos, 4, 50001 Zaragoza', 'De lunes a viernes de 09 a 14 h.');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/SOMOS%2B.png', 'asoc.somosmas@gmail.com', '653 948 651', 'www.asocsomosmas.es/', 'C. de Don Juan de Aragón, 2, Casco Antiguo,  50001 Zaragoza', 'Lunes a viernes de 9h a 18h');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/OZANAM.png', 'apoyomujer@ozanam.com', '976 283 592', 'www.ozanam.es', 'Calle Ramón Pignatelli, 17  · 50004 Zaragoza', 'Lunes a jueves de 8:00 a 14:00 y de 16:00 a 18:00, viernes de 8:00 a 15:00');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/SEPE.png', '', '060', 'www.sepe.es', 'Según oficina', 'Lunes a Viernes de 09:00 a 14:00');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/ACISJF.png', 'acisjf.zaragoza@gmail.com', '876 013 462', 'www.acisjfzaragoza.org', 'Paseo Echegaray Caballero, 116. 50001 Zaragoza ', 'Lunes a jueves de 10:00 a 17:00, viernes de 10:00 a 15:00.');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/CEPAIM.png', 'zaragoza@cepaim.org', '876 642 998', 'www.cepaim.org', 'Avenida de Madrid 7-9, local 7, 50004, Zargoza', 'Lunes a jueves de 09:00h a 14:00 y de 16:00 a 18:30, viernes de 09:00 a 14:00.');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/Atades.png', 'uavdiaragon@atades.org', '900 335 533', 'www.atades.org', 'C. de Clara Campoamor, 25,  50018 Zaragoza', '24h');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/DelegacionVG.png', 'ciudadania.igualdad@igualdad.gob.es.', '016 /  Whatapp:  600 000 016', 'www.violenciagenero.igualdad.gob.es', '', '24h');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/PoliciaNacional.png', '', ' 091', 'www.policia.es', 'Según comisaría', '24h');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/GuardiaCivil.png', '', '062 ', 'www.guardiacivil.es', 'Avda. César Augusto, 8,  50004 Zaragoza', '24h');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/MinisterioInterior.png', '', '060', 'https://www.interior.gob.es/', 'Calle Amador de los Ríos, 7  28010 Madrid', 'Lunes a viernes 9:00 a 17:30 h, sábados 9:00 a 14:00 h.

Horario de verano (16 de junio a 15 de septiembre): Lunes a viernes: 9:00 a 15:00 h, sábados: 9:00 a 14:00 h.');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/112.png', '', '112 / 976 71 40 00', 'www.112aragon.aragon.es/', 'P.º de María Agustín, 36, Edificio Pignatelli, puerta 24,  50004 Zaragoza', '24h');
INSERT INTO entidad (imagen, email, telefono, pagina_web, direccion, horario) VALUES ('https://evaespacioseguro.s3.us-east-1.amazonaws.com/IAM.png', 'iam@aragon.es', '976 716 720', 'www.aragon.es/-/iam', 'Paseo María Agustín, 16, 5ª planta,  50004 Zaragoza', 'Lunes a Viernes de 08:00-20:30');
INSERT INTO entidad (
  imagen,
  email,
  telefono,
  pagina_web,
  direccion,
  horario
) VALUES (
  'https://evaespacioseguro.s3.us-east-1.amazonaws.com/orientacionLaboralINAEM.png',
  '',
  '901 501 000',
  'https://inaem.aragon.es/',
  'Según oficina.',
  'Lunes a viernes de 9 a 14 horas, jueves de 16:30 a 18:30 horas. 
    Del 1 de julio al 15 de septiembre de 09:00 a 14:00 horas.'
);

-- Tabla de recursos
CREATE TABLE IF NOT EXISTS recurso (
    id SERIAL,
    id_entidad INTEGER,
    id_categoria INTEGER,
    imagen TEXT,
    email TEXT,
    telefono TEXT,
    direccion TEXT,
    horario TEXT,
    servicio TEXT,
    descripcion TEXT,
    requisitos TEXT,
    gratuito BOOLEAN,
    web TEXT,
    accesible BOOLEAN,

    CONSTRAINT PK_RECURSO PRIMARY KEY (id),
    CONSTRAINT FK_RECURSO_ENTIDAD FOREIGN KEY (id_entidad) REFERENCES entidad(id_entidad) ON DELETE CASCADE,
    CONSTRAINT FK_RECURSO_CATEGORIA FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) ON DELETE CASCADE,
    CONSTRAINT NN_RECURSO_ID_ENTIDAD CHECK (id_entidad IS NOT NULL),
    CONSTRAINT NN_RECURSO_ID_CATEGORIA CHECK (id_categoria IS NOT NULL),
    CONSTRAINT NN_RECURSO_SERVICIO CHECK (servicio IS NOT NULL)
);

INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (1, 5, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/InformacionAcogida.png', 'casamujer@zaragoza.es', '976 726 040', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'De Lunes a Viernes de 9 a 14 h y Lunes y Miércoles de 16:30 a 19h.

En verano(del 1 de julio al 31 de agosto) de Lunes a Viernes de 9 a 14 h.', 'Información y acogida', 'Su objetivo es ACOGER, ATENDER, ORIENTAR Y DERIVAR a la ciudadanía hacia los servicios que presta el Servicio de Mujer e Igualdad, pero también hacia otros recursos existentes en la la ciudad, para dar respuesta a la diversidad de situaciones psicosociales que se plantean o detectan.', 'Sin requisitos', TRUE, 'https://www.zaragoza.es/sede/servicio/tramite/38335', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (1, 1, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/AtencionJuridica.png', 'casamujer@zaragoza.es', '976 726 040', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'De Lunes a Viernes de 9 a 14 h y Lunes y Miércoles de 16:30 a 19h.

En verano(del 1 de julio al 31 de agosto) de Lunes a Viernes de 9 a 14 h.', 'Atención jurídica', 'Servicio de atención jurídica personalizada y gratuita del Servicio de Igualdad, donde se atiende -de forma presencial, telefónica y/o por correo electrónico- prioritariamente cuestiones de violencia machista (violencia hacia las mujeres ejercida por parte de su pareja o expareja, agresiones sexistas, acoso laboral..) y asuntos de Derecho de familia (custodias, separaciones, divorcios, pensiones alimenticias, modificación de medidas...)

También otras consultas relacionadas, como pueden ser asuntos laborales y de Seguridad Social, derecho penal o civil, sucesiones, etc.

Además se ofrece información sobre los trámites necesarios para obtener asistencia jurídica gratuita y designación de abogado/a y procurador/a por el turno de oficio.', 'Estar empadronada o tener residencia efectiva en Zaragoza.', TRUE, 'https://www.zaragoza.es/sede/portal/servicios-sociales/mujer/violencia-genero/servicio/tramite/3108?refresh', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (1, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/AtencionPsicologica.png', 'casamujer@zaragoza.es', '976 726 040', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'De Lunes a Viernes de 9 a 14 h y Lunes y Miércoles de 16:30 a 19h.

En verano(del 1 de julio al 31 de agosto) de Lunes a Viernes de 9 a 14 h.', 'Atención psicológica', 'En un espacio de escucha y orientación encontrarás la ayuda necesaria para reforzar tu autoestima y autonomía, y superar el daño causado por la violencia, tanto a ti como a tus hijos/as.', 'Estar empadronada o tener residencia efectiva en Zaragoza.', TRUE, 'https://www.zaragoza.es/sede/portal/servicios-sociales/mujer/conocenos/contacto#', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (1, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/Telealarma.png', 'casamujer@zaragoza.es', '976 726 040', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'De Lunes a Viernes de 9 a 14 h y Lunes y Miércoles de 16:30 a 19h.

En verano(del 1 de julio al 31 de agosto) de Lunes a Viernes de 9 a 14 h.', 'Telealarma', 'Dispositivo de alerta para mujeres en situación de riesgo, conectado a servicios de emergencia.', 'Estar empadronada en la ciudad de Zaragoza.', TRUE, 'https://www.zaragoza.es/sede/portal/servicios-sociales/mujer/violencia-genero/servicio/tramite/3332?refresh', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (1, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/AtencionEducativa.png', 'casamujer@zaragoza.es', '976 726 040', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'De Lunes a Viernes de 9 a 14 h y Lunes y Miércoles de 16:30 a 19h.

En verano(del 1 de julio al 31 de agosto) de Lunes a Viernes de 9 a 14 h.', 'Atención educativa', 'Acompañamiento para reforzar tus habilidades como madre y ayudar a tus hijos/as a elaborar las situaciones vividas de violencia. Pautas educativas para la resolución no violenta de conflictos. Educación en igualdad.', 'Estar empadronada o tener residencia efectiva en Zaragoza.', TRUE, 'https://www.zaragoza.es/sede/portal/servicios-sociales/mujer/conocenos/contacto#', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (1, 3, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/NecesidadesBasicas.png', 'casamujer@zaragoza.es', '976 726 040', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'De Lunes a Viernes de 9 a 14 h y Lunes y Miércoles de 16:30 a 19h.

En verano(del 1 de julio al 31 de agosto) de Lunes a Viernes de 9 a 14 h.', 'Apoyo en necesidades básicas', 'Cobertura de necesidades de alimentación, higiene y vestimenta.', 'Estar empadronada en la ciudad de Zaragoza.', TRUE, 'https://www.zaragoza.es/sede/portal/servicios-sociales/mujer/violencia-genero/servicio/tramite/3332?refresh', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (1, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/AtencionIntegralVG.png', 'casamujer@zaragoza.es', '976 726 040', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'De Lunes a Viernes de 9 a 14 h y Lunes y Miércoles de 16:30 a 19h.

En verano(del 1 de julio al 31 de agosto) de Lunes a Viernes de 9 a 14 h.', 'Servicio de Atención integral a la Violencia de Género', 'Ofrece apoyo social, psicológico, educativo y jurídico a las mujeres víctimas de violencia de género

La tramitación de dichos servicios se centraliza en la Casa de la Mujer:

Servicio de información.
Asesorías especializadas: social, jurídica,psicológica , y educativa.
Terapia psicológica individual y grupal
Servicio de atención y acogida.
Atención integral para las mujeres y sus hijos e hijas.
Alojamientos provisionales, pisos tutelados y Casa de acogida
Servicio de Telealarma.
Apoyo en las necesidades básicas.
Formación y apoyo en la búsqueda de empleo.', 'Estar empadronada en la ciudad de Zaragoza.', TRUE, 'https://www.zaragoza.es/sede/portal/servicios-sociales/mujer/violencia-genero/servicio/tramite/3332?refresh', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (1, 2, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/OrientacionLaboral.png', 'casamujer@zaragoza.es', '976 726 040', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'De Lunes a Viernes de 9 a 14 h y Lunes y Miércoles de 16:30 a 19h.

En verano(del 1 de julio al 31 de agosto) de Lunes a Viernes de 9 a 14 h.', 'Orientación laboral', 'Servicio especializado de información y orientación laboral para mujeres y otras personas con dificultad en el acceso y mantenimiento del empleo por razones de género.', 'Estar empadronada en la ciudad de Zaragoza.', TRUE, 'https://www.zaragoza.es/sede/portal/servicios-sociales/mujer/servicios/servicio/tramite/37155', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (1, 2, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/Cursos.png', 'casamujer@zaragoza.es', '976 726 040', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'De Lunes a Viernes de 9 a 14 h y Lunes y Miércoles de 16:30 a 19h.

En verano(del 1 de julio al 31 de agosto) de Lunes a Viernes de 9 a 14 h.', 'Cursos ', 'Variedad de cursos dirigidos tanto a mujeres como a hombres.', 'Dependiendo del curso.', TRUE, 'https://www.zaragoza.es/ciudad/sectores/mujer/index_CasaMujer', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (1, 5, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/OrientacionAsesoriamento.png', 'casamujer@zaragoza.es', '976 726 040', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'De Lunes a Viernes de 9 a 14 h y Lunes y Miércoles de 16:30 a 19h.

En verano(del 1 de julio al 31 de agosto) de Lunes a Viernes de 9 a 14 h.', 'Orientación y asesoramiento a familiares y amistades de víctimas de violencia de género', 'Atención a padres, madres, familiares y amistades, especialmente de chicas jóvenes y adolescentes que están viviendo relaciones de pareja en las que existen (o se sospecha) situaciones de maltrato.', 'Estar empadronadas o tener residencia efectiva en la ciudad de Zaragoza.', TRUE, 'https://www.zaragoza.es/sede/portal/servicios-sociales/mujer/violencia-genero/servicio/tramite/26940?refresh', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (1, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/AlojamientoProvisional.png', 'casamujer@zaragoza.es', '976 726 040', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'De Lunes a Viernes de 9 a 14 h y Lunes y Miércoles de 16:30 a 19h.

En verano(del 1 de julio al 31 de agosto) de Lunes a Viernes de 9 a 14 h.', 'Alojamientos provisionales, pisos tutelados y Casa de acogida', 'Podemos alojarte de forma temporal, sola o con tus hijas/os, en una vivienda compartida, cuando existen situaciones de riesgo o es necesario abandonar tu vivienda habitual', 'Ser mujer, mayor de edad, sola y/o acompañada de sus hijos/as que estando en situación de indefensión por violencia física o moral resida en la ciudad de Zaragoza. ', TRUE, 'https://www.zaragoza.es/sede/portal/servicios-sociales/mujer/conocenos/contacto#', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (1, 5, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/InfoDifusion.png', 'casamujer@zaragoza.es', '976 726 040', 'Calle Don Juan de Aragón, 2,  50001 Zaragoza', 'Sin horario', 'Información y Difusión ', 'Difusión por buzoneo electrónico de campañas y actividades de nuestra programación: ciclos, conferencias, cursos y talleres, etc.', 'Requiere de autorización previa de la ciudadanía para poder realizar estos envíos.', FALSE, 'https://www.zaragoza.es/sede/servicio/tramite/3331', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (2, 3, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/AyudasVivienda.png', '
ayudasvivienda@zaragoza.es', '976 721812', 'Pza. San Carlos, 4, 50001 Zaragoza', 'Horario: de lunes a viernes: de 09 a 14 h.', 'Ayudas para vivienda (alquiler o hipoteca) a mujeres víctimas de violencia contra las mujeres.', 'Ayudas para el pago de alquiler o hipoteca de vivienda favoreciendo la autonomía e independencia, así como de facilitar el mantenimiento de la vivienda de las mujeres víctimas de violencia contra las mujeres en la ciudad de Zaragoza, mediante una ayuda para el pago del alquiler o de la hipoteca de la vivienda.', 'Podrán ser beneficiarias de las ayudas contempladas en estas bases las mujeres víctimas de violencia, de conformidad con lo establecido en la Ley 1/2004, de 28 de diciembre de Medidas de Protección Integral contra la Violencia de Género, que acrediten la vigencia de la situación de violencia a fecha de solicitud de la ayuda.', TRUE, 'https://www.zaragoza.es/sede/servicio/tramite/36575', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (11, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/UFAM.png', '', '976 976 388', 'Pº José Atarés 105, 1ª. Planta.', '24h', 'Unidad de Atención a la Familia y la Mujer (UFAM)', 'Intervención, denuncia, protección y coordinación con servicios sociales.', 'Sin requisitos', TRUE, 'https://www.policia.es/_es/colabora_ufam.php', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (3, 7, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/MentorasAcompa%C3%B1antes.png', 'asoc.somosmas@gmail.com', '653 948 651', 'C. de Don Juan de Aragón, 2, Casco Antiguo,  50001 Zaragoza', 'Lunes a viernes de 9h a 18h', 'Programa Mentoras-Acompañantes', 'Mujeres que han superado situaciones de violencia de género acompañan a otras víctimas en su proceso, disponible las 24 horas del día', 'Ser mujer víctima de violencia de género.', FALSE, 'https://asocsomosmas.es/programa-mentoras-acompanantes/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (3, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ViolenciaDigital.png', 'asoc.somosmas@gmail.com', '653 948 651', 'C. de Don Juan de Aragón, 2, Casco Antiguo,  50001 Zaragoza', 'Lunes a viernes de 9h a 18h', 'Atención a Víctimas de Violencia Digital', 'La finalidad de este proyecto es ofrecer acompañamiento y asesoramiento a aquellas mujeres y menores víctimas de la violencia de género digital ejercida a través de internet y las redes sociales, o bien, mediante el control y espionaje de sus dispositivos móviles.', 'Ser mujer víctima de violencia de género.', FALSE, 'https://asocsomosmas.es/atencion-a-victimas-de-violencia-digital/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (3, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/TerapiaPerros.png', 'asoc.somosmas@gmail.com', '653 948 651', 'C. de Don Juan de Aragón, 2, Casco Antiguo,  50001 Zaragoza', 'Lunes a viernes de 9h a 18h', 'Terapia con perros de acompañamiento', 'Intervenciones asistidas con animales para apoyar a mujeres víctimas de violencia de género.', 'Ser mujer víctima de violencia de género.', FALSE, 'https://asocsomosmas.es/animales-de-compania-un-camino-hacia-la-sanacion-para-victimas-de-violencia-machista/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (3, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/Acogida.png', 'asoc.somosmas@gmail.com', '653 948 651', 'C. de Don Juan de Aragón, 2, Casco Antiguo,  50001 Zaragoza', 'Lunes a viernes de 9h a 18h', 'Acogida', 'ACOGIDAS de mujeres que están saliendo de una relación de maltrato, o que siguen todavía inmersas en ella y buscan información y apoyo para salir', 'Ser mujer víctima de violencia de género.', FALSE, 'https://asocsomosmas.es/que-hacemos/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (3, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/Acompa%C3%B1amiento.png', 'asoc.somosmas@gmail.com', '653 948 651', 'C. de Don Juan de Aragón, 2, Casco Antiguo,  50001 Zaragoza', 'Lunes a viernes de 9h a 18h', 'Acompañamiento', 'ACOMPAÑAMIENTOS a mujeres a Juzgados, Comisarías, Puntos de Encuentro Familiares, a tomar un café…', 'Ser mujer víctima de violencia de género.', FALSE, 'https://asocsomosmas.es/que-hacemos/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (3, 2, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ReinsercionLaboral.png', 'asoc.somosmas@gmail.com', '653 948 651', 'C. de Don Juan de Aragón, 2, Casco Antiguo,  50001 Zaragoza', 'Lunes a viernes de 9h a 18h', 'Reinserción laboral', 'REINSERCION LABORAL ayudamos a mujeres a reinsertarse en el mundo laboral con cursos de formación y con convenios firmados con diversas empresas para darles prioridad en la contratación', 'Ser mujer víctima de violencia de género.', FALSE, 'https://asocsomosmas.es/que-hacemos/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (3, 2, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/Formacion.png', 'asoc.somosmas@gmail.com', '653 948 651', 'C. de Don Juan de Aragón, 2, Casco Antiguo,  50001 Zaragoza', 'Lunes a viernes de 9h a 18h', 'Formación', 'CURSOS DE FORMACION, DE EMPODERAMIENTO PERSONAL, DE AUTODEFENSA', 'Ser mujer víctima de violencia de género.', FALSE, 'https://asocsomosmas.es/que-hacemos/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (4, 2, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/InsercionLaboral.png', 'apoyomujer@ozanam.com', '976 283 592', 'Calle Ramón Pignatelli, 17  · 50004 Zaragoza', 'Lunes a jueves de 8:00 a 14:00 y de 16:00 a 18:00 // Viernes de 8:00 a 15:00', 'Inserción Laboral de Mujeres Víctimas de Violencia', 'Este proyecto está específicamente diseñado para apoyar a mujeres que han sido víctimas de cualquier tipo de violencia, con el objetivo principal de facilitar su inserción laboral y promover su autonomía', 'Ser mujer víctima de violencia de género.', FALSE, 'https://www.ozanam.es/proyecto/insercion-laboral-de-mujeres-victimas-de-violencia/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (4, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/AtencionIntegral.png', 'apoyomujer@ozanam.com', '976 283 592', 'Calle Ramón Pignatelli, 17  · 50004 Zaragoza', 'Lunes a jueves de 8:00 a 14:00 y de 16:00 a 18:00 // Viernes de 8:00 a 15:00', 'Programa de Atención Integral', 'Este proyecto ofrece apoyo que permita a las familias expuestas a situaciones de violencia herramientas para reconstruir su vida.
 

El programa centra su labor tanto en las mujeres víctimas como en sus hijos e hijas, dotando al núcleo familiar de las habilidades y estrategias necesarias para recuperarse de las secuelas derivadas de la violencia.', 'Ser mujer víctima de violencia de género.', FALSE, 'https://www.ozanam.es/proyecto/programa-de-atencion-integral/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (5, 3, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/SubsidioVictimas.png', '', '60', 'Según oficina', 'Lunes a Viernes de 09:00 a 14:00', 'Subsidio para víctimas de violencia de género o sexual', 'Tramitación online o presencial. La duración máxima del subsidio es de treinta meses. Sin embargo, si ya has sido beneficiario del programa RAI anteriormente por la misma causa, la duración será de veinte meses si fue una vez, y de diez meses si fue en dos ocasiones. En caso de que hayan transcurrido tres o más años desde el inicio de la primera RAI, la duración volverá a ser de treinta meses. Cada vez que completes tres meses de subsidio, deberás presentar una solicitud de prórroga para seguir recibiéndolo.

En cuanto a la cuantía, durante los primeros 180 días recibirás el 95 % del IPREM. Desde el día 181 hasta el día 360, percibirás el 90 %, y a partir del día 361 hasta el final del subsidio, el 80 % del IPREM. Si estás trabajando a tiempo parcial o comienzas a hacerlo mientras cobras el subsidio, hasta el 30 de mayo de 2025 se descontará la parte proporcional al tiempo trabajado. La solicitud del subsidio puedes presentarla inmediatamente tras la emisión o notificación del certificado o resolución que acredite la condición de víctima de violencia o hacerlo dentro del plazo máximo de seis meses desde dicha fecha. ', 'Estar totalmente desempleado o desempleada, o trabajando a tiempo parcial.
Presentar la solicitud del subsidio dentro del plazo de los seis meses siguientes a la fecha del hecho causante.
No tener derecho a la prestación por desempleo de nivel contributivo.
No haber sido persona beneficiaria de tres derechos al programa de renta activa de inserción (RAI).
Haber transcurrido tres o más años desde el nacimiento del primer derecho a la renta activa de inserción como víctima de violencia de género o sexual o desde el nacimiento del derecho del subsidio, en caso de no haber percibido previamente la renta activa de inserción como víctima de violencia de género o sexual.
Estar inscrita como demandante de empleo.
Haber suscrito el acuerdo de actividad.
Carecer de rentas propias y si tienes cónyuge, pareja de hecho y/o hijos menores de veintiséis años, o mayores con discapacidad, o menores acogidos y acogidas o en guarda con fines de adopción o acogimiento, deberás cumplir necesariamente el requisito de tener responsabilidades familiares.
Ser víctima de violencia de género, de violencia sexual o de la violencia ejercida por tus padres o por tus hijos.', TRUE, 'https://www.sepe.es/HomeSepe/es/prestaciones-desempleo/subsidio-desempleo/victimas-violencia-genero-o-sexual.html', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (5, 3, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/RAI.png', '', '60', 'Según oficina', 'Lunes a Viernes de 09:00 a 14:00', 'Renta Activa de Inserción (RAI)', 'El Programa de Renta Activa de Inserción, regulado en el Real Decreto 1369/2006, de 24 de noviembre, tiene un doble objetivo: incrementar las oportunidades de inserción laboral de los desempleados con especiales necesidades económicas y dificultad para encontrar empleo; y otorgarles una ayuda económica, denominada Renta Activa de Inserción, cuando reúnan los requisitos exigidos.', 'Las mujeres víctimas de violencia de género pueden ser beneficiarias de esta ayuda económica cuando estén desempleadas, sean menores de 65 años, no hayan recibido la RAI en el último año ni más de 3 veces, acrediten por la Administración competente la condición de víctima de género, y no convivan con el agresor. 
Se percibirá por un máximo de 11 meses. Será igual al 80 % del Indicador Público de Renta de Efectos Múltiples (IPREM). ', TRUE, 'https://sede.sepe.gob.es/portalSede/es/procedimientos-y-servicios/personas/proteccion-por-desempleo/solicitud-de-prestaciones/sede-virtual/sv0309', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (6, 7, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ProyectoHimaya.png', 'acisjf.zaragoza@gmail.com', '876 013 462', 'Paseo Echegaray Caballero, 116. 50001 Zaragoza ', 'Lunes a jueves de 10:00 a 17:00, viernes de 10:00 a 15:00.', 'Proyecto HIMAYA', 'Programa de acogida integral para supervivientes de violencia de género o trata con fines de explotación sexual.', 'Ser mujer víctima de trata de seres humamos con fines de explotación sexual y encontrarse en situación de riesgo en su ciudad, por la que necesitaría traslado a otra ubicación.', TRUE, 'https://acisjfzaragoza.org/2025/01/16/proyecto-himaya/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (7, 7, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ProyectoRaise.png', 'zaragoza@cepaim.org', '876 642 998', 'Avenida de Madrid 7-9, local 7, 50004, Zargoza', 'Lunes a jueves de 09:00h a 14:00 y de 16:00 a 18:30, viernres de 09:00 a 14:00.', 'Proyecto Raise', 'Trabajamos por el empoderamiento de las mujeres migrantes y la prevención de la violencia de género en sus comunidades, mediante formación, investigación y acción comunitaria.', 'Mujeres mayores de 16 años, preferentemente migrantes, con nivel básico de castellano.', TRUE, 'https://www.cepaim.org/2025-RAISE', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (8, 8, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/uavdi.png', 'uavdiaragon@atades.org', '900 335 533', 'C. de Clara Campoamor, 25,  50018 Zaragoza', '24h', 'Uavdi Aragón - Atención especializada a mujeres con discapacidad intelectual víctimas de violencia de género', 'El propósito de la UAVDI Aragón es ayudar a las personas con discapacidad intelectual víctimas de violencia y discriminación, ya sea por género, edad, situación económica o simplemente por su grado de discapacidad. Los tipos de abuso pueden ser tanto físicos o psicológicos, como de carácter sexual, económico o situaciones de desamparo o abandono.

Servicios:
De psicología
Asesoramiento legal
Talleres de prevención', 'Ser mujer con discapacidad intelectual víctima de violencia de género.', TRUE, 'https://www.atades.org/uavdi-aragon/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (9, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/atenpro.png', 'atenpro@femp.es', '900 22 22 92', '', '24h', 'ATENPRO - Servicio de Atención
y Protección a
mujeres víctimas de la
violencia de género', 'Dispositivo de telefonía móvil que permite que las usuarias puedan
entrar en contacto en cualquier momento con
un Centro atendido por personal específicamente
preparado para dar una respuesta adecuada a su
situación personal.
Ofrece una atención inmediata y a distancia, asegurando una respuesta rápida a las eventualidades
que les puedan sobrevenir, las 24 horas del día, los
365 días del año y sea cual sea el lugar en que se
encuentren.
Desde el Centro de Atención se contacta periódicamente con las usuarias del servicio con el objetivo de realizar un seguimiento permanente. Ante
situaciones de emergencia, el personal del Centro
está preparado para dar una respuesta adecuada
a la crisis planteada, bien con medios propios o
movilizando otros recursos humanos y materiales.
Es un servicio accesible para mujeres con discapacidad auditiva (Módulo SOTA): a través de una
aplicación instalada en el terminal se permite el
contacto con el Centro de Atención a través de
un diálogo mediante mensajes de texto. Para acceder al recursos se debe contactar con su centro de Servicios Sociales correspodiente o con la Casa de la Mujer.', 'Ser mujer víctima de violencia de género.
No convivir con el agresor que te ha maltratrado.
Participar en los programas de atención especializada existentes en tu ámbito autonómico.
Aceptar las normas de funcionamiento del servicio y cooperar para su buen funcionamiento.', TRUE, 'https://www.atenpro.es/#/home', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (9, 1, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/dispositivosElectronicos.png', 'ciudadania.igualdad@igualdad.gob.es.', '016 /  Whatapp:  600 000 016', '', '24h', 'Dispositivos Electrónicos de Control Telemático', 'Permite verificar el cumplimiento de las prohibiciones de aproximación a la víctima impuestas en los procedimientos que se sigan por violencia de género y por violencia sexual en los que la Autoridad Judicial acuerde su utilización. El Sistema proporciona, además, información actualizada y permanente de las incidencias que afecten al cumplimiento o incumplimiento de las medidas, así como de las posibles incidencias, tanto accidentales como provocadas, en el funcionamiento de los dispositivos electrónicos utilizados. El dispositivo permite verificar el correcto
cumplimiento por el investigado / encausado
/ condenado de la orden de prohibición de
aproximación a una mujer víctima de
violencia de género o a una víctima de
violencia sexual impuesta por orden judicial. El investigado / encausado / condenado llevará
una pulsera de pequeñas dimensiones y ligera
junto con un teléfono inteligente
La víctima llevará otro teléfono inteligente
Existe un centro de control, Centro СОМЕТА,
atendido por personas especializadas 24 horas
al día. Contacta con el investigado / encausado /
condenado y con la víctima, atiende las alarmas
y alertas producidas por el sistema y se
comunica con Fuerzas y Cuerpos de Seguridad
del Estado en caso de ser necesario', 'Tener una orden de alejamiento en vigor en materia de violencia de género о violencia sexual Su utilización la tiene que acordar una jueza o juez', TRUE, 'https://violenciagenero.igualdad.gob.es/informacion-3/recursos/dispositivoscontroltelematico/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (9, 5, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/016InfoGen.png', '016-online@igualdad.gob.es', '016 /  Whatapp:  600 000 016', '', '24h', '016 - Información general sobre violencia de género', 'Atención telefónica confidencial 24h con información sobre qué es la violencia de género y cómo actuar.', 'Sin requisitos.', TRUE, 'https://violenciagenero.igualdad.gob.es/informacion-3/recursos/telefono016/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (9, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/016AtPsic.png', '016-online@igualdad.gob.es', '016 /  Whatapp:  600 000 016', '', '24h', '016 - Atención psicosocial inmediata', 'Apoyo emocional y contención psicológica en situaciones de urgencia, con derivación a recursos adecuados.', 'Ser víctima de violencia de género o familiar de una víctima.', TRUE, 'https://violenciagenero.igualdad.gob.es/informacion-3/recursos/telefono016/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (9, 1, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ordenProteccion.png', 'ciudadania.igualdad@igualdad.gob.es.', '016 /  Whatapp:  600 000 016', '', 'Sin horario', 'Orden de protección', 'La orden de protección es un instrumento legal diseñado para proteger a las víctimas de la violencia doméstica y/o de género frente a todo tipo de agresiones. Para ello, la orden de protección concentra en una única e inmediata resolución judicial (un auto) la adopción de medidas de protección y seguridad de naturaleza penal y de naturaleza civil, y activa al mismo tiempo los mecanismos de asistencia y protección social establecidos a favor de la víctima por el Estado, las Comunidades Autónomas y las Corporaciones Locales.

Esto es, la orden de protección unifica los distintos instrumentos de protección a la víctima previstos por el ordenamiento jurídico y le confiere un estatuto integral de protección. En los casos en que, existiendo indicios fundados de la comisión de un delito o falta contra la vida, integridad física o moral, libertad sexual, libertad o seguridad de una mujer (por parte de un hombre que sea o haya sido su cónyuge o que esté o haya estado ligado a ella por relaciones similares de afectividad, aun sin convivencia), resulta una situación objetiva de riesgo para la víctima que requiere la adopción de alguna medida de protección. Se debe solicitar a través de un formulario normalizado y único disponible en las Comisarías de Policía, los puestos de la Guardia Civil, las dependencias de las Policías Autonómicas y Locales, los órganos judiciales penales y civiles, las fiscalías, las Oficinas de Atención a las Víctimas, los Servicios de Orientación Jurídica de los Colegios de Abogados, los servicios sociales o instituciones asistenciales municipales, autonómicos o estatales.', 'Ser víctima de violencia de género y los descendientes de la víctima, sus ascendientes o hermanos por naturaleza, adopción o afinidad, propios o del cónyuge o conviviente, los menores o incapaces que convivan con la víctima o que se hallen sujetos a la potestad, tutela, curatela, acogimiento o guarda de hecho.', TRUE, 'https://violenciagenero.igualdad.gob.es/profesionalesinvestigacion/asistenciasocial/recursos-2/orden/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (10, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/comisariaDelicias.png', '', '091 - 976 791 563', ' Avda. de Valencia, 50,  50005 Zaragoza', '24h', 'Comisaría de Delicias', 'Recepción de denuncias y protección a las víctimas.

Información sobre el proceso y recursos disponibles, incluso si la víctima aún no ha decidido denunciar.

Acompañamiento durante todo el proceso, permitiendo que la víctima esté acompañada por una persona de su elección.

Disponibilidad de intérpretes y atención adaptada para personas con discapacidad.

Coordinación con servicios sociales, sanitarios y otras instituciones.', 'Sin requisitos.', TRUE, '', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (10, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/jefaturaSuperior.png', '', '091 / 976 469 900', 'P.º de María Agustín, 34,  50004 Zaragoza', '24h', 'Jefatura Superior de Policía de Aragón', 'Recepción de denuncias y protección a las víctimas.

Información sobre el proceso y recursos disponibles, incluso si la víctima aún no ha decidido denunciar.

Acompañamiento durante todo el proceso, permitiendo que la víctima esté acompañada por una persona de su elección.

Disponibilidad de intérpretes y atención adaptada para personas con discapacidad.

Coordinación con servicios sociales, sanitarios y otras instituciones.', 'Ninguno.', TRUE, '', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (10, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/comisariaActur.png', '', '091 / 976 976 388', 'Avda. Jose Atarés, 105,  50018 Zaragoza', '24h', 'Comisaría Actur-Rey Fernando', 'Recepción de denuncias y protección a las víctimas.

Información sobre el proceso y recursos disponibles, incluso si la víctima aún no ha decidido denunciar.

Acompañamiento durante todo el proceso, permitiendo que la víctima esté acompañada por una persona de su elección.

Disponibilidad de intérpretes y atención adaptada para personas con discapacidad.

Coordinación con servicios sociales, sanitarios y otras instituciones.', 'Ninguno.', TRUE, '', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (10, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ComisariaSanJose.png', '', '091 / 976 593 088', ' P.º de Rosales, 24 duplicado,  50008 Zaragoza', '24h', 'Comisaría de San José', 'Recepción de denuncias y protección a las víctimas.

Información sobre el proceso y recursos disponibles, incluso si la víctima aún no ha decidido denunciar.

Acompañamiento durante todo el proceso, permitiendo que la víctima esté acompañada por una persona de su elección.

Disponibilidad de intérpretes y atención adaptada para personas con discapacidad.

Coordinación con servicios sociales, sanitarios y otras instituciones.', 'Ninguno.', TRUE, '', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (10, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ComisariaArrabal.png', '', '091 / 976 106 530', 'C/ Almadieros del Roncal, 5,  50015 Zaragoza', '24h', 'Comisaría de Arrabal', 'Recepción de denuncias y protección a las víctimas.

Información sobre el proceso y recursos disponibles, incluso si la víctima aún no ha decidido denunciar.

Acompañamiento durante todo el proceso, permitiendo que la víctima esté acompañada por una persona de su elección.

Disponibilidad de intérpretes y atención adaptada para personas con discapacidad.

Coordinación con servicios sociales, sanitarios y otras instituciones.', 'Ninguno.', TRUE, '', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (10, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ComisariaCentro.png', '', '091 / 976 469 993', ' C/ General Mayandía, 3,  50004 Zaragoza', '24h', 'Comisaría de Centro', 'Recepción de denuncias y protección a las víctimas.

Información sobre el proceso y recursos disponibles, incluso si la víctima aún no ha decidido denunciar.

Acompañamiento durante todo el proceso, permitiendo que la víctima esté acompañada por una persona de su elección.

Disponibilidad de intérpretes y atención adaptada para personas con discapacidad.

Coordinación con servicios sociales, sanitarios y otras instituciones.', 'Ninguno.', TRUE, '', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (11, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/GuardiaCivilServ.png', '', '062 / 976 711 400', 'Avda. César Augusto, 8,  50004 Zaragoza', '24h', 'Comandancia de la Guardia Civil de Zaragoza', 'vestigación de delitos de violencia de género y doméstica.

Atención especializada y adaptada a las necesidades de las víctimas.

Coordinación con otras instituciones y servicios de apoyo.

Información sobre el proceso y recursos disponibles, incluso si la víctima aún no ha decidido denunciar.

Acompañamiento durante todo el proceso, permitiendo que la víctima esté acompañada por una persona de su elección.

Disponibilidad de intérpretes y atención adaptada para personas con discapacidad.

Coordinación con servicios sociales, sanitarios y otras instituciones.', 'Ninguno.', TRUE, '', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (12, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/Alertcops.png', '', '_060', 'Calle Amador de los Ríos, 7  28010 Madrid', 'Lunes a viernes: 9:00 a 17:30 h.

Sábados: 9:00 a 14:00 h.

Horario de verano (16 de junio a 15 de septiembre)

Lunes a viernes: 9:00 a 15:00 h.

Sábados: 9:00 a 14:00 h.', 'Aplicación Alertcops: Botón SOS para colectivos vulnerables', 'Ofrece protección reforzada para colectivos vulnerables: víctimas de violencia de género y personal sanitario.

Podrás pedir ayuda con solo pulsar un icono en la pantalla principal del móvil.

Avisa instantáneamente a las fuerzas policiales más cercanas para una atención urgente.

Se grabarán 10 segundos de audio que se enviarán como anexo a la alerta.', 'Estar dada de alta en el Sistema VioGen.', TRUE, 'https://alertcops.ses.mir.es/publico/alertcops/comoFunciona.html', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (13, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/112Emerg.png', '', '112 / 976 71 40 00', 'P.º de María Agustín, 36, Edificio Pignatelli, puerta 24,  50004 Zaragoza', '24h', 'Emergencias y asistencia', 'Atención inmediata ante cualquier emergencia: sanitaria, policial, etc.', 'Ninguno.', TRUE, 'www.112aragon.aragon.es/', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (14, 4, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/900AtencionInmediata.png', 'iam@aragon.es', '900 504 405', 'Paseo María Agustín, 16, 5ª planta,  50004 Zaragoza', '24h', '900 504 405 - Atención inmediata 24h', 'Atención telefónica confidencial y permanente a mujeres víctimas de violencia de género en Aragón.', 'Ninguno.', TRUE, 'https://www.aragon.es/-/telefono-24-horas', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (14, 1, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/900AsesoramientoJuridico.png', 'iam@aragon.es', '900 504 405', 'Paseo María Agustín, 16, 5ª planta,  50004 Zaragoza', '24h', '900 504 405 - Asesoramiento jurídico de urgencia', 'Asistencia jurídica inmediata a través de guardias de abogados/as especializadas en violencia de género.', 'Ser mujer víctima de violencia de género en Aragón.', TRUE, 'https://www.aragon.es/-/telefono-24-horas', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (14, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/900AtencionSocial.png', 'iam@aragon.es', '900 504 405', 'Paseo María Agustín, 16, 5ª planta,  50004 Zaragoza', '24h', '900 504 405 - Atención social de urgencia', 'Apoyo y orientación por personal de trabajo social, con posible activación de recursos de emergencia.', 'Ser mujer víctima de violencia de género en Aragón.', TRUE, 'https://www.aragon.es/-/telefono-24-horas', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (14, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/900Derivacion.png', 'iam@aragon.es', '900 504 405', 'Paseo María Agustín, 16, 5ª planta,  50004 Zaragoza', '24h', '900 504 405 - Derivación a recursos especializados', 'Derivación inmediata a servicios como fuerzas de seguridad, Instituto Aragonés de la Mujer o Casa de la Mujer.', 'Ninguno.', TRUE, 'https://www.aragon.es/-/telefono-24-horas', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (14, 2, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ServicioInsercionLaboral.png', 'iam@aragon.es', '976 716 720', 'Paseo María Agustín, 16, 5ª planta,  50004 Zaragoza', 'Lunes a Viernes de 08:00-20:30', 'Servicio de Inserción Socio-Laboral', 'Apoyo para la inserción laboral de mujeres víctimas de violencia de género, incluyendo orientación laboral, formación y búsqueda de empleo. Es un servicio gratuito específico de inserción socio-laboral dirigido a mujeres víctimas de cualquier tipo de violencia, que se presta tanto en las capitales de provincia como en las comarcas de Aragón. Servicios: Apoyo en el proceso de búsqueda de empleo (acceso a recursos de empleo, preparación de entrevistas de trabajo, orientación sobre el currículum, entre otras).
Asesoramiento en formación.
Acceso a cursos específicos de formación.
Se trabajan competencias personales y profesionales. 
Derivación a ofertas de empleo. 
Contacto con empresas pertenecientes a distintos sectores para facilitar el acceso al empleo de las participantes. 
Acompañamiento durante el proceso de inserción.
Información sobre el Plan Corresponsables, requisitos, etc. ', 'Ser mujer víctima de violencia de género en Aragón.', TRUE, 'https://www.aragon.es/-/servicio-de-inserci%C3%B3n-socio-laboral-para-mujeres-v%C3%ADctimas-de-violencia#anchor1', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (14, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ServicioMenores.png', 'iam@aragon.es', '976 716 720', 'Paseo María Agustín, 16, 5ª planta,  50004 Zaragoza', 'Lunes a Viernes de 08:00-20:30', 'Servicio de Atención Psicológica a Menores', 'Atención psicológica para menores que han vivido situaciones de violencia de género, con el objetivo de abordar la sintomatología derivada. Los usuarios que pueden ser atendidos en este servicio son menores (niños o niñas de 3 a 18 años) hijas/hijos de mujeres que son o han sido víctimas de agresión/violencia por parte de sus parejas y o exparejas, que han vivido estas situaciones familiares y que están afectados psicológicamente por ellas.', 'Ser menor afectado por situaciones de violencia de género. Acceso mediante derivación de un profesional.', TRUE, 'https://www.aragon.es/-/servicio-de-atencion-psicologica-a-menores-victimas-de-violencia-de-genero-en-zaragoza-huesca-teruel', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (14, 8, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ServicioSordas.png', 'iam@aragon.es', '976 716 720 /  Whatsapp: 607 622 460', 'Paseo María Agustín, 16, 5ª planta,  50004 Zaragoza / Whatsapp:', 'Lunes a Viernes de 08:00-20:30', 'Servicio de Atención Psicológica para Mujeres Sordas', 'Atención psicológica especializada para mujeres sordas víctimas de violencia de género. Existe una colaboración entre el Instituto Aragonés de la Mujer y la Agrupación de Personas Sordas de Zaragoza y Aragón desde el año 2005. Se trata de un servicio sin barreras de comunicación, atendido por una psicóloga con dominio de la lengua de signos y experta en comunidad sorda por lo que la comunicación es directa con la profesional sin necesidad de intermediar terceras personas.

Cualquier mujer sorda, hipoacúsica, o sordociega de la Comunidad Autónoma de Aragón  que se encuentre en una situación de violencia de género, problemas de pareja, baja autoestima, depresión, etc... y precise de ayuda especializada puede acudir a este servicio. Al servicio se accede previa petición de cita en el número 607 622 460 a través de WhatsApp, mensaje o llamada.', 'Ser mujer sorda víctima de violencia de género en Aragón.', TRUE, 'https://www.aragon.es/-/servicio-de-atencion-psicologica-para-mujeres-sordas', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (14, 2, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ServicioEmpresarial.png', 'iamempresarial@aragon.es, iamformacion@aragon.es', '976 716 720', 'Paseo María Agustín, 16, 5ª planta,  50004 Zaragoza', 'Lunes a Viernes de 08:00-20:30', 'Servicio de asesoramiento empresarial', 'Es un servicio de asesoramiento gratuito a las emprendedoras aragonesas en el proceso de creación y puesta en marcha de una empresa, en su fase de consolidación y desarrollo de negocio.  Servicios: Servicio de apoyo al liderazgo, Servicio de asesoramiento empresarial, Seguimiento y apoyo continuo y Servicio de formación', 'Ser mujer sorda víctima de violencia de género en Aragón.', TRUE, 'https://www.aragon.es/-/servicio-de-asesoramiento-empresarial', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (14, 6, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/ServicioEducadoraFamiliar.png', 'iam@aragon.es', '976 716 720', 'Paseo María Agustín, 16, 5ª planta,  50004 Zaragoza', 'Lunes a Viernes de 08:00-20:30', 'Servicio educadora familiar', 'Este servicio tiene como objetivo principal intervenir y acompañar de forma integral a aquellas mujeres víctimas de violencia y sus hijas e hijos que lo precisen para su plena recuperación y así facilitar la integración y su normalización social, mediante dos ejes de actuaciones:

Intervención con unidades familiares en la que las relaciones familiares se han visto afectadas por la situación de violencia vivida.

Acompañamiento a las mujeres en momentos críticos dentro de su proceso de salida de la situación de violencia, tales como la presentación de la denuncia, personación en causas judiciales seguidas contra el agresor o por la custodia de los menores, búsqueda de empleo, solicitud de prestaciones ante las distintas Administraciones Públicas y otros similares. Apoyo en la atención a los menores afectados por las situaciones de violencia.
', 'Ser mujer víctima de violencia de género. La derivación a la educadora se hace siempre desde el servicio de atención social.', TRUE, 'https://www.aragon.es/-/servicio-educadora-familiar', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (14, 3, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/AyudaEcoComplementaria.png', 'iam@aragon.es', '976 716 720', 'Paseo María Agustín, 16, 5ª planta,  50004 Zaragoza', 'Lunes a Viernes de 08:00-20:30', 'Ayudas económicas complementarias para víctimas de violencia', 'Pueden demandarlas las hijas y los hijos menores de edad de mujeres víctimas mortales por violencia de género. También pueden solicitarlas mujeres víctimas de otras formas de violencia y mujeres víctimas de violencia de género mayores de 65 años. La presentación de la solicitud se puede realizar vía telemática o presencial.', 'Mujeres mayores de 65 años víctimas de violencia de género, acreditada. 
Estar empadronadas en un municipio aragonés y carezcan de rentas que, en cómputo mensual, superen el 75 por ciento del salario mínimo interprofesional vigente, excluida la parte proporcional de dos pagas extraordinarias.', TRUE, 'https://www.aragon.es/tramitador/-/tramite/ayudas-economicas-complementarias-victimas-violencia', FALSE);
INSERT INTO recurso (id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) VALUES (14, 3, 'https://evaespacioseguro.s3.us-east-1.amazonaws.com/AyudaEconomica.png', 'iam@aragon.es', '976 716 720', 'Paseo María Agustín, 16, 5ª planta,  50004 Zaragoza', 'Lunes a Viernes de 08:00-20:30', 'Ayuda económica a mujeres víctimas de violencia de género', 'Subvención para mujeres que han sufrido violencia de género y tienen dificultades económicas y laborales.', 'Residir en Aragón, acreditar ser víctima de violencia de género sin rentas superiores al 75% del SMI, sin haber recibido esta ayuda anteriormente y con especiales dificultades para obtener un empleo.', TRUE, 'https://www.aragon.es/tramitador/-/tramite/ayudas-economicas-mujeres-victimas-violencia-genero', FALSE);
INSERT INTO recurso (
  id_entidad,
  id_categoria,
  imagen,
  email,
  telefono,
  direccion,
  horario,
  servicio,
  descripcion,
  requisitos,
  gratuito,
  web,
  accesible
) VALUES (
  15, 
  2, 
  'https://evaespacioseguro.s3.us-east-1.amazonaws.com/orientacionLabEsp.png', 
  '', 
  '901 501 000', 
  'Según oficina', 
  'Lunes a viernes de 9 a 14 horas, jueves de 16:30 a 18:30 horas. 
    Del 1 de julio al 15 de septiembre de 09:00 a 14:00 horas.', 
  'Orientación laboral', 
  'Servicio individualizado de orientación e intermediación laboral destinado a mujeres víctimas de violencia de género inscritas como demandantes de empleo. A través de este servicio ofrecemos asesoramiento y acompañamiento con el fin de facilitar los medios más adecuados para mejorar la empleabilidad y la búsqueda autónoma de empleo. El desarrollo de este servicio depende de las necesidades personales y profesionales que se detecten, pudiéndose trabajar los siguientes aspectos:
  - Acompañamiento y seguimiento a lo largo de todo el proceso
  - Herramientas y técnicas para la búsqueda de empleo: definición del currículum vitae, procesos de selección, entrevistas, dinámicas grupales
  - Información del mercado de trabajo, cartera de servicios del INAEM y políticas activas de empleo
  - Información oferta formativa y/o movilidad y derivación al sistema educativo
  - Asesoramiento para el autoempleo
Durante todo el proceso se ofrece atención especializada y confidencial por parte de personal cualificado con formación específica en igualdad y violencia de género. Disponible en todas las oficinas de empleo.',
  'Ser mujer víctima de violencia de género.', 
  TRUE, 
  'https://inaem.aragon.es/mujer-victima-de-violencia-de-genero', 
  TRUE
);



CREATE TABLE IF NOT EXISTS subida (
    id_subida SERIAL,
    fecha_subida TIMESTAMPTZ DEFAULT NOW(),
    id_usuario INTEGER,

    CONSTRAINT PK_SUBIDA PRIMARY KEY (id_subida),
    CONSTRAINT FK_SUBIDA_USUARIO FOREIGN KEY (id_usuario) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT NN_SUBIDA_ID_USUARIO CHECK (id_usuario IS NOT NULL)
);


--Triggers

-- 1. Validar categoría al insertar en recurso
CREATE OR REPLACE FUNCTION validar_categoria_recurso()
RETURNS trigger AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM categoria WHERE id_categoria = NEW.id_categoria) THEN
    RAISE EXCEPTION 'La categoría asignada no existe.';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validar_categoria_recurso
BEFORE INSERT ON recurso
FOR EACH ROW
EXECUTE FUNCTION validar_categoria_recurso();

-- 2. Validar entidad al insertar en recurso
CREATE OR REPLACE FUNCTION validar_entidad_recurso()
RETURNS trigger AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM entidad WHERE id_entidad = NEW.id_entidad) THEN
    RAISE EXCEPTION 'La entidad asignada al recurso no existe.';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validar_entidad_recurso
BEFORE INSERT ON recurso
FOR EACH ROW
EXECUTE FUNCTION validar_entidad_recurso();

-- 3. Validar users al insertar en subida
CREATE OR REPLACE FUNCTION validar_users_subida()
RETURNS trigger AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM users WHERE id = NEW.id_usuario) THEN
    RAISE EXCEPTION 'El usuario asociado a la subida no existe.';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validar_users_subida
BEFORE INSERT ON subida
FOR EACH ROW
EXECUTE FUNCTION validar_users_subida();

-- 4. Evitar duplicado de contacts
CREATE OR REPLACE FUNCTION evitar_contacts_duplicados()
RETURNS trigger AS $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM contacts 
    WHERE user_id = NEW.user_id AND email = NEW.email
  ) THEN
    RAISE EXCEPTION 'Ya has agregado a este contacto.';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER evitar_contacts_duplicados
BEFORE INSERT ON contacts
FOR EACH ROW
EXECUTE FUNCTION evitar_contacts_duplicados();

-- 7. Borrar tokens asociados al eliminar users
CREATE OR REPLACE FUNCTION borrar_tokens_users()
RETURNS trigger AS $$
BEGIN
  DELETE FROM fcm_tokens WHERE users_id = OLD.id;
  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER borrar_tokens_users
BEFORE DELETE ON users
FOR EACH ROW
EXECUTE FUNCTION borrar_tokens_users();

-- 9. Poner fecha de subida automática
CREATE OR REPLACE FUNCTION poner_fecha_subida()
RETURNS trigger AS $$
BEGIN
  IF NEW.fecha_subida IS NULL THEN
    NEW.fecha_subida := NOW();
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER poner_fecha_subida
BEFORE INSERT ON subida
FOR EACH ROW
EXECUTE FUNCTION poner_fecha_subida();


-- 11. Evitar que un users se agregue a sí mismo como contacts
CREATE OR REPLACE FUNCTION evitar_autocontacts()
RETURNS trigger AS $$
DECLARE
  user_email TEXT;
BEGIN
  SELECT email INTO user_email FROM users WHERE id = NEW.user_id;
  IF NEW.email = user_email THEN
    RAISE EXCEPTION 'No puedes agregarte a ti mismo como contacto.';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER evitar_autocontacts
BEFORE INSERT ON contacts
FOR EACH ROW
EXECUTE FUNCTION evitar_autocontacts();

-- 12. Evitar eliminar entidad con recursos asociados
CREATE OR REPLACE FUNCTION evitar_eliminacion_entidad_con_recursos()
RETURNS trigger AS $$
BEGIN
  IF EXISTS (SELECT 1 FROM recurso WHERE id_entidad = OLD.id_entidad) THEN
    RAISE EXCEPTION 'No se puede eliminar una entidad que tiene recursos asociados.';
  END IF;
  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER evitar_eliminacion_entidad_con_recursos
BEFORE DELETE ON entidad
FOR EACH ROW
EXECUTE FUNCTION evitar_eliminacion_entidad_con_recursos();

--Eliminar Triggers
-- Borrar el trigger
DROP TRIGGER IF EXISTS evitar_contacts_duplicados ON contacts;

-- Borrar la función
DROP FUNCTION IF EXISTS evitar_contacts_duplicados();
-- Borrar el trigger
DROP TRIGGER IF EXISTS borrar_tokens_users ON users;

-- Borrar la función
DROP FUNCTION IF EXISTS borrar_tokens_users();
-- Borrar el trigger
DROP TRIGGER IF EXISTS evitar_autocontacts ON contacts;

-- Borrar la función
DROP FUNCTION IF EXISTS evitar_autocontacts();
-- Borrar el trigger
DROP TRIGGER IF EXISTS validar_users_subida ON subida;

-- Borrar la función
DROP FUNCTION IF EXISTS validar_users_subida();
