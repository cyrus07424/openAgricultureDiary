# Add admin flag to users

# --- !Ups

ALTER TABLE app_user ADD COLUMN is_admin BOOLEAN DEFAULT FALSE;

# --- !Downs

ALTER TABLE app_user DROP COLUMN is_admin;