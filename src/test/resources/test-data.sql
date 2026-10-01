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

MERGE INTO users (email, login, name, birthday) KEY(email, login)
VALUES ('email@email.com', 'user', 'test', '2000-03-20');

MERGE INTO films (name, description, releaseDate, duration, mpa_id) KEY(name, releaseDate)
VALUES ('film', 'dfghdjyuyuiukjnvbnvccxfgder', '2000-03-20', 100, 2);

MERGE INTO films_genres (film_id, genre_id) KEY(film_id, genre_id) VALUES (1, 3);


