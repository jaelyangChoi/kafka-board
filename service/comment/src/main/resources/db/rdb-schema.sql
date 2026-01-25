-- 2 depth 댓글 테이블 생성
create table comment (
    comment_id bigint not null primary key,
    content varchar(3000) not null,
    article_id bigint not null,
    parent_comment_id bigint not null,
    writer_id bigint not null,
    deleted bool not null,
    created_at datetime not null
);

-- 인덱스
create index idx_article_id_parent_comment_id_comment_id
          on comment (article_id asc,parent_comment_id asc,comment_id asc);

-- 무한 depth 댓글 테이블 생성
create table comment_v2 (
                            comment_id bigint not null primary key,
                            content varchar(3000) not null,
                            article_id bigint not null,
                            writer_id bigint not null,
                            path varchar(25) character set utf8mb4 collate utf8mb4_bin not null,
                            deleted bool not null,
                            created_at datetime not null
);

-- collation 적용 확인
select table_name, column_name, collation_name
from information_schema.COLUMNS
where table_schema = 'comment' and table_name = 'comment_v2' and column_name = 'path';

-- 인덱스 생성
create unique index idx_article_id_path on comment_v2(article_id asc, path asc);
