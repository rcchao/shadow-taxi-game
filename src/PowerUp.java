import bagel.Image;
import bagel.Input;
import bagel.Keys;

/**
 * Abstract PowerUp class for collectable items, Invincible Powers and Coins
 */
abstract class PowerUp {
	// Public (protected) attributes for child classes
	/**
	 * The maximum number of frames during which the power-up remains active.
	 */
	protected int MAX_FRAMES;

	/**
	 * The image representing the power-up.
	 */
	protected Image IMAGE;

	/**
	 * The speed at which the power-up moves in the y-direction.
	 */
	protected int SPEED_Y;

	/**
	 * The radius of the power-up, used for collision detection.
	 */
	protected float RADIUS;

	/**
	 * The x-coordinate of the power-up.
	 */
	protected int x;

	/**
	 * The y-coordinate of the power-up.
	 */
	protected int y;

	/**
	 * The movement in the y-direction.
	 */
	protected int moveY;

	/**
	 * Indicates whether the power-up has collided with another object.
	 */
	protected boolean isCollided;

	/**
	 * The number of frames the power-up has been active for.
	 */
	protected int framesActive = 0;

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

	/**
	 * Move the object in y direction according to the keyboard input, and render the coin image, before collision
	 * happens with PowerCollectable objects. Once the collision happens, the coin active time will be increased.
	 * @param input The current mouse/keyboard input.
	 */
	public void update(Input input) {
		if(isCollided) {
			framesActive++;

		} else {
			if(input != null) {
				adjustToInputMovement(input);
			}

			move();
			draw();
		}
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

	/**
	 * Check if the coin has collided with any PowerCollectable objects, and power will be collected by PowerCollectable
	 * object that is collided with.
	 */
	public void collide(Taxi taxi) {
		if(hasCollidedWith(taxi)) {
			taxi.collectPower(this);
			setIsCollided();
		}
	}

	/**
	 * Check if the coin has collided with any PowerCollectable objects, and power will be collected by PowerCollectable
	 * object that is collided with.
	 */
	public void collide(Driver driver) {
		if(hasCollidedWith(driver)) {
			driver.collectPower(this);
			setIsCollided();
		}
	}

	/**
	 * Check if the object is collided with another object based on the radius of the two objects.
	 * @param entity The taxi object to be checked.
	 * @return True if the two objects are collided, false otherwise.
	 */
	public boolean hasCollidedWith(Entity entity) {
		// if the distance between the two objects is less than the sum of their radius, they are collided
		float collisionDistance = RADIUS + entity.getRadius();
		float currDistance = (float) Math.sqrt(Math.pow(x - entity.getX(), 2) + Math.pow(y - entity.getY(), 2));
		return currDistance <= collisionDistance;
	}


	/**
	 * Checks if the power-up is currently active.
	 * @return true if the object has collided and is within the active frame duration, false otherwise.
	 */
	public boolean getIsActive() {
		return isCollided && framesActive <= MAX_FRAMES && framesActive > 0;
	}

	public void setIsCollided() {
		this.isCollided = true;
	}

	public int getFramesActive() {
		return framesActive;
	}

	public int getMaxFrames(){
		return MAX_FRAMES;
	}

}

