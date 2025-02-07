import bagel.Image;

import java.util.Properties;

/**
 * Class representing coins in the game. Coins can be collected by either the player or the taxi.
 * It will set one level higher priority for the passengers that are waiting to get-in or already in the taxi.
 */
public class Coin extends PowerUp{

    /**
     * Creates a new Coin object with specified coordinates and properties.
     * @param x The x-coordinate of the coin.
     * @param y The y-coordinate of the coin.
     * @param props The properties object containing coin settings.
     */
    public Coin(int x, int y, Properties props) {
        this.x = x;
        this.y = y;
        this.moveY = 0;

        this.SPEED_Y = Integer.parseInt(props.getProperty("gameObjects.taxi.speedY"));
        this.RADIUS = Float.parseFloat(props.getProperty("gameObjects.coin.radius"));
        this.IMAGE = new Image(props.getProperty("gameObjects.coin.image"));
        this.MAX_FRAMES = Integer.parseInt(props.getProperty("gameObjects.coin.maxFrames"));
    }

    /**
     * Apply the effect of the coin on the priority of the passenger.
     * @param priority The current priority of the passenger.
     * @return The new priority of the passenger.
     */
    public Integer applyEffect(Integer priority) {
        if (framesActive <= MAX_FRAMES && priority > 1) {
            priority -= 1;
        }

        return priority;
    }

}
