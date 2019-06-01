package com.piotics.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.piotics.model.Token;

import com.piotics.common.TokenType;

public interface TokenMongoRepository extends MongoRepository<Token, String> {
	
	Token findByUsernameAndTokenType(String Username, TokenType emailverification);

    Long deleteByUsernameAndTokenAndTokenType(String Username, String token, TokenType emailverification);

}
