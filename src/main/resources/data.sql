INSERT INTO services (name, price) VALUES ('Маникюр', 1500) ON CONFLICT (name) DO NOTHING;
INSERT INTO services (name, price) VALUES ('Маникюр с покрытием', 2500) ON CONFLICT (name) DO NOTHING;
INSERT INTO services (name, price) VALUES ('Снятие покрытия', 500) ON CONFLICT (name) DO NOTHING;
