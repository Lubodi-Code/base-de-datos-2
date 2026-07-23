# =========================================================
#  Frontend SIGEC-PJ — build (Vite) + servido por nginx
# =========================================================

# --- Etapa 1: build de la SPA ---
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# --- Etapa 2: nginx sirve dist/ y hace proxy de /api al backend ---
FROM nginx:alpine AS run
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
