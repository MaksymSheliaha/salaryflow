CREATE TABLE "password_reset_tokens" (
   id UUID PRIMARY KEY,
   token VARCHAR(255) NOT NULL,
   user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
   expiry_date TIMESTAMP NOT NULL
);