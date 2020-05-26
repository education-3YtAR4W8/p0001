drop table if exists item_tbl;
create table item_tbl (
    item_id char(36) not null,
    name varchar(256) not null,
    position_id char(36) not null,
    description varchar(1024) not null default '',
    primary key(item_id)
);

drop table if exists tag_tbl;
create table tag_tbl (
    tag_id char(36) not null,
    name varchar(256) not null default '',
    primary key(tag_id)
 );

drop table if exists position_tbl;
create table position_tbl (
    position_id char(36) not null,
    name varchar(256) not null default '',
    primary key(position_id)
);

drop table if exists item_tag_tbl;
create table item_tag_tbl (
    item_id char(36) not null,
    tag_id char(36) not null,
    primary key(item_id, tag_id)
);
create index on item_tag_tbl (tag_id, item_id);
