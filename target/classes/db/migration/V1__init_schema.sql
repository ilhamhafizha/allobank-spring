CREATE TABLE bill_groups (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE participants (
                              id BIGSERIAL PRIMARY KEY,
                              group_id BIGINT NOT NULL,
                              name VARCHAR(100) NOT NULL,

                              CONSTRAINT fk_participants_group
                                  FOREIGN KEY (group_id)
                                      REFERENCES bill_groups(id)
                                      ON DELETE CASCADE
);

CREATE TABLE expenses (
                          id BIGSERIAL PRIMARY KEY,
                          group_id BIGINT NOT NULL,
                          paid_by BIGINT NOT NULL,
                          description VARCHAR(255) NOT NULL,
                          amount NUMERIC(19, 2) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_expenses_group
                              FOREIGN KEY (group_id)
                                  REFERENCES bill_groups(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_expenses_payer
                              FOREIGN KEY (paid_by)
                                  REFERENCES participants(id),

                          CONSTRAINT chk_expenses_amount_positive
                              CHECK (amount > 0)
);

CREATE TABLE expense_participants (
                                      expense_id BIGINT NOT NULL,
                                      participant_id BIGINT NOT NULL,

                                      PRIMARY KEY (expense_id, participant_id),

                                      CONSTRAINT fk_expense_participants_expense
                                          FOREIGN KEY (expense_id)
                                              REFERENCES expenses(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_expense_participants_participant
                                          FOREIGN KEY (participant_id)
                                              REFERENCES participants(id)
                                              ON DELETE CASCADE
);

CREATE INDEX idx_participants_group_id
    ON participants(group_id);

CREATE INDEX idx_expenses_group_id
    ON expenses(group_id);

CREATE INDEX idx_expenses_paid_by
    ON expenses(paid_by);

CREATE INDEX idx_expense_participants_participant_id
    ON expense_participants(participant_id);