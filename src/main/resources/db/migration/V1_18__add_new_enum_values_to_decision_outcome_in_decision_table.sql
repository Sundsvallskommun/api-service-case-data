alter table if exists decision
   modify decision_outcome enum ('APPROVAL','CANCELLATION','CONDITIONAL_APPROVAL','DISMISSAL','REJECTION','REVOCATION');
