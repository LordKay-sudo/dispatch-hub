-- Allow in-compose webhook-echo hostname for full Docker demos
INSERT INTO tenant_egress_host (id, tenant_id, host_pattern) VALUES
    ('dddddddd-dddd-dddd-dddd-ddddddddddd5', '11111111-1111-1111-1111-111111111111', 'webhook-echo'),
    ('dddddddd-dddd-dddd-dddd-ddddddddddd6', '22222222-2222-2222-2222-222222222222', 'webhook-echo');
