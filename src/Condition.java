public class Condition {
    private String columnName;
    private String operator;
    private String value;
    private Relation relation;

    public Condition(String conditionString, String alias, Relation relation) {
        this.relation = relation;

        String[] parts = conditionString.split("AND");
        for (String part : parts) {
            part = part.trim();
            if (part.contains("=") || part.contains(">") || part.contains("<")) {
                String[] elements = part.split("(?<=[=><])|(?=[=><])"); // Split par opérateur
                this.columnName = elements[0].trim().split("\\.")[1]; // Retirer l'alias
                this.operator = elements[1].trim();
                this.value = elements[2].trim();
            }
        }
    }

    public boolean evaluate(Record record) {
        try {
            // Récupérer la valeur du record pour la colonne concernée
            String recordValue = record.getValeurByNomCol(columnName);
            System.out.println("Comparaison : recordValue = '" + recordValue + "' | valeur cherchée = '" + value + "'");

            // Évaluer la condition en fonction de l'opérateur
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
                    throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        } catch (NumberFormatException e) {
            System.out.println("Erreur de format des nombres : " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Erreur d'évaluation de la condition : " + e.getMessage());
            return false;
        }
    }


    private boolean compareValues(String recordValue, String searchValue) {
        try {
            // Remove quotes if the searchValue is a string
            if (searchValue.startsWith("\"") && searchValue.endsWith("\"")) {
                String cleanedSearchValue = searchValue.substring(1, searchValue.length() - 1);
                return recordValue.equals(cleanedSearchValue);
            } else {
                // Compare as numbers if no quotes are present
                double recordNumber = Double.parseDouble(recordValue);
                double searchNumber = Double.parseDouble(searchValue);
                return recordNumber == searchNumber;
            }
        } catch (NumberFormatException e) {
            // Fallback to string comparison if number parsing fails
            return recordValue.equals(searchValue);
        }
    }

    private int compareValuesForOrder(String recordValue, String searchValue) {
        // Remove quotes if the searchValue is a string
        if (searchValue.startsWith("\"") && searchValue.endsWith("\"")) {
            String cleanedSearchValue = searchValue.substring(1, searchValue.length() - 1);
            return recordValue.compareTo(cleanedSearchValue);
        }
        // Fallback to numeric comparison if applicable
        try {
            double recordNumber = Double.parseDouble(recordValue);
            double searchNumber = Double.parseDouble(searchValue);
            return Double.compare(recordNumber, searchNumber);
        } catch (NumberFormatException e) {
            // Fallback to string comparison if numeric parsing fails
            return recordValue.compareTo(searchValue);
        }
    }


}
