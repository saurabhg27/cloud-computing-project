package com.clocomp.project.appengine.googledrive;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.appengine.repackaged.com.google.gson.Gson;

@Service
public class GoogleAuthService {
	Logger logger = Logger.getLogger(this.getClass().getName());
	private GoogleAuthorizationCodeFlow flow;
	private HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();
	private JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	@PostConstruct
	public void init() {
		try {
			String OAUTH_CLIENT_CREDENTIALS_FILE_NAME = "/client_secret.json";
			GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
					new InputStreamReader(Utils.class.getResourceAsStream(OAUTH_CLIENT_CREDENTIALS_FILE_NAME)));
			 AppEngineDataStoreFactory DATA_STORE_FACTORY = new AppEngineDataStoreFactory(
					 new AppEngineDataStoreFactory.Builder().setDisableMemcache(true));

			List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
			flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
					.setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "excep in init", e);
		}
	}

	public String generateOauthURL(HttpServletRequest req, String userName) throws Exception {
		String CALLBACK_URL = getCallBackUrl(req);
		GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
		String redirectUrl = url.setRedirectUri(CALLBACK_URL).setState(userName).setAccessType("offline").build();
		logger.info("redirectUrl, " + redirectUrl);
		return redirectUrl;
	}
	
	private String getCallBackUrl(HttpServletRequest request) {
		String CALLBACK_URI = "/oauth/callback";
		GenericUrl url1 = new GenericUrl(request.getRequestURL().toString());
		url1.setRawPath(CALLBACK_URI);
		String CALLBACK_URL = url1.build();
		return CALLBACK_URL;
	}

	public void exchangeCodeForTokens(HttpServletRequest request,String code, String userName) throws Exception {
		// exchange the code against the access token and refresh token
		String getCALLBACK_URI = getCallBackUrl(request);
		GoogleTokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(getCALLBACK_URI).execute();
		flow.createAndStoreCredential(tokenResponse, userName);
		logger.info("credential created for code");
	}
	
	public Credential getCredentials(String userName) throws IOException {
		DataStore<StoredCredential> bla = flow.getCredentialDataStore();
		logger.info("keySet :  "+bla.keySet());
		Credential cred = flow.loadCredential(userName);
		return cred;
	}

	public Drive getDriveForUserName(String userName) throws IOException {

		Credential credential = getCredentials(userName);
		logger.info("CREDDDD " + credential);
		if (credential != null) {
			credential.refreshToken();

			Drive driveS = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
			

			return driveS;
		} else {
			logger.severe("Unable to get creadential for drive for user: "+userName);
		}
		return null;

	}
	
	public boolean checkDriveConnected(String userName) throws IOException {
		Credential credential = getCredentials(userName);
		logger.info("CREDDDD " + credential);
		if (credential != null) {
			boolean isTokenValid = credential.refreshToken();

			logger.info("isTokenValid, " + isTokenValid);
			try {
				Drive driveS = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
				FileList result = driveS.files().list().setFields("files(id, name)").execute();

				int size = result.getFiles().size();
				logger.info("drive stuff size " + size);
				if (size >= 0)
					return true;
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Excep while opening drive to check ", e);
				return false;
			}

			return isTokenValid;
		}
		return false;
	}
}
