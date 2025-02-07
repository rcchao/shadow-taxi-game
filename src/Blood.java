import bagel.Image;
import bagel.Input;

import java.util.Properties;

/**
 * A class representing a blood effect in the game that moves and interacts with other objects.
 */
public class Blood extends DamageEffect{

	/**
	 * Creates a new Blood object with specified coordinates and properties.
	 * @param x The x-coordinate of the blood effect.
	 * @param y The y-coordinate of the blood effect.
	 * @param props The properties object containing image and movement configurations.
	 */
	public Blood(int x, int y, Properties props) {
		super(x, y, new Image(props.getProperty("gameObjects.blood.image")),
				Integer.parseInt(props.getProperty("gameObjects.taxi.speedY")),
				Integer.parseInt(props.getProperty("gameObjects.blood.ttl")));
	}

	/**
	 * Moves the blood effect in the y-direction and renders the image.
	 * If collision happens with PowerCollectable objects, the active time will be increased.
	 * @param input The current mouse/keyboard input.
	 */
	@Override
	public void update(Input input) {
		if(input != null) {
			adjustToInputMovement(input);
		}
		decrementFramesActive();

		move();
		draw();
	}
}