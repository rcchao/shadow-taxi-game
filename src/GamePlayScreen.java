import bagel.Font;
import bagel.Input;

import java.util.*;

/**
 * Represents the gameplay screen in the game.
 */
public class GamePlayScreen{
    private final Properties GAME_PROPS;
    private final Properties MSG_PROPS;

    // keep track of earning and coin timeout
    private float totalEarnings;
    private float coinFramesActive;

    private int currFrame = 0;

    // game objects
    ArrayList<String[]> weatherLines;
    ArrayList<Car> cars;
    ArrayList<DamagedTaxi> damagedTaxis;
    private Taxi taxi;
    private Driver driver;
    ArrayList<Passenger> passengers;
    private Coin[] coins;
    private InvinciblePower[] invinciblePowers;
    private Background background1;
    private Background background2;

    private final float TARGET;
    private final int MAX_FRAMES;

    // vars for save score into the file
    private final String PLAYER_NAME;
    private boolean savedData;

    // display text vars
    private final Font INFO_FONT;
    private final int EARNINGS_Y;
    private final int EARNINGS_X;
    private final int COIN_X;
    private final int COIN_Y;
    private final int TARGET_X;
    private final int TARGET_Y;
    private final int MAX_FRAMES_X;
    private final int MAX_FRAMES_Y;
    private final int TAXI_HEALTH_X;
    private final int TAXI_HEALTH_Y;
    private final int DRIVER_HEALTH_X;
    private final int DRIVER_HEALTH_Y;
    private final int PASSENGER_HEALTH_X;
    private final int PASSENGER_HEALTH_Y;
    private double minPassengerHealth = 1.0;

    private final int TRIP_INFO_X;
    private final int TRIP_INFO_Y;
    private final int TRIP_INFO_OFFSET_1;
    private final int TRIP_INFO_OFFSET_2;
    private final int TRIP_INFO_OFFSET_3;

    /**
     * Constructor for the GamePlayScreen class, which sets up the game environment and initializes game properties.
     * @param gameProps The properties object containing game configurations such as fonts, target, and object files.
     * @param msgProps The properties object containing game message configurations.
     * @param playerName The name of the player for the current game session.
     */
    public GamePlayScreen(Properties gameProps, Properties msgProps, String playerName) {
        this.GAME_PROPS = gameProps;
        this.MSG_PROPS = msgProps;
        this.cars = new ArrayList<>();
        this.damagedTaxis = new ArrayList<>();

        // read game objects from file and weather file and populate the game objects and weather conditions
        this.weatherLines = IOUtils.readCommaSeparatedFile(gameProps.getProperty("gamePlay.weatherFile"));
        ArrayList<String[]> lines = IOUtils.readCommaSeparatedFile(gameProps.getProperty("gamePlay.objectsFile"));
        populateGameObjects(lines);

        this.TARGET = Float.parseFloat(gameProps.getProperty("gamePlay.target"));
        this.MAX_FRAMES = Integer.parseInt(gameProps.getProperty("gamePlay.maxFrames"));

        // display text vars
        INFO_FONT = new Font(gameProps.getProperty("font"), Integer.parseInt(
                gameProps.getProperty("gamePlay.info.fontSize")));
        EARNINGS_Y = Integer.parseInt(gameProps.getProperty("gamePlay.earnings.y"));
        EARNINGS_X = Integer.parseInt(gameProps.getProperty("gamePlay.earnings.x"));
        COIN_X = Integer.parseInt(gameProps.getProperty("gameplay.coin.x"));
        COIN_Y = Integer.parseInt(gameProps.getProperty("gameplay.coin.y"));
        TARGET_X = Integer.parseInt(gameProps.getProperty("gamePlay.target.x"));
        TARGET_Y = Integer.parseInt(gameProps.getProperty("gamePlay.target.y"));
        MAX_FRAMES_X = Integer.parseInt(gameProps.getProperty("gamePlay.maxFrames.x"));
        MAX_FRAMES_Y = Integer.parseInt(gameProps.getProperty("gamePlay.maxFrames.y"));
        TAXI_HEALTH_X = Integer.parseInt(gameProps.getProperty("gamePlay.taxiHealth.x"));
        TAXI_HEALTH_Y = Integer.parseInt(gameProps.getProperty("gamePlay.taxiHealth.y"));
        DRIVER_HEALTH_X = Integer.parseInt(gameProps.getProperty("gamePlay.driverHealth.x"));
        DRIVER_HEALTH_Y = Integer.parseInt(gameProps.getProperty("gamePlay.driverHealth.y"));
        PASSENGER_HEALTH_X = Integer.parseInt(gameProps.getProperty("gamePlay.passengerHealth.x"));
        PASSENGER_HEALTH_Y = Integer.parseInt(gameProps.getProperty("gamePlay.passengerHealth.y"));

        // current trip info vars
        TRIP_INFO_X = Integer.parseInt(gameProps.getProperty("gamePlay.tripInfo.x"));
        TRIP_INFO_Y = Integer.parseInt(gameProps.getProperty("gamePlay.tripInfo.y"));
        TRIP_INFO_OFFSET_1 = 30;
        TRIP_INFO_OFFSET_2 = 60;
        TRIP_INFO_OFFSET_3 = 90;

        this.PLAYER_NAME = playerName;
    }

    /**
     * Populate the game objects from the lines read from the game objects file.
     * @param lines list of lines read from the game objects file. lines are processed into String arrays using comma as
     *             delimiter.
     */
    private void populateGameObjects(ArrayList<String[]> lines) {
        String initialWeather = getCurrentWeatherForFrame(currFrame);
        // two background images stacked in y-axis are used to create a scrolling effect
        background1 = new Background(
                Integer.parseInt(GAME_PROPS.getProperty("window.width")) / 2,
                Integer.parseInt(GAME_PROPS.getProperty("window.height")) / 2,
                initialWeather, GAME_PROPS);
        background2 = new Background(
                Integer.parseInt(GAME_PROPS.getProperty("window.width")) / 2,
                -1 * Integer.parseInt(GAME_PROPS.getProperty("window.height")) / 2,
                initialWeather, GAME_PROPS);

        // Since you haven't learned Lists in Java, we have to use two for loops to iterate over the lines.
        int passengerCount = 0;
        int coinCount = 0;
        int invinciblePowerCount = 0;
        for(String[] lineElement: lines) {
            if(lineElement[0].equals(GameObjectType.PASSENGER.name())) {
                passengerCount++;
            } else if(lineElement[0].equals(GameObjectType.COIN.name())) {
                coinCount++;
            } else if(lineElement[0].equals(GameObjectType.INVINCIBLE_POWER.name())) {
                invinciblePowerCount++;
            }
        }
        passengers = new ArrayList<Passenger>();
        coins = new Coin[coinCount];
        invinciblePowers = new InvinciblePower[invinciblePowerCount];

        // process each line in the file
        int coin_idx = 0;
        int invinciblePower_idx = 0;

        for(String[] lineElement: lines) {
            int x = Integer.parseInt(lineElement[1]);
            int y = Integer.parseInt(lineElement[2]);

            if(lineElement[0].equals(GameObjectType.TAXI.name())) {
                driver = new Driver(x, y, taxi, GAME_PROPS);
                taxi = new Taxi(x, y, passengerCount, this.GAME_PROPS, driver);
            } else if(lineElement[0].equals(GameObjectType.PASSENGER.name())) {
                int priority = Integer.parseInt(lineElement[3]);
                int travelEndX = Integer.parseInt(lineElement[4]);
                int travelEndY = Integer.parseInt(lineElement[5]);

                Passenger passenger = new Passenger(x, y, priority, travelEndX, travelEndY, GAME_PROPS);
                passengers.add(passenger);

            } else if(lineElement[0].equals(GameObjectType.COIN.name())) {
                Coin coinPower = new Coin(x, y, this.GAME_PROPS);
                coins[coin_idx] = coinPower;
                coin_idx++;
            } else if(lineElement[0].equals(GameObjectType.INVINCIBLE_POWER.name())) {
                InvinciblePower invinciblePower = new InvinciblePower(x,y, this.GAME_PROPS);
                invinciblePowers[invinciblePower_idx] = invinciblePower;
                invinciblePower_idx++;
            }
        }
    }

    /**
     * Update the states of the game objects based on the keyboard input.
     * Handle the spawning of other cars in random intervals
     * Change the background image and change priorities based on the weather condition
     * Handle collision between game objects
     * Spawn new taxi if the active taxi is destroyed
     * @param input
     * @return true if the game is finished, false otherwise
     */
    public boolean update(Input input) {
        currFrame++;

        // Check the weather for this frame
        String currWeather = getCurrentWeatherForFrame(currFrame);

        // Update backgrounds based on current weather conditions
        background1.setWeatherCondition(currWeather, GAME_PROPS);
        background2.setWeatherCondition(currWeather, GAME_PROPS);

        background1.update(input, background2);
        background2.update(input, background1);

        for(Passenger passenger: passengers) {
            if (driver.getTrip() != null && passengers.indexOf(passenger) == passengers.indexOf(driver.getTripPassenger())) {
                continue;
            }
            passenger.updateWithTaxi(input, taxi);
            if (passenger.getHealth() < minPassengerHealth){
                minPassengerHealth = passenger.getHealth();
            }
        }

        if (driver.isInTaxi()) {
            if (driver.getTrip() != null) {
                Passenger passenger= driver.getTripPassenger();
                passenger.updateWithTaxi(input, taxi);
            }
        } else {
            if (driver.getTrip() != null) {
                Passenger passenger= driver.getTripPassenger();
                passenger.updateWithDriver(input, driver);
            }
        }

        taxi.update(input);
        if (taxi.isDestroyed && !taxi.getRenderedDamagedTaxi()) {
            DamagedTaxi damagedTaxi = new DamagedTaxi(taxi.getX(), taxi.getY(), GAME_PROPS, driver);
            taxi.setRenderedDamagedTaxi(true);
            damagedTaxis.add(damagedTaxi);
            taxi.renderNewTaxi();
        }

        driver.update(input, taxi);
        totalEarnings = taxi.calculateTotalEarnings();

        if (MiscUtils.canSpawn(200)) {
            Car car = new Car(GAME_PROPS);
            cars.add(car);
        }

        if (MiscUtils.canSpawn(400)) {
            Car enemyCar = new EnemyCar(GAME_PROPS);
            cars.add(enemyCar);
        }

        if (cars != null) {
            for (Car car: cars) {
                car.update(input, taxi, driver, cars);
            }
        }

        if (damagedTaxis != null) {
            for (DamagedTaxi damagedTaxi: damagedTaxis) {
                damagedTaxi.update(input);
            }
        }


        if(coins.length > 0) {
            int minFramesActive = coins[0].getMaxFrames();
            for(Coin coinPower: coins) {
                coinPower.update(input);
                coinPower.collide(taxi);
                coinPower.collide(driver);

                // check if there's active coin and finding the coin with maximum ttl
                int framesActive = coinPower.getFramesActive();
                if(coinPower.getIsActive() && minFramesActive > framesActive) {
                    minFramesActive = framesActive;
                }
            }
            coinFramesActive = minFramesActive;
        }

        if(invinciblePowers.length > 0) {
            int minFramesActive = invinciblePowers[0].getMaxFrames();
            for(InvinciblePower invinciblePower: invinciblePowers) {
                invinciblePower.update(input);
                invinciblePower.collide(taxi);
                invinciblePower.collide(driver);

                // check if there's active coin and finding the coin with maximum ttl
                int framesActive = invinciblePower.getFramesActive();
                if(invinciblePower.getIsActive() && minFramesActive > framesActive) {
                    minFramesActive = framesActive;
                }
            }
        }

        displayInfo();

        return isGameOver(driver, taxi, passengers) || isLevelCompleted();

    }

    // Helper function for changing the weather
    private String getCurrentWeatherForFrame(int frame) {
        for (String[] weatherInfo : weatherLines) {
            String condition = weatherInfo[0];
            int startFrame = Integer.parseInt(weatherInfo[1]);
            int endFrame = Integer.parseInt(weatherInfo[2]);

            if (frame >= startFrame && frame <= endFrame) {
                return condition;
            }
        }
        // Default to sunny if no condition matches
        return "SUNNY";
    }

    /**
     * Display the game information on the screen.
     */
    public void displayInfo() {
        INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.earnings") + getTotalEarnings(), EARNINGS_X, EARNINGS_Y);
        INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.target") + String.format("%.02f", TARGET), TARGET_X,
                TARGET_Y);
        INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.remFrames") + (MAX_FRAMES - currFrame), MAX_FRAMES_X,
                MAX_FRAMES_Y);

        if(coins.length > 0 && coins[0].getMaxFrames() != coinFramesActive) {
            INFO_FONT.drawString(String.valueOf(Math.round(coinFramesActive)), COIN_X, COIN_Y);
        }

        Trip lastTrip = taxi.getLastTrip();
        if(lastTrip != null) {
            if(lastTrip.isComplete()) {
                INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.completedTrip.title"), TRIP_INFO_X, TRIP_INFO_Y);
            } else {
                INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.onGoingTrip.title"), TRIP_INFO_X, TRIP_INFO_Y);
            }
            INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.trip.expectedEarning")
                    + lastTrip.getPassenger().getTravelPlan().getExpectedFee(), TRIP_INFO_X, TRIP_INFO_Y
                    + TRIP_INFO_OFFSET_1);
            INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.trip.priority")
                    + lastTrip.getPassenger().getTravelPlan().getPriority(), TRIP_INFO_X, TRIP_INFO_Y
                    + TRIP_INFO_OFFSET_2);
            if(lastTrip.isComplete()) {
                INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.trip.penalty") + String.format("%.02f",
                        lastTrip.getPenalty()), TRIP_INFO_X, TRIP_INFO_Y + TRIP_INFO_OFFSET_3);
            }
        }

        // Represent the health values of each entity
        INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.taxiHealth") + String.format("%.02f", taxi.getHealth() * 100),
                TAXI_HEALTH_X, TAXI_HEALTH_Y);
        if (taxi.getTrip() != null) {
            INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.passengerHealth") + String.format("%.02f",
                    taxi.getPassengerHealth() * 100), PASSENGER_HEALTH_X, PASSENGER_HEALTH_Y);
        } else {
            INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.passengerHealth") + String.format("%.02f", minPassengerHealth * 100),
                    PASSENGER_HEALTH_X, PASSENGER_HEALTH_Y);
        }
        INFO_FONT.drawString(MSG_PROPS.getProperty("gamePlay.driverHealth") + String.format("%.02f", driver.getHealth() * 100),
                DRIVER_HEALTH_X, DRIVER_HEALTH_Y);
    }

    /**
     * Check if the game is over. If the game is over and not saved the score, save the score.
     * @return true if the game is over, false otherwise.
     */
    public boolean isGameOver(Driver driver, Taxi taxi, ArrayList<Passenger> passengers) {
        // Game is over if the current frame is greater than the max frames
        boolean passengerDead = false;
        for (Passenger p: passengers) {
            if (p.getHealth() <= 0) {
                passengerDead = true;
            }
        }

        boolean isGameOver = (currFrame >= MAX_FRAMES) || (driver.getIsDead()) || (taxi.isOffScreen()) || passengerDead;

        if(currFrame >= MAX_FRAMES && !savedData) {
            savedData = true;
            IOUtils.writeScoreToFile(GAME_PROPS.getProperty("gameEnd.scoresFile"), PLAYER_NAME + "," + totalEarnings);
        }
        return isGameOver;
    }

    /**
     * Check if the level is completed. If the level is completed and not saved the score, save the score.
     * @return true if the level is completed, false otherwise.
     */
    public boolean isLevelCompleted() {
        // Level is completed if the total earnings is greater than or equal to the target earnings
        boolean isLevelCompleted = totalEarnings >= TARGET;
        if(isLevelCompleted && !savedData) {
            savedData = true;
            IOUtils.writeScoreToFile(GAME_PROPS.getProperty("gameEnd.scoresFile"), PLAYER_NAME + "," + totalEarnings);
        }
        return isLevelCompleted;
    }

    public String getTotalEarnings() {
        return String.format("%.02f", totalEarnings);
    }
}
