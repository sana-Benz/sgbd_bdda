public class Condition {
    private String column;
    private String operator;
    private String value;

    public Condition(String condition) {
        // Parsez la condition pour extraire la colonne, l'opérateur et la valeur
        String[] parts = condition.split(" ");
        this.column = parts[0];
        this.operator = parts[1];
        this.value = parts[2].replace("\"", ""); // Enlever les guillemets
    }

    public boolean evaluate(Record record) {
        String recordValue = record.getValeurByNomCol(column); // Utilisez la méthode ajoutée
        switch (operator) {
            case "=":
                return recordValue.equals(value);
            case "<":
                return recordValue.compareTo(value) < 0;
            case ">":
                return recordValue.compareTo(value) > 0;
            case "<=":
                return recordValue.compareTo(value) <= 0;
            case ">=":
                return recordValue.compareTo(value) >= 0;
            case "<>":
                return !recordValue.equals(value);
            default:
                return false;
        }
    }
}
