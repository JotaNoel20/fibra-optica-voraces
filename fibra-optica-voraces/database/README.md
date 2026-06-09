# Módulo Database - Proyecto Fibra Óptica

## Configuración de la base de datos
- PostgreSQL 18 con extensión PostGIS.
- Base de datos: `fibra_optica_db`.

## Estructura de tablas
- `calles`: red vial (importada desde shapefile depurado con nombres reales).
- `nodos`: infraestructura (centrales, postes, clientes).
- `conexiones`: enlaces físicos.

## Importación del shapefile
Se utilizó QGIS y DB Manager para importar `calles_final.shp` (sin columna id duplicada). Los datos incluyen nombres de calles reales.

## Índices espaciales
- `idx_calles_geom` en `geom` de calles.
- `idx_nodos_geom` en `geom` de nodos.

## Uso
El backend puede consultar la red vial y construir el grafo.