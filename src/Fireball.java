import bagel.Image;
import bagel.Input;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Class representing a fireball that can be shot by enemy cars in the game.
 * The fireball moves, collides with entities, and can cause damage.
 */
public class Fireball {
	static final int UP = -1;
	static final int DOWN = 1;

	private final Image IMAGE;
	private final int speedY;
	private final float RADIUS;
	private int x;
	private int y;
	private double attackPoints;
	private boolean hasCollided;

	/**
	 * Constructor for the Fireball class.
	 * @param x The x-coordinate where the fireball is spawned.
	 * @param y The y-coordinate where the fireball is spawned.
	 * @param props The properties object containing fireball configurations.
	 */
	public Fireball(int x, int y, Properties props) {
		this.IMAGE = new Image(props.getProperty("gameObjects.fireball.image"));
		this.x = x;
		this.y = y;
		this.speedY = Integer.parseInt(props.getProperty("gameObjects.fireball.shootSpeedY"));
		this.RADIUS = Float.parseFloat(props.getProperty("gameObjects.fireball.radius"));
		this.attackPoints = Double.parseDouble(props.getProperty("gameObjects.fireball.damage"));
		hasCollided = false;
	}

	/**
	 * Updates the fireball's position, checks for collisions, and applies damage to entities if needed.
	 * @param input The current keyboard/mouse input.
	 * @param taxi The taxi object in the game.
	 * @param driver The driver object.
	 * @param cars The list of other cars in the game.
	 * @param index The index of the enemy car shooting the fireball.
	 */
	public void update(Input input, Taxi taxi, Driver driver, ArrayList<Car> cars, int index) {
		move();
		draw();

		if (!driver.isInTaxi()) {
			collide(driver);
		}

		if (taxi.getTrip() != null) {
			Passenger passenger = taxi.getPassenger();
			collide(passenger);
		}

		collide(taxi);

		if (cars != null) {
			for (Car car: cars) {
				if (!car.isDestroyed && (index != cars.indexOf(car))) {
					collide(car);
				}
			}
		}

	}

	/**
	 * Checks if the fireball has collided with an entity based on their radii.
	 * @param entity The entity to check for collision.
	 * @return True if the fireball has collided with the entity, false otherwise.
	 */
	public boolean hasCollidedWith(Entity entity) {
		// if the distance between the two objects is less than the sum of their radius, they are collided
		float collisionDistance = RADIUS + entity.getRadius();
		float currDistance = (float) Math.sqrt(Math.pow(x - entity.getX(), 2) + Math.pow(y - entity.getY(), 2));
		return currDistance <= collisionDistance;
	}

	/**
	 * Handles the collision logic between the fireball and an entity, applying damage if applicable.
	 * @param entity The entity the fireball has collided with.
	 */
	public void collide(Entity entity) {
		if (hasCollidedWith(entity)) {
			entity.setIsCollided();
			hasCollided = true;

			// Change crash directions for entity
			if (entity.getY() > this.y) {
				entity.setCrashDirection(DOWN);
			} else {
				entity.setCrashDirection(UP);
			}

			// Apply damage and collision movement if entity timeout isn't active/ entity isn't invincible
			if (entity.getCollisionFrames() <= 0) {
				entity.takeDamage(this.attackPoints);
				entity.resetCollisionFrames();
			}
		}
	}

	/**
	 * Moves the fireball in the y-direction based on its speed.
	 */
	public void move() {
		this.y -= speedY;
	}

	/**
	 * Draws the fireball at its current position on the screen.
	 */
	public void draw() {
		IMAGE.draw(x, y);
	}

	/**
	 * Checks if the fireball is off the screen.
	 * @return True if the fireball has moved off the screen, false otherwise.
	 */
	public boolean isOffScreen() {
		return y + IMAGE.getHeight() / 2 < 0;
	}


	public boolean getHasCollided() {
		return hasCollided;
	}
}
