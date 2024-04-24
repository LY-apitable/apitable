import { NestFastifyApplication } from '@nestjs/platform-fastify';

let appInstance: NestFastifyApplication;

export const setAppInstance = (app: NestFastifyApplication) => {
  appInstance = app;
}

export const getAppInstance = () => {
  return appInstance;
}
