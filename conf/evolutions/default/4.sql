# Add additional fields to pesticide registration table

# --- !Ups

ALTER TABLE pesticide_registration ADD COLUMN spray_volume varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN usage_time varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN main_agent_usage_count varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN usage_method varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN fumigation_time varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN fumigation_temperature varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN applicable_soil varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN applicable_zone_name varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN applicable_pesticide_name varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN mixture_count varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN active_ingredient_1_total_usage varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN active_ingredient_2_total_usage varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN active_ingredient_3_total_usage varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN active_ingredient_4_total_usage varchar(255);
ALTER TABLE pesticide_registration ADD COLUMN active_ingredient_5_total_usage varchar(255);

# --- !Downs

ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS spray_volume;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS usage_time;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS main_agent_usage_count;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS usage_method;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS fumigation_time;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS fumigation_temperature;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS applicable_soil;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS applicable_zone_name;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS applicable_pesticide_name;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS mixture_count;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS active_ingredient_1_total_usage;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS active_ingredient_2_total_usage;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS active_ingredient_3_total_usage;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS active_ingredient_4_total_usage;
ALTER TABLE pesticide_registration DROP COLUMN IF EXISTS active_ingredient_5_total_usage;