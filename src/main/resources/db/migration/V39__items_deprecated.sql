ALTER TABLE items
    ADD COLUMN deprecated BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX idx_items_deprecated ON items(deprecated) WHERE deprecated = true;
