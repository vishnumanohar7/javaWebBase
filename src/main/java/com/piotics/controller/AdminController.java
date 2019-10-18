package com.piotics.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.piotics.model.ApplicationUser;
import com.piotics.model.Invitation;
import com.piotics.resources.StringResource;
import com.piotics.service.AdminService;

@RestController
@RequestMapping(value = "/admin")
public class AdminController {

	@Autowired
	AdminService adminService;

//	@RequestMapping(value = "/invite", method = RequestMethod.POST)
//	public ResponseEntity<Invitation> invite(Principal principal, @RequestBody Invitation invitation) throws Exception {
//
//		ApplicationUser applicationUser = (ApplicationUser) ((Authentication) (principal)).getPrincipal();
//
//		invitation = adminService.invite(applicationUser, invitation);
//
//		return new ResponseEntity<Invitation>(invitation, HttpStatus.OK);
//	}
	
	@RequestMapping(value = "/invite", method = RequestMethod.POST)
	@PreAuthorize("@AccessFilter.isAdmin(authentication)")
	public ResponseEntity<StringResource> invite(Principal principal, @RequestBody StringResource invitationLi ) throws Exception {

		ApplicationUser applicationUser = (ApplicationUser) ((Authentication) (principal)).getPrincipal();
		StringResource failedList = adminService.senInvite(applicationUser, invitationLi);

		return new ResponseEntity<StringResource>(failedList, HttpStatus.OK);
	}

}
