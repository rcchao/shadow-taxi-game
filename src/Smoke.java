import bagel.Image;
import bagel.Input;
import java.util.Properties;

/**
 * Class representing the smoke effect in the game, which appears when an object is damaged.
 */
public class Smoke extends DamageEffect {

	/**
	 * Constructor for the Smoke class.
	 * @param x The x-coordinate of the smoke effect.
	 * @param y The y-coordinate of the smoke effect.
	 * @param props The properties object containing smoke effect configurations such as image, speed, and time-to-live (ttl).
	 */
	public Smoke(int x, int y, Properties props) {
		super(x, y,
				new Image(props.getProperty("gameObjects.smoke.image")),
				Integer.parseInt(props.getProperty("gameObjects.taxi.speedY")),
				Integer.parseInt(props.getProperty("gameObjects.smoke.ttl")));
	}

	/**
	 * Updates the state of the smoke effect, adjusting its movement and rendering it on the screen.
	 * @param input The current keyboard/mouse input.
	 */
	@Override
	public void update(Input input) {
		if (input != null) {
			adjustToInputMovement(input);
		}
		decrementFramesActive();
		move();
		draw();
	}
}
