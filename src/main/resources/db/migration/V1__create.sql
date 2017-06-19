CREATE TABLE companies(
    id BIGSERIAL NOT NULL,
    name VARCHAR NOT NULL,
    website VARCHAR,
    PRIMARY KEY (id)
);

CREATE TABLE job_offers(
    id BIGSERIAL NOT NULL,
    company_id BIGINT NOT NULL,
    summary VARCHAR NOT NULL,
    description VARCHAR NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE RESTRICT
);

INSERT INTO companies (name, website) VALUES ('Intent HQ', 'https://www.intenthq.com');

INSERT INTO companies (name, website) VALUES ('Another Company', NULL);

INSERT INTO job_offers (company_id, summary, description) VALUES (1, 'Software Engineer', 'We would like to grow our team and are looking for somebody passionate about technology, sensitive about client needs and that collaborates well in a team environment.')
