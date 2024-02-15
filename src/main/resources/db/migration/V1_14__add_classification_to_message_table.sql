alter table message
    add column classification enum(
    'INFORMATION',
    'COMPLETION_REQUEST',
    'OBTAIN_OPINION',
    'INTERNAL_COMMUNICATION',
    'OTHER');

