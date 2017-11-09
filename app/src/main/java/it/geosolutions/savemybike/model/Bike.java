package it.geosolutions.savemybike.model;

/**
 * Created by Robert Oehler on 26.10.17.
 *
 */

public class Bike {

    private final static String DEFAULT_BIKE_NAME = "My Bike";

    private long id;
    private String name;
    private String imagePath;
    private boolean stolen;
    private boolean selected;

    /**
     * static constructor for a default bike, selected, non stolen
     */
    public static Bike createDefaultBike() {

        return new Bike(
                -1,
                DEFAULT_BIKE_NAME,
                null,
                true,
                false);
    }

    public Bike(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Bike(int id, String name, String uri, boolean selected, boolean stolen) {
        this.id = id;
        this.name = name;
        this.imagePath = uri;
        this.selected = selected;
        this.stolen = stolen;
    }

    public String getName() {
        return name;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isStolen() {
        return stolen;
    }

    public void setStolen(boolean stolen) {
        this.stolen = stolen;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
