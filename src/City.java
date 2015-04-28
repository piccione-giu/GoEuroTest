
public class City {
	
	private int _id;
	private String name;
	private String type;
	private double latitude;
	private double longitude;
	
	/*constructor
	 * @ _id: city id number
	 * @ name: city name
	 * @ type: city type
	 * @ latitude: city latitude
	 * @ longitude: city longitude
	 */
	public City(int _id, String name, String type, double latitude, double longitude){
		setId(_id);
		this.name=name;
		this.type=type;
		this.latitude=latitude;
		this.longitude=longitude;
	}
	
	//id setter
	public void setId(int _id){
		this._id=_id;
	}
	
	//name setter
	public void setName(String name){
		this.name=name;
	}
	
	//type setter
	public void setType(String type){
		this.type=type;
	}
	
	//latitude setter
	public void setLatitude(double latitude){
		this.latitude=latitude;
	}
	
	//longitude setter
	public void setLongitude(double longitude){
		this.longitude=longitude;
	}
	
	//id getter
	public int getId(){
		return this._id;
	}
	
	//name getter
	public String getName(){
		return this.name;
	}

	//type getter
	public String getType(){
		return this.type;
	}

	//latitude getter
	public double getLatitude(){
		return this.latitude;
	}

	//longitude getter
	public double getLongitude(){
		return this.longitude;
	}
}
