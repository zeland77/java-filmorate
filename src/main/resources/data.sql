MERGE INTO genres (name) KEY(name) VALUES ('Комедия');
MERGE INTO genres (name) KEY(name) VALUES ('Драма');
MERGE INTO genres (name) KEY(name) VALUES ('Мультфильм');
MERGE INTO genres (name) KEY(name) VALUES ('Триллер');
MERGE INTO genres (name) KEY(name) VALUES ('Документальный');
MERGE INTO genres (name) KEY(name) VALUES ('Боевик');

MERGE INTO mpa (name) KEY(name) VALUES ('G');
MERGE INTO mpa (name) KEY(name) VALUES ('PG');
MERGE INTO mpa (name) KEY(name) VALUES ('PG-13');
MERGE INTO mpa (name) KEY(name) VALUES ('R');
MERGE INTO mpa (name) KEY(name) VALUES ('NC-17');

MERGE INTO friendship (name) KEY(name) VALUES ('неподтверждённая');
MERGE INTO friendship (name) KEY(name) VALUES ('подтверждённая');

