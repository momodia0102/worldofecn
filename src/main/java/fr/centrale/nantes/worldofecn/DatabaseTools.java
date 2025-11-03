/* --------------------------------------------------------------------------------
 * WoE Tools
 * 
 * Ecole Centrale Nantes - Septembre 2022
 * Equipe pédagogique Informatique et Mathématiques
 * JY Martin
 * -------------------------------------------------------------------------------- */
package fr.centrale.nantes.worldofecn;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import fr.centrale.nantes.worldofecn.world.World;
import fr.centrale.nantes.worldofecn.world.ElementDeJeu;
import fr.centrale.nantes.worldofecn.world.Personnage;
import fr.centrale.nantes.worldofecn.world.Monstre;
import fr.centrale.nantes.worldofecn.world.Objet;

/**
 * Manage database connectio, saves and retreive informations
 * @author ECN
 */
public class DatabaseTools {

    private String login;
    private String password;
    private String url;
    private Connection connection;

    /**
     * Load infos
     */
    public DatabaseTools() {
        try {
            // Get Properties file
            ResourceBundle properties = ResourceBundle.getBundle(DatabaseTools.class.getPackage().getName() + ".database");

            // USE config parameters
            login = properties.getString("login");
            password = properties.getString("password");
            String server = properties.getString("server");
            String database = properties.getString("database");
            url = "jdbc:postgresql://" + server + "/" + database;

            // Mount driver
            Driver driver = DriverManager.getDriver(url);
            if (driver == null) {
                Class.forName("org.postgresql.Driver");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DatabaseTools.class.getName()).log(Level.SEVERE, null, ex);
            // If driver is not found, cancel url
            url = null;
        }
        this.connection = null;
    }

    /**
     * Get connection to the database
     */
    public void connect() {
        if ((this.connection == null) && (url != null) && (! url.isEmpty())) {
            try {
                this.connection = DriverManager.getConnection(url, login, password);
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseTools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Disconnect from database
     */
    public void disconnect() {
        if (this.connection != null) {
            try {
                this.connection.close();
                this.connection = null;
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseTools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * get Player ID
     * @param nomJoueur : le login du joueur
     * @param password : le mot de passe du joueur
     * @return
     */
    public Integer getPlayerID(String nomJoueur, String password) {
        Integer playerId = null;

        if (nomJoueur == null || nomJoueur.isEmpty() || password == null) {
            return null;
        }

        String query = "SELECT id_joueur, passeword FROM joueur WHERE login = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nomJoueur);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_joueur");
                    String storedPassword = rs.getString("passeword");

                    if (storedPassword != null && storedPassword.equals(password)) {
                        playerId = id;
                        System.out.println("Joueur "+ nomJoueur + "Existe bien dans la BD avec id " +playerId);
                    }else{
                         System.out.println("Joueur "+ nomJoueur + "n'xiste pas dans BD");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseTools.class.getName()).log(Level.SEVERE, 
                "Erreur lors de la récupération de l'ID du joueur: " + nomJoueur, ex);
        }

        return playerId;
    }
    


    /**
     * save world as sauvegarde in database
     * @param idJoueur : l'ID du joueur dans la BD
     * @param nomPartie : le nom de la partie
     * @param nomSauvegarde : le nom de la sauvegarde
     * @param monde: le monde à enregistrer
     */
    // DANS DatabaseTools.java
    public void saveWorld(Integer idJoueur, String nomPartie, String nomSauvegarde, World monde) {
        if (idJoueur == null || nomPartie == null || monde == null) {
            throw new IllegalArgumentException("Paramètres invalides (idJoueur, nomPartie, monde ne peuvent être null)");
        }

        // Ajout de la logique de Sauvegarde Rapide
        String finalSaveName = nomSauvegarde;
        if (finalSaveName == null) {
            finalSaveName = "Sauvegarde Rapide"; 
            // NOTE: Tu devras peut-être ajouter une logique pour supprimer 
            // l'ancienne sauvegarde rapide de cette 'partie' avant.
        }

        try {
            connection.setAutoCommit(false);

            Integer idPartie = getPartie(idJoueur, nomPartie);

            if (idPartie == null) {
                throw new SQLException("La partie '" + nomPartie + "' n'existe pas pour ce joueur.");
                
            }

            Integer idSauvegarde = createSauvegarde(idPartie, finalSaveName);

           
            saveElementsForSauvegarde(idSauvegarde, idPartie, monde);


            connection.commit();
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Erreur lors de saveWorld", ex);
            try {
                connection.rollback();
            } catch (SQLException rbe) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Rollback échoué", rbe);
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Impossible de remettre autoCommit=true", e);
            }
        }
    }
    public Integer getPartie(Integer Idjoueur, String nomPartie)throws SQLException {
        Integer id = null;
        String select = "Select id_partie FROM partie WHERE id_joueur =? AND nom = ? ";
        try(PreparedStatement stmt = connection.prepareStatement(select)){
            stmt.setInt(1, Idjoueur);
            stmt.setString(2, nomPartie);
            try(ResultSet res = stmt.executeQuery()){
                if(res.next()){
                    id = res.getInt("id_partie");
                }
            } 
        }
        return id;
    };
    
    public Integer createSauvegarde(Integer id_partie , String nomSauvegarde) throws SQLException{
        String insert = "INSERT INTO sauvegarde (id_partie, nom_sauvegarde, date_sauvegarde)"
                + " VALUES (?,?,?)";
        try(PreparedStatement stmt = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)){
            stmt.setInt(1, id_partie);
            stmt.setString(2, nomSauvegarde);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            try(ResultSet key = stmt.getGeneratedKeys()){
                if(key.next()){
                    return key.getInt(1);
                }else{
                    throw new SQLException("Impossible de récupérer id_sauvegarde généré");
                }
            }
        }
    }


    private void saveElementsForSauvegarde(Integer idSauvegarde, Integer idPartie, World monde) throws SQLException {

        // Préparer les 3 requêtes de liaison
        String linkPersonnageQuery = "INSERT INTO sauvegarde_personnage (id_sauvegarde, id_personnage) VALUES (?, ?)";
        String linkMonstreQuery = "INSERT INTO sauvegarde_monstre (id_sauvegarde, id_monstre) VALUES (?, ?)";
        String linkObjetQuery = "INSERT INTO sauvegarde_objet (id_sauvegarde, id_objet) VALUES (?, ?)";

        // Préparer la requête pour créer l'élément "Parent" (AVEC LES BONNES COLONNES)
        String createParentQuery = "INSERT INTO element_de_jeu (id_world, type_element, position_x, position_y) " +
                                   "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstParent = connection.prepareStatement(createParentQuery, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstLinkPersonnage = connection.prepareStatement(linkPersonnageQuery);
             PreparedStatement pstLinkMonstre = connection.prepareStatement(linkMonstreQuery);
             PreparedStatement pstLinkObjet = connection.prepareStatement(linkObjetQuery)) {

            // Parcourir tous les éléments du monde
            for (ElementDeJeu element : monde.getListElements()) {

                // --- 1. CRÉER LE PARENT (element_de_jeu) ---

                // Déterminer le type de l'élément pour la colonne 'type_element'
                String elementType = null;
                if (element instanceof Personnage) {
                    elementType = "PERSONNAGE";
                } else if (element instanceof Monstre) {
                    elementType = "MONSTRE";
                } else if (element instanceof Objet) {
                    elementType = "OBJET";
                }

                // Remplir la requête pour créer le parent
                pstParent.setInt(1, idPartie); // <= C'est le 'id_world' !
                pstParent.setString(2, elementType);
                pstParent.setInt(3, element.getPosition().getX());
                pstParent.setInt(4, element.getPosition().getY());

                pstParent.executeUpdate();

                // Récupérer l'ID auto-généré du parent
                Integer newElementId;
                try (ResultSet generatedKeys = pstParent.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // La PK est 'id_element' (vu sur ton screenshot)
                        newElementId = generatedKeys.getInt("id_element"); 
                    } else {
                        throw new SQLException("Echec de création de element_de_jeu, pas d'ID obtenu.");
                    }
                }

                // --- 2. CRÉER L'ENFANT (personnage, monstre, ou objet) ---
                element.saveToDatabase(connection, newElementId);

                // --- 3. CRÉER LE LIEN avec la sauvegarde ---
                if (element instanceof Personnage) {
                    pstLinkPersonnage.setInt(1, idSauvegarde);
                    pstLinkPersonnage.setInt(2, newElementId);
                    pstLinkPersonnage.addBatch();
                } else if (element instanceof Monstre) {
                    pstLinkMonstre.setInt(1, idSauvegarde);
                    pstLinkMonstre.setInt(2, newElementId);
                    pstLinkMonstre.addBatch();
                } else if (element instanceof Objet) {
                    pstLinkObjet.setInt(1, idSauvegarde);
                    pstLinkObjet.setInt(2, newElementId);
                    pstLinkObjet.addBatch();
                }
            }

            // 4. Exécuter toutes les insertions de liens
            pstLinkPersonnage.executeBatch();
            pstLinkMonstre.executeBatch();
            pstLinkObjet.executeBatch();
        }
    }
    /**
     * get world sauvegarde from database
     * @param idJoueur
     * @param nomPartie
     * @param nomSauvegarde
     * @return monde
     */
    public World readWorld(Integer idJoueur, String nomPartie, String nomSauvegarde) {
        World monde = new World();
        // TO BE DEFINED
        
        // Retreive partie infos for the player
        // Retreive sauvegarde infos for the partie

        // Retreive world infos
        // Generate object world according to the infos
        
        // Retreive element de jeu from sauvegarde
        // Generate approprite objects
        // Link objects to the world
        
        // Associate player with the player's creature

        // Return created world
        return monde;
    }


    /**
     * remove world sauvegarde from database
     * @param idJoueur
     * @param nomPartie
     * @param nomSauvegarde
     */
    public void removeWorld(Integer idJoueur, String nomPartie, String nomSauvegarde) {
        World monde = new World();
        // TO BE DEFINED
        
        // Retreive partie infos for the player
        // Retreive sauvegarde infos for the partie

        // remove elements de jeu linked to the sauvegarde
        // remove sauvegarde
        // remove if partie has no mode sauvegarde, remove partie
    }
}
