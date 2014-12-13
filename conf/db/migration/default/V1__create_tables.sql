CREATE TABLE account (
  id          SERIAL    PRIMARY KEY,
  name        TEXT      NOT NULL
);


CREATE TABLE twitter_account (
  id                  BIGINT    PRIMARY KEY,
  account_id          INT       NOT NULL,
  screen_name         TEXT      NOT NULL
);

CREATE UNIQUE INDEX twitter_acc_idx ON twitter_account (account_id);

--CREATE TABLE github_account (
--);

--CREATE TABLE google_account (
--);


CREATE TABLE open_id_account (
  id          VARCHAR(4096)   PRIMARY KEY,
  provider    TEXT             NOT NULL,
  account_id  INT              NOT NULL,
  name        TEXT             NOT NULL
);

CREATE UNIQUE INDEX open_id_account ON open_id_account (account_id);

