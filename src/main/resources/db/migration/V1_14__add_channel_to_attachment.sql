alter table if exists attachment
   add column channel enum ('EMAIL','ESERVICE','ESERVICE_KATLA','MOBILE','MY_PAGES','SYSTEM','WEB_UI');
