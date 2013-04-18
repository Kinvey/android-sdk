//package com.kinvey.samples.old.citywatch.kinvey.samples.citywatch;
//
//import java.io.Serializable;
//import java.util.Arrays;
//import java.util.List;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.location.Location;
//
//import com.kinvey.persistence.mapping.FieldConstants;
//import com.kinvey.persistence.mapping.MappedEntity;
//import com.kinvey.persistence.mapping.MappedField;
//
//public class CityWatchEntity implements MappedEntity, Serializable {
//
//	private static final long serialVersionUID = 6102384593846439027L;
//	// Location is needed for v1 of Kinvey's Android library to perform
//	// GeoQueries
//	private transient Location coords;
//	private String objectId;
//
//	// but Facebook needs long/lat seperately
//	private Double latitude;
//	private Double longitude;
//
//	private String title;
//	private byte[] image;
//
//	private transient Bitmap bitmap = null;
//
//
//	// private Drawable image;
//	private String description;
//
//	private String category;
//	private String severity;
//	private String risk;
//
//	private String address;
//
//	private Integer repeat;
//
//	@Override
//	public List<MappedField> getMapping() {
//		return Arrays.asList(new MappedField[] {
//				new MappedField("coords", FieldConstants.KEY_GEOLOCATION),
//				new MappedField("objectId", FieldConstants.KEY_ID),
//				new MappedField("latitude", "latitude"),
//				new MappedField("longitude", "longitude"),
//
//				new MappedField("title", "title"),
//				new MappedField("description", "description"),
//				new MappedField("address", "address"),
//
//				new MappedField("category", "category"),
//				new MappedField("severity", "severity"),
//				new MappedField("risk", "risk"),
//				new MappedField("repeat", "repeat") });
//	}
//
//	public Location getCoords() {
//		return coords;
//	}
//
//	public void setCoords(Location coords) {
//		this.coords = coords;
//	}
//
//	public String getObjectId() {
//		return objectId;
//	}
//
//	public void setObjectId(String objectId) {
//		this.objectId = objectId;
//	}
//
//	public Double getLatitude() {
//		return latitude;
//	}
//
//	public void setLatitude(Double latitude) {
//		this.latitude = latitude;
//	}
//
//	public Double getLongitude() {
//		return longitude;
//	}
//
//	public void setLongitude(Double longitude) {
//		this.longitude = longitude;
//	}
//
//	public String getTitle() {
//		return title;
//	}
//
//	public void setTitle(String title) {
//		this.title = title;
//	}
//
//	public String getDescription() {
//		return description;
//	}
//
//	public void setDescription(String description) {
//		this.description = description;
//	}
//
//	public String getCategory() {
//		return category;
//	}
//
//	public void setCategory(String category) {
//		this.category = category;
//	}
//
//	public String getSeverity() {
//		return severity;
//	}
//
//	public void setSeverity(String severity) {
//		this.severity = severity;
//	}
//
//	public String getRisk() {
//		return risk;
//	}
//
//	public void setRisk(String risk) {
//		this.risk = risk;
//	}
//
//	public Integer getRepeat() {
//		return repeat;
//	}
//
//	public void setRepeat(Integer repeat) {
//		this.repeat = repeat;
//	}
//
//	public String getAddress() {
//		return address;
//	}
//
//	public void setAddress(String address) {
//		this.address = address;
//	}
//
//	public byte[] getImage() {
//		if(image == null){
//			image = new byte[1];
//		}
//		return image;
//	}
//
//	public void setImage(byte[] image) {
//		this.image = image;
//	}
//
//	public Bitmap getBitmap() {
//		return bitmap;
//	}
//
//	public void setBitmap(Bitmap bitmap) {
//		this.bitmap = bitmap;
//	}
//
//
//}
