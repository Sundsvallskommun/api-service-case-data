UPDATE errand
SET namespace = 'SBK_MEX'
WHERE case_type LIKE 'MEX%';

UPDATE errand
SET namespace = 'SBK_PARKINGPERMIT'
WHERE case_type LIKE '%PARKING%';

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
