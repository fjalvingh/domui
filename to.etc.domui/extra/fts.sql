-- Since this needs to be godsallejesus fast we do not use referential integrity.
create table fts_word (
	fwd_id			numeric(16, 0) not null constraint FTS_WORD_PK primary key,
	fwd_word		varchar2(100) not null
);

create unique index fts_word_byname_ix on fts_word(fwd_word);

create table fts_doc (
	fdo_id			numeric(16, 0) not null constraint FTS_DOC_PK primary key,
	fdo_type		varchar2(10) not null,
	fdo_key			varchar2(64) not null
);
create unique index fts_doc_bykey_ix on fts_doc(fdo_type,fdo_key);

create table fts_frag (
	ffr_id			numeric(16, 0) not null constraint FTS_FFR_PK primary key,
	ffr_name		varchar2(32) not null
);
create unique index fts_frag_bykey_ix on fts_frag(ffr_name);

create table fts_occurence (
	fwd_id			numeric(16, 0) not null,
	foc_wordindex	numeric(8, 0) not null,
	fdo_id			numeric(16, 0) not null,
	ffr_id			numeric(16, 0) not null,
	foc_sentence	numeric(6, 0) not null,
	
	constraint FTS_OCCURENCE_PK primary key (fwd_id, foc_wordindex, fdo_id, ffr_id)
);
