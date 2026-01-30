package usr.skyswimmer.githubwebhooks.tokens;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.UUID;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * Session Token Utility
 * 
 * @author Sky Swimmer
 *
 */
public class SessionToken {

	public String rawTokenString;

	public JsonObject header = new JsonObject();
	public JsonObject payload = new JsonObject();

	public String issuer;
	public String tokenId = UUID.randomUUID().toString();

	public long issuedAt = 0;
	public long expiry = (System.currentTimeMillis() + 10 * 60 * 1000) / 1000;

	public SessionToken stamp() {
		issuedAt = System.currentTimeMillis() / 1000;
		return this;
	}

	/**
	 * Parses tokens
	 * 
	 * @param token Token to parse
	 * @return TokenParseResult value
	 */
	public TokenParseResult parseToken(String token, String expectedIssuer, PublicKey publicKey) {
		try {
			// Parse header
			if (token.split("\\.").length != 3)
				return TokenParseResult.INVALID_DATA;
			header = JsonParser.parseString(new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]), "UTF-8"))
					.getAsJsonObject();
			if (!header.get("typ").getAsString().equalsIgnoreCase("jwt"))
				return TokenParseResult.INVALID_DATA;
			if (!header.get("alg").getAsString().equalsIgnoreCase("rs256"))
				return TokenParseResult.INVALID_DATA;

			// Verify signature
			String verifyD = token.split("\\.")[0] + "." + token.split("\\.")[1];
			String sig = token.split("\\.")[2];
			if (!verify(verifyD.getBytes("UTF-8"), Base64.getUrlDecoder().decode(sig), publicKey)) {
				return TokenParseResult.INVALID_DATA;
			}

			// Parse payload
			payload = JsonParser.parseString(new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), "UTF-8"))
					.getAsJsonObject();
			if (!payload.has("iat") || !payload.has("jti") || !payload.has("exp") || !payload.has("iss")
					|| (expectedIssuer != null && !payload.get("iss").getAsString().equals(expectedIssuer)))
				return TokenParseResult.INVALID_DATA;
			issuedAt = payload.get("iat").getAsLong();
			expiry = payload.get("exp").getAsLong();
			issuer = payload.get("iss").getAsString();
			tokenId = payload.get("jti").getAsString();
			rawTokenString = token;

			// Verify expiry
			if (payload.get("exp").getAsLong() * 1000 <= System.currentTimeMillis()) {
				// Token has expired as its too long ago that it was refreshed
				return TokenParseResult.TOKEN_EXPIRED;
			}

			// Success!
			return TokenParseResult.SUCCESS;
		} catch (IOException e) {
			return TokenParseResult.INVALID_DATA;
		}
	}

	/**
	 * Converts the token to a string
	 * 
	 * @return Token string
	 */
	public String toTokenString(PrivateKey privateKey) {
		try {
			// Build header
			JsonObject headers = this.header.deepCopy();
			headers.addProperty("alg", "RS256");
			headers.addProperty("typ", "JWT");
			String headerD = Base64.getUrlEncoder().withoutPadding()
					.encodeToString(headers.toString().getBytes("UTF-8"));

			// Build payload
			JsonObject payload = this.payload.deepCopy();
			payload.addProperty("iat", issuedAt);
			payload.addProperty("jti", tokenId);
			payload.addProperty("iss", issuer);
			payload.addProperty("exp", expiry);

			// Build
			String payloadD = Base64.getUrlEncoder().withoutPadding()
					.encodeToString(payload.toString().getBytes("UTF-8"));

			// Sign
			String token = headerD + "." + payloadD;
			String sig = Base64.getUrlEncoder().withoutPadding()
					.encodeToString(sign(token.getBytes("UTF-8"), privateKey));
			token = token + "." + sig;
			return token;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// Signature generator
	public static byte[] sign(byte[] data, PrivateKey privateKey) {
		try {
			Signature sig = Signature.getInstance("Sha256WithRSA");
			sig.initSign(privateKey);
			sig.update(data);
			return sig.sign();
		} catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}

	// Signature verification
	public static boolean verify(byte[] data, byte[] signature, PublicKey publicKey) {
		try {
			Signature sig = Signature.getInstance("Sha256WithRSA");
			sig.initVerify(publicKey);
			sig.update(data);
			return sig.verify(signature);
		} catch (SignatureException | NoSuchAlgorithmException | InvalidKeyException e) {
			return false;
		}
	}

}
