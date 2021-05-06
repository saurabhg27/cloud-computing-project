package com.clocomp.project.appengine.googledrive;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.appengine.api.users.UserServiceFactory;

@Controller
public class GoogleDriveController {
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Autowired
	GoogleAuthService googleAuthService;

	@GetMapping("/checkDriveAccount")
	public @ResponseBody String checkDriveAccount(HttpServletResponse response) throws Exception {
		
		checkUserLoggedIn(response);
		
		String userName = UserServiceFactory.getUserService().getCurrentUser().getEmail();
		logger.info("checkDriveAccount userName:"+userName);
		if (googleAuthService.checkDriveConnected(userName)) {
			logger.info("Drive is connected");
			return "true";
		} else {
			logger.info("User is not authenticated");
			return "false";
		}
	}
	
	private void checkUserLoggedIn(HttpServletResponse response) throws IOException {
		if(!UserServiceFactory.getUserService().isUserLoggedIn())
			response.sendRedirect("/");
	}
	
	@GetMapping("/googledrivelink")
	public void linkGoogleDrive(HttpServletRequest request,HttpServletResponse response) throws Exception {
		
		String currentUser = "";
		checkUserLoggedIn(response);
		currentUser = UserServiceFactory.getUserService().getCurrentUser().getEmail();
		
		if(googleAuthService.checkDriveConnected(currentUser))
			response.sendRedirect("/");
		
		String url = googleAuthService.generateOauthURL(request, currentUser);
		logger.info("sendRedirect "+url);
		response.sendRedirect(url);
	}

	@GetMapping("/oauth/callback")
	public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
		logger.info("SSO Callback invoked...");
		String code = request.getParameter("code");
		String userName = request.getParameter("state");
		logger.info("userName: "+userName);
		logger.info("SSO Callback Code Value..., " + code);

		if (code != null) {
			googleAuthService.exchangeCodeForTokens(request,code,userName);
			return "redirect:/";
		}
		return "Please grant access to drive!!";
	}
	
	

	
}
