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
 * @author ECN
 */
public class Monstre extends Creature {
    
    private static final String RACELOUP = "Loup";
    private static final String RACEOURS = "Ours";
    private static final String RACEBUFFLE = "Buffle";
    private static final String RACEJAGUAR = "Jaguar";
    private static final String RACEVACHE = "Vache";
    private static final String RACELAPIN = "Lapin";

    private String race;

    private static List<String> racesList;

    /**
     *
     */
    public static void init() {
        racesList = new ArrayList<String>();
        racesList.add(RACELOUP);
        racesList.add(RACEOURS);
        racesList.add(RACEBUFFLE);
        racesList.add(RACEJAGUAR);
        racesList.add(RACEVACHE);
        racesList.add(RACELAPIN);
    }
    
    /**
     * Get nb races
     * @return
     */
    public static int getNbRaces() {
        if (racesList != null) {
            return racesList.size();
        }
        return 0;
    }

    /**
     *
     * @param race
     * @return
     */
    public static String intToRace(int race) {
        if (race < racesList.size()) {
            return racesList.get(race);
        }
        return "";
    }

    /**
     *
     * @param world
     */
    public Monstre(World world) {
        super(world);
    }

    /**
     * 
     * @return 
     */
    public String getRace() {
        return race;
    }

    /**
     * 
     * @param race 
     */
    public void setRace(String race) {
        this.race = race;
    }
    
    /**
     *
     */
    public void setRaceCaracteristiques() {
        switch (this.getRace()) {
            case RACELOUP :
                this.setPourcentAttaque(30.0f);
                this.setDegatsAttaque(3.0f);
                this.setPourcentEsquive(20.0f);
                this.setAbsorbe(2.0f);
                this.setPVieMax(20.0f);
                break;
            case RACEOURS :
                this.setPourcentAttaque(30.0f);
                this.setDegatsAttaque(5.0f);
                this.setPourcentEsquive(5.0f);
                this.setAbsorbe(3.0f);
                this.setPVieMax(40.0f);
                break;
            case RACEBUFFLE :
                this.setPourcentAttaque(20.0f);
                this.setDegatsAttaque(5.0f);
                this.setPourcentEsquive(0.0f);
                this.setAbsorbe(2.0f);
                this.setPVieMax(30.0f);
                break;
            case RACEJAGUAR :
                this.setPourcentAttaque(20.0f);
                this.setDegatsAttaque(2.0f);
                this.setPourcentEsquive(20.0f);
                this.setAbsorbe(1.0f);
                this.setPVieMax(20.0f);
                break;
            case RACEVACHE :
                this.setPourcentAttaque(20.0f);
                this.setDegatsAttaque(4.0f);
                this.setPourcentEsquive(0.0f);
                this.setAbsorbe(2.0f);
                this.setPVieMax(20.0f);
                break;
            case RACELAPIN :
                this.setPourcentAttaque(1.0f);
                this.setDegatsAttaque(0.1f);
                this.setPourcentEsquive(40.0f);
                this.setAbsorbe(0.5f);
                this.setPVieMax(5.0f);
                break;
            default : // UNDEFINED
                this.setPourcentAttaque(0.0f);
                this.setDegatsAttaque(0.0f);
                this.setPourcentEsquive(0.0f);
                this.setAbsorbe(0.0f);
                this.setPVieMax(0.0f);
                break;
        }
        this.setPVie(this.getPVieMax());
    }



    @Override
    public void saveToDatabase(Connection connection, Integer idElementDeJeu) throws SQLException {
        // !! pos_x ET pos_y SONT RETIRÉS de la requête
        String query = "INSERT INTO monstre (id_monstre, race, p_vie, p_vie_max, pourcent_attaque, " +
                       "degats_attaque, pourcent_esquive, absorbe) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"; // 8 params

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, idElementDeJeu); // L'ID du parent
            stmt.setString(2, this.getRace());
            stmt.setFloat(3, this.getPVie());
            stmt.setFloat(4, this.getPVieMax());
            stmt.setFloat(5, this.getPourcentAttaque());
            stmt.setFloat(6, this.getDegatsAttaque());
            stmt.setFloat(7, this.getPourcentEsquive());
            stmt.setFloat(8, this.getAbsorbe());
            // Les params 9 (pos_x) et 10 (pos_y) sont supprimés

            stmt.executeUpdate();
        }
    }


// Dans Monstre.java
// (Assure-toi d'avoir les imports java.sql.ResultSet et java.sql.SQLException)

@Override
public void getFromDatabase(Connection connection, Integer id) {
    String query = "SELECT * FROM monstre m " +
                   "JOIN element_de_jeu e ON m.id_monstre = e.id_element " +
                   "WHERE m.id_monstre = ?";

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, id);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                // Champs du Prérequis
                this.setIdElement(id);

                // Champs de Monstre
                this.setRace(rs.getString("race"));
                this.setPVie(rs.getFloat("p_vie"));
                this.setPVieMax(rs.getFloat("p_vie_max"));
                this.setPourcentAttaque(rs.getFloat("pourcent_attaque"));
                this.setDegatsAttaque(rs.getFloat("degats_attaque"));
                this.setPourcentEsquive(rs.getFloat("pourcent_esquive"));
                this.setAbsorbe(rs.getFloat("absorbe"));
                
                // Champs de ElementDeJeu (le parent)
                Point2D pos = new Point2D(rs.getInt("position_x"), rs.getInt("position_y"), this);
                this.setPosition(pos);
            }
        }
    } catch (SQLException ex) {
        Logger.getLogger(Monstre.class.getName()).log(Level.SEVERE, null, ex);
    }
}
    @Override
    public void removeFromDatabase(Connection connection, Integer id) {
    }
    
}
