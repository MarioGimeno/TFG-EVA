# 1) Imagen base ligera de Node.js
FROM node:18-alpine AS build

# 2) Crea carpeta de la app y ponla como working dir
WORKDIR /usr/src/app

# 3) Copia solo el package.json e instala deps
COPY package*.json ./
RUN npm ci --omit=dev

# 4) Instala PM2 global (usa pm2-runtime para producción)
RUN npm install pm2@latest -g

# 5) Copia el resto del código
COPY . .

# 6) Expón el puerto que usa tu app (ajusta según tu server.js)
EXPOSE 3000

# 7) Usa pm2-runtime para lanzar tu app y que respete señales SIGTERM/SIGINT
CMD ["pm2-runtime", "server.js", "--name", "tfg-backend"]
