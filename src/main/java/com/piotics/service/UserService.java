package com.piotics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseToken;
import com.piotics.common.TokenType;
import com.piotics.common.utils.BCryptPasswordUtils;
import com.piotics.common.utils.HttpServletRequestUtils;
import com.piotics.common.utils.UtilityManager;
import com.piotics.config.JwtTokenProvider;
import com.piotics.constants.UserRoles;
import com.piotics.exception.TokenException;
import com.piotics.exception.UserException;
import com.piotics.model.ApplicationUser;
import com.piotics.model.Invitation;
import com.piotics.model.PasswordReset;
import com.piotics.model.SignUpUser;
import com.piotics.model.Tenant;
import com.piotics.model.Token;
import com.piotics.model.UserProfile;
import com.piotics.model.UserShort;
import com.piotics.repository.UserMongoRepository;
import com.piotics.repository.UserShortMongoRepository;

@Service
@Lazy
public class UserService {

	@Autowired
	UserMongoRepository userMongoRepository;

	@Autowired
	TokenService tokenService;

	@Autowired
	BCryptPasswordUtils bCryptPasswordUtils;

	@Autowired
	HttpServletRequestUtils httpServletRequestUtils;

	@Autowired
	UtilityManager utilityManager;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Autowired
	UserProfileService userProfileService;

	@Autowired
	InvitationService invitationService;

	@Autowired
	MailService mailService;

	@Autowired
	UserShortMongoRepository userShortMongoRepository;

	@Autowired
	NotificationService notificationService;

	@Autowired
	TenantService tenantService;

	@Value("${invite.required}")
	public boolean inviteRequired;

	public void signUp(SignUpUser signUpUser) {

		if (isExistingUser(signUpUser.getUsername()))
			throw new UserException("user already exists");

		if (inviteRequired && !invitationService.isInvited(signUpUser.getUsername()))
			throw new UserException("user not invited");

		ApplicationUser applicationUser = proceedToSignUp(signUpUser);
		
		if(tenantService.isTenantEnabled()) {
			setTenantAndUserRole(applicationUser);
		}
	}

	private ApplicationUser proceedToSignUp(SignUpUser signUpUser) {

		String encodedPassword = bCryptPasswordUtils.encodePassword(signUpUser.getPassword());

		ApplicationUser newUser = new ApplicationUser(signUpUser.getUsername(), encodedPassword, UserRoles.ROLE_USER,
				true);
		Token token = new Token();
		Token dbToken = tokenService.getTokenFromDBWithTokenType(signUpUser.getUsername(), TokenType.INVITATION);

		if (invitationService.isInvited(signUpUser.getUsername())) {
			if (signUpUser.getToken() != null && tokenService.isTokenValid(dbToken)
					&& signUpUser.getToken().getToken().equals(dbToken.getToken())) {
				newUser.setEnabled(true);
				newUser = userMongoRepository.save(newUser);
			}
			tokenService.deleteInviteTkenByUsername(signUpUser.getUsername());
		}

		if (utilityManager.isEmail(signUpUser.getUsername()) && signUpUser.getToken() == null) {

			newUser.setEnabled(false);
			newUser = userMongoRepository.save(newUser);
			token = tokenService.getTokenForEmailVerification(newUser);
			mailService.sendMail(token);
		}

		newUser = userMongoRepository.save(newUser);
		tokenService.save(token);

		UserProfile userProfile = new UserProfile(newUser.getId(), newUser.getEmail(), newUser.getPhone());
		userProfileService.save(userProfile);
		
		return newUser;
	}

	private void setTenantAndUserRole(ApplicationUser applicationUser) {
		Invitation invitation = invitationService.getInviationByUsername(applicationUser.getEmail());
		applicationUser.setRole(invitation.getUserRole());
		Tenant tenant = tenantService.getTenantById(invitation.getTenantId());
		tenant.setOwnerId(applicationUser.getId());
		tenantService.save(tenant);
		applicationUser.setCompany(tenant);
		
		UserProfile userProfile = userProfileService.getProfile(applicationUser.getId());
		tenantService.updateTenatRelation(userProfile, tenant, invitation.getUserRole());
		userMongoRepository.save(applicationUser);
	}

	public boolean isExistingUser(String userName) {
		return !(userMongoRepository.findByEmail(userName) == null
				&& userMongoRepository.findByPhone(userName) == null);

	}

	public void verifyEmail(Token token) {

		Token dbToken = tokenService.getTokenFromDBWithTokenType(token.getUsername(), TokenType.EMAILVERIFICATION);

		if (dbToken == null) {
			if (userMongoRepository.findByEmail(token.getUsername()) == null)
				throw new UserException("UnRegistered");
			else
				throw new UserException("ExistingUser");
		}

		tokenService.isTokenValid(dbToken);
		if (!dbToken.getToken().equals(token.getToken()))
			throw new TokenException("InvalidToken");

		Optional<ApplicationUser> appUserOptional = userMongoRepository.findById(dbToken.getUserId());

		if (appUserOptional.isPresent()) {
			ApplicationUser user = appUserOptional.get();
			user.setEnabled(true);
			userMongoRepository.save(user);
			tokenService.deleteByUsernameAndTokenType(token.getUsername(), TokenType.EMAILVERIFICATION);
		}
	}

	public void forgotPassword(String username) {

		if (!isExistingUser(username))
			throw new UserException("user not exist");

		Token dbToken = tokenService.getTokenByUserNameAndTokenType(username, TokenType.PASSWORDRESET);

		if (dbToken != null && tokenService.isTokenValid(dbToken)) {

			mailService.sendMail(dbToken);
		} else {
			ApplicationUser appUser = userMongoRepository.findByEmail(username);
			Token token = tokenService.getPasswordResetToken(username);
			token.setUserId(appUser.getId());
			tokenService.save(token);
			mailService.sendMail(token);
		}

	}

	public void resetPassword(PasswordReset passwordReset) {
		Token dbToken = tokenService.getTokenByUserNameAndTokenType(passwordReset.getUsername(),
				TokenType.PASSWORDRESET);

		if (dbToken == null)
			throw new TokenException("no token found for password reset");

		tokenService.isTokenValid(dbToken);
		if (!passwordReset.getToken().equals(dbToken.getToken()))
			throw new TokenException("InvalidToken");

		ApplicationUser user = userMongoRepository.findByEmail(passwordReset.getUsername());

		user.setPassword(bCryptPasswordUtils.encodePassword(passwordReset.getPassword()));
		user.setAccountNonLocked(true);
		userMongoRepository.save(user);

		tokenService.deleteByUsernameAndTokenAndTokenType(passwordReset.getUsername(), passwordReset.getToken(),
				TokenType.PASSWORDRESET);
	}

	public String verifyIdToken(Authentication authentication, FirebaseToken decodedToken) {

		Optional<ApplicationUser> applicationUserOptional = userMongoRepository.findById(decodedToken.getUid());
		String token = null;
		if (applicationUserOptional.isPresent()) {

			// user exists. generate JWT
			token = jwtTokenProvider.generateToken(authentication);
		} else {
			// user not exist. create a new user with given uid
			// and generate JWT
			try {
				SignUpUser signUpUser = new SignUpUser(decodedToken.getEmail(), "welcome");

				signUp(signUpUser);

				token = jwtTokenProvider.generateToken(authentication);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return token;
	}

	public ApplicationUser getApplicationUser(String id) {

		Optional<ApplicationUser> applicationUserOptional = userMongoRepository.findById(id);
		ApplicationUser applicationUser = new ApplicationUser();
		if (applicationUserOptional.isPresent())
			applicationUser = applicationUserOptional.get();
		return applicationUser;
	}

	public ApplicationUser save(ApplicationUser applicationUser) {

		return userMongoRepository.save(applicationUser);
	}

	public List<UserShort> getUserShortOfAdmins() {

		List<ApplicationUser> adminUsers = userMongoRepository.findByRole(UserRoles.ROLE_ADMIN);

		List<UserShort> adminUserShortLi = new ArrayList<>();

		for (ApplicationUser admin : adminUsers) {

			Optional<UserShort> adminUserShortOptional = userShortMongoRepository.findById(admin.getId());
			if (adminUserShortOptional.isPresent())
				adminUserShortLi.add(adminUserShortOptional.get());
		}

		return adminUserShortLi;
	}

	public UserShort getUserShort(String id) {
		Optional<UserShort> userShortOptional = userShortMongoRepository.findById(id);
		UserShort userShort = new UserShort();
		if (userShortOptional.isPresent())
			userShort = userShortOptional.get();
		return userShort;
	}

	public ApplicationUser getUserByEmail(String email) {
		return userMongoRepository.findByEmail(email);
	}
}
