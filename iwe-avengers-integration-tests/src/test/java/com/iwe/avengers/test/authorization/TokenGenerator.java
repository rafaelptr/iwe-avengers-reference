package com.iwe.avengers.test.authorization;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.ChallengeNameType;
import com.amazonaws.services.cognitoidp.model.InitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.InitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.RespondToAuthChallengeRequest;
import com.amazonaws.services.cognitoidp.model.RespondToAuthChallengeResult;

public class TokenGenerator {

	public String getToken() {
		
		String authresult = null;

		final String clientId = "494kpkmh3mk6rpp38c991l608m";
		final String userPoolId = "us-east-1_IjEeQEsVj";

		final AuthenticationHelper auth = new AuthenticationHelper(userPoolId, clientId);

		final InitiateAuthRequest initiateAuthRequest = new InitiateAuthRequest();
		initiateAuthRequest.setAuthFlow(AuthFlowType.USER_SRP_AUTH);
		initiateAuthRequest.setClientId(clientId);
		initiateAuthRequest.addAuthParametersEntry("USERNAME", "roger");

		//O conceito de algoritmos de chave pública é que você tem duas chaves, um público que está disponível para todos e um que é privado e conhecido apenas por você
		// nesse passo estamos passando uma chave pública gerada a partir de calculos e criptografia
		initiateAuthRequest.addAuthParametersEntry("SRP_A", auth.getA().toString(16));

		final AnonymousAWSCredentials awsCreds = new AnonymousAWSCredentials();
		final AWSCognitoIdentityProvider cognitoIdentityProvider = AWSCognitoIdentityProviderClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.US_EAST_1).build();
		
		final InitiateAuthResult initiateAuthResult = cognitoIdentityProvider.initiateAuth(initiateAuthRequest);

		if (ChallengeNameType.PASSWORD_VERIFIER.toString().equals(initiateAuthResult.getChallengeName())) {
			// Nesse passo estamos respondendo ao desafio do PASSWORD, e a senha será totalmente criptografada para o envio a AWS
			// Essa criptografia é realizada junto com um outro Hash SRP que é devolvido pela AWS como retorno da requisição
			RespondToAuthChallengeRequest challengeRequest = auth.userSrpAuthRequest(initiateAuthResult, "12345678");
			RespondToAuthChallengeResult result = cognitoIdentityProvider.respondToAuthChallenge(challengeRequest);
			authresult = result.getAuthenticationResult().getIdToken();
		}

		return authresult;
	}

}
