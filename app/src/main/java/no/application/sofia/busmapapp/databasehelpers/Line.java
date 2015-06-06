package no.application.sofia.busmapapp.databasehelpers;

/**
 * Created by Sofia on 05.03.15.
 * The class describes the lines added to the database with attributes needed when saving it.
 * This class is only used in the LineDbHelper.
 */
public class Line {

    private int id; //The autoincrement of the database
    private int lineId; //The id from the transportation company's server
    private String name; //The name of the line, e.g. 80E is the name of a bus
    private int transportation;

    public Line(){
        lineId = 0;
        name = null;
        transportation = -1; //nothing
    }


    public Line(int lineId, String name, int transportation){
        this.lineId = lineId;
        this.name = name;
        this.transportation = transportation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTransportation() {
        return transportation;
    }

    public void setTransportation(int transportation) {
        this.transportation = transportation;
    }
}
