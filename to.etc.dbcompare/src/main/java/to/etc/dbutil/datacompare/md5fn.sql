CREATE OR REPLACE FUNCTION lo_md5(id oid)
RETURNS text
as $$
DECLARE
 fd        integer;
 size      integer;
 hashval   text;
 INV_READ  constant integer := 262144; -- 0x40000 from libpq-fs.h
 SEEK_SET  constant integer := 0;
 SEEK_END  constant integer := 2;
BEGIN
 IF id is null THEN
   RETURN NULL;
 END IF;
 fd   := lo_open(id, INV_READ);
 size := lo_lseek(fd, 0, SEEK_END);
 PERFORM lo_lseek(fd, 0, SEEK_SET);
 hashval := md5(loread(fd, size));
 PERFORM lo_close(fd);
 RETURN hashval;
END;
$$
language plpgsql stable strict;
