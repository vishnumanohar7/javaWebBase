package com.piotics.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.piotics.model.Activity;
import com.piotics.model.ApplicationUser;
import com.piotics.model.Post;
import com.piotics.model.Session;
import com.piotics.service.PostService;

@RestController
@RequestMapping(value = "/post")
public class PostController {

	@Autowired
	PostService postService;

	@PostMapping(value = "/create")
	public ResponseEntity<Activity> createPost(Principal principal, @RequestBody Post post) {

		Session session = (Session) ((Authentication) (principal)).getPrincipal();
		Activity activity = postService.createPost(session, post);

		return new ResponseEntity<>(activity, HttpStatus.OK);
	}

	@DeleteMapping(value = "/delete/{id}")
	@PreAuthorize("@AccessFilter.hasAccessToDeletePost(authentication,#id)")
	public ResponseEntity<HttpStatus> deletePost(@PathVariable String id) {

		postService.deletePost(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/edit")
	@PreAuthorize("@AccessFilter.hasAccessToEditPost(authentication,#post.id)")
	public ResponseEntity<Post> editPost(@RequestBody Post post) {

		post = postService.editPost(post);
		return new ResponseEntity<>(post, HttpStatus.OK);
	}
}
