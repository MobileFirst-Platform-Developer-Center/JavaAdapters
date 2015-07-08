/*
 *
    COPYRIGHT LICENSE: This information contains sample code provided in source code form. You may copy, modify, and distribute
    these sample programs in any form without payment to IBM® for the purposes of developing, using, marketing or distributing
    application programs conforming to the application programming interface for the operating platform for which the sample code is written.
    Notwithstanding anything to the contrary, IBM PROVIDES THE SAMPLE SOURCE CODE ON AN "AS IS" BASIS AND IBM DISCLAIMS ALL WARRANTIES,
    EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, ANY IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
    FITNESS FOR A PARTICULAR PURPOSE, TITLE, AND ANY WARRANTY OR CONDITION OF NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR ANY DIRECT,
    INDIRECT, INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR OPERATION OF THE SAMPLE SOURCE CODE.
    IBM HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS OR MODIFICATIONS TO THE SAMPLE SOURCE CODE.

 */

package com.sample;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.wink.json4j.utils.XML;
import org.xml.sax.SAXException;

import com.worklight.adapters.rest.api.WLServerAPI;
import com.worklight.adapters.rest.api.WLServerAPIProvider;

@Path("/")
public class RSSAdapterResource {
	/*
	 * For more info on JAX-RS see https://jsr311.java.net/nonav/releases/1.1/index.html
	 */
	
	//Define logger (Standard java.util.Logger)
    static Logger logger = Logger.getLogger(RSSAdapterResource.class.getName());

    //Define the server api to be able to perform server operations
    WLServerAPI api = WLServerAPIProvider.getWLServerAPI();
    
	private static CloseableHttpClient client;
	private static HttpHost host;

	public static void init() {
		client = HttpClients.createDefault();
		host = new HttpHost("rss.cnn.com");
	}

	public void execute(HttpUriRequest req, HttpServletResponse resultResponse)
			throws ClientProtocolException, IOException,
			IllegalStateException, SAXException {
		HttpResponse RSSResponse = client.execute(host, req); 
		ServletOutputStream os = resultResponse.getOutputStream();
		if (RSSResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){	
			resultResponse.addHeader("Content-Type", "application/json");
			String json = XML.toJson(RSSResponse.getEntity().getContent());
			os.write(json.getBytes(Charset.forName("UTF-8")));
			
		}else{
			resultResponse.setStatus(RSSResponse.getStatusLine().getStatusCode());
			RSSResponse.getEntity().getContent().close();
			os.write(RSSResponse.getStatusLine().getReasonPhrase().getBytes());
		}
		os.flush();
		os.close();
	}

	@GET
	@Produces("application/json")
	public void get(@Context HttpServletResponse response, @QueryParam("topic") String topic)
			throws ClientProtocolException, IOException, IllegalStateException, SAXException {
		if(topic!=null && !topic.isEmpty()){
			execute(new HttpGet("/rss/edition_"+ topic+".rss"), response);
		}
		else{
			execute(new HttpGet("/rss/edition.rss"), response);
		}
		
	}

}
