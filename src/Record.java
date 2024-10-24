import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Record {
	private Relation relation;
	private ArrayList<String> valeursRec;

	public Record(Relation relation) {
		this.relation = relation;
		this.valeursRec = new ArrayList<>();
	}

	public ArrayList<String> getValeursRec() {
		return valeursRec;
	}

	public void setValeursRec(ArrayList<String> valeursRec) {
		this.valeursRec = valeursRec;
	}

	public String valeurRec(int indexCol) {
		return valeursRec.get(indexCol);
	}

	/**
	 * Méthode writeToBuffer qui écrit l'enregistrement dans un tampon. Elle gère
	 * les types de colonnes tels que INT, FLOAT, CHAR (longueur fixe) et VARCHAR
	 * (longueur variable).
	 *
	 * @param buff : ByteBuffer - le tampon dans lequel écrire.
	 * @param pos  : int - la position de départ dans le tampon.
	 * @return int : la taille totale de l'enregistrement en octets, ou -1 en cas
	 *         d'erreur.
	 */

	public int writeToBuffer(ByteBuffer buff, int pos) {
		try {
			// Définir la position du buffer à la valeur spécifiée
			buff.position(pos);
			int totalSize = 0; // Variable pour suivre la taille totale du record

			// Parcourir toutes les colonnes du schéma de la table
			for (int i = 0; i < relation.getNbCol(); i++) {
				String value = valeursRec.get(i); // Récupérer la valeur actuelle pour la colonne

				// Switch basé sur le type de la colonne
				switch (relation.getTableCols().get(i).getTypeCol()) {

				// Cas pour les colonnes de type INT
				case INT:
					int intValue = Integer.parseInt(value);
					buff.putInt(intValue); // Écrire la valeur entière dans le buffer
					totalSize += 4; // Un INT occupe 4 octets
					break;

				// Cas pour les colonnes de type FLOAT
				case FLOAT:
					float floatValue = Float.parseFloat(value);
					buff.putFloat(floatValue); // Écrire la valeur flottante dans le buffer
					totalSize += 4; // Un FLOAT occupe 4 octets
					break;

				// Cas pour les chaînes de caractères de longueur fixe (CHAR)
				case CHAR:
					// Écrire une valeur CHAR de longueur fixe
					String charValue = value;
					int charLength = relation.getTableCols().get(i).getLengthString(); // Obtenir la longueur fixe
					byte[] charBytes = new byte[charLength]; // Créer un tableau de bytes de longueur fixe
					byte[] charValueBytes = charValue.getBytes(); // Convertir la chaîne en tableau de bytes

					// Copier les bytes de la chaîne dans le tableau de longueur fixe (troncature si
					// nécessaire)
					System.arraycopy(charValueBytes, 0, charBytes, 0, Math.min(charValueBytes.length, charLength));

					buff.put(charBytes); // Écrire les bytes de longueur fixe dans le buffer
					totalSize += charLength; // Ajouter la longueur du champ CHAR à la taille totale
					break;

				// Cas pour les chaînes de caractères de longueur variable (VARCHAR)
				case VARCHAR:
					// Écrire une valeur VARCHAR de longueur variable
					int varcharLength = value.length(); // Obtenir la longueur de la chaîne
					buff.putInt(varcharLength); // Écrire d'abord la longueur de la chaîne (4 octets)
					byte[] varcharBytes = value.getBytes(); // Convertir la chaîne en tableau de bytes
					buff.put(varcharBytes); // Écrire les bytes de la chaîne dans le buffer
					totalSize += 4 + varcharLength; // Ajouter 4 octets pour la longueur et la longueur de la chaîne à
													// la taille totale
					break;

				// Gérer les types de colonnes non pris en charge
				default:
					System.out.println("le type de la colonne invalide !!");
					break;
				}
			}

			// Retourner la taille totale du record en octets
			return totalSize;
		} catch (Exception e) {
			System.err.println("Erreur dans writeToBuffer : " + e.getMessage());
			return -1; // Retourner -1 en cas d'erreur
		}
	}

	public int readFromBuffer(ByteBuffer buff, int pos) {
		try {
			valeursRec.clear();
			buff.position(pos);
			int totalSize = 0;

			for (int i = 0; i < relation.getNbCol(); i++) {
				switch (relation.getTableCols().get(i).getTypeCol()) {
				case INT:
					int valeur_int = buff.getInt(); // lit 4 octs et les interpter comme un entier et avance la pos du
													// tampon de 4 octs
					valeursRec.add(Integer.toString(valeur_int));
					totalSize += 4;
					break;
				case FLOAT:
					float valeur_float = buff.getFloat();
					valeursRec.add(Float.toString(valeur_float));
					totalSize += 4;
					break;
				case CHAR:
					int charLength = relation.getTableCols().get(i).getLengthString();
					byte[] charBytes = new byte[charLength]; // Créer un tableau de bytes pour stocker les données lues
																// depuis le tampon
					buff.get(charBytes); // Lire les bytes correspondant à la longueur de la chaîne CHAR
					String valeur_char = new String(charBytes).trim(); // remove spaces or extra-padding
					valeursRec.add(valeur_char); // Ajouter la valeur lue (chaîne) dans la liste des valeurs
					totalSize += charLength;
					break;
				case VARCHAR:
					int varCharLength = buff.getInt();
					byte[] varCharBytes = new byte[varCharLength];
					buff.get(varCharBytes);
					String varCharValue = new String(varCharBytes);
					valeursRec.add(varCharValue);
					 totalSize += 4 + varCharLength;
					break;
				default:
					System.out.println("Ce type de la colonne invalide !!");
					break;
					
				}
			}
			return totalSize;
		} catch (Exception e) {
			System.err.println("Error in readFromBuffer: " + e.getMessage());
			return -1; // Or another error value as needed
		}
	}

}
