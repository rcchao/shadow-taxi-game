import bagel.Image;
import bagel.Input;
import bagel.Keys;

import java.util.Properties;

/**
 * A class representing the trip end flag in the game play.
 * Objects of this class will only move up and down based on the keyboard input. No other functionalities needed.
 */
public class TripEndFlag {

    private final Image IMAGE;
    private final int SPEED_Y;
    private final float RADIUS;

    private int x;
    private int y;
    private int moveY;

    /**
     * Constructor for the TripEndFlag class, which sets up the destination flag for the trip.
     * @param x The x-coordinate of the trip end flag.
     * @param y The y-coordinate of the trip end flag.
     * @param props The properties object containing configurations such as speed, radius, and image.
     */
    public TripEndFlag(int x, int y, Properties props) {
        this.x = x;
        this.y = y;
        this.moveY = 0;

        this.SPEED_Y = Integer.parseInt(props.getProperty("gameObjects.taxi.speedY"));
        this.RADIUS = Float.parseFloat(props.getProperty("gameObjects.tripEndFlag.radius"));
        this.IMAGE = new Image(props.getProperty("gameObjects.tripEndFlag.image"));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getRadius() {
        return RADIUS;
    }

    /**
     * Move the object in y direction according to the keyboard input, and render the trip flag image.
     * @param input The current mouse/keyboard input.
     */
    public void update(Input input) {
        if(input != null) {
            adjustToInputMovement(input);
        }

        move();
        draw();
    }

    /**
     * Move the GameObject object in the y-direction based on the speedY attribute.
     */
    public void move() {
        this.y += SPEED_Y * moveY;
    }

    /**
     * Draw the GameObject object into the screen.
     */
    public void draw() {
        IMAGE.draw(x, y);
    }

    /**
     * Adjust the movement direction in y-axis of the GameObject based on the keyboard input.
     * @param input The current mouse/keyboard input.
     */
    public void adjustToInputMovement(Input input) {
        if (input.wasPressed(Keys.UP)) {
            moveY = 1;
        }  else if(input.wasReleased(Keys.UP)) {
            moveY = 0;
        }
    }
}
