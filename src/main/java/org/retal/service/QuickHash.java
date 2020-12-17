package org.retal.service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class QuickHash 
{
	public static void main(String[] args) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] pass = "manager".getBytes(StandardCharsets.ISO_8859_1);
		pass = md.digest(pass);
		String hash = new String(pass, StandardCharsets.ISO_8859_1);
		String h2 = new String(Base64.getEncoder().encode(pass), StandardCharsets.UTF_8);
		System.out.println(hash);
		System.out.println(h2);
		BigInteger h = new BigInteger(1, pass);
		System.out.println(h.toString());
	}
}
