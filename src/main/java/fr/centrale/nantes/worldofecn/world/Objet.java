/* --------------------------------------------------------------------------------
 * WoE
 * 
 * Ecole Centrale Nantes - Septembre 2022
 * Equipe pédagogique Informatique et Mathématiques
 * JY Martin
 * -------------------------------------------------------------------------------- */
package fr.centrale.nantes.worldofecn.world;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 *
 * @author kwyhr
 */
public class Objet extends ElementDeJeu {

    private static final String OBJETPOTIONDESOIN = "Potion de Soin";
    private static final String OBJETPOTIONDUGUERRIER = "Potion du Guerrier";
    private static final String OBJETPOTIONDEMAGIE = "Potion de Magie";
    private static final String OBJETPOTIONDEMAGE = "Potion de Mage";
    private static final String OBJETEPEEDEMONIAQUE = "Epée démoniaque";
    private static final String OBJETARCENCHANTE = "Arc enchanté";
    private static final String OBJETARBRE = "Arbre";
    private static final String OBJETROCHER = "Rocher";
    private static final String OBJETNUAGETOXIQUE = "Nuage toxique";

    private String type;

    private static List<String> typesList;

    /**
     *
     */
    public static void init() {
        typesList = new ArrayList<String>();
        typesList.add(OBJETPOTIONDESOIN);
        typesList.add(OBJETPOTIONDUGUERRIER);
        typesList.add(OBJETPOTIONDEMAGIE);
        typesList.add(OBJETPOTIONDEMAGE);
        typesList.add(OBJETEPEEDEMONIAQUE);
        typesList.add(OBJETARCENCHANTE);
        typesList.add(OBJETARBRE);
        typesList.add(OBJETROCHER);
        typesList.add(OBJETNUAGETOXIQUE);
    }

    /**
     *
     * @return
     */
    public static int getNbTypes() {
        if (typesList != null) {
            return typesList.size();
        }
        return 0;
    }

    /**
     *
     * @param type
     * @return
     */
    public static String intToType(int type) {
        if (type < typesList.size()) {
            return typesList.get(type);
        }
        return "";
    }

    /**
     *
     * @param world
     */
    public Objet(World world) {
        super(world);
    }

    /**
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Can any creature walk on this object
     * @return 
     */
    public boolean canWalkOn() {
        switch (this.getType()) {
            case OBJETARBRE :
            case OBJETROCHER :
                return false;
            default :
                return true;
        }
    }

   
    @Override
    public void saveToDatabase(Connection connection, Integer idElementDeJeu) throws SQLException {
        // !! pos_x ET pos_y SONT RETIRÉS de la requête
        String query = "INSERT INTO objet (id_objet, type) VALUES (?, ?)"; // 2 params

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, idElementDeJeu); // L'ID du parent
            stmt.setString(2, this.getType());
            // Les params 3 (pos_x) et 4 (pos_y) sont supprimés

            stmt.executeUpdate();
        }
    }


// Dans Objet.java
// (Assure-toi d'avoir les imports java.sql.ResultSet et java.sql.SQLException)

@Override
public void getFromDatabase(Connection connection, Integer id) {
    String query = "SELECT * FROM objet o " +
                   "JOIN element_de_jeu e ON o.id_objet = e.id_element " +
                   "WHERE o.id_objet = ?";

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, id);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                // Champs du Prérequis
                this.setIdElement(id);

                // Champs de Objet
                this.setType(rs.getString("type"));
                
                // Champs de ElementDeJeu (le parent)
                Point2D pos = new Point2D(rs.getInt("position_x"), rs.getInt("position_y"), this);
                this.setPosition(pos);
            }
        }
    } catch (SQLException ex) {
        Logger.getLogger(Objet.class.getName()).log(Level.SEVERE, null, ex);
    }
}
    @Override
    public void removeFromDatabase(Connection connection, Integer id) {
    }
}
