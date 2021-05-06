package com.clocomp.project.appengine.dropbox;

import com.clocomp.project.appengine.Settings;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.google.api.client.http.HttpResponse;
import com.google.cloud.datastore.Entity;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.logging.type.HttpRequest;
import com.squareup.okhttp.*;
import com.squareup.okhttp.RequestBody;
import okio.BufferedSink;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import utilities.GoogleCloudDatastore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

@RestController
public class DropboxController {
    String appKey = "";
    String scretKey = "";
    String authURL = "https://www.dropbox.com/1/oauth2/authorize?client_id="+appKey+"&response_type=code&redirect_uri="+Settings.HOME_URL+"/dropboxauth";
    String tokenReqUrl = "https://api.dropbox.com/oauth2/token";//    -d code=<AUTHORIZATION_CODE> \\\n" +
//            "    -d grant_type=authorization_code \\\n" +
//            "    -d redirect_uri=<REDIRECT_URI> \\\n" +
//            "    -u <APP_KEY>:<APP_SECRET>";

    @Autowired
    DropboxService dropboxService;
    
    private static Logger mLogger = Logger.getLogger("DropboxController");

    @RequestMapping(value = "/dropboxauth", method =  {RequestMethod.GET,RequestMethod.POST})
    void getDropBoxCode(@RequestParam("code") String code, HttpServletRequest request, HttpServletResponse response) throws Exception{

        String url = tokenReqUrl+"?code="+code+"&grant_type=authorization_code&redirect_uri="+Settings.HOME_URL+"/dropboxauth&" +
                "client_id="+appKey+"&client_secret="+scretKey;
        Objects postObject = null;

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return null;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {

            }
        };

        Request req = new Request.Builder()
                .url(url).post(requestBody)
                .build();

        PrintWriter responseWriter = response.getWriter();
        Response res = client.newCall(req).execute();

        String jsonResponse = new Gson().toJson(res.body().string());
        jsonResponse = jsonResponse.replace("\\\"","'");
        JSONObject jsonObject = new JSONObject(jsonResponse.substring(1,jsonResponse.length()-1));
        System.out.println(jsonObject);

        String userEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
        
        GoogleCloudDatastore.putEntity("dropboxtoken", userEmail, "value", (String) jsonObject.get("access_token"));
        
        response.sendRedirect("/");

    }

    @RequestMapping(value = "/dropbox/token")
    String getDropBoxToken(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String a = request.getParameter("access_token");
        return a;
    }

    @RequestMapping(value = "/dropbox/auth")
    void getDropBoxAuthPage(HttpServletRequest request, HttpServletResponse response) throws Exception{
        response.sendRedirect(authURL);

    }

    class DropBoxResponse{
        String access_token;
        String token_type;
        String uid;
        String account_id;
        String scope;

    }
    
    private static DbxClientV2 getDropBoxClientForAccessToken(String accessToken) {
        DbxClientV2 client = new DbxClientV2(DbxRequestConfig.
        		newBuilder("storage-optimiser").build(), accessToken);
        return client;
    }
    
    public static DbxClientV2 getDropBoxClientForUserEmail(String email) {
    	Entity dropBoxTokenEntity = GoogleCloudDatastore.getEntity("dropboxtoken", email);
    	if(dropBoxTokenEntity == null) {
    		mLogger.severe("Could not find dropbox access token for user: " + email);
    		return null;
    	}
    	mLogger.info("ACCESS TOKEN = "+dropBoxTokenEntity.getProperties().get("value").get().toString());
    	return getDropBoxClientForAccessToken(dropBoxTokenEntity.getProperties().get("value").get().toString());
    }
}
