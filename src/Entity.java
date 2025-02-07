/**
 * Abstract class representing an entity in the game.
 * Different entities (such as cars, drivers, etc.) inherit from this class
 * and implement the required behavior for collisions, movement, and attacks.
 */
public abstract class Entity {
	// Constants for entity behavior
	static final int UP = -1;
	static final int DOWN = 1;
	static final int RESET_FRAMES = 200;
	static final int NON_COLLISIONMOVE_FRAMES = 190;

	/**
	 * Applies damage to the entity.
	 * @param damage The amount of damage to apply.
	 */
	public abstract void takeDamage(double damage);

	/**
	 * Resets the collision frames, restarting the collision timeout.
	 */
	public abstract void resetCollisionFrames();

	/**
	 * Handles the entity's movement during a collision.
	 */
	public abstract void collisionMove();

	// Getters and setters
	public abstract int getX();
	public abstract int getY();
	public abstract float getRadius();
	public abstract void setIsCollided();
	public abstract void setCrashDirection(int direction);
	public abstract double getAttackPoints();
	public abstract int getCollisionFrames();
	public abstract boolean getIsInvincible();

}
