/* --------------------------------------------------------------------------------
 * ECN Tools
 * 
 * Ecole Centrale Nantes - Septembre 2022
 * Equipe pédagogique Informatique et Mathématiques
 * JY Martin
 * -------------------------------------------------------------------------------- */

package fr.centrale.nantes.worldofecn;

import fr.centrale.nantes.worldofecn.world.*;

/**
 *
 * @author ECN
 */
public class WorldOfECN {

    /**
     * main program
     * @param args
     */
// Dans WorldOfECN.java

    public static void main(String[] args) {
        // 1. Initialiser les données statiques
        Personnage.init();
        Monstre.init();
        Objet.init();
        
        System.out.println("Initialisation des données de jeu...");

        // 2. Générer un monde aléatoire pour la sauvegarde
        World worldToSave = new World(); // Monde 20x20 par défaut
        worldToSave.setPlayer("Saegusa");
        System.out.println("Monde 'worldToSave' (20x20) généré aléatoirement.");

        // 3. Connexion à la base de données
        DatabaseTools database = new DatabaseTools();
        database.connect();
        System.out.println("Connexion à la base de données établie.");

        // 4. Récupérer l'ID du joueur
        Integer playerId = database.getPlayerID("Saegusa", "Mayumi");

        if (playerId == null) {
            System.out.println("ERREUR: Joueur 'Saegusa' non trouvé.");
            System.out.println("Veuillez d'abord créer le joueur, le monde et la partie dans PgAdmin.");
        } else {
            System.out.println("Joueur 'Saegusa' authentifié (ID: " + playerId + ").");

            // --- TEST 1: SAUVEGARDE ---
            System.out.println("\n--- Test de Sauvegarde ---");
            System.out.println("Sauvegarde du monde dans 'mapartie' / 'Start'...");
            try {
                database.saveWorld(playerId, "mapartie", "Start", worldToSave);
                System.out.println("SUCCES: Sauvegarde 'Start' terminée.");
            } catch (Exception e) {
                System.out.println("ERREUR lors de la sauvegarde: " + e.getMessage());
                e.printStackTrace();
            }

            // --- TEST 2: LECTURE (Ta demande) ---
            System.out.println("\n--- Test de Lecture ---");
            System.out.println("Lecture de la sauvegarde 'Start' de 'mapartie'...");
            
            // On appelle readWorld (code fourni à l'étape 2)
            World worldCharge = database.readWorld(playerId, "mapartie", "Start");
            
            if (worldCharge != null) {
                System.out.println("SUCCES: Monde chargé !");
                System.out.println("Taille du monde : " + worldCharge.getWidth() + "x" + worldCharge.getHeight());
                System.out.println("Nombre d'éléments chargés : " + worldCharge.getListElements().size());
                
                // Vérifier si le personnage du joueur a été correctement associé
                if (worldCharge.getPlayer().getPersonnage() != null) {
                    System.out.println("Personnage du joueur : " + worldCharge.getPlayer().getPersonnage().getRace());
                } else {
                    System.out.println("ERREUR: Le personnage du joueur n'a pas été associé.");
                }
            } else {
                System.out.println("ERREUR: La lecture du monde a échoué (readWorld a retourné null).");
            }
        }

        // 5. Déconnexion
        database.disconnect();
        System.out.println("\nDéconnexion de la base de données.");
    }
}
