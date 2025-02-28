UPDATE errand e
    JOIN (SELECT es1.errand_id, es1.status_type, es1.description, es1.created
          FROM errand_statuses es1
          WHERE NOT EXISTS (SELECT 1
                            FROM errand_statuses es2
                            WHERE es2.errand_id = es1.errand_id
                              AND es2.created > es1.created)) ls ON e.id = ls.errand_id
SET e.status             = ls.status_type,
    e.status_description = ls.description,
    e.status_created     = ls.created
where e.description is null
   or e.status is null
   or e.status_created is null;
