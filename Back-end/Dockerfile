FROM node:16

# Crea el directorio de trabajo
WORKDIR /usr/src/app

# Copia los archivos package.json y package-lock.json
COPY package*.json ./

# Instala dependencias de npm
RUN npm install

# Instala FFmpeg en el contenedor
RUN apt-get update && apt-get install -y ffmpeg

# Copia el resto de los archivos
COPY . .

# Expone el puerto
EXPOSE 3000

# Comando para iniciar el servidor
CMD ["node", "server.js"]
