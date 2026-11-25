package com.boi.headless.rest.vkyc.internal.token;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Issues and validates HMAC-SHA256 signed tokens.
 * Token format: base64url(payloadJson) + "." + base64url(signature)
 * Default TTL: 300 seconds (5 minutes).
 */
public class VkycTokenService {

	private static final Log _log = LogFactoryUtil.getLog(VkycTokenService.class);
	private static final String _HMAC_ALG = "HmacSHA256";

	private final byte[] _secret;
	private final long _ttlSeconds;

	public VkycTokenService(String secret, long ttlSeconds) {
		_secret = secret.getBytes(StandardCharsets.UTF_8);
		_ttlSeconds = ttlSeconds;
	}

	public long getTtlSeconds() {
		return _ttlSeconds;
	}

	public String issue(long userId, String screenName, String role) {
		long now = System.currentTimeMillis() / 1000L;
		long exp = now + _ttlSeconds;

		String payloadJson =
			"{\"sub\":" + userId +
				",\"sn\":\"" + _esc(screenName) + "\"" +
				",\"role\":\"" + _esc(role) + "\"" +
				",\"iat\":" + now +
				",\"exp\":" + exp + "}";

		String payloadB64 = Base64.getUrlEncoder().withoutPadding()
			.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

		return payloadB64 + "." + _sign(payloadB64);
	}

	public TokenPayload validate(String token) {
		if ((token == null) || token.isBlank()) {
			return null;
		}

		int dot = token.indexOf('.');

		if ((dot <= 0) || (dot == (token.length() - 1))) {
			return null;
		}

		String payloadB64 = token.substring(0, dot);
		String providedSig = token.substring(dot + 1);
		String expectedSig = _sign(payloadB64);

		if (!MessageDigest.isEqual(
				expectedSig.getBytes(StandardCharsets.UTF_8),
				providedSig.getBytes(StandardCharsets.UTF_8))) {

			_log.warn("VKYC token signature mismatch");
			return null;
		}

		try {
			String json = new String(
				Base64.getUrlDecoder().decode(payloadB64),
				StandardCharsets.UTF_8);

			Map<String, String> fields = _parseJson(json);
			long exp = Long.parseLong(fields.getOrDefault("exp", "0"));

			if ((System.currentTimeMillis() / 1000L) > exp) {
				_log.warn("VKYC token expired");
				return null;
			}

			TokenPayload payload = new TokenPayload();

			payload.userId = Long.parseLong(fields.getOrDefault("sub", "0"));
			payload.screenName = fields.get("sn");
			payload.role = fields.get("role");
			payload.expiresAt = exp;

			return payload;
		}
		catch (Exception e) {
			_log.error("VKYC token parse failed", e);
			return null;
		}
	}

	private String _sign(String data) {
		try {
			Mac mac = Mac.getInstance(_HMAC_ALG);

			mac.init(new SecretKeySpec(_secret, _HMAC_ALG));

			byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

			return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
		}
		catch (Exception e) {
			throw new IllegalStateException("HMAC init failed", e);
		}
	}

	private String _esc(String s) {
		if (s == null) {
			return "";
		}

		return s.replace("\"", "\\\"");
	}

	private Map<String, String> _parseJson(String json) {
		Map<String, String> map = new HashMap<>();

		String content = json.trim();

		if (content.startsWith("{")) {
			content = content.substring(1);
		}

		if (content.endsWith("}")) {
			content = content.substring(0, content.length() - 1);
		}

		for (String pair : content.split(",")) {
			int colon = pair.indexOf(':');

			if (colon < 0) {
				continue;
			}

			String key = pair.substring(0, colon).trim().replace("\"", "");
			String value = pair.substring(colon + 1).trim().replace("\"", "");

			map.put(key, value);
		}

		return map;
	}

	public static class TokenPayload {

		public long userId;
		public String screenName;
		public String role;
		public long expiresAt;

	}
}