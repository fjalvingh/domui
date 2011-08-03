create sequence sys_smm_SEQ start with 1 increment by 1;

create sequence sys_smr_SEQ start with 1 increment by 1;

create table sys_mail_messages (
	smm_id			number(19, 0) not null constraint smm_pk primary key,
	smm_date		date not null,
	smm_subject		varchar2(250 char) not null,
	smm_from_address	varchar2(128 char) not null,
	smm_from_name		varchar2(64 char) not null,
	smm_data		blob not null
);
comment on table sys_mail_messages is 'The body of a mail message that is still to be sent to its recipients';

create table sys_mail_recipients (
	smr_id			number(19, 0) not null constraint smr_pk primary key,
	smr_address		varchar2(128 char) not null,
 	smr_type      	varchar2(10 char) not null,
	smr_date_posted	date not null,
	smr_retries		number(3, 0) not null,
	smr_nextretry	date not null,
	smr_state		varchar2(4 char) not null,
	smr_name		varchar2(64 char) null,
	smr_lasterror	varchar2(128 char) null,
	smm_id			number(19, 0) not null,

	constraint sys_smm_fk foreign key(smm_id) references sys_mail_messages(smm_id)
);
