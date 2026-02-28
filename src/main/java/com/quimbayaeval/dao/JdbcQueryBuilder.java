package com.quimbayaeval.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper para construir SQL dinámico con filtros avanzados, ordenación y paginación.
 * Soporta operadores: =, LIKE, >, <, >=, <=, IN, BETWEEN, IS NULL, IS NOT NULL
 */
public class JdbcQueryBuilder {
    
    /**
     * Enumeración de operadores soportados
     */
    public enum FilterOperator {
        EQUALS("="),
        LIKE("LIKE"),
        ILIKE("ILIKE"),  // Case-insensitive para PostgreSQL
        GT(">"),
        LT("<"),
        GTE(">="),
        LTE("<="),
        IN("IN"),
        BETWEEN("BETWEEN"),
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL");

        private final String sql;

        FilterOperator(String sql) {
            this.sql = sql;
        }

        public String getSql() {
            return sql;
        }
    }

    /**
     * Criterio de filtro individual
     */
    public static class FilterCriteria {
        public final String field;
        public final FilterOperator operator;
        public final Object value;
        public final Object valueTwo;  // Para BETWEEN: rango superior

        public FilterCriteria(String field, FilterOperator operator, Object value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
            this.valueTwo = null;
        }

        public FilterCriteria(String field, FilterOperator operator, Object value, Object valueTwo) {
            this.field = field;
            this.operator = operator;
            this.value = value;
            this.valueTwo = valueTwo;
        }
    }

    public static class QueryData {
        public final String sql;
        public final Object[] args;

        public QueryData(String sql, Object[] args) {
            this.sql = sql;
            this.args = args;
        }
    }

    /**
     * Versión simplificada: genera consulta con filtros básicos (igualdad)
     * @deprecated Usar build(String, List, String, String, Integer, Integer) para filtrado avanzado
     */
    @Deprecated
    public static QueryData build(String base,
                                  Map<String, Object> filters,
                                  String sortBy,
                                  String direction,
                                  Integer page,
                                  Integer size) {
        StringBuilder sb = new StringBuilder(base);
        List<Object> args = new ArrayList<>();

        if (filters != null && !filters.isEmpty()) {
            sb.append(" WHERE ");
            boolean first = true;
            for (Map.Entry<String, Object> e : filters.entrySet()) {
                if (!first) {
                    sb.append(" AND ");
                }
                // Validar nombre de columna (básico)
                if (!isValidColumnName(e.getKey())) {
                    throw new IllegalArgumentException("Nombre de columna inválido: " + e.getKey());
                }
                sb.append(e.getKey()).append(" = ?");
                args.add(e.getValue());
                first = false;
            }
        }

        appendOrderAndPagination(sb, args, sortBy, direction, page, size);
        return new QueryData(sb.toString(), args.toArray());
    }

    /**
     * Versión avanzada: genera consulta con filtros complejos y operadores personalizados
     */
    public static QueryData build(String base,
                                  List<FilterCriteria> filters,
                                  String sortBy,
                                  String direction,
                                  Integer page,
                                  Integer size) {
        StringBuilder sb = new StringBuilder(base);
        List<Object> args = new ArrayList<>();

        if (filters != null && !filters.isEmpty()) {
            sb.append(" WHERE ");
            boolean first = true;
            for (FilterCriteria criterion : filters) {
                if (!first) {
                    sb.append(" AND ");
                }
                buildFilterClause(sb, args, criterion);
                first = false;
            }
        }

        appendOrderAndPagination(sb, args, sortBy, direction, page, size);
        return new QueryData(sb.toString(), args.toArray());
    }

    /**
     * Construye una cláusula WHERE individual para un criterio
     */
    private static void buildFilterClause(StringBuilder sb, List<Object> args, FilterCriteria criterion) {
        // Validar nombre de columna
        if (!isValidColumnName(criterion.field)) {
            throw new IllegalArgumentException("Nombre de columna inválido: " + criterion.field);
        }

        switch (criterion.operator) {
            case EQUALS:
                if (criterion.value == null) {
                    sb.append(criterion.field).append(" IS NULL");
                } else {
                    sb.append(criterion.field).append(" = ?");
                    args.add(criterion.value);
                }
                break;

            case LIKE:
            case ILIKE:
                sb.append(criterion.field).append(" ").append(criterion.operator.getSql()).append(" ?");
                args.add("%" + criterion.value + "%");
                break;

            case GT:
            case LT:
            case GTE:
            case LTE:
                sb.append(criterion.field).append(" ").append(criterion.operator.getSql()).append(" ?");
                args.add(criterion.value);
                break;

            case IN:
                if (criterion.value instanceof List<?> list) {
                    sb.append(criterion.field).append(" IN (");
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) sb.append(",");
                        sb.append("?");
                        args.add(list.get(i));
                    }
                    sb.append(")");
                } else {
                    throw new IllegalArgumentException("IN operator requiere valores tipo List");
                }
                break;

            case BETWEEN:
                sb.append(criterion.field).append(" BETWEEN ? AND ?");
                args.add(criterion.value);
                args.add(criterion.valueTwo);
                break;

            case IS_NULL:
                sb.append(criterion.field).append(" IS NULL");
                break;

            case IS_NOT_NULL:
                sb.append(criterion.field).append(" IS NOT NULL");
                break;

            default:
                throw new IllegalArgumentException("Operador no soportado: " + criterion.operator);
        }
    }

    /**
     * Añade cláusulas ORDER BY, LIMIT y OFFSET
     */
    private static void appendOrderAndPagination(StringBuilder sb, List<Object> args,
                                                 String sortBy, String direction,
                                                 Integer page, Integer size) {
        if (sortBy != null && !sortBy.isEmpty()) {
            // Validar nombre de columna en ORDER BY
            if (!isValidColumnName(sortBy)) {
                throw new IllegalArgumentException("Nombre de columna inválido: " + sortBy);
            }
            sb.append(" ORDER BY ").append(sortBy);
            if ("DESC".equalsIgnoreCase(direction)) {
                sb.append(" DESC");
            } else {
                sb.append(" ASC");
            }
        }

        if (page != null && size != null && size > 0) {
            sb.append(" LIMIT ? OFFSET ?");
            args.add(size);
            args.add(page * size);
        }
    }

    /**
     * Valida que un nombre de columna sea seguro (previene SQL injection básico)
     * Acepta: letras, números, guiones bajos, puntos (para aliases)
     */
    private static boolean isValidColumnName(String columnName) {
        return columnName != null && columnName.matches("^[a-zA-Z0-9_\\.]+$");
    }
}
