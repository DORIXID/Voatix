FROM node:20-alpine3.20

WORKDIR /app

# Устанавливаем Angular CLI глобально, чтобы Docker не тупил
RUN npm install -g @angular/cli

COPY package*.json ./
RUN npm ci

COPY . .

# Открываем порт для дев-сервера
EXPOSE 4200

# Запуск с флагом --poll, чтобы Hot Reload работал внутри Docker на Windows
CMD ["ng", "serve", "--host", "0.0.0.0", "--poll", "2000"]