create sequence sys_smm_SEQ start with 1 increment by 1;
--
create sequence sys_smr_SEQ start with 1 increment by 1;
--
create table sys_mail_messages (
	smm_id			numeric(19, 0) not null constraint smm_pk primary key,
	smm_date		date not null,
	smm_subject		varchar(250) not null,
	smm_from_address	varchar(128) not null,
	smm_from_name		varchar(64) not null,
	smm_data		blob
);
--
create table sys_mail_recipients (
	smr_id			numeric(19, 0) not null constraint smr_pk primary key,
	smr_address		varchar(128) not null,
 	smr_type      	varchar(10) not null,
	smr_date_posted	date not null,
	smr_retries		numeric(3, 0) not null,
	smr_nextretry	date not null,
	smr_state		varchar(4) not null,
	smr_name		varchar(64) null,
	smr_lasterror	varchar(128) null,
	smm_id			numeric(19, 0) not null,

	constraint sys_smm_fk foreign key(smm_id) references sys_mail_messages(smm_id)
);
