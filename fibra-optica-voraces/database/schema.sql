-- Tabla nodos
DROP TABLE IF EXISTS nodos CASCADE;
CREATE TABLE nodos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100),
    tipo VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    capacidad_max INTEGER,
    clientes_actuales INTEGER DEFAULT 0,
    geom GEOMETRY(POINT, 4326) NOT NULL,
    CONSTRAINT check_tipo CHECK (tipo IN ('CENTRAL', 'POSTE_PRINCIPAL', 'POSTE_SECUNDARIO', 'CLIENTE')),
    CONSTRAINT check_estado CHECK (estado IN ('DISPONIBLE', 'SATURADO', 'INACTIVO'))
);
CREATE INDEX idx_nodos_geom ON nodos USING GIST (geom);

-- Tabla conexiones
DROP TABLE IF EXISTS conexiones CASCADE;
CREATE TABLE conexiones (
    id SERIAL PRIMARY KEY,
    origen_id INTEGER NOT NULL REFERENCES nodos(id) ON DELETE CASCADE,
    destino_id INTEGER NOT NULL REFERENCES nodos(id) ON DELETE CASCADE,
    distancia NUMERIC(10,2),
    costo NUMERIC(10,2)
);
CREATE INDEX idx_conexiones_origen ON conexiones(origen_id);
CREATE INDEX idx_conexiones_destino ON conexiones(destino_id);