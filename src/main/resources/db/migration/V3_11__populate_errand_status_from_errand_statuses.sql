UPDATE errand e
    JOIN (SELECT es1.errand_id, es1.status_type, es1.description, es1.date_time
          FROM errand_statuses es1
          WHERE NOT EXISTS (SELECT 1
                            FROM errand_statuses es2
                            WHERE es2.errand_id = es1.errand_id
                              AND es2.date_time > es1.date_time)) ls ON e.id = ls.errand_id
SET e.status             = ls.status_type,
    e.status_description = ls.description,
    e.status_created     = ls.date_time
where e.description is null
   or e.status is null
   or e.status_created is null;
