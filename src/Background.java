import bagel.Image;
import bagel.Input;
import bagel.Keys;

import java.util.Properties;

/**
 * A class representing the background of the game play.
 */
public class Background {

    private final int WINDOW_HEIGHT;
    private Image IMAGE;
    private final int SPEED_Y;
    private final int x;
    private int y;
    private int moveY;

    /**
     * Creates a new Background object with specified coordinates, weather condition, and properties.
     * @param x The x-coordinate of the background.
     * @param y The y-coordinate of the background.
     * @param weather The current weather condition.
     * @param props The properties object containing background image and movement configurations.
     */
    public Background(int x, int y, String weather, Properties props) {
        this.x = x;
        this.y = y;
        this.moveY = 0;

        this.SPEED_Y = Integer.parseInt(props.getProperty("gameObjects.taxi.speedY"));
        this.WINDOW_HEIGHT = Integer.parseInt(props.getProperty("window.height"));
        if (weather.equals("SUNNY")) {
            this.IMAGE = new Image(props.getProperty("backgroundImage.sunny"));
        } else if (weather.equals("RAINING")) {
            this.IMAGE = new Image(props.getProperty("backgroundImage.raining"));
        }
    }

    /**
     * Updates the background position and renders the image based on input.
     * @param input The current mouse/keyboard input.
     * @param background The current background object.
     */
    public void update(Input input, Background background) {
        if(input != null) {
            adjustToInputMovement(input);
        }

        move();
        draw();

        if (y >= WINDOW_HEIGHT * 1.5) {
            y = background.getY() - WINDOW_HEIGHT;
        }
    }

    /**
     * Sets the weather condition by changing the background image.
     * @param weather The current weather condition (SUNNY or RAINING).
     * @param props The properties object containing the paths to background images.
     */
    public void setWeatherCondition(String weather, Properties props) {
        if (weather.equals("SUNNY")) {
            this.IMAGE = new Image(props.getProperty("backgroundImage.sunny"));
        } else if (weather.equals("RAINING")) {
            this.IMAGE = new Image(props.getProperty("backgroundImage.raining"));
        }
    }

    /**
     * Moves the background in the y-direction based on the speed.
     */
    public void move() {
        this.y += SPEED_Y * moveY;
    }

    /**
     * Draws the background image on the screen.
     */
    public void draw() {
        IMAGE.draw(x, y);
    }

    /**
     * Adjusts the movement direction of the background in the y-axis based on keyboard input.
     * @param input The current mouse/keyboard input.
     */
    public void adjustToInputMovement(Input input) {
        if (input.wasPressed(Keys.UP)) {
            moveY = 1;
        }  else if(input.wasReleased(Keys.UP)) {
            moveY = 0;
        }
    }

    public int getY() {
        return y;
    }
}
