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


--MERGE INTO users (email, login, name, birthday) KEY(email, login) VALUES ('user1@user.com', 'user1', 'max1', '2001-01-01');
--MERGE INTO users (email, login, name, birthday) KEY(email, login) VALUES ('user2@user.com', 'user2', 'max2', '2002-02-02');
--MERGE INTO users (email, login, name, birthday) KEY(email, login) VALUES ('user3@user.com', 'user3', 'max3', '2003-03-03');

--MERGE INTO friends (user_id, friend_id, friendship_id) KEY(user_id, friend_id) VALUES (1, 2, 1);
--MERGE INTO friends (user_id, friend_id, friendship_id) KEY(user_id, friend_id) VALUES (1, 3, 2);
--MERGE INTO friends (user_id, friend_id, friendship_id) KEY(user_id, friend_id) VALUES (2, 3, 2);

--MERGE INTO films (name, description, releaseDate, duration, mpa_id) KEY(name, releaseDate) VALUES ('Фильм1', 'кнеенгенрпа кпекуенкн', '2011-01-01', 100, 5);
--MERGE INTO films (name, description, releaseDate, duration, mpa_id) KEY(name, releaseDate) VALUES ('Фильм2', 'кпкеркеренренононго', '2012-02-02', 60, 1);
--MERGE INTO films (name, description, releaseDate, duration, mpa_id) KEY(name, releaseDate) VALUES ('Фильм3', 'енегншщдлбттп', '2013-03-03', 95, 2);

--MERGE INTO likes (user_id, film_id) KEY(user_id, film_id) VALUES (1, 1);
--MERGE INTO likes (user_id, film_id) KEY(user_id, film_id) VALUES (2, 1);

--MERGE INTO films_genres (film_id, genre_id) KEY(film_id, genre_id) VALUES (1, 1);
--MERGE INTO films_genres (film_id, genre_id) KEY(film_id, genre_id) VALUES (1, 2);
--MERGE INTO films_genres (film_id, genre_id) KEY(film_id, genre_id) VALUES (1, 3);
--MERGE INTO films_genres (film_id, genre_id) KEY(film_id, genre_id) VALUES (2, 5);


