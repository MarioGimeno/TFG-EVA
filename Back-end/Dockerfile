# 1) Imagen base en Debian slim (glibc en lugar de musl)
FROM node:18-slim

# 2) Carpeta de la app
WORKDIR /usr/src/app

# 3) Copia package*.json e instala dependencias (bcrypt se compila para Debian aquí)
COPY package*.json ./
RUN npm ci --omit=dev

# 4) Copia el resto del código (gracias a .dockerignore, NO se copiará node_modules ni uploads)
COPY . .

# 5) Expone el puerto
EXPOSE 3000

# 6) Arranca el servidor
CMD ["node", "server.js"]
