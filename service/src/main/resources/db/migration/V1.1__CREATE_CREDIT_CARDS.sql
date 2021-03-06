CREATE TABLE "credit_cards"(
"number" varchar(16) PRIMARY KEY,
"account_id" bigint NOT NULL,
"name" varchar(100),
"brand_name" varchar(50),
"security_code" varchar(3)NOT NULL,
"expiration" date NOT NULL,
"remaining_limit" float,
"score_category_id" bigint,
"created_at" timestamp,
"updated_at" timestamp,
"deleted_on" timestamp,
CONSTRAINT fk_score_id FOREIGN KEY("score_category_id") REFERENCES "score_categories"("id"),
UNIQUE(number, deleted_on)
);
