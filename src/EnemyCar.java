import bagel.Image;
import bagel.Input;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Class representing an enemy car in the game, capable of shooting fireballs.
 */
public class EnemyCar extends Car{
	private static final int FIREBALL_SPAWN_RATE = 400;
	private final Properties props;
	private ArrayList<Fireball> fireballs;

	/**
	 * Constructor for the EnemyCar class.
	 * @param props The properties file containing enemy car configurations.
	 */
	public EnemyCar(Properties props) {
		super(props);
		this.props = props;

		this.IMAGE = new Image(props.getProperty("gameObjects.enemyCar.image"));
		this.health = Double.parseDouble(props.getProperty("gameObjects.enemyCar.health"));
		this.attackPoints = Double.parseDouble(props.getProperty("gameObjects.enemyCar.damage"));

		// Initialise fireballs list
		fireballs = new ArrayList<>();
	}

	/**
	 * Updates the enemy car's state, including shooting fireballs and handling collisions.
	 * @param input  The current keyboard input.
	 * @param taxi   The player's taxi object.
	 * @param driver The driver object.
	 * @param cars   The list of other cars in the game.
	 */
	public void update(Input input, Taxi taxi, Driver driver, ArrayList<Car> cars) {
		super.update(input, taxi, driver, cars);

		if (!isDestroyed) {
			shootFireball();
		}

		// Update and draw fireballs
		if (fireballs != null) {
			for (int i = fireballs.size() - 1; i >= 0; i--) {
				Fireball fireball = fireballs.get(i);
				if (!fireball.getHasCollided()) {
					fireball.update(input, taxi, driver, cars, cars.indexOf(this));
				}

				if (fireball.isOffScreen() || fireball.getHasCollided()) {
					fireballs.remove(i);
				}
			}
		}
	}

	/**
	 * Shoots a fireball from the enemy car if the spawn rate condition is met.
	 */
	private void shootFireball() {
		if (MiscUtils.canSpawn(FIREBALL_SPAWN_RATE)) {
			Fireball fireball = new Fireball(this.x, this.y, props);
			fireballs.add(fireball);
		}
	}
}
