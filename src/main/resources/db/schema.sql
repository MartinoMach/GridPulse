create table if not exists network_events (
    event_id varchar(64) primary key,
    asset_id varchar(64) not null,
    event_timestamp timestamptz not null,
    event_type varchar(32) not null,
    severity varchar(16) not null,
    metadata jsonb not null,
    kdd_label varchar(128) not null
);

create index if not exists idx_network_events_asset_id
    on network_events (asset_id);

create index if not exists idx_network_events_timestamp
    on network_events (event_timestamp desc);

create index if not exists idx_network_events_event_type
    on network_events (event_type);

create index if not exists idx_network_events_asset_timestamp
    on network_events (asset_id, event_timestamp desc);

create table if not exists prediction_records (
    id bigserial primary key,
    asset_id varchar(64) not null,
    predicted_at timestamptz not null,
    failure_probability double precision not null,
    predicted_failure boolean not null,
    confirmed_failure boolean not null,
    kdd_label varchar(128) not null
);

create index if not exists idx_prediction_records_asset_predicted_at
    on prediction_records (asset_id, predicted_at desc);

create index if not exists idx_prediction_records_outcome
    on prediction_records (predicted_failure, confirmed_failure);
