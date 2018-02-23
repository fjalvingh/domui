-- Create the initial database.
create table au_user (
	id						varchar(23) not null constraint usr_pk primary key,
	fullname				varchar(64) not null,
	email					varchar(128) not null,
	password				varchar(128) null,
	phonenumber				varchar(30) null
);

create unique index usr_email_ix on au_user(email);

create table au_group (
	id						varchar(23) not null constraint grp_pk primary key,
	name					varchar(64) not null,
	description				varchar(1024) null
);
create unique index grp_name_ix on au_group(name);

create table au_group_member (
	id						varchar(23) not null constraint grm_pk primary key,
	groupid					varchar(23) not null,
	userid					varchar(23) not null
);

alter table au_group_member
	add constraint grm_usr_fk
		foreign key(userid) references au_user(id)
		on delete cascade
;

alter table au_group_member
	add constraint grm_grp_fk
		foreign key(groupid) references au_group(id)
		on delete cascade
;

create table au_group_permission (
	id						varchar(23) not null constraint gpm_pk primary key,
	groupid					varchar(23) not null,
	right_name				varchar(64) not null
);

alter table au_group_permission
	add constraint gpm_grp_fk
		foreign key(groupid) references au_group(id)
		on delete cascade
;

insert into au_user values('1', 'Test User', 'jal@laj.moc', 'abc', 'asa');
insert into au_user values('2', 'Frats Jilvingh', 'laj@cte.moc', 'f34a19caa7c84fb70937ad3555ae845e8f0621a9;1420facffce2ddd8b41140562d93ac498b177663', '1234-34');
insert into au_user values('3', 'Admin', 'admin@example.com', 'f34a19caa7c84fb70937ad3555ae845e8f0621a9;1420facffce2ddd8b41140562d93ac498b177663', '1234-34');

insert into au_group(id, name) values ('1', 'Administrators');
insert into au_group(id, name) values ('2', 'Users');

insert into au_group_member(id, groupid, userid) values('1', '2', '1');
insert into au_group_member(id, groupid, userid) values('2', '2', '2');
insert into au_group_member(id, groupid, userid) values('3', '1', '3');

insert into au_group_permission(id, groupid, right_name) values ('1', '1', 'admin');
insert into au_group_permission(id, groupid, right_name) values ('2', '2', 'user');
