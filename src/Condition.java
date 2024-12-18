public class Condition {
    private String columnName;
    private String operator;
    private String value;

    public Condition(String whereClause) {
        String[] parts = whereClause.split("\\s*([=<>]+)\\s*");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Clause WHERE invalide : " + whereClause);
        }
        this.columnName = parts[0].trim();
        this.operator = whereClause.replaceAll(".*?([=<>]+).*", "$1");
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
            default:
                throw new IllegalArgumentException("Opérateur non supporté : " + operator);
        }
    }
}
