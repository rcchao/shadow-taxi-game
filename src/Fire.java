import bagel.Image;
import bagel.Input;
import java.util.Properties;

/**
 * Class representing the fire effect in the game, which moves and eventually expires after a set time.
 */
public class Fire extends DamageEffect {

	/**
	 * Constructor for the Fire class.
	 * @param x The x-coordinate of the fire effect.
	 * @param y The y-coordinate of the fire effect.
	 * @param props The properties object containing fire image and movement configurations.
	 */
	public Fire(int x, int y, Properties props) {
		super(x, y,
				new Image(props.getProperty("gameObjects.fire.image")),
				Integer.parseInt(props.getProperty("gameObjects.taxi.speedY")),
				Integer.parseInt(props.getProperty("gameObjects.fire.ttl")));
	}

	/**
	 * Updates the fire's state, adjusting its movement and rendering it on the screen.
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
