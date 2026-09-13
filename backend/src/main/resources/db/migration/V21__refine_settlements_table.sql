ALTER TABLE settlements
    ADD COLUMN settlement_utr VARCHAR(100),
    ADD COLUMN fee BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN tax BIGINT NOT NULL DEFAULT 0;

ALTER TABLE settlements
    ADD CONSTRAINT chk_settlements_fee
    CHECK (fee >= 0);

ALTER TABLE settlements
    ADD CONSTRAINT chk_settlements_tax
    CHECK (tax >= 0);