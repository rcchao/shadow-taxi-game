import bagel.Font;
import bagel.Image;
import bagel.Input;
import bagel.Keys;

import java.util.Properties;

/**
 * Class representing a passenger in the game. Passengers can interact with taxis and initiate trips.
 */
public class Passenger extends Entity{
    private final Properties props;

    // Movement related attributes
    private final int WALK_SPEED_X;
    private final int WALK_SPEED_Y;
    private int walkDirectionX;
    private int walkDirectionY;
    private final Image IMAGE;
    private final int SPEED_Y;
    private int x;
    private int y;
    private int moveY;

    // Trip related attributes
    private final TravelPlan TRAVEL_PLAN;
    private boolean reachedFlag;
    private Trip trip;
    private boolean isGetInTaxi;
    private final int PRIORITY_OFFSET;
    private final int EXPECTED_FEE_OFFSET;
    private final int TAXI_DETECT_RADIUS;

    // Damage related attributes
    private double health;
    private boolean bloodRendered;
    private Blood blood;
    private final float radius;
    private boolean isCollided;
    private boolean isDestroyed;
    private int crashDirection;
    private int attackPoints;
    private int collisionFrames;
    private boolean inTaxi;
    private final boolean isInvincible;

    /**
     * Constructor for the Passenger class.
     *
     * @param x The x-coordinate of the passenger.
     * @param y The y-coordinate of the passenger.
     * @param priority The priority of the passenger for starting a trip.
     * @param endX The x-coordinate of the trip end destination.
     * @param distanceY The distance from the passenger's starting point to the destination.
     * @param props The properties object containing configurations for the passenger.
     */
    public Passenger (int x, int y, int priority, int endX, int distanceY, Properties props) {

        this.WALK_SPEED_X = Integer.parseInt(props.getProperty("gameObjects.passenger.walkSpeedX"));
        this.WALK_SPEED_Y = Integer.parseInt(props.getProperty("gameObjects.passenger.walkSpeedY"));
        this.radius = Float.parseFloat(props.getProperty("gameObjects.passenger.radius"));
        this.props = props;

        this.TRAVEL_PLAN = new TravelPlan(endX, distanceY, priority, props);
        this.TAXI_DETECT_RADIUS = Integer.parseInt(props.getProperty("gameObjects.passenger.taxiDetectRadius"));

        this.x = x;
        this.y = y;
        this.moveY = 0;
        this.PRIORITY_OFFSET = 30;
        this.EXPECTED_FEE_OFFSET = 100;

        this.SPEED_Y = Integer.parseInt(props.getProperty("gameObjects.taxi.speedY"));
        this.IMAGE = new Image(props.getProperty("gameObjects.passenger.image"));
        this.health = Double.parseDouble(props.getProperty("gameObjects.passenger.health"));
        isInvincible = false;
    }

    /**
     * Update the passenger status, move according to the input, active taxi and trip status.
     * Initiate the trip if the passenger is in the taxi.
     * See move method below to understand the movement of the passenger better.
     * @param input The current mouse/keyboard input.
     * @param taxi The active taxi in the game play.
     */
    public void updateWithTaxi(Input input, Taxi taxi) {
        // if the passenger is not in the taxi or the trip is completed, update the passenger status based on keyboard
        // input. This means the passenger is go down when taxi moves up.
        if(!isGetInTaxi || (trip != null && trip.isComplete())) {
            if(input != null) {
                adjustToInputMovement(input);
            }

            move();
            draw();
        }

        // if the passenger is not in the taxi and there's no trip initiated, draw the priority number on the passenger.
        if(!isGetInTaxi && trip == null) {
            drawPriority();
        }

        if(adjacentToObject(taxi) && !isGetInTaxi && trip == null && taxi.getDriverIsInTaxi()) {
            // if the passenger has not started the trip yet,
            // Taxi must be stopped in passenger's vicinity and not having another trip.
            setIsGetInTaxi(taxi);
            move(taxi);
        } else if(isGetInTaxi) {
            // if the passenger is in the taxi, initiate the trip and move the passenger along with the taxi.
            if(trip == null) {
                //Create new trip
                getTravelPlan().setStartY(y);
                trip = new Trip(this, taxi, props);
                taxi.setTrip(trip);
            }

            move(taxi);
            draw();

        } else if(!isGetInTaxi && trip != null && trip.isComplete()) {
            move(taxi);
            draw();
        }

    }

    /**
     * Updates the passenger's status based on the driver's state and renders the blood effect if needed.
     * @param input The current keyboard/mouse input.
     * @param driver The driver object in the game.
     */
    public void updateWithDriver(Input input, Driver driver){
        // Render blood
        if (bloodRendered && blood.getFramesActive() > 0) {
            blood.update(input);
        }

        // Decrement collision frames
        if (isCollided) {
            collisionFrames--;
        }
        collisionMove();

        // Update with driver
        moveWithDriver(driver);

        draw();

    }

    /**
     * Draw the priority number on the passenger.
     */
    private void drawPriority() {
        Font font = new Font(props.getProperty("font"),
                Integer.parseInt(props.getProperty("gameObjects.passenger.fontSize")));
        font.drawString(String.valueOf(TRAVEL_PLAN.getPriority()), x - PRIORITY_OFFSET, y);
        font.drawString(String.valueOf(TRAVEL_PLAN.getExpectedFee()), x - EXPECTED_FEE_OFFSET, y);
    }

    /**
     * Adjust the movement direction in y-axis of the GameObject based on the keyboard input.
     * @param input The current mouse/keyboard input.
     */
    private void adjustToInputMovement(Input input) {
        if (input.wasPressed(Keys.UP)) {
            moveY = 1;
        }  else if(input.wasReleased(Keys.UP)) {
            moveY = 0;
        }
    }

    /**
     * Move in relevant to the taxi and passenger's status.
     * @param taxi active taxi
     */
    private void move(Taxi taxi) {
        if (inTaxi && isGetInTaxi) {
            // if the passenger is in the taxi, move the passenger along with the taxi.
            moveWithTaxi(taxi);
        } else if(!isGetInTaxi && trip != null && trip.isComplete()) {
            //walk towards end flag if the trip is completed and not in the taxi.
            if(!hasReachedFlag()) {
                TripEndFlag tef = trip.getTripEndFlag();
                walkXDirectionObj(tef.getX());
                walkYDirectionObj(tef.getY());
                walk();
            }
        } else {
            // Walk towards the taxi if other conditions are not met.
            // (That is when taxi is stopped with not having a trip and adjacent to the passenger and the passenger
            // hasn't initiated the trip yet.)
            walkXDirectionObj(taxi.getX());
            walkYDirectionObj(taxi.getY());
            walk();
        }
    }

    /**
     * Move the GameObject object in the y-direction based on the speedY attribute.
     */
    private void move() {
        this.y += SPEED_Y * moveY;
    }

    /**
     * Draw the GameObject object into the screen.
     */
    private void draw() {
        IMAGE.draw(x, y);
    }

    /**
     * Walk the people object based on the walk direction and speed.
     */
    private void walk() {
        x += WALK_SPEED_X * walkDirectionX;
        y += WALK_SPEED_Y * walkDirectionY;
    }

    /**
     * Move the people object along with taxi when the people object is in the taxi.
     * @param taxi Active taxi in the game play
     */
    private void moveWithTaxi(Taxi taxi) {
        x = taxi.getX();
        y = taxi.getY();
    }

    /**
     * Move the people object along with taxi when the people object is in the taxi.
     * @param driver Active taxi in the game play
     */
    private void moveWithDriver(Driver driver) {
        x = driver.getX() - 50;
        y = driver.getY();
    }

    /**
     * Determine the walk direction in x-axis of the passenger based on the x direction of the object.
     */
    private void walkXDirectionObj(int otherX) {
        if (otherX > x) {
            walkDirectionX = 1;
        } else if (otherX < x) {
            walkDirectionX = -1;
        } else {
            walkDirectionX = 0;
        }
    }

    /**
     * Determine the walk direction in y-axis of the passenger based on the x direction of the object.
     */
    private void walkYDirectionObj(int otherY) {
        if (otherY > y) {
            walkDirectionY = 1;
        } else if (otherY < y) {
            walkDirectionY = -1;
        } else {
            walkDirectionY = 0;
        }
    }

    /**
     * Check if the passenger has reached the end flag of the trip.
     * @return a boolean value indicating if the passenger has reached the end flag.
     */
    public boolean hasReachedFlag() {
        if(trip != null) {
            TripEndFlag tef = trip.getTripEndFlag();
            if(tef.getX() == x && tef.getY() == y) {
                reachedFlag = true;
            }
            return reachedFlag;
        }
        return false;
    }

    /**
     * Check if the taxi is adjacent to the passenger. This is evaluated based on multiple crietria.
     * @param taxi The active taxi in the game play.
     * @return a boolean value indicating if the taxi is adjacent to the passenger.
     */
    private boolean adjacentToObject(Taxi taxi) {
        // Check if Taxi is stopped and health > 0
        boolean taxiStopped = !taxi.isMovingX() && !taxi.isMovingY();
        // Check if Taxi is in the passenger's detect radius
        float currDistance = (float) Math.sqrt(Math.pow(taxi.getX() - x, 2) + Math.pow(taxi.getY() - y, 2));
        // Check if Taxi is not having another trip
        boolean isHavingAnotherTrip = taxi.getTrip() != null && taxi.getTrip().getPassenger() != this;

        return currDistance <= TAXI_DETECT_RADIUS && taxiStopped && !isHavingAnotherTrip;
    }

    /**
     * Set the get in taxi status of the people object.
     * This is used to set an indication to check whether the people object is in the taxi or not.
     * @param taxi The taxi object to be checked. If it is null, the people object is not in a taxi at the moment in
     *             the game play.
     */
    public void setIsGetInTaxi(Taxi taxi) {
        if(taxi == null) {
            isGetInTaxi = false;
            inTaxi = false;
        } else if((float) Math.sqrt(Math.pow(taxi.getX() - x, 2) + Math.pow(taxi.getY() - y, 2)) <= 0) {
            isGetInTaxi = true;
            inTaxi = true;
        }
    }

    /**
     * Ejects the driver from the taxi when the taxi becomes damaged.
     */
    public void eject() {
        inTaxi = false;
        x -= 100; // Update coordinates as per specification
        // y remains the same
    }

    /**
     * Handles the passenger's movement during a collision.
     */
    public void collisionMove() {
        if (this.collisionFrames >= 190) {
            this.y += crashDirection * 2;
            this.x += crashDirection * 2;
        }
    }

    /**
     * Applies damage to the passenger.
     * @param damage The amount of damage to apply.
     */
    public void takeDamage(double damage) {
        this.health -= damage;
        if (health <= 0) {
            isDestroyed = true;
            blood = new Blood(this.x, this.y, props);
            bloodRendered = true;
        }
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

    public void resetCollisionFrames() {
        collisionFrames = 200;
    }

    public boolean getIsInvincible() {
        return isInvincible;
    }

    public double getHealth() {
        return health;
    }

    public boolean isInTaxi() {
        return inTaxi;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public TravelPlan getTravelPlan() {
        return TRAVEL_PLAN;
    }
}
