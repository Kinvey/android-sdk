package com.kinvey.samples.citywatch;


import android.graphics.Bitmap;
import android.location.Location;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

import java.io.Serializable;

public class CityWatchEntity extends GenericJson {

    private static final long serialVersionUID = 6102384593846439027L;

    @Key("_geoloc")
    private double[] coords;
    @Key("_id")
    private String objectId;

    // but Facebook needs long/lat seperately
    @Key("latitude")
    private Double latitude;
    @Key("longitude")
    private Double longitude;
    @Key("title")
    private String title;
    private byte[] image;

    private transient Bitmap bitmap = null;


    // private Drawable image;
    @Key()
    private String description;
    @Key()
    private String category;
    @Key()
    private String severity;
    @Key()
    private String risk;
    @Key()
    private String address;
    @Key()
    private Integer repeat;

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Key()
    private String imageURL;

//    @Override
//    public List<MappedField> getMapping() {
//        return Arrays.asList(new MappedField[] {
//                new MappedField("coords", FieldConstants.KEY_GEOLOCATION),
//                new MappedField("objectId", FieldConstants.KEY_ID),
//                new MappedField("latitude", "latitude"),
//                new MappedField("longitude", "longitude"),
//
//                new MappedField("title", "title"),
//                new MappedField("description", "description"),
//                new MappedField("address", "address"),
//
//                new MappedField("category", "category"),
//                new MappedField("severity", "severity"),
//                new MappedField("risk", "risk"),
//                new MappedField("repeat", "repeat") });
//    }

    public Location getCoords() {
        Location geoloc = new Location(CityWatchEntity.class.getCanonicalName());
        geoloc.setLatitude(coords[1]);
        geoloc.setLongitude(coords[0]);
        return geoloc;
    }

    public void setCoords(Location coords) {
        double[] geoloc = new double[2];
        geoloc[0] = coords.getLongitude();
        geoloc[1] = coords.getLatitude();
        this.coords = geoloc;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getRisk() {
        return risk;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public Integer getRepeat() {
        return repeat;
    }

    public void setRepeat(Integer repeat) {
        this.repeat = repeat;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public byte[] getImage() {
        if(image == null){
            image = new byte[1];
        }
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


}