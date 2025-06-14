PGDMP                       }           postgres    17.2    17.4 "    �           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                           false            �           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                           false            �           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                           false            �           1262    5    postgres    DATABASE     t   CREATE DATABASE postgres WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.UTF-8';
    DROP DATABASE postgres;
                     postgres    false            �           0    0    DATABASE postgres    COMMENT     N   COMMENT ON DATABASE postgres IS 'default administrative connection database';
                        postgres    false    4338            �            1259    16516    contacts    TABLE     �   CREATE TABLE public.contacts (
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
       public               postgres    false    220            �           0    0    contacts_id_seq    SEQUENCE OWNED BY     C   ALTER SEQUENCE public.contacts_id_seq OWNED BY public.contacts.id;
          public               postgres    false    219            �            1259    16529 
   fcm_tokens    TABLE     �   CREATE TABLE public.fcm_tokens (
    user_id integer NOT NULL,
    token text NOT NULL,
    updated_at timestamp with time zone DEFAULT now()
);
    DROP TABLE public.fcm_tokens;
       public         heap r       postgres    false            �            1259    16548    tokens    TABLE     �   CREATE TABLE public.tokens (
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
       public               postgres    false    223            �           0    0    tokens_id_seq    SEQUENCE OWNED BY     ?   ALTER SEQUENCE public.tokens_id_seq OWNED BY public.tokens.id;
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
       public               postgres    false    218            �           0    0    users_id_seq    SEQUENCE OWNED BY     =   ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;
          public               postgres    false    217            C           2604    16519    contacts id    DEFAULT     j   ALTER TABLE ONLY public.contacts ALTER COLUMN id SET DEFAULT nextval('public.contacts_id_seq'::regclass);
 :   ALTER TABLE public.contacts ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    219    220    220            E           2604    16551 	   tokens id    DEFAULT     f   ALTER TABLE ONLY public.tokens ALTER COLUMN id SET DEFAULT nextval('public.tokens_id_seq'::regclass);
 8   ALTER TABLE public.tokens ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    223    222    223            A           2604    16507    users id    DEFAULT     d   ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);
 7   ALTER TABLE public.users ALTER COLUMN id DROP DEFAULT;
       public               postgres    false    218    217    218            �          0    16516    contacts 
   TABLE DATA           M   COPY public.contacts (id, user_id, name, email, contact_user_id) FROM stdin;
    public               postgres    false    220   I%       �          0    16529 
   fcm_tokens 
   TABLE DATA           @   COPY public.fcm_tokens (user_id, token, updated_at) FROM stdin;
    public               postgres    false    221   �%       �          0    16548    tokens 
   TABLE DATA           @   COPY public.tokens (id, user_id, token, created_at) FROM stdin;
    public               postgres    false    223   (       �          0    16504    users 
   TABLE DATA           @   COPY public.users (id, email, password, created_at) FROM stdin;
    public               postgres    false    218   1(       �           0    0    contacts_id_seq    SEQUENCE SET     =   SELECT pg_catalog.setval('public.contacts_id_seq', 8, true);
          public               postgres    false    219            �           0    0    tokens_id_seq    SEQUENCE SET     <   SELECT pg_catalog.setval('public.tokens_id_seq', 1, false);
          public               postgres    false    222            �           0    0    users_id_seq    SEQUENCE SET     :   SELECT pg_catalog.setval('public.users_id_seq', 6, true);
          public               postgres    false    217            L           2606    16523    contacts contacts_pkey 
   CONSTRAINT     T   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_pkey PRIMARY KEY (id);
 @   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_pkey;
       public                 postgres    false    220            N           2606    16536    fcm_tokens fcm_tokens_pkey 
   CONSTRAINT     ]   ALTER TABLE ONLY public.fcm_tokens
    ADD CONSTRAINT fcm_tokens_pkey PRIMARY KEY (user_id);
 D   ALTER TABLE ONLY public.fcm_tokens DROP CONSTRAINT fcm_tokens_pkey;
       public                 postgres    false    221            P           2606    16556    tokens tokens_pkey 
   CONSTRAINT     P   ALTER TABLE ONLY public.tokens
    ADD CONSTRAINT tokens_pkey PRIMARY KEY (id);
 <   ALTER TABLE ONLY public.tokens DROP CONSTRAINT tokens_pkey;
       public                 postgres    false    223            H           2606    16514    users users_email_key 
   CONSTRAINT     Q   ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);
 ?   ALTER TABLE ONLY public.users DROP CONSTRAINT users_email_key;
       public                 postgres    false    218            J           2606    16512    users users_pkey 
   CONSTRAINT     N   ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);
 :   ALTER TABLE ONLY public.users DROP CONSTRAINT users_pkey;
       public                 postgres    false    218            Q           2606    16542 &   contacts contacts_contact_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_contact_user_id_fkey FOREIGN KEY (contact_user_id) REFERENCES public.users(id);
 P   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_contact_user_id_fkey;
       public               postgres    false    4170    220    218            R           2606    16524    contacts contacts_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.contacts
    ADD CONSTRAINT contacts_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;
 H   ALTER TABLE ONLY public.contacts DROP CONSTRAINT contacts_user_id_fkey;
       public               postgres    false    218    220    4170            S           2606    16537 "   fcm_tokens fcm_tokens_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.fcm_tokens
    ADD CONSTRAINT fcm_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);
 L   ALTER TABLE ONLY public.fcm_tokens DROP CONSTRAINT fcm_tokens_user_id_fkey;
       public               postgres    false    218    221    4170            T           2606    16557    tokens tokens_user_id_fkey    FK CONSTRAINT     y   ALTER TABLE ONLY public.tokens
    ADD CONSTRAINT tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);
 D   ALTER TABLE ONLY public.tokens DROP CONSTRAINT tokens_user_id_fkey;
       public               postgres    false    223    218    4170            �   p   x�3�4��M,����M��M���K,K�r��/�M���K�����2�*(*MMJD�@ac,�F\&pQ#8m�e�i��(C.3��)�6�2��L�	��g �~�td��qqq 3<�      �   ;  x���ۮ�@ ��k}�}�hf�w���E�(�	Q�At8��w���4Y���bgW&��)�J7T`��(Y� /�ٚM�[�t�;������;�P&7-[��́���MO^{�^j�Z>F�o{v�,=O2th[6٥����{��D2Ô���~S��(f�����%�Q<�0G��ԑ�m�A�RV�������8�FI
UEuQ�:t/dM�)���8��wr�i���Fm��t�k��a������z�0���P�q� �@����Zo���Z���p��E$���M����m]�r�rK�3.Bo.�����!�U��y�%��n�_C�ց/����j��f/�Ӆq���$75�m���ei��}z�Җ�y��z���C��4mME�~���oL��*<<�����)�d�r̛���r\���8/jz�L��W⍿�pΉD��Sx���b�.�9j�yrjv�Pjn,�lE�ͥ3��y�%�6���y7S�y-��$I���.�G��D���A�Wݡ�WBlG�S�3-!ZҀ�x�������?�`�      �      x������ � �      �   �  x�]�K��@ ��
�R�	�D��\DԈ�niv7U}����ʺ���d��po[�?W�_�=�/`\��&kC�v2N�KBvFƭ|��Sپ��9Qw���}����t��J0մ/���@$�hKQhVcs�c+��D�N�}l��ȻȲ��
�t������G�:A:� !j�!w?�8/�h�]��x��c�u#�9:oe>���x�l�G3y�>!\G�� �HU9�!������Ҏ�.u"������Ӳ@�)�9/��re���4��֗�f� xt��J)%o�#�_Y��IM?8
����v������?��9LZ�z�,����*�L�7D��nķ��He���X��zi�,N.�_��Vm���T�܌���������w��!��� \Q��6�A���>׫'     