package com.nnvmso.lib;

import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.google.appengine.repackaged.com.google.common.util.Base64;

public class AmazonLib {
	
	protected static final Logger logger = Logger.getLogger(AmazonLib.class.getName());
	
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	private static final String AWS_KEY = "fdXkONXak8YC8TylX7fVSte5nvhFJ0RB7KnXGhpl";
	public static final String AWS_ID = "AKIAIUZXV6X5RKSG3QRQ";
	
	/**
	* Computes RFC 2104-compliant HMAC signature.
	* * @param data
	* The data to be signed.
	* @return
	* The Base64-encoded RFC 2104-compliant HMAC signature.
	* @throws
	* java.security.SignatureException when signature generation fails
	*/
	public static String calculateRFC2104HMAC(String data) throws java.security.SignatureException {
		
		String result = null;
		
		try {
			
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(AWS_KEY.getBytes(), HMAC_SHA1_ALGORITHM);
			
			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			
			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());
			
			// base64-encode the hmac
			result = Base64.encode(rawHmac);
			
		} catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}
		return result;
	}
	
	public static String buildS3Policy(String buket, String acl, String contentType) {
		
		String result = "";
		result += "{ 'expiration': '" + AmazonLib.getFormattedExpirationDate() + "',";
		result += "'conditions': [";
		result += "{ 'bucket': '" + buket + "' },";
		result += "[ 'starts-with', '$key', ''],";
		result += "{ 'acl': '" + acl + "' },";
		result += "[ 'starts-with', '$Content-Type', '" + contentType + "' ],";
		result += "{ 'success_action_status': '201' },";
		result += "[ 'starts-with', '$Filename', '' ],";
		result += "[ 'content-length-range', 0, 50000000 ],";
		result += "]";
		result += "}";
		
		logger.info(result);
		return Base64.encode(result.getBytes());
	}
	public static String getFormattedExpirationDate() {
		Date now = new Date();
		Date oneHourFromNow = new Date(now.getTime() + 3600 * 1000);
		TimeZone tz = TimeZone.getTimeZone( "UTC" );
		SimpleDateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		dfm.setTimeZone(tz);
		String formattedExpirationDate = dfm.format(oneHourFromNow);
		return formattedExpirationDate;
	}
}
