/*public class Condition {
    private String columnName;
    private String operator;
    private String value;

    public Condition(String whereClause) {
        String[] parts = whereClause.split("\\s*([=<>]+)\\s*");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Clause WHERE invalide : " + whereClause);
        }
        this.columnName = parts[0].trim();
        this.operator = whereClause.replaceAll(".*?(<=|>=|<>|[=<>]).*", "$1");
        this.value = parts[1].trim();
    }

    public boolean evaluate(Record record) {
        String recordValue = record.getValeurByNomCol(columnName).toString();
        switch (operator) {
            case "=":
                return recordValue.equals(value);
            case ">":
                return Double.parseDouble(recordValue) > Double.parseDouble(value);
            case "<":
                return Double.parseDouble(recordValue) < Double.parseDouble(value);
            case ">=":
                return Double.parseDouble(recordValue) >= Double.parseDouble(value);
            case "<=":
                return Double.parseDouble(recordValue) <= Double.parseDouble(value);
            case "<>":
                return !recordValue.equals(value);
            default:
                throw new IllegalArgumentException("Opérateur non supporté : " + operator);
        }
    }
}*/


import java.util.ArrayList;
import java.util.List;

public class Condition {
    private List<Condition> subConditions; // Liste des sous-conditions
    private String columnName;
    private String operator;
    private String value;

    // Constructeur pour les conditions simples
    public Condition(String whereClause) {
        this.subConditions = new ArrayList<>();
        if (whereClause.contains(" AND ")) {
            // Découper la clause WHERE en conditions séparées par AND
            String[] conditions = whereClause.split("\\s+AND\\s+");
            for (String condition : conditions) {
                this.subConditions.add(new Condition(condition.trim())); // Ajouter chaque sous-condition
            }
        } else {
            // Condition simple (pas de "AND")
            String[] parts = whereClause.split("\\s*(<=|>=|<>|[=<>])\\s*");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Clause WHERE invalide : " + whereClause);
            }
            this.columnName = parts[0].trim();
            this.operator = whereClause.replaceAll(".*?(<=|>=|<>|[=<>]).*", "$1");
            this.value = parts[1].trim();
        }
    }

    // Évaluation de la condition
    public boolean evaluate(Record record) {
        if (!subConditions.isEmpty()) {
            // Évaluer toutes les sous-conditions avec "AND"
            for (Condition subCondition : subConditions) {
                if (!subCondition.evaluate(record)) {
                    return false; // Une sous-condition échoue -> résultat global = false
                }
            }
            return true; // Toutes les sous-conditions sont vraies
        }

        // Évaluation d'une condition simple
        String recordValue = record.getValeurByNomCol(columnName).toString();
        switch (operator) {
            case "=":
                return recordValue.equals(value);
            case ">":
                return Double.parseDouble(recordValue) > Double.parseDouble(value);
            case "<":
                return Double.parseDouble(recordValue) < Double.parseDouble(value);
            case ">=":
                return Double.parseDouble(recordValue) >= Double.parseDouble(value);
            case "<=":
                return Double.parseDouble(recordValue) <= Double.parseDouble(value);
            case "<>":
                return !recordValue.equals(value);
            default:
                throw new IllegalArgumentException("Opérateur non supporté : " + operator);
        }
    }
}

