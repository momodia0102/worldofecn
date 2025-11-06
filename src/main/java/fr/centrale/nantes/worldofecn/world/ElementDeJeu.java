/* --------------------------------------------------------------------------------
 * WoE
 * 
 * Ecole Centrale Nantes - Septembre 2022
 * Equipe pédagogique Informatique et Mathématiques
 * JY Martin
 * -------------------------------------------------------------------------------- */

package fr.centrale.nantes.worldofecn.world;

import java.sql.Connection;
import java.util.Random;

/**
 * Element on the grid
 * @author ECN
 */
public abstract class ElementDeJeu {

    public static final String UNDEFINED = "UNDEFINED";
    private static final int MAXPOSITIONTRY = 20;

    private Point2D position;
    private World world;
    private Integer IdElement;
    
    /**
     * generate element in the world
     * @param world
     */
    public ElementDeJeu(World world) {
        this.world = world;
        
        // Try to get an unused position
        Random rand = new Random();
        int nbTry = 1;
        Point2D newPoint2D = new Point2D(rand.nextInt(world.getWidth()), rand.nextInt(world.getHeight()), this);
        while ((newPoint2D.isAlreadyUsed(world)) && (nbTry < MAXPOSITIONTRY)) {
            nbTry++;
            newPoint2D = new Point2D(rand.nextInt(world.getWidth()), rand.nextInt(world.getHeight()), this);
        }
        this.position = newPoint2D;
    }

    /**
     * Get element location
     * @return 
     */
    public Point2D getPosition() {
        return position;
    }
    
    /**
     * Get element ID
     * @return 
     */
    public Integer getIdElement() {
        return IdElement;
    }

    /**
     * Set element ID
     * @param IdElement 
     */
    public void setIdElement(Integer IdElement) {
        this.IdElement = IdElement;
    }

    /**
     * Set element location
     * @param position 
     */
    public void setPosition(Point2D position) {
        this.position = position;
    }
    
    /**
     * Save element to database
     * @param connection, idElementDeJeu
     */
    // NOUVELLE LIGNE
    public abstract void saveToDatabase(Connection connection, Integer idElementDeJeu) throws java.sql.SQLException;
    
    /**
     * Get element from database
     * @param connection
     * @param id : ID of the element in the database
     */
    public abstract void getFromDatabase(Connection connection, Integer id);
    
    /**
     * Get element from database
     * @param connection
     * @param id : ID of the element in the database
     */
    public abstract void removeFromDatabase(Connection connection, Integer id);
}
