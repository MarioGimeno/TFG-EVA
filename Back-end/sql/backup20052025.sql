PGDMP      .                }           postgres    17.2    17.4 1               0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                           false                       0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                           false                       0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                           false                       1262    5    postgres    DATABASE     t   CREATE DATABASE postgres WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.UTF-8';
    DROP DATABASE postgres;
                     postgres    false                       0    0    DATABASE postgres    COMMENT     N   COMMENT ON DATABASE postgres IS 'default administrative connection database';
                        postgres    false    4358            �            1259    16773 	   categoria    TABLE     w   CREATE TABLE public.categoria (
    id_categoria integer NOT NULL,
    img_categoria text,
    nombre text NOT NULL
);
    DROP TABLE public.categoria;
       public         heap r       postgres    false            �            1259    16772    categoria_id_categoria_seq    SEQUENCE     �   CREATE SEQUENCE public.categoria_id_categoria_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 1   DROP SEQUENCE public.categoria_id_categoria_seq;
       public               postgres    false    225                       0    0    categoria_id_categoria_seq    SEQUENCE OWNED BY     Y   ALTER SEQUENCE public.categoria_id_categoria_seq OWNED BY public.categoria.id_categoria;
          public               postgres    false    224            �            1259    16516    contacts    TABLE     �   CREATE TABLE public.contacts (
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
       public               postgres    false    220            	           0    0    contacts_id_seq    SEQUENCE OWNED BY     C   ALTER SEQUENCE public.contacts_id_seq OWNED BY public.contacts.id;
          public               postgres    false    219            �            1259    16717    entidad    TABLE     �   CREATE TABLE public.entidad (
    id_entidad integer NOT NULL,
    imagen text,
    email text,
    telefono text,
    pagina_web text,
    direccion text,
    horario text
);
    DROP TABLE public.entidad;
       public         heap r       postgres    false            �            1259    16716    entidad_id_entidad_seq    SEQUENCE     �   CREATE SEQUENCE public.entidad_id_entidad_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 -   DROP SEQUENCE public.entidad_id_entidad_seq;
       public               postgres    false    223            
           0    0    entidad_id_entidad_seq    SEQUENCE OWNED BY     Q   ALTER SEQUENCE public.entidad_id_entidad_seq OWNED BY public.entidad.id_entidad;
          public               postgres    false    222            �            1259    16529 
   fcm_tokens    TABLE     �   CREATE TABLE public.fcm_tokens (
    user_id integer NOT NULL,
    token text NOT NULL,
    updated_at timestamp with time zone DEFAULT now()
);
    DROP TABLE public.fcm_tokens;
       public         heap r       postgres    false            �            1259    16783    recurso    TABLE     P  CREATE TABLE public.recurso (
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
       public         heap r       postgres    false            �            1259    16782    recurso_id_seq    SEQUENCE     �   CREATE SEQUENCE public.recurso_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 %   DROP SEQUENCE public.recurso_id_seq;
       public               postgres    false    227                       0    0    recurso_id_seq    SEQUENCE OWNED BY     A   ALTER SEQUENCE public.recurso_id_seq OWNED BY public.recurso.id;
          public               postgres    false    226            �            1259    16504    users    TABLE     �   CREATE TABLE public.users (
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
       public               postgres    false    218                       0    0    users_id_seq    SEQUENCE OWNED BY     =   ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;
          public               postgres    false    217            P           2604    16776    categoria id_categoria    DEFAULT     �   ALTER TABLE ONLY public.categoria ALTER COLUMN id_categoria SET DEFAULT nextval('public.categoria_id_categoria_seq'::regclass);
 E   ALTER TABLE public.categoria ALTER COLUMN id_categoria DROP DEFAULT;
       public               postgres    false    225    224    225            M           2604    16519    contacts id    DEFAULT     j   ALTER TABLE ONLY public.contacts ALTER COLUMN id SET DEFAULT nextval('public.contacts_id_seq'::regclass);
 :   ALTER TABLE public.contacts ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    220    219    220            O           2604    16720    entidad id_entidad    DEFAULT     x   ALTER TABLE ONLY public.entidad ALTER COLUMN id_entidad SET DEFAULT nextval('public.entidad_id_entidad_seq'::regclass);
 A   ALTER TABLE public.entidad ALTER COLUMN id_entidad DROP DEFAULT;
       public               postgres    false    222    223    223            Q           2604    16786 
   recurso id    DEFAULT     h   ALTER TABLE ONLY public.recurso ALTER COLUMN id SET DEFAULT nextval('public.recurso_id_seq'::regclass);
 9   ALTER TABLE public.recurso ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    227    226    227            K           2604    16507    users id    DEFAULT     d   ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);
 7   ALTER TABLE public.users ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    217    218    218            �          0    16773 	   categoria 
   TABLE DATA           H   COPY public.categoria (id_categoria, img_categoria, nombre) FROM stdin;
    public               postgres    false    225   8       �          0    16516    contacts 
   TABLE DATA           M   COPY public.contacts (id, user_id, name, email, contact_user_id) FROM stdin;
    public               postgres    false    220   �8       �          0    16717    entidad 
   TABLE DATA           f   COPY public.entidad (id_entidad, imagen, email, telefono, pagina_web, direccion, horario) FROM stdin;
    public               postgres    false    223   �9       �          0    16529 
   fcm_tokens 
   TABLE DATA           @   COPY public.fcm_tokens (user_id, token, updated_at) FROM stdin;
    public               postgres    false    221    >                  0    16783    recurso 
   TABLE DATA           �   COPY public.recurso (id, id_entidad, id_categoria, imagen, email, telefono, direccion, horario, servicio, descripcion, requisitos, gratuito, web, accesible) FROM stdin;
    public               postgres    false    227   \A       �          0    16504    users 
   TABLE DATA           @   COPY public.users (id, email, password, created_at) FROM stdin;
    public               postgres    false    218   m                  0    0    categoria_id_categoria_seq    SEQUENCE SET     H   SELECT pg_catalog.setval('public.categoria_id_categoria_seq', 8, true);
          public               postgres    false    224                       0    0    contacts_id_seq    SEQUENCE SET     >   SELECT pg_catalog.setval('public.contacts_id_seq', 36, true);
          public               postgres    false    219                       0    0    entidad_id_entidad_seq    SEQUENCE SET     E   SELECT pg_catalog.setval('public.entidad_id_entidad_seq', 14, true);
          public               postgres    false    222                       0    0    recurso_id_seq    SEQUENCE SET     =   SELECT pg_catalog.setval('public.recurso_id_seq', 53, true);
          public               postgres    false    226                       0    0    users_id_seq    SEQUENCE SET     :   SELECT pg_catalog.setval('public.users_id_seq', 8, true);
          public               postgres    false    217            ]           2606    16780    categoria categoria_pkey 
   CONSTRAINT     `   ALTER TABLE ONLY public.categoria
    ADD CONSTRAINT categoria_pkey PRIMARY KEY (id_categoria);
 B   ALTER TABLE ONLY public.categoria DROP CONSTRAINT categoria_pkey;
       public                 postgres    false    225            W           2606    16523    contacts contacts_pkey 
   CONSTRAINT     T   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_pkey PRIMARY KEY (id);
 @   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_pkey;
       public                 postgres    false    220            [           2606    16724    entidad entidad_pkey 
   CONSTRAINT     Z   ALTER TABLE ONLY public.entidad
    ADD CONSTRAINT entidad_pkey PRIMARY KEY (id_entidad);
 >   ALTER TABLE ONLY public.entidad DROP CONSTRAINT entidad_pkey;
       public                 postgres    false    223            Y           2606    16536    fcm_tokens fcm_tokens_pkey 
   CONSTRAINT     ]   ALTER TABLE ONLY public.fcm_tokens
    ADD CONSTRAINT fcm_tokens_pkey PRIMARY KEY (user_id);
 D   ALTER TABLE ONLY public.fcm_tokens DROP CONSTRAINT fcm_tokens_pkey;
       public                 postgres    false    221            _           2606    16790    recurso recurso_pkey 
   CONSTRAINT     R   ALTER TABLE ONLY public.recurso
    ADD CONSTRAINT recurso_pkey PRIMARY KEY (id);
 >   ALTER TABLE ONLY public.recurso DROP CONSTRAINT recurso_pkey;
       public                 postgres    false    227            S           2606    16514    users users_email_key 
   CONSTRAINT     Q   ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);
 ?   ALTER TABLE ONLY public.users DROP CONSTRAINT users_email_key;
       public                 postgres    false    218            U           2606    16512    users users_pkey 
   CONSTRAINT     N   ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);
 :   ALTER TABLE ONLY public.users DROP CONSTRAINT users_pkey;
       public                 postgres    false    218            `           2606    16542 &   contacts contacts_contact_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_contact_user_id_fkey FOREIGN KEY (contact_user_id) REFERENCES public.users(id);
 P   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_contact_user_id_fkey;
       public               postgres    false    4181    220    218            a           2606    16524    contacts contacts_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
 H   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_user_id_fkey;
       public               postgres    false    220    218    4181            b           2606    16537 "   fcm_tokens fcm_tokens_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.fcm_tokens
    ADD CONSTRAINT fcm_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);
 L   ALTER TABLE ONLY public.fcm_tokens DROP CONSTRAINT fcm_tokens_user_id_fkey;
       public               postgres    false    218    4181    221            c           2606    16796    recurso fk_categoria    FK CONSTRAINT     �   ALTER TABLE ONLY public.recurso
    ADD CONSTRAINT fk_categoria FOREIGN KEY (id_categoria) REFERENCES public.categoria(id_categoria) ON DELETE CASCADE;
 >   ALTER TABLE ONLY public.recurso DROP CONSTRAINT fk_categoria;
       public               postgres    false    227    225    4189            d           2606    16791    recurso fk_entidad    FK CONSTRAINT     �   ALTER TABLE ONLY public.recurso
    ADD CONSTRAINT fk_entidad FOREIGN KEY (id_entidad) REFERENCES public.entidad(id_entidad) ON DELETE CASCADE;
 <   ALTER TABLE ONLY public.recurso DROP CONSTRAINT fk_entidad;
       public               postgres    false    223    227    4187            �   �   x���Kn1���\��x�E]���d�4hbG���#�b��XG��[���x��4A?��H�:Q��(F����6��A�ga<���}]鬨	\���,Z�[�X_Y�n��;�>���2L��d���[~�#��x�bEl��G��,���I�+q����������u�@�`��Xb��i�T|daC�*�U'b}K0��/�/�%��1 �~��q      �   �   x�3�4��M,����M��M���K,K�r��/�M���K�����2�*(*MMJD�@ac,�F\&pQ#8m�e�i
��i.N3� 0*萎l�1P�/�y
�`�A܊����$Q�9�(/39����,3.c3����+�R� j
P͊���� �OU,      �   r  x��VMo�8=K�b.l[%Y�8E�x�)�ƨ�,P��H��@"�r����䴇z���I�#N,t0$jhμ�7oH��z]��`�6����\*�7��T�5�ϨZ��GK�(�W^*���*z�>����^%r'�/�^�=Қ��zL9�Q� ?���{��6P,c�J�kZ�{���s_ɔ���A*�L��j��hQ08�>4T@� �ö��=���������`
(l8��n���D��Aa�P���:����x�z�d�݈������k�
 z�]Sp	���,ѓZ�w�u����;�:�x�s��$�ȅ,��c��mvL��C�Q��Qd�'y'crpM���ֵ�.�\��"8�,1�3ZR� Ꝧ�	{�M��/����j�K�łu��,�*�:�K��ɉ�!L�1�Cb������g��-V OS	�X���$+k�r�N���$��K��Ҷ��4�te�q�I` �&����gZb�๠kV�d����=z�]�66���7���e���M�c���C|��n��/���Ƕ���F�d��� �[Z"�>&�Q�n��2�X~�ݦ:����^]��3�t�$�(��;��"��YP�$�����r�VW�F�{'���$��-��i���g�u��"����#LYEyi���(��dl ��<�:�K��<�Q��'S԰�mt�w���L�Vo�*|��EU=wܭ�k����M�bqF�g�j�N�p�0�5=���
�ֱ�$-e�J0��OƇ�	��;��9+�*ZE�����Q�3*8�Pgh����K+��� �0ܪ����?��H���L ���1�;�����DM۶X/+����.��+Zo�l����}Ck,�'T�S� ��ܚRcB��&�94�~G��4y����V���e#�꥙���B�G}���y:6���f�U椤��5�pN��퓞���o(�>��{5R&{i��^=���[<�`0��/7�YSt�]�Hs��ށ2�W(�k��ۚ�����ܡ9��pz��%��6!��� ��U�@��lJ��m�z��\3��V�4� ��>���3�G�|1�������t���p�<ۇg�KD_�,�C���lGī�	�>|����5}������G6�7�����Đ      �   ,  x���Y��H��g�*�o�d0$�!��ZBfƀmf��R��ս���>]����L鴗�?�cү�L����y2R����A{뽑����"�k�;XH}EK�Kk�n�C��W��fe(�����me�^]dW���#�꾈z�8 Ƈ2�maX����#�Վ��l(@1��w� #�`�p"�_ ��M,g��^����.�&�W�z����d�vIHQL��_��/�y��fӞ��E?�FgM�z�:��̃���j��G�T.+�pO:u��Y
�h�;M�q���$@^`�-i����fzK�vƄʫ�Ы��7�7W�}�\_3�(mV�� �Ĳam� ���;S�*��(ۜ�#P�#��w�Z5����J2z��h�tۨ�@"Xh��`L
�'-�)<�å�;��ȟ\������=��wi��⒛l��Oh�ا��w����j,��yN��9�%z@��m��_�U\IW�-�zxqf�Z���w�,-��|����1G9+���'��q*/�f��qF�+�S�ZQ�|~��c �@��,dY���2��^v�ޕ��>]���+���힎�48A	+��_SdvF�k8�ez�?1_�q".콝�h4�e������s9�w�W��{��iq��2�5��r��ɓd`(x�O;>q������$HR �4��/.��c��� �%g4�JOy���A���`�t��f?����io����Z�7:+�^���{�n��_{�T��>�Ҳ��)��#y��Q4�W^���y����5,=��8���������޾��� �2v}             x��}�nW��5�h4`$E��,y.&lYIر %�����D�Q�ͮb�C_�G8s1w�2� �� �b.��3�'������]��ډs:Aw`������׷V;��a�f��W���[�Vs;�\�N����Q��z���a���+WؿW����=.�]9õ�x�Yb��bڙ����i��+[ک{e�i�9ypd�����sj�<5�\a>�ma�Ԍ�»��s8���͝G�yRie�y��%>�'����ܘ���4O��oˉ������ ���_/���ܦ�-ܽ$����������I�Uw_3`��M3�a���sYw�2]d���-��g��]t��˳/�ó��g_|9�0�d�����5�5��Nlb��יּ��җ�2UZ�f8�:5�2����/�{L�)v٤��9=�k�i���ήh+
}�[���2��eEү�j��bZ`�0����$����y�C��IF[W�"[e��O�1������m�T+�mn��t���"�,l��\f=�ouF����$�����Q�^�&�_�ޢ��l�G����G�D�Ý�uL�Ƭ?��,�&�b�U�R_���wXx'�!��;���}˳WD�4�iiuFĐ�`Cx�+>{��R|���&L���]�H����(�=g�4W�)�LN�R�o�G7e��!���@�fr��'�f�sx�!>��@���le�hA��τ�$*�b�5>�/�G�-�c�v�*f[u�䕣G^�����}�}UD�x�#dr�[rM����&5�O���*c	�t�L�ł]�6뤍�3���H��:K�}է�A_z'ަUN\Q�9͓,�q���1D���5�Syf��`:��_�u��/�S����9��}]�ݙ�I��q����VwMw�&��c�ʔ��7���tUE'K_��qWD�<=�L8���a�U6-�M�ULm���ۼt$��rO�(uQ������;gt���ln�����&C��:�&:�n-��g�������y�ZUϋ�=���@��)�s��pp��˔���!17"!w�Nb��9'����t�����PF�w�դ�܀hH��)A�ͮ��(Aͤ��tb�U+�J�J�?�-c녃L��Q��p3��]���I�Z&����1��+"N�Lzv ���*i�f�	�ҧ�27�KW��@���p���y`c�d��D�y@�y�a~I��D����ve�ܑ��O�S�z����
oW�t#ڬ�B����4�t<K�)��&*�N;\�k���h?e��,�ΒzbA߿�iY��!����h�S�UiD"�^��A��V"IfD)DY�"X�	6J6�b����=�-;�o�I�їaJ��sL�ry-3&�+�����3U��)zYk25{��u��~e!G�2f�P';wK��)�0W�G��SwE�.� �+X��+d�%�y�hoY��߶p{����1y7d���?�}K�?6�.�]�x�����E���CG�y&�eb��S�L=ҕ^89Z�w!�i���w�M`������T�r����o�1=��!���]?YI�N�G�<�����m�"�I�`��)��Nw�0�0�B�ج503�,�'}I�3��_�	d9IZ������i���¬֏Ee}��0rU������;s�RUT��6�����~Z�R<���n����E�"=�KLa���k>i�����l�j���F_�#T�I���$T�y��	K��w!T�Ş��7I�����zw�:_j�E#cr1�A�%��
��g��g�����	ٱ�W��d*q~Uj?l ��C�XOv��S�1�>hN�j:�-�����ؓ�$KBλ����ǿq�+��O���$��?�~���X(�Ύ��2*	���B��ڡ3v��DBD���`���X[�Q^�w����P��ĸj˾Ѱ�;8��!ԫ�n~n7�.����n���˻�oS�j�+�� C���7�qLX>*,�Ԑ�*S�^RB枃QCLC���-�Ah���XlW՛|��Y��N�jw7��漱l~L�Nv]��%��!�AO�܋�DCж]���?���)l��I�����lB�=�K�҂�=��ЫD�gcC���+�SH7<�����`�%�k،���"ie},F,�*�(�ɴVCz���H�p����H���`��dgl}0��s�/���Qv]�0~�@��̭��e\|�4��W5l�vr����d�	!Մ$
$K�-y>:�C:�I�8�V\�&/$Q��~Iz��R��X��n����&MM���&��6�>b��g{sN�ʶj	J�{U�j!��s�ss���:"kn������r	닂��g��;<&::�^ұ�ڒwz���i>��P�s+⇿D�ǔM
L{��ʹ��67��-�	i-�&W_�<�7�~~
������Y:�k{KV�D�,�F!d��a�i��Vw߉��i�	����V�I�����k�*;��MƐ,X%ϵ��<^�L(^4�>�{	s�+R�H�"cV������� �	�us�-V���d�{�UCS䎟rEܝ%���.�po08�{��5z������Sɦ�#irZ�����5��1�e!o'%=e!C�f�p�ٖ��!���b��r�ѢN�ޣ7�C�~t� ��3�5/��'���̽�ot|�9����5c�һo+3v����s�{h��7��
��[Z�'
�X�`�����w���^�3*j9�ytK:W&���㸣؛*�7���˼���Ĺ�kM�v~3���h|���=�����̟OG��l%���9�+b��H����f9�������9:vN�ظm
��(�V�b�Mk����%&l7��7�sU^�ϱH�`��(��F��5�Њ	�ɍi����E���S3��&�]��+2�P���V�����dco�gl7��\��8]��{3�^�d����9�9�f�!��������xcV{��F^�\;OH�d�r)�����-@�e,���9UUC뮯%j��1`�n�&�c�CL7�L�cq�������`�$�1��]RCW4�Hw�*�b�t��G^�� O��;͗|�L��{�6�����<�u����x��R��;ӝ���Ӳt���3��s���x1�D�]pm�te���%#�qgln Fω�Nͼ��y��DNС�E�܂�p=��r۫l�°��n9k@��>���c���-�������#?����RE"E��lFJ���5R�eL0M���%���!/5K�J��n5yj����yI�0�`��=�?�	����ɻ[���~G��I:ʧ���6~
��ˈ�,��
(J��n�U���]s��gŤF��y���\��nF�[�if�����߿���:��iVT�5�l�=�0�8Suq���˳���Ͼ0O�yv1~"G͢�*�n�ȫT�N�Z|O�ZA��$��{��h�Ͱ��3�	�:�d�*?΀���pB�;�5c�Is�����/t�Ý�_���Ϻ�HwN���$�{tf>yv�t���⯳����](g��3����m�՗��'g_\��]�'�q�]~��� �%�G[S�����G�<2�'��3��9�������������o��:���=~8���]j�[����� ������<n�N'ͱ ��f42-s�t��d�@_�2Y�"��5��B��e�Lj��б.��[	x�3��CQ�!�0�y7$r��E�@�g��4q�)	޴���[�!{A��t\�F�����݈�������]��[1��
s1ȉ+��0$1i�U\R��Yn��o�J�Ktz�+�EYgL���1�,�C�[qM���U�ؚ1\��9�Ɏ�
"uA�!�,�~� �3�><W��A����6I�h���RsE_�93S���d���g�B�!�@D�@�b�>��m���HDd�2�D������ԱyF78������7�Uy߯���2��"ϊԸ���O��$u�W���Z�3���1eB�*D�3������ٕ-i�2�Dʭ��%���b���ه"�V(�P������aJ$�HDc�?��1�u-��m��+�d���D�0��    �J�S��_�b�,R|Ir�(����*1�x�.�B�V_%��
fˑO����5�L���<k�oؼSdi�<��y�T?�,�稇�?�W)W�ȹ-�J�����\��F�A�O����;�BF�u�V������������x�Q�J�ĳ��89���?�}|~q��o�=��t�� ����h�լ����z��!�H�GO��W�(���x�:��LT:��^ٗ"�H#-�1@󜕆}���S�Ӂg,$!���Ok�0��@��%���`�а�8�Cϭ$�Ϲ0����,tf�cދ1�9.�<;��L����)��p�)ܢUeH���U���㔇� H���F���^%�?2o�$�Tp�Ti�Pq%��͔�BM�F�b0�̱��"��c$r>��o�ɵ�&��c�i"��	چ��'urR���d�D?ӈ_xX����"
��'!K����ih3��a��sHM��Qb+ۦb��#m�R�!�&�
"���f�G2�>�������(B���׌�6Û�qPLr��MB�m���]��Sq�8M�=��i�
��&(_�����U �"��v�фK� H봔�Z@%Ѕ�VB���3��@�׀�|��4�z�].	Њ�e>^��cV�}��ɂ���@5��\��½��qU�ІOk[&&	����5!W��EJjR�~�(ƞK�7i=�m�.��Y�n����7V��CD_��E�d�$A���-]��V�<��������y��G�w����{�Z���˴�l빞���b���<���$n�ٲ�`VVy�"E:�F����t���1d]�t�RR2t����	r�G��?F��~WH���!��]��xt��y:-�ꢁ�lp�U�G�E�C/�v�\���g��\� p�P�2b�����qWN%�� .��p
�,�}�n��V�td��I7�o���/��'?��%n�ߐ +��;2��-b̽hf��9:�҅�0�g�0_���|��륋LT=`D�j�瀸P'`�Оe�����/Է[(A,1'�Di�n��������Q�"6]���ް��5�лd���9�+�9���*W�م/�<B���)/~�Ǧ����,Sw��(��.	�tB;���޲�@�}��V ���Xy3m,�xT�C%`�{��`48��9�툗8W�3:��f�&Y��: ]� ��#3����~��Vd�MnR�"|�����ã�JH�l�*�п�8X~A����s4�=~:��q+x��n�(����Z�4���bE;�_�s��+��-�
���ep�M=��������H�2�Te����H�6��g�؇ȥ�S_y�!ւr��D�o~��`�7<
���SGLt�#I]جJ��)Mҹ�f�i��`ߜ�w����))�,1z'䐓ϟ�Q<肘�DY�l��<y��gD_<c�[��$�i	\2iml��,���]	�
v�(��/�ih�6�k�G�䈺D�\w;�z[�J�r;�~ yc�7�Ky��c��qa���F���[!k����� �~z�*.�@.��or��m�1��'&��c˥L)'tV�ѡ9�4-t���O-S�5���7�b��>sdzQ��U�٪j�,X T߾��D��ｉL�������ai�����k?o�%�AN���u�#uϔt�b�!���A�4�<:�R���	���ds�\�U�P�������Q��|��Ezy������L���v䜨 �p��Z& Q0_IX=|Q<Ju07%��Kx=�椢r4h8����F����`{���*����Y��tNv�"�M&�Z>|M���(�����~��}��/��o��~Q,[`[ڹ�&jN?l$��*�I�1r^�]Q�Db�����5��_�D��9�{\���Cn�A�__d�J8�O2m��ۣ�%_�q�/�	L4���KR��@U�(">�O%��Jw��Rl��q�3����q�l���"ҟ�["q�xQ��~g�Qtc�݌Ԅ���Fd�K��Q�0֗,�������l�4Ķ�$E�{����X[$,�ԟ���ge�w|���Զ�.�h�Rԅ��}�"ZH���� �$hz�p�8�xfMk�d�[���B"�%�k�o���%p�<y�����qy�X �v��#���03�	��~���pE2�o[��k���I�罧��u���/���!��0Z�<1cr���ret\�L�S����6|<�^��}CB�5�
QAe_j~)�z�Е	wZ%�f�nJ����W1�ą��R]���ƔbJؚ�z�}~VR�÷�ff力��@bɃ�@K�U#��l?�dY���źhw�\�����F ��C?2�|���(p��ә6��Jb<�t5S��\�׾g]1�
!w�O{7$[��n�9ٱ�g��=�.��.-8�b*2Q}s��C�!q���Y�{������Cc�H����H�أ9|Ս��*��)fw���ιR�-�!t���l[�7�UֈK� ��Mj"KI3���Gn��Z���ZL��˕��+�G��4c�P���F���H4�Ȭ,��ϣ1� 1n�V]�9Dl�m�e{��mĮ� Z�>J&c��ٕt�rÎj_̕�*�k�r�5D�7���h�Y�[� �Q"��xAb�1q�-��mJ��U_Da�@ jH&�h/L������߃/_{�J5�Q4[�*fCS�K�A��6yA1�ɬ��,`I�o�R��)O[&�RI�wa�2L�
��oa�C�Ӄ�J�W�<%��"����S*9�ٔ�xQ���R��#{��[2��(Ȃ^��Īt�g,@���t���雟��O��������_�/�M��j?�`O�`3��>��sT%l�,V��[K�������.��qǜ
ֶtd���N�(qU��K��ķ��4>!�Ł�S���Wh���N@�K��>��,TXw��T3�&��׏��l�X����5j���[�]�m����t��iL��^��+8��7�KၒY�舽�ik��y���{� ���
���i�F;�5ާ��}_�,��~I	8�m��p������5~�*Бo��>��" 7#�n�>G|͡"F�#۬ Ȧ�Zxi��ӥ�PĚ6���	:�����E��G��at�K0C�2	�WGZ�Q�b*b���_Ym��\�V'/}r�.t�ylB�9�	�r5�PG�y�ZJ��z��h��o/�ͅOŝ;mbP��@7���Z��]�.y�P(;��F�g���C�Ѽr\dl�D����m+�3�\宓�Q��hz��D�����k�^��5���8�¬ �*�E�bK�J��!@��"�_��#�I$�HʁM�k2УJkFOvu|���&�kZ�/>��pmBj4�V���VAj�	�q7�x3���BN��kR�ӹh+�6܁��S�ݑ[�TYi�"	?2+�qa����-w@Y�P��F)�45�W����f�)����Q^�3��*�f��ŮsF�?����"
\\��0֨��8ǣ}�ðU(�S �!O��n*�C��sX�#V��G�}_N�s�R1���v�,0��P�A.z.�8���d�U��������tm���硵�ڴ�$��z�XZ��C���'Z�q}�`C��!�:�k��"`X�0߈�Y���Ddir�e� �a�0��7���F�\g�Py���q���xlWŴ��A�D����qL�λh7#�@<�3�^
`Ϯ6��V"V>E8z��j���%C��N}��(�㊽���h����k�-�!Y4�u�!�=Z>MŘ�?{'^�a��#�=�j��F��_/��f[�K'X���d���mxc��j��ܘbu� � �\mC�si����mG���,>:x��Z��e�ƚ���>�lA��2D��fݟJ�w<�I�|Wt��k�W�`Z-��P;C�P��<Z�_#�"p��ӆ�^�l��5�*z��KV��Z=v�ur�څ�������E) m,�Z��#�j������K�Z�$�2�c�μʹ̬�u����7��	�Uy�-,���`�i�K� �  �YÏ]
:����E0\ٗ�	��@�M:�M꼞u�V�-q���cK�n��'��pԜ�J$CH8 �X��?������x�K?��*O /��V�;Rҕ��� n�d��Q����sh�Z>*�mF�����a���lȒv�񺴣����@@X�oD�4�2?#�s����g�z� ���r�Q�k������2\3~����O� 5Y����1��n�f�A����ಲQg�q�g���	ny���}�v��������W!r:^�,��@�B��ƗaI_u�fы�4b��@��d�\�Wd9P f#h���&P��I�O䶔]J|����Ϋo��Eͽ�ۡ���J�>��V'Ϩc{W�g��`!���&���AJ����F%aH�%��?)lo˔��m޼Q�ǝv��*O�����yLV��*�y��u
���ݭ�3��Ȁz&M��~%3�΂������T'�ig��X�6���ǭ�&���夡�F,�'e��s��^dp�u"]E^[)�D��d���LY�<�Ҋt
�7�=�Z-����d��B��[�F���YӋ���KV��"~J��"�TAF�J�5�^|��q�;�юp��p�����_�	m�xD����?`�k�9"�<c :�G\�L�����,���r�b��F��	�Zy�7S��CSY�:��r��EVLk��aKiQ����¹A�k'�AS�sݤ�ɡ<��wer�X��į�&�B���l%CF��F���p���?�q����p��f��������R�����i*"v�6�SȴiUVJQ���e�-��Iֺ�m�KV������|;��M�
��� �h�ۛ��w�EnA�ru쌓��m,��p�BL~/I���Qת����E���ʛDH�٢��״Ô�L�[$Wܘ�N���#o�(h�����6�/Ƹi�p��@����Q���M_Jqû�81�
�2��79�N��^��_��j@u7y���j��.C1ǠœD�9���k����+\���e*�ȶ�������o%���Cq��3�����LI�ZX��P�ٲ�� ��[i�0F/�2@�!
ߡ7��ĝ:�ifx����ڽ8�L��fU��D�\<3-S-s+yEvI~�|Կ�b]
θ8)�����&�����>��U�J��� U����+Ϩb�ץ1Anb��+�]8)7�[�m���T �{�p�����[�Z�,�O� �ɤ�d�����D�Xi��$>�Xϑ2��ӑ\�$�Cys���O������d�?J��'U'9�ߖ^�����r�:Ii���κ��-�ѷ�K+��1��V�qG�&6�;�ˀ���B��a0�L��-p����K���9�r�_v�V^'����h.#+�!�C?ot�*Τ0�2yf��@�RSy��q�����PMժ�{�,W�y[ki���.��ޑ.ך`�Vt#���9����D�Wzm%|�h����w<Μ2�R��o
9����`zF����$(P޽m���I*�*�����3��Nh��G)����ip2�mg�A��F3��ȧ�<��L=��8l;�z��'�M�T�-�jJ���3�)+���e��	H�J3k������)���4�I��H?�r����k�*vV;
W��%~��h��!E� u}��E�H���ؤ��%ՖJaed'v����[�����oѮ�B%�y�\P�)��;ܝ�^����Tiڐ�ė�N��k�K[�G�냍��p4Q�����O�Q�	�x���xBĳJ���"
?����Ot��Jo#a��]�K�	�R(����������w�����U�;<��}�1^^�Jf�45׾%��v2���#~���G��� �%`(�*D����q>�I�ʋr��b����Z���?h���N��I�FC�����O`����{��
lӝ<���G#����/S�<�瑱S�;j��p��;%[�\�q��6�x+�qO�Ix�Z�������6L�y���-A��`I�dK���9��O�B���~:6�ˌ"�>�����0w;���c�2͉�kX诃#�b��;�i��� �����^������$t:�����+֚�u���/�df���{�#���.�[0dh����B�/��][�=l��u��8�!�����-��R�/6�H��m�C!��;�%�$eJ|�*����E�h���D����\ϋ�����|�u^I��`ͭ�ksi�]�7�ڮF��T�t�(�L�����cj�Q⎩i9���鵻�p��[3�y
+���IZ,��?B�JZ�fr����H���RC�_�����N�F���k=�6p���!q��\�Rs��G��'Zk/�M����٭�x���9��������mMcϗ�+BZŵmS�j�K�����˹���^���P�M�����E�Kڰ���-Mv�����v�B��S*�ߌ'}'���ت��й_�x���x|���
G�hs�(�yWu�b�i4:�3/FY�i�m�G��ģ�=O�?�m���#���ۿ[�B�p���U>�Wq��h����W�kZ���JX[yh��g[�B��ne���6��4����������o��9����a�so^�
b?����l����\����@o��II�i��`ڏC�,�����L;u�}�ww������-#��1�ֳ��K��3
8�m<aO:��]�bo̯q�С������IgG�s���%ڀMx5�ӻi�� ���^q;����N؎�&y�N=��M�(�d#F[��&�`�1Ԇ�t�R>�%]��0�^��8���s�Xs�ρq�^|F�h1d'5���"�>������L_K@��hW��Vq�^�sv�V`n��]�)͠�	���i~9d��J�Vٖp/����>���8vy      �   -  x�]�Is�P���
��zG��jETLA��2�Q�8��ִ&ݩ:���:/��6Y��>^��ot�^΅��RW�w0��u�������"��p-�Y�޷��(b�t0��'��AIƒ)�SI�a��@|��y��h�Ɓ�F�����M��S�9���|"`oDSp�Yi56�$�ɔ�$(�7���@��x���^=�h��s�	���ET��i]f��TO��`#>��''���� #8E�n�#��17� ;F���H�Q�ٺ X���S�UW}}�*q��rK��!=��L1)��aw�}.ё��T$�[K��ѓ|�������B���Vi�K�0��w�ʌ �P&�"t��j�_Y�[��K��uDۉ��Q�h~�{m�8\����O��Zjǈ���xe��0�CL�P�� � ��߈7,/����؃k6��Zw�xl��bᚻ'⨫��h8X�ͷ ��,._{0�kc�֘ 	�	���YX�y������D���9�;���L�g%#UO�-A��� Xf��EH>2����R��     