package com.kinvey.java.testing;

import java.io.IOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

public class MockHttpForMIC extends HttpTransport{


	
	  @Override
	  public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
	
	    MockLowLevelHttpRequest request = new MockLowLevelHttpRequest(url);
	    
	    MockLowLevelHttpResponse response = null;
	    
	    if (url.contains("oauth/token")){
	    	response = oauthToken();
	    }else if (url.contains("oauth/auth")){
	    	response = oauthAuth();
	    }else if (url.contains("tempURL")){
	    	response = tempURL();
	    }
	    
	    
	    request.setResponse(response);
	    
	    return request;
	  }
	  
	  private MockLowLevelHttpResponse oauthToken(){
		  return null;
	  }
	  
	  private MockLowLevelHttpResponse oauthAuth(){
		  return null;
	  }
	  
	  private MockLowLevelHttpResponse tempURL(){
		  return null;
	  }

}
