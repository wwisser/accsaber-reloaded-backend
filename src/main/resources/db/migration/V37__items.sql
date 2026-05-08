CREATE TABLE item_types (
    id             UUID         PRIMARY KEY DEFAULT uuidv7(),
    parent_type_id UUID         REFERENCES item_types(id) ON DELETE SET NULL,
    key            TEXT         NOT NULL UNIQUE,
    name           TEXT         NOT NULL,
    description    TEXT,
    value_schema   JSONB,
    active         BOOLEAN      NOT NULL DEFAULT true,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_item_types_parent ON item_types(parent_type_id);

CREATE TRIGGER trg_item_types_updated_at
    BEFORE UPDATE ON item_types
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE items (
    id          UUID         PRIMARY KEY DEFAULT uuidv7(),
    type_id     UUID         NOT NULL REFERENCES item_types(id),
    name        TEXT         NOT NULL,
    description TEXT,
    icon_url    TEXT,
    value       JSONB,
    tradeable   BOOLEAN      NOT NULL DEFAULT false,
    visible     BOOLEAN      NOT NULL DEFAULT true,
    active      BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (type_id, name)
);

CREATE INDEX idx_items_type           ON items(type_id);
CREATE INDEX idx_items_active_visible ON items(active, visible);

CREATE TRIGGER trg_items_updated_at
    BEFORE UPDATE ON items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE user_item_links (
    id         UUID         PRIMARY KEY DEFAULT uuidv7(),
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    item_id    UUID         NOT NULL REFERENCES items(id),
    source     TEXT         NOT NULL CHECK (source IN
                            ('milestone', 'milestone_set', 'campaign_milestone', 'level', 'trade', 'manual')),
    source_id  TEXT,
    awarded_by UUID         REFERENCES staff_users(id),
    awarded_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    reason     TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uq_user_item_links_idempotent
    ON user_item_links (user_id, item_id, source, source_id);

CREATE INDEX idx_user_item_links_user ON user_item_links(user_id);
CREATE INDEX idx_user_item_links_item ON user_item_links(item_id);

CREATE TRIGGER trg_user_item_links_updated_at
    BEFORE UPDATE ON user_item_links
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TABLE user_item_trades (
    id                UUID         PRIMARY KEY DEFAULT uuidv7(),
    from_user_id      BIGINT       NOT NULL REFERENCES users(id),
    to_user_id        BIGINT       NOT NULL REFERENCES users(id),
    user_item_link_id UUID         NOT NULL REFERENCES user_item_links(id) ON DELETE CASCADE,
    status            TEXT         NOT NULL DEFAULT 'pending'
                                   CHECK (status IN ('pending', 'accepted', 'declined', 'cancelled', 'expired')),
    message           TEXT,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    resolved_at       TIMESTAMPTZ,
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT trade_distinct_users CHECK (from_user_id <> to_user_id)
);

CREATE UNIQUE INDEX uq_user_item_trades_pending_link
    ON user_item_trades (user_item_link_id) WHERE status = 'pending';

CREATE INDEX idx_user_item_trades_to_pending
    ON user_item_trades (to_user_id) WHERE status = 'pending';
CREATE INDEX idx_user_item_trades_from_pending
    ON user_item_trades (from_user_id) WHERE status = 'pending';
CREATE INDEX idx_user_item_trades_link
    ON user_item_trades (user_item_link_id);

CREATE TRIGGER trg_user_item_trades_updated_at
    BEFORE UPDATE ON user_item_trades
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

ALTER TABLE milestone_sets      DROP COLUMN awards_badge_id;
ALTER TABLE campaign_milestones DROP COLUMN awards_badge_id;

ALTER TABLE milestone_sets      ADD COLUMN awards_item_id UUID REFERENCES items(id);
ALTER TABLE campaign_milestones ADD COLUMN awards_item_id UUID REFERENCES items(id);
ALTER TABLE milestones          ADD COLUMN awards_item_id UUID REFERENCES items(id);
ALTER TABLE level_thresholds    ADD COLUMN awards_item_id UUID REFERENCES items(id);

DROP TABLE user_badge_links;
DROP TABLE badges;

INSERT INTO item_types (key, name, description) VALUES
    ('profile_border',  'Profile Border',  'Parent grouping for player avatar border subtypes.'),
    ('profile_visual',  'Profile Visual',  'Parent grouping for profile background imagery.');

INSERT INTO item_types (parent_type_id, key, name, description, value_schema) VALUES
    (NULL, 'badge', 'Badge',
    'Visual badge displayed on a player profile.',
    '{"type":"object","properties":{"image_url":{"type":"string"}},"required":["image_url"]}'),

    (NULL, 'title', 'Title',
    'Player name title displayed on the profile and in the player card.',
    '{"type":"object","properties":{"text":{"type":"string"},"color":{"type":"string"}},"required":["text"]}'),

    ((SELECT id FROM item_types WHERE key = 'profile_border'),
    'profile_border_shape', 'Profile Border Shape',
    'Shape of the player avatar border. value.kind discriminates between static, animated, and morphing variants.',
    '{"type":"object","properties":{"kind":{"type":"string","enum":["static","animated","morph"]}},"required":["kind"]}'),

    ((SELECT id FROM item_types WHERE key = 'profile_border'),
    'profile_border_color', 'Profile Border Color',
    'Color of the player avatar border. value.kind discriminates between solid, gradient, and animated variants.',
    '{"type":"object","properties":{"kind":{"type":"string","enum":["solid","gradient","animated"]}},"required":["kind"]}'),

    (NULL, 'theme', 'Theme',
    'Custom site/profile theme.',
    '{"type":"object","properties":{"key":{"type":"string"}},"required":["key"]}'),

    ((SELECT id FROM item_types WHERE key = 'profile_visual'),
    'profile_background', 'Profile Background',
    'Background image of the profile page.',
    '{"type":"object","properties":{"image_url":{"type":"string"}},"required":["image_url"]}'),

    ((SELECT id FROM item_types WHERE key = 'profile_visual'),
    'profile_thumbnail_background', 'Profile Thumbnail Background',
    'Background of the small player thumbnail card.',
    '{"type":"object","properties":{"image_url":{"type":"string"}},"required":["image_url"]}'),

    (NULL, 'statistic', 'Statistic',
    'Showcase a specific player statistic on the profile.',
    '{"type":"object","properties":{"stat_key":{"type":"string"}},"required":["stat_key"]}'),

    (NULL, 'perk', 'Perk',
    'Functional unlock that expands a profile capability. Stack copies to grow the effect.',
    '{"type":"object","properties":{"effect":{"type":"string"},"amount":{"type":"number"}},"required":["effect"]}');
