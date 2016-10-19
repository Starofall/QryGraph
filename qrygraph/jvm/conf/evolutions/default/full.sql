
CREATE TABLE IF NOT EXISTS data_sources (
  id          VARCHAR(36)  NOT NULL,
  name        VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL,
  loadCommand    VARCHAR(1024) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users (
  id          VARCHAR(36)  NOT NULL,
  email       VARCHAR(255) NOT NULL,
  password    VARCHAR(255) NOT NULL,
  first_name  VARCHAR(45)  NOT NULL,
  last_name   VARCHAR(45)  NOT NULL,
  user_role   VARCHAR(45)  NOT NULL,
  create_time TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- -----------------------------------------------------
-- Table pig_queries
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS pig_queries (
  id                         VARCHAR(36)  NOT NULL,
  name                       VARCHAR(45)  NOT NULL,
  description                VARCHAR(255) NOT NULL,
  serialized_draft_querie    VARCHAR      NULL,
  serialized_deployed_querie VARCHAR      NULL,
  undeployed_changes         BOOLEAN      NOT NULL,
  creator_user_id            VARCHAR(36)  NOT NULL,
  authorization_status       VARCHAR(45)  NOT NULL,
  execution_status           VARCHAR(45)  NOT NULL,
  cronjob                    VARCHAR(45)  NOT NULL,
  PRIMARY KEY (id)
);

-- -----------------------------------------------------
-- Table pig_components
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS pig_components (
  id                VARCHAR(36)  NOT NULL,
  name              VARCHAR(45)  NOT NULL,
  description       VARCHAR(255) NOT NULL,
  serialized_querie VARCHAR      NULL,
  published         BOOLEAN      NOT NULL,
  creator_user_id   VARCHAR(36)  NOT NULL,
  PRIMARY KEY (id)
);

--------------------------------------------------
-- Table pig_queries
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS pig_queries (
  id                         VARCHAR(36)  NOT NULL,
  name                       VARCHAR(45)  NOT NULL,
  description                VARCHAR(255) NOT NULL,
  serialized_draft_querie    VARCHAR      NULL,
  serialized_deployed_querie VARCHAR      NULL,
  undeployed_changes         BOOLEAN      NOT NULL,
  creator_user_id            VARCHAR(36)  NOT NULL,
  authorization_status       VARCHAR(45)  NOT NULL,
  execution_status           VARCHAR(45)  NOT NULL,
  cronjob                    VARCHAR(45)  NOT NULL,
  PRIMARY KEY (id)
);

-- -----------------------------------------------------
-- Table query_access_rights
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS query_access_rights (
  id          VARCHAR(36) NOT NULL,
  right_level VARCHAR(45) NULL,
  user_id     VARCHAR(36) NOT NULL,
  query_id    VARCHAR(36) NOT NULL,
  PRIMARY KEY (id)
);

-- -----------------------------------------------------
-- Table query_executions
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS query_executions (
  id             VARCHAR(36) NOT NULL,
  result_status  VARCHAR(45) NOT NULL,
  queries_id     VARCHAR(36) NOT NULL,
  result_log     VARCHAR     NULL,
  execution_time TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- -----------------------------------------------------
-- Table tokens
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS user_tokens (
  token         VARCHAR(36) NOT NULL,
  user_id       VARCHAR(36) NOT NULL,
  creation_time TIMESTAMP   NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (token)
);

-- -----------------------------------------------------
-- Table settings
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS global_settings (
  id                 VARCHAR(36)   NOT NULL,
  hadoop_user        VARCHAR(36)   NOT NULL,
  qrygraph_folder    VARCHAR(1023) NOT NULL,
  fs_default_name    VARCHAR(1023) NOT NULL,
  mapred_job_tracker VARCHAR(1023) NOT NULL,
  PRIMARY KEY (id)
);
