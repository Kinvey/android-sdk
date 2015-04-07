package com.kinvey.java.testing;

import java.io.IOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.Json;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.gson.Gson;

public class MockHttpForMIC extends HttpTransport {

	@Override
	public LowLevelHttpRequest buildRequest(String method, final String url)
			throws IOException {

		return new MockLowLevelHttpRequest() {
			@Override
			public LowLevelHttpResponse execute() throws IOException {
				System.out.println("mock HTTP for MIC recieved: " + url);
				
				if (url.contains("oauth/token")) {
					return oauthToken();
				} else if (url.contains("oauth/auth")) {
					return oauthAuth();
				} else if (url.contains("tempURL")) {
					return tempURL();
				}else if(url.contains("/login")){
					return userLogin();
				}
				return null;
			}
		};


	}

	private LowLevelHttpResponse oauthToken() {

		MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();

		GenericJson content = new GenericJson();

		content.put("access_token", "myAccess");
		content.put("refresh_token", "myRefresh");
		content.put("token_type", "bearer");
		content.put("expires_in", "100");

		response.setContentType(Json.MEDIA_TYPE);
		
		response.setContent(new Gson().toJson(content));
		response.setStatusCode(200);
		
		return response;
	}

	private MockLowLevelHttpResponse oauthAuth() {
		return null;
	}

	private MockLowLevelHttpResponse tempURL() {
		return null;
	}
	
	private  MockLowLevelHttpResponse userLogin() {

		MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();

		GenericJson content = new GenericJson();

		content.put("_id", "123");


		response.setContentType(Json.MEDIA_TYPE);
		 System.out.println("------------ " + new Gson().toJson(content));

		response.setContent(new Gson().toJson(content));
		response.setStatusCode(200);
		
		return response;	}
	

}
