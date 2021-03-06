package com.piotics.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.auth.FirebaseAuthException;
import com.piotics.common.utils.HttpServletRequestUtils;
import com.piotics.config.JwtTokenProvider;
import com.piotics.model.PasswordReset;
import com.piotics.model.SignUpUser;
import com.piotics.model.Token;
import com.piotics.service.UserService;

@RestController
@RequestMapping(value = "/user")
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	HttpServletRequestUtils httpServletRequestUtils;

	@PostMapping(value = "/signUp")
	public ResponseEntity signUp(@Valid @RequestBody SignUpUser signUpUser) throws Exception {
		userService.signUp(signUpUser);
		return new ResponseEntity(HttpStatus.ACCEPTED);
	}

	@PostMapping(value = "/verifyEmail")
	public ResponseEntity verifyEmail(@Valid @RequestBody Token token) {
		userService.verifyEmail(token);
		return new ResponseEntity(HttpStatus.OK);
	}

	@GetMapping(value = "/forgotPassword")
	public ResponseEntity forgotPassword(@RequestParam String username) {
		userService.forgotPassword(username);
		return new ResponseEntity(HttpStatus.OK);
	}

	@PostMapping(value = "/resetPassword")
	public ResponseEntity resetPassword(@Valid @RequestBody PasswordReset passwordReset) {
		userService.resetPassword(passwordReset);
		return new ResponseEntity(HttpStatus.ACCEPTED);
	}

	@PostMapping(value = "/verifyIdToken")
	public ResponseEntity verifyIdToken(Authentication authentication, @RequestParam String idToken,
			HttpServletResponse res) throws FirebaseAuthException {
//		FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
//		String uid = decodedToken.getUid();

		String uid = "12333";

//		String token = userService.verifyIdToken(authentication, decodedToken);
		JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
		String token = jwtTokenProvider.generateJwtToken(authentication, null);
//		String token = userService.(authentication,uid);
		httpServletRequestUtils.addHeader(res, token);

		return new ResponseEntity(HttpStatus.OK);
	}

}
