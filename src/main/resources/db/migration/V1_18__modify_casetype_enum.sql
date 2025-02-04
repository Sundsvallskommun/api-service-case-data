alter table errand
    modify case_type enum (
        'NYBYGGNAD_ANSOKAN_OM_BYGGLOV',
        'ANMALAN_ATTEFALL',
        'REGISTRERING_AV_LIVSMEDEL',
        'ANMALAN_INSTALLATION_VARMEPUMP',
        'ANSOKAN_TILLSTAND_VARMEPUMP',
        'ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP',
        'ANMALAN_INSTALLATION_ENSKILT_AVLOPP_UTAN_WC',
        'ANMALAN_ANDRING_AVLOPPSANLAGGNING',
        'ANMALAN_ANDRING_AVLOPPSANORDNING',
        'ANMALAN_HALSOSKYDDSVERKSAMHET',
        'PARKING_PERMIT',
        'PARKING_PERMIT_RENEWAL',
        'LOST_PARKING_PERMIT',
        'MEX_LEASE_REQUEST',
        'MEX_BUY_LAND_FROM_THE_MUNICIPALITY',
        'MEX_SELL_LAND_TO_THE_MUNICIPALITY',
        'MEX_APPLICATION_SQUARE_PLACE',
        'MEX_BUY_SMALL_HOUSE_PLOT',
        'MEX_APPLICATION_FOR_ROAD_ALLOWANCE',
        'MEX_UNAUTHORIZED_RESIDENCE',
        'MEX_LAND_RIGHT',
        'MEX_EARLY_DIALOG_PLAN_NOTIFICATION',
        'MEX_PROTECTIVE_HUNTING',
        'MEX_LAND_INSTRUCTION',
        'MEX_OTHER',
        'MEX_LAND_SURVEYING_OFFICE',
        'MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOGUE_PLANNING_NOTICE',
        'MEX_INVOICE',
        'MEX_REQUEST_FOR_PUBLIC_DOCUMENT',
        'MEX_TERMINATION_OF_LEASE',
        'MEX_TERMINATION_OF_HUNTING_LEASE');
