alter table if exists errand
   modify channel enum ('EMAIL','ESERVICE','ESERVICE_KATLA','MOBILE','SYSTEM','WEB_UI');