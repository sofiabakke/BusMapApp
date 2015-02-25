package no.application.sofia.busmapapp.databasehelpers;

/**
 * Created by Sofia on 25.02.15.
 * This class is used to create Stops in the database locally on the phone
 * A stop has some properties which are represented in this class with respective getters and setters
 */
public class Stop {
    private int id; //The autoincrement of the database
    private int entryId; //The id from the transportation company's server
    private String name; //The name of the stop
    private double lat; //latitude of the stops location
    private double lng; //the longitude of the stops location

    public Stop(){
        entryId = 0;
        name = null;
        lat = 0;
        lng = 0;
    }

    public Stop(int entryId, String name, int lat, int lng){
        this.entryId = entryId;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
