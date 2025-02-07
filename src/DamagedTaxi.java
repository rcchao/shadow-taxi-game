import bagel.Image;
import bagel.Input;

import java.util.Properties;

/**
 * Class representing a damaged taxi in the game. The damaged taxi is not controllable by the player,
 * and it moves in a downward direction like other objects.
 */
public class DamagedTaxi extends Taxi {

	private final Image IMAGE;
	private final int damagedX;
	private int damagedY;

	/**
	 * Creates a new DamagedTaxi object with specified coordinates, properties, and driver.
	 * @param x The x-coordinate of the damaged taxi.
	 * @param y The y-coordinate of the damaged taxi.
	 * @param props The properties object containing taxi settings.
	 * @param driver The driver associated with the taxi.
	 */
	public DamagedTaxi(int x, int y, Properties props, Driver driver) {
		super(x, y, 0, props, driver);
		this.IMAGE = new Image(props.getProperty("gameObjects.taxi.damagedImage"));
		this.damagedX = x;
		this.damagedY = y;
		this.isDestroyed = true;
	}

	/**
	 * Draws the damaged taxi on the screen at its current position.
	 */
	@Override
	public void draw() {
		IMAGE.draw(damagedX, damagedY);
	}

	/**
	 * Updates the state of the damaged taxi. Since it is not controllable, it only moves downward.
	 * @param input The current mouse/keyboard input
	 */
	@Override
	public void update(Input input) {
		adjustToInputMovement(input);
		move();
		draw();
	}

	/**
	 * Moves the damaged taxi downward when it's in motion.
	 */
	public void move() {
		if (isMovingY) {
			this.damagedY += SPEED_Y;
		}
	}
}
