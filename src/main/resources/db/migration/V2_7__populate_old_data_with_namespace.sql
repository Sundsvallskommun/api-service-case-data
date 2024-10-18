UPDATE errand
SET namespace = 'SBK_MEX'
WHERE case_type LIKE 'MEX%';

UPDATE errand
SET namespace = 'SBK_PARKINGPERMIT'
WHERE case_type LIKE '%PARKING%';
