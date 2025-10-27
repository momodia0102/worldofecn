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
import fr.centrale.nantes.worldofecn.worl.ElementDeJeu;

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
    public void saveWorld(Integer idJoueur, String nomPartie, String nomSauvegarde, World monde) {
        if (idJoueur == null || nomPartie == null || nomSauvegarde == null || monde == null) {
        throw new IllegalArgumentException("Paramètres invalides");
        }
        try {
            
            connection.setAutoCommit(false);

            
            Integer idPartie = getPartie(idJoueur, nomPartie);

            
            Integer idSauvegarde = createSauvegarde(idPartie, nomSauvegarde);

            saveElementsForSauvegarde(idSauvegarde, monde);
            
           
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
    private void saveElementsForSauvegarde(Integer idSauvegarde, World monde) throws SQLException {
        String insertLiaison = "INSERT INTO sauvegarde_element (id_sauvegarde, id_element) VALUES (?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(insertLiaison)) {
            // Parcourir tous les éléments du monde
            for (ElementDeJeu element : monde.getListElements()) {
                // D'abord sauvegarder l'élément et récupérer son ID
                Integer idElement = element.saveToDatabase(connection);

                if (idElement != null) {
                    // Puis créer la liaison sauvegarde <-> élément
                    pst.setInt(1, idSauvegarde);
                    pst.setInt(2, idElement);
                    pst.executeUpdate();
                }
            }
        }
    }

    private void saveObjetsForSauvegarde(Integer idSauvegarde) throws SQLException {
        String insertLiaison = "INSERT INTO sauvegarde_objet (id_sauvegarde, id_objet) "
                + "VALUES (?)";
        try (PreparedStatement pst = connection.prepareStatement(insertLiaison)) {
            pst.setInt(1, idSauvegarde);
            pst.executeUpdate(); 
        }
    }

    private void saveMonstresForSauvegarde(Integer idSauvegarde) throws SQLException {
        String insertLiaison = "INSERT INTO sauvegarde_monstre (id_sauvegarde, id_monstre) "
                + "VALUES (?)";
        try (PreparedStatement pst = connection.prepareStatement(insertLiaison)) {
                pst.setInt(1, idSauvegarde);
                pst.executeUpdate(); 
        }
    }

    private void savePersonnagesForSauvegarde(Integer idSauvegarde) throws SQLException {
        
        String insertLiaison = "INSERT INTO sauvegarde_personnage (id_sauvegarde) VALUES (?)";
        try (PreparedStatement pst = connection.prepareStatement(insertLiaison)) {
            pst.setInt(1, idSauvegarde);
            pst.executeUpdate(); 
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
