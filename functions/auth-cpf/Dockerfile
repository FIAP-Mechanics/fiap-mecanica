FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci --no-audit --no-fund
COPY tsconfig.json vitest.config.ts ./
COPY src ./src
RUN npm run build

FROM public.ecr.aws/lambda/nodejs:22
COPY --from=build /app/dist/index.js ${LAMBDA_TASK_ROOT}/index.js
CMD ["index.handler"]
