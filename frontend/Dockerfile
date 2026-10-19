FROM node:20.18.0-alpine AS build
WORKDIR /app
# Real package-lock.json must be materialized by the authorized runner first.
COPY package.json package-lock.json ./
RUN npm ci
# Include tests and configuration: TypeScript builds may reference both.
COPY . .
RUN npm run build

FROM nginx:1.27.2-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=10s --timeout=5s --start-period=5s --retries=12 \
    CMD wget --quiet --spider http://127.0.0.1/ || exit 1
