import java.util.ArrayList;
import java.util.List;

public class Condition {
    private List<Condition> subConditions; // Liste des sous-conditions pour AND
    private String columnName;
    private String operator;
    private String value;
    private Relation relation;

    // Constructeur pour les conditions complexes et simples
    public Condition(String whereClause, String alias, Relation relation) {
        this.relation = relation;
        this.subConditions = new ArrayList<>();
        if (whereClause.contains(" AND ")) {
            // Découper la clause WHERE en sous-conditions avec "AND"
            String[] conditions = whereClause.split("\\s+AND\\s+");
            for (String condition : conditions) {
                this.subConditions.add(new Condition(condition.trim(), alias,relation));
            }
        } else {
            // Condition simple
            String[] parts = whereClause.split("\\s*(<=|>=|<>|[=<>])\\s*");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Clause WHERE invalide : " + whereClause);
            }
            this.columnName = parts[0].trim();
            if (alias != null && this.columnName.contains(".")) {
                this.columnName = this.columnName.split("\\.")[1]; // Retirer l'alias
            }
            this.operator = whereClause.replaceAll(".*?(<=|>=|<>|[=<>]).*", "$1");
            this.value = parts[1].trim();
        }
    }

    // Méthode d'évaluation principale
    public boolean evaluate(Record record) {
        if (!subConditions.isEmpty()) {
            // Évaluer chaque sous-condition avec "AND"
            for (Condition subCondition : subConditions) {
                if (!subCondition.evaluate(record)) {
                    return false; // Une condition échoue -> résultat global = false
                }
            }
            return true; // Toutes les sous-conditions sont vraies
        }

        // Évaluation d'une condition simple
        try {
            String recordValue = record.getValeurByNomCol(columnName).toString();
            System.out.println("Comparaison : recordValue = '" + recordValue + "' | valeur cherchée = '" + value + "'");

            switch (operator) {
                case "=":
                    return compareValues(recordValue, value);
                case "<":
                    return compareValuesForOrder(recordValue, value) < 0;
                case ">":
                    return compareValuesForOrder(recordValue, value) > 0;
                case "<=":
                    return compareValuesForOrder(recordValue, value) <= 0;
                case ">=":
                    return compareValuesForOrder(recordValue, value) >= 0;
                case "<>":
                    return !compareValues(recordValue, value);
                default:
                    throw new IllegalArgumentException("Opérateur non supporté : " + operator);
            }
        } catch (NumberFormatException e) {
            System.out.println("Erreur de format des nombres : " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Erreur d'évaluation de la condition : " + e.getMessage());
            return false;
        }
    }

    // Comparaison des valeurs pour "=" et "<>"
    private boolean compareValues(String recordValue, String searchValue) {
        try {
            if (searchValue.startsWith("\"") && searchValue.endsWith("\"")) {
                String cleanedSearchValue = searchValue.substring(1, searchValue.length() - 1);
                return recordValue.equals(cleanedSearchValue);
            } else {
                double recordNumber = Double.parseDouble(recordValue);
                double searchNumber = Double.parseDouble(searchValue);
                return recordNumber == searchNumber;
            }
        } catch (NumberFormatException e) {
            return recordValue.equals(searchValue); // Comparaison en tant que chaîne
        }
    }

    // Comparaison des valeurs pour les opérateurs "<", ">", "<=", ">="
    private int compareValuesForOrder(String recordValue, String searchValue) {
        if (searchValue.startsWith("\"") && searchValue.endsWith("\"")) {
            String cleanedSearchValue = searchValue.substring(1, searchValue.length() - 1);
            return recordValue.compareTo(cleanedSearchValue);
        }
        try {
            double recordNumber = Double.parseDouble(recordValue);
            double searchNumber = Double.parseDouble(searchValue);
            return Double.compare(recordNumber, searchNumber);
        } catch (NumberFormatException e) {
            return recordValue.compareTo(searchValue); // Comparaison en tant que chaîne
        }
    }
}