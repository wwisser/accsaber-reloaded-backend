CREATE TABLE item_modifiers (
    id          UUID         PRIMARY KEY DEFAULT uuidv7(),
    key         TEXT         NOT NULL UNIQUE,
    name        TEXT         NOT NULL,
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_item_modifiers_updated_at
    BEFORE UPDATE ON item_modifiers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

INSERT INTO item_modifiers (key, name, description) VALUES
    ('normal',       'Normal',       'Default item state - no special markers.'),
    ('unique',       'Unique',       'Standard awarded item. Most player-earned items use this.'),
    ('vintage',      'Vintage',      'Issued automatically to instances of an item when it is reworked into a new version.'),
    ('genuine',      'Genuine',      'Originated from an external promotion or partner event.'),
    ('strange',      'Strange',      'Tracks personal-best counts while equipped.'),
    ('unusual',      'Unusual',      'Carries a particle / animated effect declared in items.value.'),
    ('haunted',      'Haunted',      'Awarded with a chance during the Halloween season.'),
    ('jolly',        'Jolly',        'Awarded with a chance during the Christmas season.'),
    ('collectors',   'Collector''s', 'Top-tier reward, e.g. perfect set completion.'),
    ('holographic',  'Holographic',  'Animated shimmer overlay on the item display.'),
    ('decorated',    'Decorated',    'Has wear sub-states on top of the base item.'),
    ('ascendant',    'Ascendant',    'Highest-tier ranked-staff award.'),
    ('battle_worn',  'Battle-Worn',  'Visibly weathered; earned through heavy use of the equipped slot.'),
    ('founders',     'Founder''s',   'Reserved for the first holders of an item (low serial numbers).');

ALTER TABLE items
    ADD COLUMN rarity      TEXT   NOT NULL DEFAULT 'common'
               CHECK (rarity IN ('common', 'uncommon', 'rare', 'epic', 'legendary', 'mythic')),
    ADD COLUMN next_serial BIGINT NOT NULL DEFAULT 1
               CHECK (next_serial > 0);

ALTER TABLE user_item_links
    ADD COLUMN modifier_id   UUID   REFERENCES item_modifiers(id),
    ADD COLUMN serial_number BIGINT;

UPDATE user_item_links
SET modifier_id = (SELECT id FROM item_modifiers WHERE key = 'normal')
WHERE modifier_id IS NULL;

ALTER TABLE user_item_links
    ALTER COLUMN modifier_id SET NOT NULL;

CREATE INDEX idx_user_item_links_modifier ON user_item_links(modifier_id);
CREATE INDEX idx_user_item_links_item_serial ON user_item_links(item_id, serial_number);
CREATE UNIQUE INDEX uq_user_item_links_item_serial
    ON user_item_links(item_id, serial_number) WHERE serial_number IS NOT NULL;
