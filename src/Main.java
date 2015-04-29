import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;



public class Main {

	public static void main(String[] args) {
		
		//if there is a input parameter
		if (args.length>0){
			String cityName= args[0];
			//capitalize every type of entry
			cityName= cityName.substring(0,1).toUpperCase()+cityName.substring(1).toLowerCase();
		
			boolean internet=false;
				
			//check internet connection
			if (internetTest("www.google.com") || internetTest("www.amazon.com")){
				System.out.println("Internet connection ok");
				internet=true;

			}
			else {
				System.out.println("Internet connection not available");
				JOptionPane.showMessageDialog(new JFrame(),  
						"To run this program you need internet connection. Check your internet connection.",
						"Connection error",
						JOptionPane.ERROR_MESSAGE);

				internet=false;
				//close program
				System.exit(0);
			}
		
			//if internet connection is ok 
			if (internet){
				ArrayList<City> cityArray= sendRequest(cityName);
				if (cityArray.size()>0){
					if(writeFile(cityName, cityArray)){
						System.out.println("File successfully created");
					}
					else{
						System.out.println("Unable to create file. Check you have the right permissions");
					}
				} else{
					//if there are possible spelling mistake
					List correction=checkSpelling(cityName);
					if (correction != null){		
						String choice=makeChoice(correction.get(0).toString());
						if (choice != null){
							ArrayList<City> cityArray2= sendRequest(choice);
							if (cityArray2.size()>0){
								if(writeFile(choice, cityArray2)){
									System.out.println("File successfully created");
								}
								else{
									System.out.println("Unable to create file. Check you have the right permissions");
								}
							} else{
								System.out.println("Sorry, can't find this city");
							}
						}
					} else{
						System.out.println("Sorry, can't find this city");
					}
				}
			}
		} else{
			System.out.println("Sorry, you must enter the name of the city to search");
		}
	}
	
	/*method to write the the arraylist on a file.
	 * Returns true if everything ok and writes data a file called "@cityName.csv"
	 * @ cityName: the name of the city we searched. Is also the name of the output file
	 * @ cityArray: the array of cities we found
	 */
	private static boolean writeFile(String cityName, ArrayList<City> cityArray) {
		//check if file exists or can be created
		Path path= Paths.get(cityName+".csv");
		if (checkFile(path)){
			try {
				//write on file
				PrintWriter writer= new PrintWriter(cityName+".csv","UTF-8");
				for(City c : cityArray){
					writer.write(c.getId()+","+c.getName()+","+c.getType()+","+c.getLatitude()+","+c.getLongitude()+"\n");
				}
				writer.close();
				return true;
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
				return false;
			}
		} else{
			System.out.println("Something went wrong during file creation. Check yours permissions");
			return false;
		}
	}
	
	
	/*method to check if file exists. If exists it will be delete and recreate, otherwise it will be 
	 * created.
	 * Returns true if everything goes right
	 * @ path: the path were the file will be searched and created
	 */
	private static boolean checkFile(Path path){
		//check if file exist
		if (Files.exists(path)){
			try {
				Files.delete(path);
				File f=new File(path.toString());
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
				return false;
			}
		} else{
			File f=new File(path.toString());
			return true;
		}
	}

	/*internet connection checking method.
	 * Returns true if connection available otherwise else
	 * 
	 * @ site: website you want to connect to test your internet connection
	 */
	private static boolean internetTest(String site){
		
		//creating socket
		Socket sock=new Socket();
		
		//creating address
		InetSocketAddress address= new InetSocketAddress(site, 80);
		
		//trying to connect
		System.out.println("Checking internet connection");
		try{
			sock.connect(address, 1000);
			return true;
		}
		catch(IOException io){
			System.exit(0);
			return false;
		}
		//closing connection
		finally {
			try {
				sock.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}	
	}
	
	/*this method send the json request and parse the results-
	 * Returns the ArrayList<City> containing the search results
	 * @ cityName: the name of the city you want to search
	 */
	private static ArrayList<City> sendRequest(String cityName){
		ArrayList<City> results= new ArrayList<City>();
		
		try {
			URL serverUrl=new URL("http://api.goeuro.com/api/v2/position/suggest/en/"+cityName);
			
			//opening connection
			URLConnection conn = serverUrl.openConnection();
			
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line=rd.readLine();
			
			//parsing server response
			JSONArray array= (JSONArray) JSONValue.parse(line);
					
			
			for(int i=0; i<array.size(); i++){
				//convert each element in the JSONArray into a JSONObject
				JSONObject obj= new JSONObject((Map) array.get(i));
				
				int id= Integer.parseInt(Long.toString((long) obj.get("_id")));
				String name= (String) obj.get("name");
				String type= (String) obj.get("type");
				
				//creating JSONObject of geo coordinates
				JSONObject geo=  (JSONObject)obj.get("geo_position");
				double latitude= (double)geo.get("latitude");
				double longitude= (double)geo.get("longitude");

				//System.out.println("id= "+id+"  nome= "+name+"  tipo= "+type+"  latitude= "+latitude+"  longitude= "+longitude);
				
				//Adding elements to the results array
				results.add(new City(id, name, type, latitude, longitude));
			}
			
			rd.close();

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
				
		return results;
	}
	
	/*method used to find spelling mistake.
	 * Return the list of possible correction if found, null otherwise
	 * @ cityName: the word that must be checked
	 */
	private static List checkSpelling(String cityName){
		SpellDictionaryHashMap dictionary = null;
	    SpellChecker spellChecker = null;
	    		
		try {
           dictionary = new SpellDictionaryHashMap(new File("english.0"));
	    }catch (IOException e) {
			System.exit(0);
	    	e.printStackTrace();
	    }
	     
		spellChecker = new SpellChecker(dictionary);	 
		 
		return spellChecker.getSuggestions(cityName, 0);
	}

	
	/*method to choice if make another research with the corrected word or terminate.
	 * Return the corrected string if you want to maje another research, null otherwise
	 * @ correction: the list of possible correction
	 */
	private static String makeChoice(String newCity){
		newCity= newCity.substring(0,1).toUpperCase()+newCity.substring(1).toLowerCase();

		System.out.println("No results were found for the previously city, maybe you were searching "+newCity);
		System.out.println("if you want to make a new research with \""+newCity+"\" as keyword digit 1, otherwise 0");
		Scanner keyboard = new Scanner(System.in);
		boolean control=true;
		String newWord=null;
		while (control){
			int choice = keyboard.nextInt();
			switch (choice){
			default: 
				System.out.println("Unvalid choice. Please insert 0 or 1");
				break;
			case 1: 
				newWord=newCity;
				control=false;
				break;
			case 0: 
				newWord=null;
				control=false;
				break;
			}
		}
		return newWord;
	}
}
