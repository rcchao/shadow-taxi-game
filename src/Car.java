import bagel.Image;
import bagel.Input;
import bagel.Keys;

import java.util.ArrayList;
import java.util.Properties;

/**
 * A class representing a Car in the game, including its movement, collision, and damage behaviors.
 */
public class Car extends Entity {

	// Constants for random car generation
	private static final int MIN_CAR_NO = 1;
	private static final int MAX_CAR_NO = 3;
	private static final int CAR_YVAL1 = -50;
	private static final int CAR_YVAL2 = 768;
	private static final int CAR_XVAL1 = 1;
	private static final int CAR_XVAL2 = 4;
	private static final int MIN_CAR_SPEED = 2;
	private static final int MAX_CAR_SPEED = 5;

	// Public (protected) attributes for EnemyCar child class
	/**
	 * The x-coordinate of the car.
	 */
	public int x;

	/**
	 * The y-coordinate of the car.
	 */
	public int y;

	/**
	 * The image representing the car.
	 */
	public Image IMAGE;

	/**
	 * The health points of the car.
	 */
	public double health;

	/**
	 * The attack points of the car.
	 */
	public double attackPoints;

	/**
	 * The destruction state of the car.
	 */
	public boolean isDestroyed;

	private final Properties props;
	private final int TAXI_SPEED_Y;
	private final boolean isInvincible;
	private final float RADIUS;
	private int speedY;
	private int moveY;

	private int crashDirection;
	private boolean isCollided;
	private int collisionFrames;
	private boolean fireRendered;
	private Fire fire;
	private boolean smokeRendered;
	private Smoke smoke;

	/**
	 * Creates a new Car object with randomized coordinates, speed, and image.
	 * @param props The properties object containing car settings.
	 */
	public Car(Properties props) {
		this.props = props;
		this.TAXI_SPEED_Y = Integer.parseInt(props.getProperty("gameObjects.taxi.speedY"));
		this.health = Double.parseDouble(props.getProperty("gameObjects.otherCar.health"));
		this.attackPoints = Double.parseDouble(props.getProperty("gameObjects.otherCar.damage"));
		this.RADIUS = Float.parseFloat(props.getProperty("gameObjects.otherCar.radius"));
		collisionFrames = 0;
		isInvincible = false;

		// Randomly set car image
		int carNumber = MiscUtils.getRandomInt(MIN_CAR_NO, MAX_CAR_NO);
		String carImagePath = String.format("res/otherCar-%d.png", carNumber);
		this.IMAGE = new Image(carImagePath);

		// Randomly set the coordinates of the car
		this.y = MiscUtils.selectAValue(CAR_YVAL1, CAR_YVAL2);
		int xVal = MiscUtils.getRandomInt(CAR_XVAL1, CAR_XVAL2);
		if (xVal == 1) {
			this.x = Integer.parseInt(props.getProperty("roadLaneCenter1"));
		} else if (xVal == 2) {
			this.x = Integer.parseInt(props.getProperty("roadLaneCenter2"));
		} else {
			this.x = Integer.parseInt(props.getProperty("roadLaneCenter3"));
		}

		this.speedY = MiscUtils.getRandomInt(MIN_CAR_SPEED, MAX_CAR_SPEED);
	}

	/**
	 * Draws the car on the screen.
	 */
	public void draw() {
		IMAGE.draw(x, y);
	}

	/**
	 * Updates the car's state, including movement, collision detection, and rendering fire/smoke.
	 * @param input The current mouse/keyboard input.
	 * @param taxi The player's taxi object.
	 * @param driver The driver object.
	 * @param cars The list of other cars in the game.
	 */
	public void update(Input input, Taxi taxi, Driver driver, ArrayList<Car> cars) {
		if (isDestroyed && collisionFrames < 190) {
			return;
		}

		if(input != null) {
			adjustToInputMovement(input);
		}

		if (isCollided) {
			collisionFrames--;
			this.collisionMove();

			if (collisionFrames <= 0) {
				isCollided = false;
				this.speedY = MiscUtils.getRandomInt(MIN_CAR_SPEED, MAX_CAR_SPEED);
			}
		}

		move();
		draw();

		if (fireRendered && fire.getFramesActive() > 0) {
			fire.update(input);
		}

		if (smokeRendered && smoke.getFramesActive() > 0) {
			smoke.update(input);
		}

		if (!driver.isInTaxi()) {
			collide(driver);
		}

		if (taxi.getTrip() != null) {
			Passenger passenger = taxi.getPassenger();
			collide(passenger);
		}

		collide(taxi);

		if (cars != null) {
			for (Car car : cars) {
				if (cars.indexOf(this) != cars.indexOf(car)) {
					if (!this.isDestroyed && !car.isDestroyed) {
						collide(car);
					}
				}
			}
		}
	}

	/**
	 * Moves the car in the y-direction based on its speed and collision state.
	 */
	public void move() {
		if (isCollided) {
			this.y += (TAXI_SPEED_Y * moveY);
		} else {
			this.y += (TAXI_SPEED_Y * moveY) - speedY;
		}
	}

	/**
	 * Adjusts the car's movement in the y-axis based on keyboard input.
	 * @param input The current mouse/keyboard input.
	 */
	private void adjustToInputMovement(Input input) {
		if (input.isDown(Keys.UP)) {
			moveY = 1;
		} else if (input.isUp(Keys.UP)) {
			moveY = 0;
		}
	}

	/**
	 * Checks if the car has collided with another entity based on their radii.
	 * @param entity The entity to check for collision.
	 * @return True if the car has collided with the entity, false otherwise.
	 */
	public boolean hasCollidedWith(Entity entity) {
		float collisionDistance = RADIUS + entity.getRadius();
		float currDistance = (float) Math.sqrt(Math.pow(x - entity.getX(), 2) + Math.pow(y - entity.getY(), 2));
		return currDistance <= collisionDistance;
	}

	/**
	 * Handles the collision between the car and another entity, applying damage and updating states.
	 * @param entity The entity to collide with.
	 */
	public void collide(Entity entity) {
		if (hasCollidedWith(entity)) {
			entity.setIsCollided();
			this.setIsCollided();

			if (entity.getY() > this.y) {
				crashDirection = UP;
				entity.setCrashDirection(DOWN);
			} else {
				crashDirection = DOWN;
				entity.setCrashDirection(UP);
			}

			if (collisionFrames <= 0) {
				this.takeDamage(entity.getAttackPoints());
				this.resetCollisionFrames();
			}

			if (entity.getCollisionFrames() <= 0) {
				if (!entity.getIsInvincible()) {
					entity.takeDamage(this.attackPoints);
					entity.resetCollisionFrames();
				}
			}
		}
	}



	/**
	 * Reduces the car's health by the specified damage value. If the health reaches zero, the car is destroyed.
	 * @param damage The damage to be applied to the car.
	 */
	public void takeDamage(double damage) {
		this.health -= damage;
		if (health <= 0) {
			isDestroyed = true;
			fire = new Fire(this.x, this.y, props);
			fireRendered = true;
		} else {
			smoke = new Smoke(this.x, this.y, props);
			smokeRendered = true;
		}
	}

	/**
	 * Handles the car's movement during a collision.
	 */
	public void collisionMove() {
		if (this.collisionFrames >= NON_COLLISIONMOVE_FRAMES) {
			this.y += crashDirection;
		}
	}

	/**
	 * Resets the collision frames, starting the collision timeout.
	 */
	public void resetCollisionFrames() {
		collisionFrames = RESET_FRAMES;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public float getRadius() {
		return RADIUS;
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

	public boolean getIsInvincible() {
		return isInvincible;
	}

	public void setIsCollided() {
		this.isCollided = true;
	}
}
