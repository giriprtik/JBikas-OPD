FROM node:20-alpine
WORKDIR /app

# Copy package configurations first to leverage Docker layer caching
COPY package*.json ./
RUN npm install

# Copy the rest of the application files
COPY . .

EXPOSE 5173

# Run in development mode instead of building static chunks
CMD ["npm", "run", "dev", "--", "--host"]
