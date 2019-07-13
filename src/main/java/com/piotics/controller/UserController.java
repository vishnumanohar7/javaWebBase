package com.piotics.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.piotics.model.PasswordReset;
import com.piotics.model.SignUpUser;
import com.piotics.model.Token;
import com.piotics.service.UserService;

@RestController
@RequestMapping(value="/user")
public class UserController {
	
	@Autowired
	UserService userService;

	@RequestMapping(value = "/signUp", method = RequestMethod.POST)
	public ResponseEntity SignUp(@Valid @RequestBody SignUpUser signUpUser, HttpServletRequest req) throws Exception {
		userService.signUp(signUpUser, req);
		return new ResponseEntity(HttpStatus.ACCEPTED);
	}

	@RequestMapping(value = "/verifyEmail", method = RequestMethod.POST)
	public ResponseEntity verifyEmail(@Valid @RequestBody Token token) {
		userService.verifyEmail(token);
		return new ResponseEntity(HttpStatus.OK);
	}

	@RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
	public ResponseEntity forgotPassword(@Valid @RequestParam String username) throws Exception {
		userService.forgotPassword(username);
		return new ResponseEntity(HttpStatus.OK);
	}

	@RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
	public ResponseEntity resetPassword(@Valid @RequestBody PasswordReset passwordReset) {
		userService.resetPassword(passwordReset);
		return new ResponseEntity(HttpStatus.ACCEPTED);
	}

	@RequestMapping(value = "/verifyIdToken", method = RequestMethod.POST)
	public ResponseEntity verifyIdToken(Authentication authentication, @RequestParam String idToken,
			HttpServletRequest req, HttpServletResponse res) throws FirebaseAuthException {
		FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
		String uid = decodedToken.getUid();

//		String uid = "12333";

		userService.verifyIdToken(authentication, res, decodedToken, req);
//		userService.verifyIdToken(authentication,res,uid);

		return new ResponseEntity(HttpStatus.OK);
	}
	
	
	
}
