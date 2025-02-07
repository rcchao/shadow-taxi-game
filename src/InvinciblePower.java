import bagel.Image;
import java.util.Properties;

/**
 * Class representing the invincible power-up in the game. When collected, it grants the player temporary invincibility.
 */
public class InvinciblePower extends PowerUp{

	/**
	 * Constructor for the InvinciblePower class.
	 * @param x The x-coordinate of the invincible power-up.
	 * @param y The y-coordinate of the invincible power-up.
	 * @param props The properties object containing invincible power-up configurations such as image, speed, and radius.
	 */
	public InvinciblePower(int x, int y, Properties props) {
		this.x = x;
		this.y = y;
		this.moveY = 0;

		this.SPEED_Y = Integer.parseInt(props.getProperty("gameObjects.taxi.speedY"));
		this.RADIUS = Float.parseFloat(props.getProperty("gameObjects.invinciblePower.radius"));
		this.IMAGE = new Image(props.getProperty("gameObjects.invinciblePower.image"));
		this.MAX_FRAMES = Integer.parseInt(props.getProperty("gameObjects.invinciblePower.maxFrames"));
	}

}
