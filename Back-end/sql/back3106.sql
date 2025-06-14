PGDMP      7                }           postgres    17.2    17.4 >               0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                           false                       0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                           false                       0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                           false                       1262    5    postgres    DATABASE     t   CREATE DATABASE postgres WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.UTF-8';
    DROP DATABASE postgres;
                     postgres    false                       0    0    DATABASE postgres    COMMENT     N   COMMENT ON DATABASE postgres IS 'default administrative connection database';
                        postgres    false    4375            �            1255    16595    borrar_tokens_users()    FUNCTION     �   CREATE FUNCTION public.borrar_tokens_users() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  DELETE FROM fcm_tokens WHERE user_id = OLD.id;
  RETURN OLD;
END;
$$;
 ,   DROP FUNCTION public.borrar_tokens_users();
       public               postgres    false            �            1255    16479    borrar_tokens_usuario()    FUNCTION     �   CREATE FUNCTION public.borrar_tokens_usuario() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  DELETE FROM public.fcm_tokens WHERE usuario_id = OLD.id;
  RETURN OLD;
END;
$$;
 .   DROP FUNCTION public.borrar_tokens_usuario();
       public               postgres    false            �            1255    16480    evitar_autocontacts()    FUNCTION     F  CREATE FUNCTION public.evitar_autocontacts() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
  user_email TEXT;
BEGIN
  SELECT email INTO user_email FROM users WHERE id = NEW.user_id;
  IF NEW.email = user_email THEN
    RAISE EXCEPTION 'No puedes agregarte a ti mismo como contacto.';
  END IF;
  RETURN NEW;
END;
$$;
 ,   DROP FUNCTION public.evitar_autocontacts();
       public               postgres    false            �            1255    16590    evitar_contacts_duplicados()    FUNCTION     +  CREATE FUNCTION public.evitar_contacts_duplicados() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM contacts 
    WHERE user_id = NEW.user_id AND email = NEW.email
  ) THEN
    RAISE EXCEPTION 'Ya has agregado a este contacto.';
  END IF;
  RETURN NEW;
END;
$$;
 3   DROP FUNCTION public.evitar_contacts_duplicados();
       public               postgres    false            �            1255    16482 )   evitar_eliminacion_entidad_con_recursos()    FUNCTION     8  CREATE FUNCTION public.evitar_eliminacion_entidad_con_recursos() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF EXISTS (SELECT 1 FROM recurso WHERE id_entidad = OLD.id_entidad) THEN
    RAISE EXCEPTION 'No se puede eliminar una entidad que tiene recursos asociados.';
  END IF;
  RETURN OLD;
END;
$$;
 @   DROP FUNCTION public.evitar_eliminacion_entidad_con_recursos();
       public               postgres    false            �            1255    16483    notificar_contacts_subida()    FUNCTION        CREATE FUNCTION public.notificar_contacts_subida() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  UPDATE fcm_token
  SET updated_at = NOW()
  WHERE users_id IN (
    SELECT id FROM contacts WHERE users_id = NEW.id_users
  );
  RETURN NEW;
END;
$$;
 2   DROP FUNCTION public.notificar_contacts_subida();
       public               postgres    false            �            1255    16484    poner_fecha_subida()    FUNCTION     �   CREATE FUNCTION public.poner_fecha_subida() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF NEW.fecha_subida IS NULL THEN
    NEW.fecha_subida := NOW();
  END IF;
  RETURN NEW;
END;
$$;
 +   DROP FUNCTION public.poner_fecha_subida();
       public               postgres    false            �            1255    16485    validar_categoria_recurso()    FUNCTION       CREATE FUNCTION public.validar_categoria_recurso() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM categoria WHERE id_categoria = NEW.id_categoria) THEN
    RAISE EXCEPTION 'La categoría asignada no existe.';
  END IF;
  RETURN NEW;
END;
$$;
 2   DROP FUNCTION public.validar_categoria_recurso();
       public               postgres    false            �            1255    16486    validar_entidad_recurso()    FUNCTION       CREATE FUNCTION public.validar_entidad_recurso() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM entidad WHERE id_entidad = NEW.id_entidad) THEN
    RAISE EXCEPTION 'La entidad asignada al recurso no existe.';
  END IF;
  RETURN NEW;
END;
$$;
 0   DROP FUNCTION public.validar_entidad_recurso();
       public               postgres    false            �            1255    16601    validar_users_subida()    FUNCTION     	  CREATE FUNCTION public.validar_users_subida() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM users WHERE id = NEW.id_usuario) THEN
    RAISE EXCEPTION 'El users asociado a la subida no existe.';
  END IF;
  RETURN NEW;
END;
$$;
 -   DROP FUNCTION public.validar_users_subida();
       public               postgres    false            �            1259    16488 	   categoria    TABLE     w   CREATE TABLE public.categoria (
    id_categoria integer NOT NULL,
    img_categoria text,
    nombre text NOT NULL
);
    DROP TABLE public.categoria;
       public         heap r       postgres    false            �            1259    16493    categoria_id_categoria_seq    SEQUENCE     �   CREATE SEQUENCE public.categoria_id_categoria_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 1   DROP SEQUENCE public.categoria_id_categoria_seq;
       public               postgres    false    217                       0    0    categoria_id_categoria_seq    SEQUENCE OWNED BY     Y   ALTER SEQUENCE public.categoria_id_categoria_seq OWNED BY public.categoria.id_categoria;
          public               postgres    false    218            �            1259    16494    contacts    TABLE     �   CREATE TABLE public.contacts (
    id integer NOT NULL,
    user_id integer NOT NULL,
    name text NOT NULL,
    email text NOT NULL,
    contact_user_id integer
);
    DROP TABLE public.contacts;
       public         heap r       postgres    false            �            1259    16499    contacts_id_seq    SEQUENCE     �   CREATE SEQUENCE public.contacts_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 &   DROP SEQUENCE public.contacts_id_seq;
       public               postgres    false    219                       0    0    contacts_id_seq    SEQUENCE OWNED BY     C   ALTER SEQUENCE public.contacts_id_seq OWNED BY public.contacts.id;
          public               postgres    false    220            �            1259    16500    entidad    TABLE     �   CREATE TABLE public.entidad (
    id_entidad integer NOT NULL,
    imagen text,
    email text,
    telefono text,
    pagina_web text,
    direccion text,
    horario text
);
    DROP TABLE public.entidad;
       public         heap r       postgres    false            �            1259    16505    entidad_id_entidad_seq    SEQUENCE     �   CREATE SEQUENCE public.entidad_id_entidad_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 -   DROP SEQUENCE public.entidad_id_entidad_seq;
       public               postgres    false    221                       0    0    entidad_id_entidad_seq    SEQUENCE OWNED BY     Q   ALTER SEQUENCE public.entidad_id_entidad_seq OWNED BY public.entidad.id_entidad;
          public               postgres    false    222            �            1259    16506 
   fcm_tokens    TABLE     �   CREATE TABLE public.fcm_tokens (
    user_id integer NOT NULL,
    token text NOT NULL,
    updated_at timestamp with time zone DEFAULT now()
);
    DROP TABLE public.fcm_tokens;
       public         heap r       postgres    false            �            1259    16512    recurso    TABLE     P  CREATE TABLE public.recurso (
    id integer NOT NULL,
    id_entidad integer NOT NULL,
    id_categoria integer NOT NULL,
    imagen text,
    email text,
    telefono text,
    direccion text,
    horario text,
    servicio text,
    descripcion text,
    requisitos text,
    gratuito boolean,
    web text,
    accesible boolean
);
    DROP TABLE public.recurso;
       public         heap r       postgres    false            �            1259    16517    recurso_id_seq    SEQUENCE     �   CREATE SEQUENCE public.recurso_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 %   DROP SEQUENCE public.recurso_id_seq;
       public               postgres    false    224                       0    0    recurso_id_seq    SEQUENCE OWNED BY     A   ALTER SEQUENCE public.recurso_id_seq OWNED BY public.recurso.id;
          public               postgres    false    225            �            1259    16518    subida    TABLE     �   CREATE TABLE public.subida (
    id_subida integer NOT NULL,
    fecha_subida timestamp with time zone DEFAULT now(),
    id_usuario integer NOT NULL
);
    DROP TABLE public.subida;
       public         heap r       postgres    false            �            1259    16522    subida_id_subida_seq    SEQUENCE     �   CREATE SEQUENCE public.subida_id_subida_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 +   DROP SEQUENCE public.subida_id_subida_seq;
       public               postgres    false    226                       0    0    subida_id_subida_seq    SEQUENCE OWNED BY     M   ALTER SEQUENCE public.subida_id_subida_seq OWNED BY public.subida.id_subida;
          public               postgres    false    227            �            1259    16523    users    TABLE     �   CREATE TABLE public.users (
    id integer NOT NULL,
    email text NOT NULL,
    password text NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    nombre text
);
    DROP TABLE public.users;
       public         heap r       postgres    false            �            1259    16529    users_id_seq    SEQUENCE     �   CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 #   DROP SEQUENCE public.users_id_seq;
       public               postgres    false    228                       0    0    users_id_seq    SEQUENCE OWNED BY     =   ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;
          public               postgres    false    229            Z           2604    16530    categoria id_categoria    DEFAULT     �   ALTER TABLE ONLY public.categoria ALTER COLUMN id_categoria SET DEFAULT nextval('public.categoria_id_categoria_seq'::regclass);
 E   ALTER TABLE public.categoria ALTER COLUMN id_categoria DROP DEFAULT;
       public               postgres    false    218    217            [           2604    16531    contacts id    DEFAULT     j   ALTER TABLE ONLY public.contacts ALTER COLUMN id SET DEFAULT nextval('public.contacts_id_seq'::regclass);
 :   ALTER TABLE public.contacts ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    220    219            \           2604    16532    entidad id_entidad    DEFAULT     x   ALTER TABLE ONLY public.entidad ALTER COLUMN id_entidad SET DEFAULT nextval('public.entidad_id_entidad_seq'::regclass);
 A   ALTER TABLE public.entidad ALTER COLUMN id_entidad DROP DEFAULT;
       public               postgres    false    222    221            ^           2604    16533 
   recurso id    DEFAULT     h   ALTER TABLE ONLY public.recurso ALTER COLUMN id SET DEFAULT nextval('public.recurso_id_seq'::regclass);
 9   ALTER TABLE public.recurso ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    225    224            _           2604    16534    subida id_subida    DEFAULT     t   ALTER TABLE ONLY public.subida ALTER COLUMN id_subida SET DEFAULT nextval('public.subida_id_subida_seq'::regclass);
 ?   ALTER TABLE public.subida ALTER COLUMN id_subida DROP DEFAULT;
       public               postgres    false    227    226            a           2604    16535    users id    DEFAULT     d   ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);
 7   ALTER TABLE public.users ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    229    228            d           2606    16539    categoria categoria_pkey 
   CONSTRAINT     `   ALTER TABLE ONLY public.categoria
    ADD CONSTRAINT categoria_pkey PRIMARY KEY (id_categoria);
 B   ALTER TABLE ONLY public.categoria DROP CONSTRAINT categoria_pkey;
       public                 postgres    false    217            f           2606    16541    contacts contacts_pkey 
   CONSTRAINT     T   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_pkey PRIMARY KEY (id);
 @   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_pkey;
       public                 postgres    false    219            h           2606    16543    entidad entidad_pkey 
   CONSTRAINT     Z   ALTER TABLE ONLY public.entidad
    ADD CONSTRAINT entidad_pkey PRIMARY KEY (id_entidad);
 >   ALTER TABLE ONLY public.entidad DROP CONSTRAINT entidad_pkey;
       public                 postgres    false    221            j           2606    16545    fcm_tokens fcm_tokens_pkey 
   CONSTRAINT     ]   ALTER TABLE ONLY public.fcm_tokens
    ADD CONSTRAINT fcm_tokens_pkey PRIMARY KEY (user_id);
 D   ALTER TABLE ONLY public.fcm_tokens DROP CONSTRAINT fcm_tokens_pkey;
       public                 postgres    false    223            l           2606    16547    recurso recurso_pkey 
   CONSTRAINT     R   ALTER TABLE ONLY public.recurso
    ADD CONSTRAINT recurso_pkey PRIMARY KEY (id);
 >   ALTER TABLE ONLY public.recurso DROP CONSTRAINT recurso_pkey;
       public                 postgres    false    224            n           2606    16549    subida subida_pkey 
   CONSTRAINT     W   ALTER TABLE ONLY public.subida
    ADD CONSTRAINT subida_pkey PRIMARY KEY (id_subida);
 <   ALTER TABLE ONLY public.subida DROP CONSTRAINT subida_pkey;
       public                 postgres    false    226            p           2606    16551    users users_email_key 
   CONSTRAINT     Q   ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);
 ?   ALTER TABLE ONLY public.users DROP CONSTRAINT users_email_key;
       public                 postgres    false    228            r           2606    16553    users users_pkey 
   CONSTRAINT     N   ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);
 :   ALTER TABLE ONLY public.users DROP CONSTRAINT users_pkey;
       public                 postgres    false    228            �           2620    16596    users borrar_tokens_users    TRIGGER     }   CREATE TRIGGER borrar_tokens_users BEFORE DELETE ON public.users FOR EACH ROW EXECUTE FUNCTION public.borrar_tokens_users();
 2   DROP TRIGGER borrar_tokens_users ON public.users;
       public               postgres    false    228    236            y           2620    16597    contacts evitar_autocontacts    TRIGGER     �   CREATE TRIGGER evitar_autocontacts BEFORE INSERT ON public.contacts FOR EACH ROW EXECUTE FUNCTION public.evitar_autocontacts();
 5   DROP TRIGGER evitar_autocontacts ON public.contacts;
       public               postgres    false    219    237            z           2620    16591 #   contacts evitar_contacts_duplicados    TRIGGER     �   CREATE TRIGGER evitar_contacts_duplicados BEFORE INSERT ON public.contacts FOR EACH ROW EXECUTE FUNCTION public.evitar_contacts_duplicados();
 <   DROP TRIGGER evitar_contacts_duplicados ON public.contacts;
       public               postgres    false    219    235            {           2620    16598 /   entidad evitar_eliminacion_entidad_con_recursos    TRIGGER     �   CREATE TRIGGER evitar_eliminacion_entidad_con_recursos BEFORE DELETE ON public.entidad FOR EACH ROW EXECUTE FUNCTION public.evitar_eliminacion_entidad_con_recursos();
 H   DROP TRIGGER evitar_eliminacion_entidad_con_recursos ON public.entidad;
       public               postgres    false    232    221            ~           2620    16603    subida poner_fecha_subida    TRIGGER     |   CREATE TRIGGER poner_fecha_subida BEFORE INSERT ON public.subida FOR EACH ROW EXECUTE FUNCTION public.poner_fecha_subida();
 2   DROP TRIGGER poner_fecha_subida ON public.subida;
       public               postgres    false    233    226            |           2620    16604 !   recurso validar_categoria_recurso    TRIGGER     �   CREATE TRIGGER validar_categoria_recurso BEFORE INSERT ON public.recurso FOR EACH ROW EXECUTE FUNCTION public.validar_categoria_recurso();
 :   DROP TRIGGER validar_categoria_recurso ON public.recurso;
       public               postgres    false    224    234            }           2620    16605    recurso validar_entidad_recurso    TRIGGER     �   CREATE TRIGGER validar_entidad_recurso BEFORE INSERT ON public.recurso FOR EACH ROW EXECUTE FUNCTION public.validar_entidad_recurso();
 8   DROP TRIGGER validar_entidad_recurso ON public.recurso;
       public               postgres    false    224    239                       2620    16602    subida validar_users_subida    TRIGGER     �   CREATE TRIGGER validar_users_subida BEFORE INSERT ON public.subida FOR EACH ROW EXECUTE FUNCTION public.validar_users_subida();
 4   DROP TRIGGER validar_users_subida ON public.subida;
       public               postgres    false    226    238            s           2606    16554 &   contacts contacts_contact_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_contact_user_id_fkey FOREIGN KEY (contact_user_id) REFERENCES public.users(id);
 P   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_contact_user_id_fkey;
       public               postgres    false    228    4210    219            t           2606    16559    contacts contacts_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
 H   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_user_id_fkey;
       public               postgres    false    228    4210    219            u           2606    16564 "   fcm_tokens fcm_tokens_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.fcm_tokens
    ADD CONSTRAINT fcm_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);
 L   ALTER TABLE ONLY public.fcm_tokens DROP CONSTRAINT fcm_tokens_user_id_fkey;
       public               postgres    false    4210    223    228            v           2606    16569    recurso fk_categoria    FK CONSTRAINT     �   ALTER TABLE ONLY public.recurso
    ADD CONSTRAINT fk_categoria FOREIGN KEY (id_categoria) REFERENCES public.categoria(id_categoria) ON DELETE CASCADE;
 >   ALTER TABLE ONLY public.recurso DROP CONSTRAINT fk_categoria;
       public               postgres    false    4196    224    217            w           2606    16574    recurso fk_entidad    FK CONSTRAINT     �   ALTER TABLE ONLY public.recurso
    ADD CONSTRAINT fk_entidad FOREIGN KEY (id_entidad) REFERENCES public.entidad(id_entidad) ON DELETE CASCADE;
 <   ALTER TABLE ONLY public.recurso DROP CONSTRAINT fk_entidad;
       public               postgres    false    4200    221    224            x           2606    16579    subida fk_usuario_subida    FK CONSTRAINT     �   ALTER TABLE ONLY public.subida
    ADD CONSTRAINT fk_usuario_subida FOREIGN KEY (id_usuario) REFERENCES public.users(id) ON DELETE CASCADE;
 B   ALTER TABLE ONLY public.subida DROP CONSTRAINT fk_usuario_subida;
       public               postgres    false    226    228    4210           