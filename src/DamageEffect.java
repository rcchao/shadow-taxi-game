import bagel.Image;
import bagel.Input;
import bagel.Keys;

/**
 * Abstract class representing a damage effect in the game, providing shared movement and rendering logic.
 */
public abstract class DamageEffect {
	private final Image IMAGE;
	private final int SPEED_Y;
	private int x;
	private int y;
	private int moveY;
	private int framesActive;

	/**
	 * Constructor for the DamageEffect class, initializing the position, image, speed, and active frames.
	 * @param x The x-coordinate of the effect.
	 * @param y The y-coordinate of the effect.
	 * @param image The image representing the effect.
	 * @param speedY The speed of the effect in the y-direction.
	 * @param framesActive The number of frames the effect remains active.
	 */
	public DamageEffect(int x, int y, Image image, int speedY, int framesActive) {
		this.x = x;
		this.y = y;
		this.IMAGE = image;
		this.SPEED_Y = speedY;
		this.framesActive = framesActive;
	}

	/**
	 * Abstract method for update logic, to be implemented by subclasses.
	 * @param input The current mouse/keyboard input.
	 */
	public abstract void update(Input input);

	/**
	 * Moves the effect in the y-direction based on its speed.
	 */
	public void move() {
		this.y += SPEED_Y * moveY;
	}

	/**
	 * Draws the effect's image on the screen.
	 */
	public void draw() {
		IMAGE.draw(x, y);
	}

	/**
	 * Adjusts the movement of the effect based on keyboard input.
	 * @param input The current mouse/keyboard input.
	 */
	public void adjustToInputMovement(Input input) {
		if (input.wasPressed(Keys.UP)) {
			moveY = 1;
		} else if (input.wasReleased(Keys.UP)) {
			moveY = 0;
		}
	}

	/**
	 * Decreases the number of frames the effect remains active by one.
	 */
	public void decrementFramesActive() {
		this.framesActive--;
	}

	public int getFramesActive() {
		return framesActive;
	}
}
