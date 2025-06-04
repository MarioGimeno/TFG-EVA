# 1) Imagen base en Debian slim (glibc en lugar de musl)
FROM node:18-slim

# 2) Crea carpeta de la app y ponla como working dir
WORKDIR /usr/src/app

# 3) Copia sólo package.json y package-lock.json, para aprovechar cache de dependencias
COPY package*.json ./

# 4) Instala dependencias (aquí bcrypt se compilará o bajará la versión precompilada adecuada a Debian)
RUN npm ci --omit=dev

# 5) Copia el resto del código
COPY . .

# 6) Expón el puerto que usa tu app
EXPOSE 3000

# 7) Inicia tu servidor directamente con Node (sin PM2)
CMD ["node", "server.js"]
