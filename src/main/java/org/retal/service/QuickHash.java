package org.retal.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;

public class QuickHash 
{
	public static void main(String[] args) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] pass = "admin111".getBytes(StandardCharsets.UTF_8);
		pass = md.digest(pass);
		String h2 = new String(Base64.getEncoder().encode(pass), StandardCharsets.UTF_8);
		System.out.println(h2);
	}
}
