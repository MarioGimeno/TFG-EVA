# Usar una imagen base de Node.js (la versión puede ser la que necesites)
FROM node:16

# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /usr/src/app

# Copiar el archivo package.json y package-lock.json (si los tienes) al contenedor
COPY package*.json ./

# Instalar las dependencias de la aplicación
RUN npm install

# Copiar el resto de los archivos de la aplicación al contenedor
COPY . .

# Exponer el puerto en el que tu servidor escucha (por defecto 3000 en Node.js)
EXPOSE 3000

# Comando para ejecutar el servidor Node.js cuando se inicie el contenedor
CMD ["node", "server.js"]
