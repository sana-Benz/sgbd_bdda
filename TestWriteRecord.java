package Projet_SGBD;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class TestWriteRecord {
    public static void main(String[] args) {
        // Créer un Record avec des valeurs simples 1111
        Record record = new Record(Arrays.asList("123", "456.78", "Hello"));

        // Créer une Relation avec les types de colonnes (sans ColInfo)
        List<String> columnNames = Arrays.asList("ID", "Salary", "Name");
        List<String> columnTypes = Arrays.asList("INT", "REAL", "CHAR(10)");
        Relation relation = new Relation("Employee", 3, columnNames, columnTypes);

        // Initialiser un ByteBuffer avec une taille suffisante
        ByteBuffer buffer = ByteBuffer.allocate(100);

        // Appeler writeRecordToBuffer
        int bytesWritten = relation.writeRecordToBuffer(record, buffer, 0);

        // Afficher le nombre total d'octets écrits
        System.out.println("Octets écrits: " + bytesWritten);

        // Vérifier le contenu du buffer
        buffer.position(0);  // Revenir au début du buffer
        int intValue = buffer.getInt();   // Lire l'entier 123
        float floatValue = buffer.getFloat();  // Lire le float 456.78
        byte[] nameBytes = new byte[10];  // Lire le CHAR(10) "Hello"
        buffer.get(nameBytes);
        String nameValue = new String(nameBytes).trim();  // Supprimer les espaces supplémentaires

        // Afficher les valeurs pour vérifier
        System.out.println("ID: " + intValue);  // Devrait être 123
        System.out.println("Salary: " + floatValue);  // Devrait être 456.78
        System.out.println("Name: " + nameValue);  // Devrait être "Hello"
    }
}