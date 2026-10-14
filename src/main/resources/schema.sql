CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    login VARCHAR(40) UNIQUE NOT NULL,
    name VARCHAR(40) NOT NULL,
    birthday TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS friends (
    user_id BIGINT,
    friend_id BIGINT,
    friendship_id BIGINT,
    PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS friendship (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(40) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(40) NOT NULL,
    description VARCHAR(200) NOT NULL,
    releaseDate TIMESTAMP NOT NULL,
    duration BIGINT NOT NULL,
    mpa_id BIGINT
);

CREATE TABLE IF NOT EXISTS likes (
    user_id BIGINT,
    film_id BIGINT,
    PRIMARY KEY (user_id, film_id)
);

CREATE TABLE IF NOT EXISTS genres (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(40) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS films_genres (
    film_id BIGINT,
    genre_id BIGINT,
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS mpa (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(20)
);

ALTER TABLE friends ADD FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE friends ADD FOREIGN KEY (friend_id) REFERENCES users (id);
ALTER TABLE friends ADD FOREIGN KEY (friendship_id) REFERENCES friendship (id);

ALTER TABLE likes ADD FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE likes ADD FOREIGN KEY (film_id) REFERENCES films (id);

ALTER TABLE films_genres ADD FOREIGN KEY (film_id) REFERENCES films (id);
ALTER TABLE films_genres ADD FOREIGN KEY (genre_id) REFERENCES genres (id);

ALTER TABLE films ADD FOREIGN KEY (mpa_id) REFERENCES mpa (id);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    content VARCHAR(300) NOT NULL,
    isPositive BOOLEAN DEFAULT TRUE,
    user_id BIGINT,
    film_id BIGINT,
    CONSTRAINT unique_user_film UNIQUE (user_id, film_id)
);

CREATE TABLE IF NOT EXISTS reviews_likes (
    review_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (user_id, review_id)
);

ALTER TABLE reviews_likes ADD FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE reviews_likes ADD FOREIGN KEY (review_id) REFERENCES reviews (id);

CREATE TABLE IF NOT EXISTS reviews_dislikes (
    review_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (user_id, review_id)
);

ALTER TABLE reviews_dislikes ADD FOREIGN KEY (user_id) REFERENCES users (id);
ALTER TABLE reviews_dislikes ADD FOREIGN KEY (review_id) REFERENCES reviews (id);

