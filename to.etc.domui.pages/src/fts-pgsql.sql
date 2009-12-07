drop table fts_word;
drop table fts_doc;
drop table fts_occurence;
drop table fts_frag;

-- Since this needs to be godsallejesus fast we do not use referential integrity.
create table fts_word (
	fwd_id			numeric(16, 0) not null constraint FTS_WORD_PK primary key,
	fwd_noccurences numeric(6, 0) not null default 0,
	fwd_word		varchar(100) not null
);

create unique index fts_word_byname_ix on fts_word(fwd_word);

create table fts_frag (
	ffr_id			numeric(16, 0) not null constraint FTS_FFR_PK primary key,
	ffr_name		varchar(32) not null
);
create unique index fts_frag_bykey_ix on fts_frag(ffr_name);

create table fts_occurence (
	fwd_id			numeric(16, 0) not null,
	fdo_type		varchar(4) not null,
	foc_wordindex	numeric(8, 0) not null,
	fdo_id			numeric(16, 0) not null,
	ffr_id			numeric(16, 0) not null,
	foc_sentence	numeric(6, 0) not null,
	
	constraint FTS_OCCURENCE_PK primary key (fwd_id, fdo_type, foc_wordindex, fdo_id, ffr_id)
);
create sequence fts_doc_sq increment 1 minvalue 1 start 1;
create sequence fts_frag_sq increment 1 minvalue 1 start 1;
create sequence fts_word_sq increment 1 minvalue 1 start 1;
