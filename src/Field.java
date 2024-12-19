import java.util.*;

public class Field {
    private ColInfo colInfo; // Référence à ColInfo pour obtenir le type et la longueur
    private Object value;     // Valeur du champ

    // Constructeur
    public Field(ColInfo colInfo, Object value) {
        this.colInfo = colInfo;
        this.value = value;
    }

    // Méthodes pour obtenir le type et la valeur
    public ColmType getType() {
        return colInfo.getTypeCol(); // Retourne le type de la colonne
    }

    public Object getValue() {
        return value; // Retourne la valeur du champ
    }

    // Méthodes pour obtenir des valeurs spécifiques
    public int getIntValue() {
        if (colInfo.getTypeCol() == ColmType.INT) {
            return (Integer) value; // Cast de l'objet en Integer
        }
        throw new IllegalArgumentException("Type de champ non valide pour obtenir un entier.");
    }

    public char getCharValue() {
        if (colInfo.getTypeCol() == ColmType.CHAR) {
            return (Character) value; // Cast de l'objet en Character
        }
        throw new IllegalArgumentException("Type de champ non valide pour obtenir un caractère.");
    }

    public String getStringValue() {
        if (colInfo.getTypeCol() == ColmType.VARCHAR) {
            return (String) value; // Cast de l'objet en String
        }
        throw new IllegalArgumentException("Type de champ non valide pour obtenir une chaîne.");
    }
}
