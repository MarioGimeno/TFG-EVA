# 1) Imagen base ligera de Node.js
FROM node:18-alpine

# 2) Crea carpeta de la app y ponla como working dir
WORKDIR /usr/src/app

# 3) Copia solo el package.json e instala deps
COPY package*.json ./
RUN npm ci --omit=dev

# 4) Copia el resto del código
COPY . .

# 5) Expón el puerto que usa tu app
EXPOSE 3000

# 6) Ejecuta tu servidor directamente
CMD ["node", "server.js"]
