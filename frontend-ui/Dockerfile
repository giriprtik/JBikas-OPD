# Stage 1: Build the static production bundle using Node
FROM node:20-alpine AS build
WORKDIR /app

# Copy the package definitions first to use Docker caching layer locks
COPY package.json ./

# Run a clean, forced installation of all structural dependencies
RUN npm install

# Copy all local project source files and folders into the working directory
COPY . .

# Run the project compiler to generate the static deployment bundle
RUN npx vite build

# Stage 2: Serve the compiled production chunks using high-performance Nginx
FROM nginx:stable-alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
