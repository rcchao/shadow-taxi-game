import bagel.Image;
import bagel.Input;
import bagel.Keys;

import java.util.Properties;

/**
 * The class representing the taxis in the game play
 */
public class Taxi extends Entity{
    // Constants for random taxi generation
    private static final int TAXI_YVAL1 = 200;
    private static final int TAXI_YVAL2 = 400;
    private static final int TAXI_XVAL1 = 360;
    private static final int TAXI_XVAL2 = 620;
    private static final int OFF_SCREEN = 768;

    private Properties props;
    private final Image IMAGE;
    private final int SPEED_X;

    private final float RADIUS;
    private double attackPoints;
    private double health;

    private final Trip[] TRIPS;
    private int tripCount;
    private int x;
    private int y;
    private boolean isMovingX;
    private Driver driver;
    private int collisionFrames;
    private boolean isCollided;
    private boolean isInvincible;
    private boolean renderedDamagedTaxi;
    private int crashDirection;

    private Coin coinPower;
    private InvinciblePower invinciblePower;
    private Trip trip;

    // Public (protected) attributes for child class
    /**
     * Taxi speed moving down
     */
    public final int SPEED_Y;
    /**
     * Flag for if the taxi's moving down
     */
    public boolean isMovingY;
    /**
     * The destruction state of the car.
     */
    public boolean isDestroyed;

    private boolean smokeRendered;
    private Smoke smoke;
    private boolean fireRendered;
    private Fire fire;

    private boolean renderedNewTaxi;
    private boolean offScreen;


    /**
     * Constructor for the Taxi class.
     * @param x The x-coordinate of the taxi.
     * @param y The y-coordinate of the taxi.
     * @param maxTripCount The maximum number of trips the taxi can take.
     * @param props The properties object containing taxi configurations.
     * @param driver The driver of the taxi.
     */
    public Taxi(int x, int y, int maxTripCount, Properties props, Driver driver) {
        this.x = x;
        this.y = y;
        this.props = props;
        this.isDestroyed = false;
        isInvincible = false;
        renderedDamagedTaxi = false;
        offScreen = false;
        collisionFrames = 0;
        TRIPS = new Trip[maxTripCount];

        this.SPEED_X = Integer.parseInt(props.getProperty("gameObjects.taxi.speedX"));
        this.SPEED_Y = Integer.parseInt(props.getProperty("gameObjects.taxi.speedY"));
        this.IMAGE = new Image(props.getProperty("gameObjects.taxi.image"));
        this.RADIUS = Float.parseFloat(props.getProperty("gameObjects.taxi.radius"));
        this.attackPoints = Double.parseDouble(props.getProperty("gameObjects.taxi.damage"));
        this.health = Double.parseDouble(props.getProperty("gameObjects.taxi.health"));

        this.driver = driver;
    }


    /**
     * Update the GameObject object's movement states based on the input.
     * Render the game object into the screen.
     * @param input The current mouse/keyboard input.
     */
    public void update(Input input) {
        if (driver.isInTaxi()) {
            driver.setPosition(x, y);
        }

        // if the taxi has coin power, apply the effect of the coin on the priority of the passenger
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

        // check if invincible power is active, then no damage is taken
        if (invinciblePower != null && invinciblePower.getIsActive()) {
            isInvincible = true;
        } else {
            isInvincible = false;
        }

        if (isCollided) {
            collisionFrames--;
            collisionMove();
            if (collisionFrames <= 0) {
                isCollided = false;
            }
        }

        if(input != null) {
            adjustToInputMovement(input);
            if (!driver.isInTaxi()) {
                move();
            }
        }

        if(trip != null && trip.hasReachedEnd()) {
            getTrip().end();
        }

        if (!isDestroyed || renderedNewTaxi) {
            draw();
        }

        if (smokeRendered && smoke.getFramesActive() > 0) {
            smoke.update(input);
        }

        if (fireRendered && fire.getFramesActive() > 0) {
            fire.update(input);
        }

        // the flag of the current trip renders to the screen
        if(tripCount > 0) {
            Trip lastTrip = TRIPS[tripCount - 1];
            if(!lastTrip.getPassenger().hasReachedFlag()) {
                lastTrip.getTripEndFlag().update(input);
            }
        }

    }

    /**
     * Draw the GameObject object into the screen.
     */
    public void draw() {
        IMAGE.draw(x, y);
    }

    /**
     * Adjust the movement of the taxi based on the keyboard input.
     * If the taxi has a driver, and taxi has health>0 the taxi can only move left and right (fixed in y direction).
     * If the taxi does not have a driver, the taxi can move in all directions.
     * @param input The current mouse/keyboard input.
     */
    public void adjustToInputMovement(Input input) {
        if (input.wasPressed(Keys.UP)) {
            isMovingY = true;
        }  else if(input.wasReleased(Keys.UP)) {
            isMovingY = false;
        }

        if (driver != null && driver.isInTaxi()) {
            if(input.isDown(Keys.LEFT)) {
                x -= SPEED_X;
                isMovingX = true;
            }  else if(input.isDown(Keys.RIGHT)) {
                x += SPEED_X;
                isMovingX =  true;
            } else if(input.wasReleased(Keys.LEFT) || input.wasReleased(Keys.RIGHT)) {
                isMovingX = false;
            }
        }
    }

    /**
     * Moves the taxi vertically when unoccupied.
     */
    public void move() {
        if (isMovingY) {
            this.y += SPEED_Y;

            if (this.y >= OFF_SCREEN) {
                offScreen = true;
            }
        }
    }

    /**
     * Collects a power-up (coin or invincibility) for the taxi.
     * @param powerUp The power-up object.
     */
    public void collectPower(PowerUp powerUp) {
        if (powerUp instanceof Coin) {
            coinPower = (Coin) powerUp;
        } else if (powerUp instanceof InvinciblePower) {
            invinciblePower = (InvinciblePower) powerUp;
        }
    }

    /**
     * Calculate total earnings. (See how fee is calculated for each trip in Trip class.)
     * @return int, total earnings
     */
    public float calculateTotalEarnings() {
        float totalEarnings = 0;
        for(Trip trip : TRIPS) {
            if (trip != null) {
                totalEarnings += trip.getFee();
            }
        }
        return totalEarnings;
    }

    /**
     * Applies damage to the taxi and handles destruction.
     * If destroyed, the driver and passenger are ejected, and smoke or fire is rendered.
     * @param damage The amount of damage taken.
     */
    public void takeDamage(double damage) {
        this.health -= damage;
        if (health <= 0) {
            isDestroyed = true;
            ejectDriver();
            ejectPassenger();
            fire = new Fire(this.x, this.y, props);
            fireRendered = true;
        } else {
            smoke = new Smoke(this.x, this.y, props);
            smokeRendered = true;
        }
    }

    /**
     * Eject driver from taxi when taxi is damaged
     */
    private void ejectDriver() {
        if (driver.isInTaxi()) {
            driver.eject();
        }
    }

    /**
     * Eject passenger from taxi when taxi is damaged
     */
    private void ejectPassenger() {
        if (trip != null) {
            Passenger passenger = getPassenger();
            if (passenger.isInTaxi()) {
                passenger.eject();
            }
        }
    }

    /**
     * Renders a new taxi on the screen with a random position.
     */
    public void renderNewTaxi() {
        this.x = MiscUtils.selectAValue(TAXI_XVAL1, TAXI_XVAL2);
        this.y =  MiscUtils.getRandomInt(TAXI_YVAL1, TAXI_YVAL2);
        this.health = Double.parseDouble(props.getProperty("gameObjects.taxi.health"));
        renderedNewTaxi = true;
    }

    /**
     * If it's a new trip, it will be added to the list of trips.
     * @param trip trip object
     */
    public void setTrip(Trip trip) {
        this.trip = trip;
        if(trip != null) {
            this.TRIPS[tripCount] = trip;
            tripCount++;
        }
    }

    /**
     * Get the last trip from the list of trips.
     * @return Trip object
     */
    public Trip getLastTrip() {
        if(tripCount == 0) {
            return null;
        }
        return TRIPS[tripCount - 1];
    }

    /**
     * Handles collision movement for car
     */
    public void collisionMove() {
        if (collisionFrames >= 190) {
            this.y += crashDirection;
        }
    }

    /**
     * Reset collision Frames
     */
    public void resetCollisionFrames() {
        collisionFrames = RESET_FRAMES;
    }

    public boolean getRenderedDamagedTaxi() {
        return renderedDamagedTaxi;
    }

    public void setRenderedDamagedTaxi(boolean renderedDamagedTaxi) {
        this.renderedDamagedTaxi = renderedDamagedTaxi;
    }

    public int getCollisionFrames() {
        return collisionFrames;
    }

    public double getAttackPoints() {
        return attackPoints;
    }

    public void setCrashDirection(int crashDirection) {
        this.crashDirection = crashDirection;
    }

    public boolean getIsInvincible() {
        return isInvincible;
    }

    public boolean isOffScreen() {
        return offScreen;
    }

    public boolean getDriverIsInTaxi() {
        return this.driver.isInTaxi();
    }

    public void setIsCollided() {
        this.isCollided = true;
    };

    public boolean isMovingY() {
        return isMovingY;
    }

    public boolean isMovingX() {
        return isMovingX;
    }

    public float getRadius() {
        return RADIUS;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getHealth() {
        return health;
    }

    public Trip getTrip() {
        return this.trip;
    }

    public Passenger getPassenger() {
        return this.trip.getPassenger();
    }

    public double getPassengerHealth() {
        Passenger passenger = this.trip.getPassenger();
        return passenger.getHealth();
    }
}
