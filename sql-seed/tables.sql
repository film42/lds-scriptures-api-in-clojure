

CREATE TABLE "books"(
  id INT
, volume_id INT
, book_title VARCHAR(22)
, book_title_jst VARCHAR(27)
, book_title_long VARCHAR(59)
, book_title_short VARCHAR(8)
, book_subtitle VARCHAR(80)
, lds_org VARCHAR(6)
, num_chapters INT
, num_verses INT
);


CREATE INDEX "index_books_on_id" ON "books" ("id");
CREATE INDEX "index_books_on_volume_id" ON "books" ("volume_id");

CREATE TABLE "schema_migrations" (
"version" varchar(255) NOT NULL
);


CREATE UNIQUE INDEX "unique_schema_migrations" ON "schema_migrations" ("version");

CREATE TABLE "verses"(
  id INT
, volume_id INT
, book_id INT
, chapter INT
, verse INT
, pilcrow INT
, verse_scripture TEXT
, verse_title VARCHAR(30)
, verse_title_short VARCHAR(14)
);


CREATE INDEX "index_verses_on_book_id" ON "verses" ("book_id");
CREATE INDEX "index_verses_on_id" ON "verses" ("id");
CREATE INDEX "index_verses_on_volume_id" ON "verses" ("volume_id");

CREATE TABLE "volumes"(
  id INT
, volume_title VARCHAR(22)
, volume_title_long VARCHAR(26)
, volume_subtitle VARCHAR(36)
, lds_org VARCHAR(4)
, num_chapters INT
, num_verses INT
);


CREATE INDEX "index_volumes_on_id" ON "volumes" ("id");