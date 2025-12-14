FROM node:24-alpine as build
WORKDIR /app
COPY package.json yarn.lock /app/
RUN --mount=type=cache,target=/root/.yarn YARN_CACHE_FOLDER=/root/.yarn \
    yarn install
COPY . /app
RUN yarn build

FROM nginx:alpine
COPY --from=build /app/.vitepress/dist /dist
COPY nginx.conf /etc/nginx/conf.d/default.conf
