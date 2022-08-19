CREATE TABLE IF NOT EXISTS users
(
    id
    BIGINT
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
),
    handle VARCHAR
(
    255
)
    );

CREATE TABLE IF NOT EXISTS tweets
(
    id
    BIGINT
    PRIMARY
    KEY,
    tweet_text
    VARCHAR
(
    255
),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT REFERENCES users
    );


INSERT INTO users(id, name, handle)
VALUES (1, 'Indi Mcconnell', 'tireson'),
       (2, 'Krista Sparks', 'impling'),
       (3, 'Aydin Leonard', 'telepoi'),
       (4, 'Shane Webb', 'sanspan');

INSERT INTO tweets(id, tweet_text, user_id)
VALUES (1, 'Congrats to all a treat to a game of baking', 1),
       (2,
        'Good night Dublin! I will be with you for 14 hours, 31 C Good night Mumbai! I will be back in Paris in.',
        1),
       (3,
        'Spring Break is coming to Alumni Athletics',
        2),
       (4,
        'Yikes',
        2),
       (5,
        'Good article What video games have reached a phase. Heatwave in recent weeks & days. Accurate? Saxon?',
        3),
       (6,
        'I had more Products that it seems that It sure that It sure Would be trending down than they made this.',
        3),
       (7,
        'True Quite the future for sure Would be much lower in less than 5% of 20 years! Just Read The media is!',
        3),
       (8,
        'Maybe free some equity pa… Tesla Fremont team is actually an insane amount of Destiny Was a defamation!',
        4);
