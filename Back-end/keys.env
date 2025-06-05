name: Deploy to EC2

on:
  push:
    branches:
      - main

jobs:
  deploy:
    name: SSH & Deploy
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code (en GitHub Actions)
        uses: actions/checkout@v3

      - name: Deploy via SSH
        uses: appleboy/ssh-action@v0.1.7
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          port: 22
          script: |
            cd /home/${{ secrets.EC2_USER }}/TFG-Somos-M-s/Back-end

            # 1) Asegúrate de que en EC2 tenemos el keys.json (contenido del Service Account de Google)
            #    Supongamos que en GitHub Actions tienes un secret GCP_CREDENTIALS_JSON que contiene TODO el JSON.
            echo "${{ secrets.GCP_CREDENTIALS_JSON }}" > keys.json
            chmod 600 keys.json

            # 2) Crea (o actualiza) el keys.env con variables adicionales
            cat <<EOF > keys.env
            GOOGLE_APPLICATION_CREDENTIALS=/usr/src/app/keys.json
            PGHOST=tu_host_postgres
            PGUSER=tu_usuario
            PGPASSWORD=tu_password
            PGDATABASE=tu_db
            PGPORT=5432
            JWT_SECRET=tu_secreto_jwt
            EOF

            # 3) Elimina la imagen antigua (por si existe)
            docker rmi -f tfg-backend:latest || true

            # 4) Limpia Docker (contenedores, imágenes y volúmenes huérfanos) para liberar espacio
            docker system prune --volumes -f || true

            # 5) Reconstruye la imagen sin usar cache
            docker build --no-cache -t tfg-backend:latest .

            # 6) Para y elimina cualquier contenedor viejo
            docker rm -f tfg-backend || true

            # 7) Libera el puerto 3000 en el host (por si estuviera ocupado)
            sudo lsof -ti tcp:3000 | xargs -r kill -9 || true

            # 8) Arranca el contenedor, montando keys.json y cargando keys.env
            docker run -d \
              --name tfg-backend \
              -p 3000:3000 \
              --env-file /home/${{ secrets.EC2_USER }}/TFG-Somos-M-s/Back-end/keys.env \
              -v /home/${{ secrets.EC2_USER }}/TFG-Somos-M-s/Back-end/keys.json:/usr/src/app/keys.json:ro \
              --restart unless-stopped \
              tfg-backend:latest

            logger -t GH-Deploy "=== Despliegue Docker completado ==="
