CREATE INDEX idx_vehicles_status ON vehicles(status);
CREATE INDEX idx_vehicles_year ON vehicles(year);
CREATE INDEX idx_vehicles_assigned_driver ON vehicles(assigned_driver_id);
CREATE INDEX idx_maintenance_vehicle_id ON maintenance_records(vehicle_id);
CREATE INDEX idx_maintenance_next_due ON maintenance_records(next_due_date);
CREATE INDEX idx_location_updates_vehicle_recorded ON location_updates(vehicle_id, recorded_at DESC);
CREATE INDEX idx_drivers_active ON drivers(active);