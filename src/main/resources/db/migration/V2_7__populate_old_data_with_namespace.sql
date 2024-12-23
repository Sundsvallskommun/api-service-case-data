UPDATE errand
SET namespace = 'SBK_MEX'
WHERE case_type IN (
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
                    'MEX_TERMINATION_OF_HUNTING_LEASE',
                    'MEX_FORWARDED_FROM_CONTACTSUNDSVALL'
    );

UPDATE errand
SET namespace = 'SBK_PARKINGPERMIT'
WHERE case_type IN (
                    'PARKING_PERMIT',
                    'PARKING_PERMIT_RENEWAL',
                    'LOST_PARKING_PERMIT'
    );

update attachment
set namespace = 'SBK_MEX'
where errand_id in (select id from errand where namespace = 'SBK_MEX');

update attachment
set namespace = 'SBK_PARKINGPERMIT'
where errand_id in (select id from errand where namespace = 'SBK_PARKINGPERMIT');

update appeal
set namespace = 'SBK_MEX'
where errand_id in (select id from errand where namespace = 'SBK_MEX');

update appeal
set namespace = 'SBK_PARKINGPERMIT'
where errand_id in (select id from errand where namespace = 'SBK_PARKINGPERMIT');

update decision
set namespace = 'SBK_MEX'
where errand_id in (select id from errand where namespace = 'SBK_MEX');

update decision
set namespace = 'SBK_PARKINGPERMIT'
where errand_id in (select id from errand where namespace = 'SBK_PARKINGPERMIT');

update facility
set namespace = 'SBK_MEX'
where errand_id in (select id from errand where namespace = 'SBK_MEX');

update facility
set namespace = 'SBK_PARKINGPERMIT'
where errand_id in (select id from errand where namespace = 'SBK_PARKINGPERMIT');

update message
set namespace = 'SBK_MEX'
where errand_number in (select errand.errand_number from errand where namespace = 'SBK_MEX');

update message
set namespace = 'SBK_PARKINGPERMIT'
where errand_number in (select errand.errand_number from errand where namespace = 'SBK_PARKINGPERMIT');

update message_attachment
set namespace = 'SBK_MEX'
where messageid in (select messageid from message where namespace = 'SBK_MEX');

update message_attachment
set namespace = 'SBK_PARKINGPERMIT'
where messageid in (select messageid from message where namespace = 'SBK_PARKINGPERMIT');

update notification
set namespace = 'SBK_MEX'
where errand_id in (select id from errand where namespace = 'SBK_MEX');

update notification
set namespace = 'SBK_PARKINGPERMIT'
where errand_id in (select id from errand where namespace = 'SBK_PARKINGPERMIT');

update note
set namespace = 'SBK_MEX'
where errand_id in (select id from errand where namespace = 'SBK_MEX');

update note
set namespace = 'SBK_PARKINGPERMIT'
where errand_id in (select id from errand where namespace = 'SBK_PARKINGPERMIT');

update stakeholder
set namespace = 'SBK_MEX'
where errand_id in (select id from errand where namespace = 'SBK_MEX');

update stakeholder
set namespace = 'SBK_PARKINGPERMIT'
where errand_id in (select id from errand where namespace = 'SBK_PARKINGPERMIT');
