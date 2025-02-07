import bagel.Image;
import bagel.Input;
import bagel.Keys;

import java.util.Properties;

/**
 * The class representing the driver in the game
 */
public class Driver extends Entity{
	// Constants
	private static final int COLLISION_MOVE_X = 2;
	private static final int COLLISION_MOVE_Y = 2;
	private static final int EJECT_X = 50;

	private final Properties props;
	private final Image IMAGE;
	private final float radius;
	private final int getInCarRadius;
	private final int walkSpeedX;
	private final int walkSpeedY;

	private int x;
	private int y;
	private boolean inTaxi;
	private Trip trip;
	private Taxi taxi;

	private boolean isCollided;
	private int crashDirection;
	private int collisionFrames;
	private int attackPoints;
	private double health;
	private boolean bloodRendered;
	private Blood blood;
	private boolean isInvincible;
	private Coin coinPower;
	private InvinciblePower invinciblePower;
	private boolean isDestroyed = false;
	private boolean isDead;

	/**
	 * Constructor for the Driver class.
	 *
	 * @param startX          The starting x-coordinate.
	 * @param startY          The starting y-coordinate.
	 * @param taxi            The taxi the driver starts in.
	 * @param props The file path to the driver's image.
	 */
	public Driver(int startX, int startY, Taxi taxi, Properties props) {
		this.props = props;
		collisionFrames = 0;
		isDead = false;
		this.x = startX;
		this.y = startY;
		this.taxi = taxi;
		this.IMAGE = new Image(props.getProperty("gameObjects.driver.image"));
		this.inTaxi = true; // Driver starts the game in the taxi
		this.radius = Float.parseFloat(props.getProperty("gameObjects.driver.radius"));
		this.health = Double.parseDouble(props.getProperty("gameObjects.driver.health"));
		this.getInCarRadius = Integer.parseInt(props.getProperty("gameObjects.driver.taxiGetInRadius"));
		this.walkSpeedX = Integer.parseInt(props.getProperty("gameObjects.driver.walkSpeedX"));
		this.walkSpeedY = Integer.parseInt(props.getProperty("gameObjects.driver.walkSpeedY"));
	}

	/**
	 * Updates the driver's state based on user input and taxi status.
	 * @param input The current keyboard input.
	 * @param taxi  The current taxi object.
	 */
	public void update(Input input, Taxi taxi) {
		this.taxi = taxi; // Update the taxi reference in case a new taxi is created

		if (isCollided) {
			collisionFrames--;
			this.collisionMove();
			if (collisionFrames <= 0) {
				isCollided = false;
			}
		}

		if (inTaxi) {
			// Update driver coordinates to match the taxi's coordinates
			x = taxi.getX();
			y = taxi.getY();
			this.trip = taxi.getTrip();
			// Driver image is not rendered when in the taxi
		} else {
			// Move based on user input
			move(input);

			// check if invincible power is active, then no damage is taken
			if (invinciblePower != null && invinciblePower.getIsActive()) {
				isInvincible = true;
			} else {
				isInvincible = false;
			}

			// if the driver has coin power, apply the effect of the coin on the priority of the passenger

			if (trip != null && coinPower != null) {
				TravelPlan tp = trip.getPassenger().getTravelPlan();
				int newPriority = tp.getPriority();
				if(!tp.getCoinPowerApplied()) {
					newPriority = coinPower.applyEffect(tp.getPriority());
				}

				if(newPriority < tp.getPriority()) {
					tp.setCoinPowerApplied();
				}
				tp.setPriority(newPriority);
			}

			// Check if close enough to taxi to get in
			if (taxi != null) {
				double distance = Math.hypot(x - taxi.getX(), y - taxi.getY());
				if (distance <= getInCarRadius) {
					inTaxi = true;
					taxi.isDestroyed = false;
					taxi.setRenderedDamagedTaxi(false);
				}
			}
		}

		// Render the driver image if not in taxi
		if (!inTaxi) {
			if (bloodRendered && blood.getFramesActive() > 0) {
				blood.update(input);
			}
			draw();
		}
	}

	/**
	 * Draws the driver image at the current coordinates.
	 */
	public void draw() {
		IMAGE.draw(x, y);
	}

	/**
	 * Ejects the driver from the taxi when the taxi becomes damaged.
	 */
	public void eject() {
		x -= EJECT_X;
		inTaxi = false;
		taxi = null; // Driver is no longer associated with a taxi
	}

	/**
	 * Moves the driver based on user input when not in a taxi.
	 * @param input The current keyboard input.
	 */
	private void move(Input input) {
		// Move at a speed of one pixel per frame in the direction of key press
		if (input.isDown(Keys.UP)) {
			y -= walkSpeedY;
		}
		if (input.isDown(Keys.DOWN)) {
			y += walkSpeedY;
		}
		if (input.isDown(Keys.LEFT)) {
			x -= walkSpeedX;
		}
		if (input.isDown(Keys.RIGHT)) {
			x += walkSpeedX;
		}
	}

	/**
	 * Inflicts damage on the driver and checks if they are destroyed.
	 * @param damage The amount of damage to inflict.
	 */
	public void takeDamage(double damage) {
		this.health -= damage;
		if (health <= 0) {
			isDestroyed = true;
			isDead = true;
			blood = new Blood(this.x, this.y, props);
			bloodRendered = true;
		}
	}

	/**
	 * Resets the collision frames, starting the collision timeout.
	 */
	public void resetCollisionFrames() {
		collisionFrames = RESET_FRAMES;
	}

	/**
	 * Handles the driver's movement during a collision.
	 */
	public void collisionMove() {
		if (this.collisionFrames >= NON_COLLISIONMOVE_FRAMES) {
			this.y += crashDirection * COLLISION_MOVE_Y;
			this.x += crashDirection * COLLISION_MOVE_X;
		}
	}

	/**
	 * Collects a power-up, either coin or invincibility, for the driver.
	 * @param powerUp The power-up object.
	 */
	public void collectPower(PowerUp powerUp) {
		if (powerUp instanceof Coin) {
			coinPower = (Coin) powerUp;
		} else if (powerUp instanceof InvinciblePower) {
			invinciblePower = (InvinciblePower) powerUp;
		}
	}

	public boolean getIsInvincible() {
		return isInvincible;
	}

	public boolean getIsDead() {
		return isDead;
	}

	public Trip getTrip() {
		return trip;
	}

	public Passenger getTripPassenger() {
		return trip.getPassenger();
	}

	public double getHealth() {
		return health;
	}


	@Override
	public float getRadius() {
		return radius;
	}

	public void setIsCollided() {
		isCollided = true;
	}

	@Override
	public void setCrashDirection(int crashDirection) {
		this.crashDirection = crashDirection;
	}

	@Override
	public double getAttackPoints() {
		return attackPoints;
	}

	@Override
	public int getCollisionFrames() {
		return collisionFrames;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean isInTaxi() {
		return inTaxi;
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

}
