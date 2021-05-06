package com.clocomp.project.appengine.controller;

import java.util.logging.Logger;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;

import com.clocomp.project.appengine.Settings;
import com.clocomp.project.appengine.dropbox.DropboxController;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import utilities.GoogleCloudDatastore;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.cloud.datastore.Entity;

@Controller
public class HomeController {

	private Logger mLogger = Logger.getLogger("HomeController");
	
	@GetMapping("/")
	public String goToLogin() {
		if(!UserServiceFactory.getUserService().isUserLoggedIn()) {
			String loginURL = UserServiceFactory.getUserService().createLoginURL(
								Settings.HOME_URL + "/");
			return "redirect:"+loginURL;
		}
		return "index.html";
	}

	
	@GetMapping("/home")
	public String goToHome() {
		return "home.html";
	}
}
