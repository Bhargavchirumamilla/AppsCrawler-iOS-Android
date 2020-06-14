package com.landmarkgroup.appscrawler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;



public class AppsCrawler{
	static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36"; 
	static CloseableHttpClient httpClient;
	static HttpResponse response = null;
    static String access_token = null;
	static Object navigationData = null;
	String jsonData = "";
	static JSONArray navNodesArray;
    static String concept = null;
	static String iOSClientSecret= null;
	static String clientID = "mobile_iphone";
	static String appID = null;
	static String baseSite_ID = null;
	static String navURL=null;
	static String sitePrefix = null;
	static String langPrefix = null;
	static Logger logger = LogManager.getLogger(Logger.class.getName());
	
	public static void main(String[] args) throws Exception
	{
		try{
			
			loadProperties();
			httpClient = HttpClients.createDefault();
			String[] conceptName = new String[]{"homecentre","babyshopstores", "splashfashions", "lifestyleshops", "shoemartstores", "centrepointstores", "homeboxstores", "maxfashion"};
			String [] baseSite = new String[] {"homecentre", "babyshop", "splash", "lifestyle", "shoemart", "centrepoint", "homebox", "max"};
			String [] siteLocale = new String[] {"ae", "sa"};
			String [] langLocale = new String[] {"en", "ar"};
			
			for(int i=0; i<conceptName.length; i++)
			{
				
				for(int j=0; j<siteLocale.length; j++)
				{
					for(int k=0; k<langLocale.length; k++) {
					generate_token(conceptName[i],siteLocale[j]);
					postRequest(conceptName[i],baseSite[i].concat(siteLocale[j]),langLocale[k]);
					
					
					if(conceptName[i].equalsIgnoreCase("homecentre") || conceptName[i].equalsIgnoreCase("lifestyleshops") || conceptName[i].equalsIgnoreCase("centrepointstores") || conceptName[i].equalsIgnoreCase("homeboxstores")){
						get_Content(conceptName[i],siteLocale[j],langLocale[k]);
					}
					
					if(conceptName[i].equalsIgnoreCase("babyshopstores")){
						get_BabyshopContent(conceptName[i],siteLocale[j],langLocale[k]);
					}
					
					if(conceptName[i].equalsIgnoreCase("maxfashion") || conceptName[i].equalsIgnoreCase("shoemartstores")){
						get_MaxContent(conceptName[i],siteLocale[j],langLocale[k]);
					}
				
					if(conceptName[i].equalsIgnoreCase("splashfashions")){
						get_SplashContent(conceptName[i],siteLocale[j],langLocale[k]);
					}
				
				  }		
				
				}
			}	
			
			httpClient.close();
	  }catch(ClientProtocolException e) {

		e.printStackTrace();

	  } catch(IOException e) {

		e.printStackTrace();
	  }

	}

	
	private static void generate_token(String conceptName, String sitePrefix){
		
		BufferedReader br = null;
		String jsonTokenData = "";
		String line="";
		
		
		String tokenURL="https://www."+conceptName+".com/landmarkshopscommercews/"+sitePrefix+"/oauth/token?grant_type=client_credentials&client_id="+clientID+"&client_secret="+iOSClientSecret+"&appId="+appID;
		HttpPost postRequest = new HttpPost(tokenURL);
		postRequest.addHeader("Content-Type", "form-data");
		
		try{
		
		response = httpClient.execute(postRequest);
		InputStreamReader reader = new InputStreamReader(response.getEntity().getContent(),"UTF-8");
		 br = new BufferedReader(reader);
		
		while((line = br.readLine()) != null){
			jsonTokenData = jsonTokenData+line;
		}

		System.out.print(jsonTokenData+"\n");
		
		JSONObject jsonObject =  new JSONObject(jsonTokenData);
		access_token = jsonObject.get("access_token").toString();
		}catch(IOException ex){
			ex.printStackTrace();
			
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
	private static void postRequest(String concept, String baseSite_ID, String langPrefix){
		
		BufferedReader br = null;
		String jsonData = "";
		String line="";
		
		HttpPost postRequest = new HttpPost("https://www."+concept+".com/landmarkshopscommercews/v2/"+baseSite_ID+"/"+langPrefix+"/navigationNodes?fields=FULL&pageId=homepage&appId="+appID+"&access_token="+access_token);
		postRequest.addHeader("accept", "application/json");
		
		try{
			response = httpClient.execute(postRequest);
			InputStreamReader reader = new InputStreamReader(response.getEntity().getContent(),"UTF-8");
			 br = new BufferedReader(reader);
			
			while((line = br.readLine()) != null){
				jsonData = jsonData+line;
			}
			
		
			JSONObject jsonObject =  new JSONObject(jsonData);
			navNodesArray = jsonObject.getJSONArray("navNodes");
			
			}catch(IOException ex){
				ex.printStackTrace();
				
			} catch (UnsupportedOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				try {
					if (br != null)
						br.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}	
	}
	
	
	
	private static void get_SplashContent(String con, String siteP, String langP){
		JSONArray categoryNode = null;
		JSONArray subCategoryNode = null;
		      try{
					JSONArray childNodesArray = navNodesArray.getJSONObject(0).getJSONArray("childNodes");
					for(int i=0; i< childNodesArray.length(); i++){
						if(childNodesArray.getJSONObject(i).get("nodeType").toString().equalsIgnoreCase("DEPARTMENT"))
						{
							
							JSONArray departmentNodeArray = childNodesArray.getJSONObject(i).getJSONArray("childNodes");
							
							for(int j=0; j<departmentNodeArray.length(); j++) //3 childNodes
							{
								if(departmentNodeArray.getJSONObject(j).get("nodeType").toString().equalsIgnoreCase("DEPARTMENT") && departmentNodeArray.getJSONObject(j).has("childNodes"))
								{
									categoryNode = departmentNodeArray.getJSONObject(j).getJSONArray("childNodes");
									
									if(categoryNode.getJSONObject(0).has("childNodes"))
									{
									 subCategoryNode = categoryNode.getJSONObject(0).getJSONArray("childNodes");
									 
									   for(int k=0; k<subCategoryNode.length(); k++)
									   {     
										   navURL = subCategoryNode.getJSONObject(k).get("navigationData").toString();
											getURLResponse(con, siteP, langP);
								       }
									}
									else
									{
										for(int l=0; l<categoryNode.length(); l++){
											navURL=categoryNode.getJSONObject(l).get("navigationData").toString();
											getURLResponse(con, siteP, langP);
										}
										
									}
									
									
								}
								
					     	}
						}
					
					}
			      }catch (UnsupportedOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
		}	
	
	
	
		private static void get_BabyshopContent(String con, String siteP, String langP){
			JSONArray categoryNode = null;
		      try{
				JSONArray childNodesArray = navNodesArray.getJSONObject(0).getJSONArray("childNodes");
				for(int i=0; i< childNodesArray.length(); i++){
					if(childNodesArray.getJSONObject(i).get("nodeType").toString().equalsIgnoreCase("DEPARTMENT")){
						
						JSONArray departmentNodeArray = childNodesArray.getJSONObject(i).getJSONArray("childNodes");
						
						for(int j=0; j<departmentNodeArray.length(); j++){
							if(departmentNodeArray.getJSONObject(j).get("nodeType").toString().equalsIgnoreCase("DEPARTMENT") && departmentNodeArray.getJSONObject(j).has("childNodes"))
							{
								categoryNode = departmentNodeArray.getJSONObject(j).getJSONArray("childNodes");
								for(int k=0; k<categoryNode.length(); k++){
									navURL=categoryNode.getJSONObject(k).get("navigationData").toString();
									getURLResponse(con, siteP, langP);
							     }
							}	
							else
							{
								navURL=departmentNodeArray.getJSONObject(j).get("navigationData").toString();
								getURLResponse(con, siteP, langP);
								
								
							}
							
				     	}
					}
				
				}
				
				}catch (UnsupportedOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
		}
	
	
		private static void get_MaxContent(String con, String siteP, String langP){
		      try{
				JSONArray childNodesArray = navNodesArray.getJSONObject(0).getJSONArray("childNodes");
				for(int i=0; i< childNodesArray.length(); i++){
					if(childNodesArray.getJSONObject(i).get("nodeType").toString().equalsIgnoreCase("DEPARTMENT")){
						
						JSONArray departmentNodeArray = childNodesArray.getJSONObject(i).getJSONArray("childNodes");
						
						for(int j=0; j<departmentNodeArray.length()-2; j++){
							
							JSONArray categoryNode = departmentNodeArray.getJSONObject(j).getJSONArray("childNodes");
						
						for(int k=0; k<categoryNode.length(); k++){
							navURL=categoryNode.getJSONObject(k).get("navigationData").toString();
							getURLResponse(con, siteP, langP);
						}
							
				     	}
					}
				
				}
				
				}catch (UnsupportedOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
		}
	
	
		
		private static void get_Content(String con, String siteP, String langP){
		      try{
				JSONArray childNodesArray = navNodesArray.getJSONObject(0).getJSONArray("childNodes");
				for(int i=0; i< childNodesArray.length(); i++){
					if(childNodesArray.getJSONObject(i).get("nodeType").toString().equalsIgnoreCase("DEPARTMENT")){
						
						JSONArray departmentNodeArray = childNodesArray.getJSONObject(i).getJSONArray("childNodes");
						
						for(int j=0; j<departmentNodeArray.length(); j++){
							JSONArray categoryNode = departmentNodeArray.getJSONObject(j).getJSONArray("childNodes");
						
						for(int k=0; k<categoryNode.length(); k++){
							navURL=categoryNode.getJSONObject(k).get("navigationData").toString();
							getURLResponse(con, siteP, langP);
						}
							
				     	}
					}
				
				}
				
				}catch (UnsupportedOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
		}

	
	
	private static void getURLResponse(String concept, String sitePrefix, String langPrefix)
	{    
		String finalURL = null;
		try{
			{
				finalURL="https://www."+concept+".com/"+sitePrefix+"/"+langPrefix+navURL;
				  Response response =  Jsoup.connect(finalURL)
						              .userAgent(USER_AGENT)
							          .ignoreHttpErrors(false)
							          .followRedirects(true)
							          .execute();
				  
				 
				 if(response.statusCode() == 200)
				 {
					  logger.info(response.statusCode()+" : "+finalURL+"\n");
			     }
				 
				 else
				 {
					 logger.error(response.statusCode()+" : "+finalURL+"\n");
					 
				 }
				 
		      }
			
			}catch(HttpStatusException ex)
		
		      {
				logger.error("HTTPStatusException: "+finalURL);
				
			  }
		
		    catch(IOException ex)
		    {
		    	logger.error(ex.getMessage());
		    }
		
	    }
	
	
	private static void loadProperties() {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			
			logger.info("Reading of config file started");
			input = new FileInputStream(System.getProperty("user.dir")+"/appcrawler.properties");
			prop.load(new InputStreamReader(input, "UTF-8"));
			iOSClientSecret = prop.getProperty("IOSClientSecret");
			clientID = prop.getProperty("ClientID");
			appID = prop.getProperty("appId");
			

		} catch (IOException e) {
			logger.error("Reading the config file failed because of IOException.");
		}

	}
	
}