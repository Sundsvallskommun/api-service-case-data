alter table message
    add column viewed bit not null default 1;
    
alter table message 
	alter column viewed drop default;
