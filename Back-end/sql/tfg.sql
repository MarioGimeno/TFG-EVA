PGDMP  .    *                }           postgres    17.2    17.4 9               0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                           false                       0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                           false                       0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                           false                       1262    5    postgres    DATABASE     t   CREATE DATABASE postgres WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.UTF-8';
    DROP DATABASE postgres;
                     postgres    false                       0    0    DATABASE postgres    COMMENT     N   COMMENT ON DATABASE postgres IS 'default administrative connection database';
                        postgres    false    4370            �            1259    16604 	   categoria    TABLE     _   CREATE TABLE public.categoria (
    id_categoria integer NOT NULL,
    nombre text NOT NULL
);
    DROP TABLE public.categoria;
       public         heap r       postgres    false            �            1259    16603    categoria_id_categoria_seq    SEQUENCE     �   CREATE SEQUENCE public.categoria_id_categoria_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 1   DROP SEQUENCE public.categoria_id_categoria_seq;
       public               postgres    false    227                       0    0    categoria_id_categoria_seq    SEQUENCE OWNED BY     Y   ALTER SEQUENCE public.categoria_id_categoria_seq OWNED BY public.categoria.id_categoria;
          public               postgres    false    226            �            1259    16516    contacts    TABLE     �   CREATE TABLE public.contacts (
    id integer NOT NULL,
    user_id integer NOT NULL,
    name text NOT NULL,
    email text NOT NULL,
    contact_user_id integer
);
    DROP TABLE public.contacts;
       public         heap r       postgres    false            �            1259    16515    contacts_id_seq    SEQUENCE     �   CREATE SEQUENCE public.contacts_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 &   DROP SEQUENCE public.contacts_id_seq;
       public               postgres    false    220                       0    0    contacts_id_seq    SEQUENCE OWNED BY     C   ALTER SEQUENCE public.contacts_id_seq OWNED BY public.contacts.id;
          public               postgres    false    219            �            1259    16572    entidad    TABLE     �   CREATE TABLE public.entidad (
    id_entidad integer NOT NULL,
    imagen text,
    email text,
    telefono text,
    pagina_web text,
    direccion text,
    horario text
);
    DROP TABLE public.entidad;
       public         heap r       postgres    false            �            1259    16571    entidad_id_entidad_seq    SEQUENCE     �   CREATE SEQUENCE public.entidad_id_entidad_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 -   DROP SEQUENCE public.entidad_id_entidad_seq;
       public               postgres    false    225                       0    0    entidad_id_entidad_seq    SEQUENCE OWNED BY     Q   ALTER SEQUENCE public.entidad_id_entidad_seq OWNED BY public.entidad.id_entidad;
          public               postgres    false    224            �            1259    16529 
   fcm_tokens    TABLE     �   CREATE TABLE public.fcm_tokens (
    user_id integer NOT NULL,
    token text NOT NULL,
    updated_at timestamp with time zone DEFAULT now()
);
    DROP TABLE public.fcm_tokens;
       public         heap r       postgres    false            �            1259    16632    recurso    TABLE     P  CREATE TABLE public.recurso (
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
       public         heap r       postgres    false            �            1259    16631    recurso_id_seq    SEQUENCE     �   CREATE SEQUENCE public.recurso_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 %   DROP SEQUENCE public.recurso_id_seq;
       public               postgres    false    229                       0    0    recurso_id_seq    SEQUENCE OWNED BY     A   ALTER SEQUENCE public.recurso_id_seq OWNED BY public.recurso.id;
          public               postgres    false    228            �            1259    16548    tokens    TABLE     �   CREATE TABLE public.tokens (
    id integer NOT NULL,
    user_id integer NOT NULL,
    token text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);
    DROP TABLE public.tokens;
       public         heap r       postgres    false            �            1259    16547    tokens_id_seq    SEQUENCE     �   CREATE SEQUENCE public.tokens_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 $   DROP SEQUENCE public.tokens_id_seq;
       public               postgres    false    223                       0    0    tokens_id_seq    SEQUENCE OWNED BY     ?   ALTER SEQUENCE public.tokens_id_seq OWNED BY public.tokens.id;
          public               postgres    false    222            �            1259    16504    users    TABLE     �   CREATE TABLE public.users (
    id integer NOT NULL,
    email text NOT NULL,
    password text NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);
    DROP TABLE public.users;
       public         heap r       postgres    false            �            1259    16503    users_id_seq    SEQUENCE     �   CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 #   DROP SEQUENCE public.users_id_seq;
       public               postgres    false    218                       0    0    users_id_seq    SEQUENCE OWNED BY     =   ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;
          public               postgres    false    217            W           2604    16607    categoria id_categoria    DEFAULT     �   ALTER TABLE ONLY public.categoria ALTER COLUMN id_categoria SET DEFAULT nextval('public.categoria_id_categoria_seq'::regclass);
 E   ALTER TABLE public.categoria ALTER COLUMN id_categoria DROP DEFAULT;
       public               postgres    false    227    226    227            R           2604    16519    contacts id    DEFAULT     j   ALTER TABLE ONLY public.contacts ALTER COLUMN id SET DEFAULT nextval('public.contacts_id_seq'::regclass);
 :   ALTER TABLE public.contacts ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    220    219    220            V           2604    16575    entidad id_entidad    DEFAULT     x   ALTER TABLE ONLY public.entidad ALTER COLUMN id_entidad SET DEFAULT nextval('public.entidad_id_entidad_seq'::regclass);
 A   ALTER TABLE public.entidad ALTER COLUMN id_entidad DROP DEFAULT;
       public               postgres    false    225    224    225            X           2604    16635 
   recurso id    DEFAULT     h   ALTER TABLE ONLY public.recurso ALTER COLUMN id SET DEFAULT nextval('public.recurso_id_seq'::regclass);
 9   ALTER TABLE public.recurso ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    228    229    229            T           2604    16551 	   tokens id    DEFAULT     f   ALTER TABLE ONLY public.tokens ALTER COLUMN id SET DEFAULT nextval('public.tokens_id_seq'::regclass);
 8   ALTER TABLE public.tokens ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    222    223    223            P           2604    16507    users id    DEFAULT     d   ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);
 7   ALTER TABLE public.users ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    218    217    218            
          0    16604 	   categoria 
   TABLE DATA           9   COPY public.categoria (id_categoria, nombre) FROM stdin;
    public               postgres    false    227   �@                 0    16516    contacts 
   TABLE DATA           M   COPY public.contacts (id, user_id, name, email, contact_user_id) FROM stdin;
    public               postgres    false    220   �A                 0    16572    entidad 
   TABLE DATA           f   COPY public.entidad (id_entidad, imagen, email, telefono, pagina_web, direccion, horario) FROM stdin;
    public               postgres    false    225   ;B                 0    16529 
   fcm_tokens 
   TABLE DATA           @   COPY public.fcm_tokens (user_id, token, updated_at) FROM stdin;
    public               postgres    false    221   �F                 0    16632    recurso 
   TABLE DATA           �   COPY public.recurso (id, id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) FROM stdin;
    public               postgres    false    229   I                 0    16548    tokens 
   TABLE DATA           @   COPY public.tokens (id, user_id, token, created_at) FROM stdin;
    public               postgres    false    223   �t                 0    16504    users 
   TABLE DATA           @   COPY public.users (id, email, password, created_at) FROM stdin;
    public               postgres    false    218   �t                  0    0    categoria_id_categoria_seq    SEQUENCE SET     I   SELECT pg_catalog.setval('public.categoria_id_categoria_seq', 13, true);
          public               postgres    false    226                       0    0    contacts_id_seq    SEQUENCE SET     =   SELECT pg_catalog.setval('public.contacts_id_seq', 8, true);
          public               postgres    false    219                       0    0    entidad_id_entidad_seq    SEQUENCE SET     E   SELECT pg_catalog.setval('public.entidad_id_entidad_seq', 14, true);
          public               postgres    false    224                       0    0    recurso_id_seq    SEQUENCE SET     =   SELECT pg_catalog.setval('public.recurso_id_seq', 53, true);
          public               postgres    false    228                       0    0    tokens_id_seq    SEQUENCE SET     <   SELECT pg_catalog.setval('public.tokens_id_seq', 1, false);
          public               postgres    false    222                       0    0    users_id_seq    SEQUENCE SET     :   SELECT pg_catalog.setval('public.users_id_seq', 6, true);
          public               postgres    false    217            f           2606    16611    categoria categoria_pkey 
   CONSTRAINT     `   ALTER TABLE ONLY public.categoria
    ADD CONSTRAINT categoria_pkey PRIMARY KEY (id_categoria);
 B   ALTER TABLE ONLY public.categoria DROP CONSTRAINT categoria_pkey;
       public                 postgres    false    227            ^           2606    16523    contacts contacts_pkey 
   CONSTRAINT     T   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_pkey PRIMARY KEY (id);
 @   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_pkey;
       public                 postgres    false    220            d           2606    16579    entidad entidad_pkey 
   CONSTRAINT     Z   ALTER TABLE ONLY public.entidad
    ADD CONSTRAINT entidad_pkey PRIMARY KEY (id_entidad);
 >   ALTER TABLE ONLY public.entidad DROP CONSTRAINT entidad_pkey;
       public                 postgres    false    225            `           2606    16536    fcm_tokens fcm_tokens_pkey 
   CONSTRAINT     ]   ALTER TABLE ONLY public.fcm_tokens
    ADD CONSTRAINT fcm_tokens_pkey PRIMARY KEY (user_id);
 D   ALTER TABLE ONLY public.fcm_tokens DROP CONSTRAINT fcm_tokens_pkey;
       public                 postgres    false    221            h           2606    16639    recurso recurso_pkey 
   CONSTRAINT     R   ALTER TABLE ONLY public.recurso
    ADD CONSTRAINT recurso_pkey PRIMARY KEY (id);
 >   ALTER TABLE ONLY public.recurso DROP CONSTRAINT recurso_pkey;
       public                 postgres    false    229            b           2606    16556    tokens tokens_pkey 
   CONSTRAINT     P   ALTER TABLE ONLY public.tokens
    ADD CONSTRAINT tokens_pkey PRIMARY KEY (id);
 <   ALTER TABLE ONLY public.tokens DROP CONSTRAINT tokens_pkey;
       public                 postgres    false    223            Z           2606    16514    users users_email_key 
   CONSTRAINT     Q   ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);
 ?   ALTER TABLE ONLY public.users DROP CONSTRAINT users_email_key;
       public                 postgres    false    218            \           2606    16512    users users_pkey 
   CONSTRAINT     N   ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);
 :   ALTER TABLE ONLY public.users DROP CONSTRAINT users_pkey;
       public                 postgres    false    218            i           2606    16542 &   contacts contacts_contact_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_contact_user_id_fkey FOREIGN KEY (contact_user_id) REFERENCES public.users(id);
 P   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_contact_user_id_fkey;
       public               postgres    false    220    218    4188            j           2606    16524    contacts contacts_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
 H   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_user_id_fkey;
       public               postgres    false    220    4188    218            k           2606    16537 "   fcm_tokens fcm_tokens_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.fcm_tokens
    ADD CONSTRAINT fcm_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);
 L   ALTER TABLE ONLY public.fcm_tokens DROP CONSTRAINT fcm_tokens_user_id_fkey;
       public               postgres    false    221    218    4188            m           2606    16645    recurso fk_categoria    FK CONSTRAINT     �   ALTER TABLE ONLY public.recurso
    ADD CONSTRAINT fk_categoria FOREIGN KEY (id_categoria) REFERENCES public.categoria(id_categoria) ON DELETE CASCADE;
 >   ALTER TABLE ONLY public.recurso DROP CONSTRAINT fk_categoria;
       public               postgres    false    4198    227    229            n           2606    16640    recurso fk_entidad    FK CONSTRAINT     �   ALTER TABLE ONLY public.recurso
    ADD CONSTRAINT fk_entidad FOREIGN KEY (id_entidad) REFERENCES public.entidad(id_entidad) ON DELETE CASCADE;
 <   ALTER TABLE ONLY public.recurso DROP CONSTRAINT fk_entidad;
       public               postgres    false    4196    225    229            l           2606    16557    tokens tokens_user_id_fkey    FK CONSTRAINT     y   ALTER TABLE ONLY public.tokens
    ADD CONSTRAINT tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);
 D   ALTER TABLE ONLY public.tokens DROP CONSTRAINT tokens_user_id_fkey;
       public               postgres    false    218    4188    223            
   �   x�E�=NA�k�s�$�L(�"RZgƉ�v�+�.�p'*�����"�l���gGx6'��
��zJr�.ᕎj��ӱJ��L�Sr���iqk�[�*ے�dh���;ض)S��h/�*�Á�C�P��d��� �=ۙK7�����ނ�p�1�v����u`u�I�0�`oz6꽶���q���kؓQت����&��)��"� ��e�         p   x�3�4��M,����M��M���K,K�r��/�M���K�����2�*(*MMJD�@ac,�F\&pQ#8m�e�i��(C.3��)�6�2��L�	��g �~�td��qqq 3<�         w  x��VMo�8=ۿb.��-��,[>��d�)�ƨ�.P�2�X��D
�� �5�c�i�����G�f������9�f޼!���RO�A�KL��<�+����u��^����+���KT1���S�/�^y��:	})���+��=z\w�q�a~�wnoo�#�@�JU�1��j#��V������A��J�TzЙc�s8U��(!�0�ö�{ #��|it>Ԓk@�^�7�Ӓ���An�wP���*Q���h�fO���<���J�&�90��΅�!�Kr���y����ҟ��J��$%U�t�*kA9^6�9��xW��7�P��z6a�B
����e��ԡ��?��R:�*W�a�y6Oy��}B�����xyq��e����T{O�B��IV��ͦN4
 '��%�ٷ�f�=�k� �&
fr-�Z�?���ɪ�u�e�qv� ��N�� _Ȕf8	`-��DEs���I\�<=`c��l����oj�q�O���ph�w��'c����ѳ;jW��ř���׼�˒g��Wjly���A��(�n�*���|��7��D蛯{�9bЄ� ��%���w����5Wp��xF��5צ1���{��I�ߠ?O��b2�u��$�l1;w��#Lx�����Q8�8�X�G�نK�����J�0��$�*!!�^��'l����i�*���h�T��Nڕu�i�r5nRa�t�����FA�jz09=���'�cQ*,TEZ0r��&���n�*�S�U��~~�&� -NQ
�Hi0�����Z�� �(ܲ�D�7?YA�H��g\��g'tl��o��"t�G5�q)?v�Z:�Q�ҿ��j��2�X+��j���sS�A-�`�fΘX#y�mRG��w��:�i��`�w��t��d��c�WB�K���Elw��N��a�zV`�*�2��i�`����ɺ���͞��)�{����C���k:��b�]?~W�k��]C���^B��������Z���o��Ǒ=�?<vY�	����^�J��� 4���Ϯw��ao��'G�Z03E�>и����d��5��"�Wx�nR�7cZ`q��ݜ��=9����ٌ��3�}���9�5���gЌ���':t��n��/�`��         ;  x���ۮ�@ ��k}�}�hf�w���E�(�	Q�At8��w���4Y���bgW&��)�J7T`��(Y� /�ٚM�[�t�;������;�P&7-[��́���MO^{�^j�Z>F�o{v�,=O2th[6٥����{��D2Ô���~S��(f�����%�Q<�0G��ԑ�m�A�RV�������8�FI
UEuQ�:t/dM�)���8��wr�i���Fm��t�k��a������z�0���P�q� �@����Zo���Z���p��E$���M����m]�r�rK�3.Bo.�����!�U��y�%��n�_C�ց/����j��f/�Ӆq���$75�m���ei��}z�Җ�y��z���C��4mME�~���oL��*<<�����)�d�r̛���r\���8/jz�L��W⍿�pΉD��Sx���b�.�9j�yrjv�Pjn,�lE�ͥ3��y�%�6���y7S�y-��$I���.�G��D���A�Wݡ�WBlG�S�3-!ZҀ�x�������?�`�            x��}͎G���4� �E�W��Ke[�d�d�@@#���J!���dV�Z�G����`О� ��1��b /f�z?�}�9�9'"#Y�$S���2`C,23���o;��n�z��Wvv��Vs;�\�N����Q��z���a���+WؿU����<*�\9ó�x�Yb��bڙ����i��+[ک{e�i�9><0��f�7��<O�CW�/j[�$5cz����kv��������r�aj�EZk�gi�O��1�9�3�f��.͓���r�ry`x�`4�C���ŋ�07iiw/Is3�#/�<s��f��t������L��}ӊiZ+{�\��]�Lٍ3�����g��]3~v��C|xz����g�s�O��~��>Y�[3���&���Κk��t����&�_���˴ZXC��ﱤ'8e��G���4F���ҙ��]�Q:�[�4J�N겢��Y�H�-1-0X�Ngn[��ռ�)��$���h�	���E�P�!�U6q��yp�XjE��-�O��*�da�~�"+hؿ����"������~�);U��;~�;��βE�3:��;W�%t=�
]Ǵk����̒l� �����.o���;1ٻ��*:�<{ExIk��vQg��
6���"a��E��c��g2a��6�E��W4fY�83w����L�drB��s=z)s%MYfvd5����|���aLCtx�Q�5�)��0h���τ�'$,����ӗD#D�K'Ko�L��� ��ѐ���y�t_�a2zH�L��H�]s��ޤ&�$^�R�PL��J`,ȵЙ谱O:X�0���04�,Mh�U�fz<�N�M�����sZ'X���h�z:�� ?	+�m0�� &�'`Jt?�>B8G��&���z�'�5�b:N���78*���d1{��e�2C#�~J�LAOUY����$��1g�\���Vٴh�v1����o�����2a�.�����;�K�e6�I�gD[���D�N����ɺ�&vE3.l�V��,q�1p'�fo����6��2�#���͍���������s�M�c���~��wh��0z|x#�&��XC,��N1�^v��:�гK�Y��*Aȕt~�\���4�P��p3�]�����Z�����Q��+�NFM;��D���Y�Ȅ^�S]��쥫v��? ��q�IZ�:p0v�p$��#�<�
1����%��q�c���ì�;R|�b�<���
"�����7��j����P	P��
�ǳ��2��a�(w����_���F�/�'�9x'Vv����8�ad��x�,,����	�`�܈X��$̓��V�If�)XY��X�	�J֔b����=)-E�oΈ�їaI*�s,�ry-+&�+ﰺ��3bU�0=�5�ꎿ=w@�z��~	a! �3i3��	����2Es ��(��s�.���%#D�sB�{-���SBo �KV���fn�ﮨ="������g��d��2=�8<�=��:��S10,c[��J�둰����~��L����n�mŒ�?�2����%C��4�u�3C6qz����	A�Gv�<�`�c�v\�u��V�{b��zv��n��虁�H�:s��CY����e;��ۼ�����,��.X��7�-W�I�/q;z3w/UFU��n2X��0UԢ^��IF�3���4�g1X�:6C�|�r�5/�3?T%;؊�4�ӵ��o��m�2=�&�#o�G�T�Ʀ�:8N��*\5���z�T_�����b�K�Kܡ�	!&x��"Se��c��d�q~Ul?�A���>���[N���q����t�[B��zٓ�$UL�����ȿv�K��O���Ĥ���u0� ؑ�Z��O���)���p �d�\D��a���/��'�/�]���3Q�j�o�,"��?�g�������%�p��5�yy��M*'C\�Ne"aDaH{���`K�1�;�e���Vi��KT��s�j�i�k{�e�msb�u��z����D����� ����o������F��8���ΙKҙC��F������C�]º�?�z*l��0I
���l�B���%vi��W�U|h��!�`�%�)ċF���^f�A�5HsI�J#G�x��[Y����+J~��U����>즦�.qu�YZ��&��P��e���wJx�]�@�_�(C���*s�Qx��;�G�ee�_d&j��>���D�Xc�ȥ#�G�x@0���j���Bbu*�$�i+���u�릿�
jդ{jt�Lo2o$��V8�D)࡬���$�W�R�:W?7l>��t;�����R	��E�.�ʓ����΀��[���uW!��@�����a?�� b@<�R �{6�c�s'����n�NHj�7�������)������L c�
���a��@�r�z��[�~'
 G�'$�%Z�$H�S�o�_���F���2�d�*�������eA�������K�2_�F�1��/]���;��XL��K(Xo�îw(�s��"w<�%Qw�x{�q�4Ý��`���=R�M�B��O$���$�iSB����ZOT�����4�B��ɦ�)�����~�;!4+�.�%-�D��Cz�Bt���a�������է�'L�L��tt�9���n�5c���o+3�w����3$��rw��;_>7(�oiK�j6�2���=Lu���.o�a@����K��+����������Es>f>���|rb���&}�?�����;��p;��[�J4:���CP����ٯ��+��O�3��x�s�?2�{G�`�9���6�@ҤH`��E6��]����4Yg��<��2~��E�;O�܀��4�D���V��u�L���i�zO�Id:!奚�"#"e2��c)^1�I�S6*WcӼ�����x�����~e�ow�> ��#���e�P����7&��1�lx�����N��.�S��*tKx�K�NـUE�]����mG0���÷�4�qYR��Ş�pV���Y���yJ�(��uD�+U^���&X�&y�z�<i��^��3��ݬ�oz�G���K�^c��N��G<u쟥e�~\�����&5�b�����!�+S�X�t8��ux�9>����A]�&���V=����8����^ef�=�w�}""C������!ʤ���E-w�1?z8������DC]L1u^�2)�&����".'�:H6,\B��S���T�a\��VMl�ꠗh�&,�К e�&n��SiK��&�آJ擳��?A.�����&��Jb�'n�U�Ò]s�\O�I������Ŀ݌�&�^�~�������<ۅ��Ӭ� o�a�
���8du~��ˋ��GO�4��~z>~L@�e��Z��N�y�j�iV�*�+pZ�Po[�V�2M���UF?�o'1�%�2��E��	��yى�rot������3��!���O���nbӝ���/�^����ӧ�O� u�>9{���\iӜ��c_��o㯞�o��~y1��Ny���%m=ZKY`�����)���Qð�=���]u���P����������p����:���=z0`W��]��[��vvbO�z���<jO�N����4B-��ğ�fn�C���g�*�_�m�m��oEKљ��p!�.��[�|�2��C��!�0�u7�$2���I��9�k<�8���$�
S1��@v���Џt��u&ƻQg�}%P�@�`��|%~+(��|'.KKd$i>��Q�%�ZX�F���TuQ<K��]Q-�:c�����a�<8�5�	O0\5���û*;�7��_IB��DE%��&H��1O�(��Xa�A����6�h��d_��劾Js��2���];t[�a��au�}�c�^gK��E}I<"sϕr�;w0�Lo(���h�3¸�q��?�z��d[D��F�Y���<+R�r�>�N�ԥ>1���k9Μ��sʂ�Uog�j�7pץ�K["������JXb�>/t����#���Z-� Uh�
�.W+�%C"��b�T�ǌW�+7髮Pp���ڿ    C'r+5O}Ri��Y���%1r�Т"5����g�"
u�}���"+|^[��RF��ie�7.��Us����;Ȃ��K�z����̲~�}��ñz�rE��m�z�m������d�O5 ����d\e����`2­�����/X>�7��0�F�Gv�U:���x�������a�����I�<�g���!�A�]�����K[[�0
�ǫ^de�&�����j!e_VWq��L�Y�K�RX	c�1p���ࢯ��B�*sx�L����fK�&^2�v�3��� zn�:��~�U�X<���Е%�i/bDs\�<;;�HO����)��P�)ܢUrH���U�����5\����|Յ�JĂd�Ή�)��3�Ҭ��J�6�2�M:Y�	�0�����,�#��Y��|�I�Yj�jqvX&<���0Q��NN%�̖�g��K� ��+����I�W�M�dc���.kh~2�I(s`9��؊�)ۨ�LȻh)�"�>�Jn{]��x�x�}��s��m��cQ��0��F_3'�o��A0	��Λ�B��7|��g��$q�|z�ˤ3|�&�e������T.MF��MJu�KZ����B~=xb�Q���8��Hr��Y���e=M�>׋�D���e�k��cV�~�)dA?���z�@5��\8ƅw%��UM�80�|Z�2'1q`-�H�\�	yZ 	�IM�A�<({�4o�{��>�lN
l�16t}N%#���"�j�/Z��HN��=��|����*��P?w��>��(����}�ck�U��Vs�z��(v���P3�;�`n�?�Z�<gRRy��E:dF�����i��1�d����)	��ptp�h��D����o$�����1q�0z��?�L������h}�����#�"��0�v�L���g������%�+��'N#w�T�iM�E3��I�����&��)����&�|8��~��ϩ|���I�~ +�	x�:}[�"���"6s���a�ϢJ�4|���*�N��`D�j�>�{�F�8�3�`/^=O��o�P#XbM���Y��½ \�7`'�
ʳ�E�����a���!wIV=k@�sv��e�yh�T����'��9����[�~H�2u�l�r��rC�6���h�fo�k��}�O�@@˕1�jژQ�ćJ$H�T7���|�s�m�ęџ ��d�˫���h��`82{��3[�rt:�N��,RM/9�`8�W��f��f8P.vH�vcwm�Κ~ǯ�|������-��)N��Ĩ�k�'X9]�����y��te��_!o��Һ�gx�pp�x�$A�u%皽�	�A�η�����#��ԗ�|�Š�4d1�ہ�3��'�w�X�.�����Թͪ�Q*��$��l����v���Qg����=!��%�wL&9Y��9�tŽ.�i�/�%��Fo���W�F�F�+��@�=L`��Rk�����ϲ)ޕ$XA��.�U&B>u�сyY�S4a�.�6W�N�VW��ҫܝ��������<-�se�hra����FJ�4+��3�wJ` 2�HX\�_�:G�:�k{�d�'��q���r���1k4�7�����Nr���%��ʮ�ݗ@��qsNF��#Ӌ���:�VeP[�fւ�eP�KK�g;���k�����>
�H��@~����ͼ�7����y]��H�3�������T$�A�4�<b;�RK����cdsM6�����">iɠw��GU�������3�n�6���s�$�3�֚q��������a���)qE_��	6'!��a�䆋�-Pi���
�]�mZi0XP�'�����s�-���M(&�Z>rE�Ӿ�Rvw���n�	 ���Ο�-�~Q,[��ttk�Ԝ~X��+=U����<zI�"&���U�1��"R��Y��"�bq3�Ʉ��EA߉����$ӆ>�_ڝh�:"���Á��_��zIJ�1��ED(~T�8-|g{!��V��@��,s@����\��sI��Q �]"^|D�?���Xv�'mBJ�k#R����9�Pؗ�p�������tıK&I����=����[V��7���l������ض3e���Uԅ��}7B\H���4�v�{ ��UӞ8"��Q��$���=�D�#/�u��W#
!� lW�:B,�3��pf4\P�W=.P�D�Vu��c�$c���:�:w������dM��b��1�d�h�2Z���
ƃ@�)��@k
�u��gb�K��o�˺FY!,��K�1�_/�� �N�DWQ������A&a��s�܁�k#<>���L	+�Z������
,�WLͬ\15�B.��9���r ��t$��FD$nP/��>���fv	�� D��W�MR@����C�6�L��|�r����Dn_����ID��ܹ&ފ^w���ْq~�)x9�wi���V���}�����HW�1�\#��s@�l���"��9ħQщ&�Y�����)��B�&/�ͶO'q�]f�����Q	E�&R�4���?2�}�Պ�����H�_�����)bҌ�^|��~���3�^0S�N�\ Ÿ�gZu����39-+4��h�v�� �E��d1NZ�]9@�'ל�6�\)��v��ZS��{�j�$�����!��Vv��O#G�B=��[_�����x�����q�X��P�E��έ�P�%�%�b�4�� �n����^݂
,!��O*y$�e�B^*
�)l؆�wA���t0}�ΫTzG�SB	.�L�J[���M���Z55�<�o�%�Љ&��,�����D,J7�p���[��Њ���[?}���������_?�K�E�N9\m����F��g^�z������TOk�E��V7����;n�SAۖ���7�z¡E�J	xɺ�X�YƧD��ع}B���
c_�	s��އ�`��z��3� �5i-����dS�rMu�������]�u�fI8		H���T�e���Չ#���JEF�dM���h�+-٣�v@�Sk�6�9�H�����T2W�Ϟ$�
���_x�gZ�ҼQheܝ��*�ͨ�T�_3��i�����[�E���ЄT'�\-��;p�t�y���*�0^������J�Z�mj�9�Lo�4�c)e�kZ�F\�@��Ja����<���Z N+��"����B��
g���3���x_:`�t�i$Z���un���#|l�������+��;�^SC�fd-|��@W�a�B�ih�p��\͢4�]wUC�%��TO��q��|y�, /��=�%���\�b�Ih[�:Ӓm���"���ю��Ն�˕o����ge�#��s>�'�d�	A�����i%�lM����WFk��y{jP�(�M| '��:2�f��R�W�����cD��4H7=
5�k����s�a�4�n���Y�|m� �B���S�R�� �5Q��<��UA�W���Q)U_VQDR	G����ܲDb�Q-��䋨Y�wu~ƈ�>(tj���/�g�pmDj��{�>�u����F�l4x������2Z�Ʊl�L*�M.j��25��ִT�HYϭ�w��<�L�KV���y�ܯ_21��z�l��-v���ҷY�7�O�����B= 啖��3��?uΩ���~yKa.�����Pc�ah�4l
��AD�'B͒��c�V~��8�B)J��������s!8.�B/5`�!�ꦊ^�R6��T�[�Uo~�2K��g[]���!xB\z�O��b�n��^��u\+ZiоQ������3�@�61�Qt/}6�D��iP�e. h��ěDo8q���u����C�.����Ʌa�ʦ%��wx�$��x��~�k�P�#q�l�v$ Ϯ���n<V>E	f4���%��s���U�{]E!QW�4p����+�k4{�·��J3���Ô�Xh��xH�$wڡO����$��ϭ��h��~;2��,��#w��^kX�o�Q��%��ۣ��$�k3̠x6���B�a�*k�"�Ӄh �  $�3�k�bJ�rd�E��>g姙��n�S��
����xIJO��ƈ�>߫�E�P7��F
$�n�-I K�q�Ґ��}u6�Zyؓ��rBtj%��n�-6�Ό��o�/�+&���d�l�o��o���������C�h5��t��]3�S�k��:��#�҃y�Ĭ����X�eV�Z�<:�v8��"��Z$��Ϸ-�Jߢui3�A{���|��bl�c�?�n�}'�.l�Zſ�����]%�D�r�a�ė�oFC�WI�[�#�?��H�Wp8 $:';f��,��Ҝ''L$m��.�_�����?��ܓ��ZE��½�4��	��)4Z���NF�����no�����/�4b��1�w��w�á�#Dt���li�5�:���h�%xM�Ζo
߅�[Ќ�y�>���M/X�!1g�@�޹�D}am���5�����k��#~���0uQ��m?��\L�\�B�����s�ͣ���ǿ�ݣ�p������P3� q��%��͏\� ?j�f�n3o���˺@��!M�������k7N�ߌ�q��8�7����!c�Y�a����˹�sH$ ��C*%z��G��/>x��e���7#���N�RK�\�8�E2R3td��Zst�ͥUD���kj���Nɸ,Љ/դXm�y�����\a��[�&*�bs�:��M�r_g� c��$��lZ�d��lR�_k�����"���I��yc�3�t���b��n��숭�h��� ���e�Zؽ��p�����)}��� eHȘ������?�d�B�����;y�̗�?9ʿh��V.����MN�oh���>��A&mJ����4��u�{[���`�tbҵ�	fv�I��+�i�{�}��s�C:���U������i%S4���oN_i����@^�N�N���ζz�{=���q�ḷ���ט�}��W;�V�K\��O:�uWyG����{ia�D�v���q�ػ��mA�gl��Ї���ġi-q����g�j�$�*�e���Wi5��M�^���Ŀx �-/��C{��R�5�+����Ԭ�EuU'~*&�F�d��=�Z��E� ��D�cދ�[����T1l��q�۵��A��m$W ������ĺ�������ۺ�4jg�u=]���O�X�D�ŉ�XK���C�m�n]w��Y��!�mQs�P�����V���`��n�|moJU�_@�= P�d�K2�Mƻ�=s�hˉ�c(�7�L��32V��5�`����v��Ǯ/�
��ϐ6e���u�0v2z щ�y=�ʫb��ﯖ|��i������d�1/7�	m�BV=WN�֤$�9E��Џ���#�׸�*���'ӂ���VM���`P�u[�?�p���+�F��0�� mX6h����m#Ȱ=��nbݢ��/m<M�#���{|��^���5|hw�g���<��-&�5q)��Qg�u�SO!O$��3�F�8�3���,�3^��,e������ou��θ�Er�?��S�9�B˳��*�R7�uOQ9�/���k�\���^�q4����t�A�ip_ﰓv[I�}>rZK�.�U��dZ���Bc/�2�ׇ��]=C��O�u�d�ȟ�9�3PS�ϗ~�>�/H���a�7�
C�f��.�g{[��W}>���w��������J��oһ�ӊ^^�e��/��=P������+�??���]�w�-��xa��@�L��χ�g=��ܭ"�_|��=L��bo3Xu����^J��m�ʘ/<�u潻����O��&�� �����9:��z2{/-	�R�$���~�:0vZ�I�~���*{���+�s S��;�J%7�5{�R�r�l>F��歉��n�ZB��)#��E�X�:�Ӓi�D��$���w��O�������1�[��k����!_�ɖ��<Ӹ���<�p{Z��Ӗ.Qm%�%s�����)���̸��\Y�h�>�O���6Zۙ��� ��m��aQt�j���ӑ����r�Uq-n@Sܘ��N���|��5�u��P��	-��T��L����S��)nE���T��2r���W�$���B��Sn�W`E+��^w�������E�&�IC����ob�xR���ӒZ.�,��@��.������4��;�����cS�.ϥj:u�ɱ���-[\�)h��Х��N�˅�>��_ڃR����E<J���*���C�z���iRso?�o���y��=�[ `�TȲ!��R�Lo��Ji�*F\�����ncè���,W�~�3�"t� ��hZ���c��o�<,#���M�J������R�-C��Tt�!���:�;�e5>�
EU@�3���Q��z��[o�TTv�6z~�^W�KT���n��_��ܽ�q���F'��7?ЮԍD^������d��U���u���r��op��1L���d��F5��:�Nw�c}ӛR�b\N$��E�@���i5(�~�b��h�m�D֑�6)�,���i×(��7h�ȗ1i���V���dΊ����b��X��]��w�)�t�N�o��?Ի��֪�=�kYU�L\ڪ���r�L�m�ŝکj�w������Ʀ,}���������%z��d�\�V=1�}�PfԳ���e�\G�V�,i�i�e~��Ƕ�	Y�"�O�6rm��n�T���&�+|l�x�ny'�&\��_�jb5F��AjX�A0�	��+K�}��b���I���Ji�0MYHƖՍ�A}�$��p�C"bhn�X��II,^n&]��� �^�,k���񼷂���Ao"�ѻ��L���;�$�3���'��������;ؐ�ÚS����\]���:������ڠsɾ�7*�u1�VZ,�����W��ɣ.?%7���Q����9�Վ歽�˅6m�!�w��;7��v�'�|��������0            x������ � �         �  x�]�K��@ ��
�R�	�D��\DԈ�niv7U}����ʺ���d��po[�?W�_�=�/`\��&kC�v2N�KBvFƭ|��Sپ��9Qw���}����t��J0մ/���@$�hKQhVcs�c+��D�N�}l��ȻȲ��
�t������G�:A:� !j�!w?�8/�h�]��x��c�u#�9:oe>���x�l�G3y�>!\G�� �HU9�!������Ҏ�.u"������Ӳ@�)�9/��re���4��֗�f� xt��J)%o�#�_Y��IM?8
����v������?��9LZ�z�,����*�L�7D��nķ��He���X��zi�,N.�_��Vm���T�܌���������w��!��� \Q��6�A���>׫'     