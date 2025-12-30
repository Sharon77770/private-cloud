package com.sharon77770.private_cloud.utill;

import java.util.Date;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "jwt")
public class JWTManager {
    private String jwtkey;

    private long tokenValidTime = 1000L * 60 * 60 * 24; //24시간
	
	public String createToken(String userId) {
		try {
			Claims claims = Jwts.claims().setId(userId);
			Date now = new Date();
			return Jwts.builder().setClaims(claims).setIssuedAt(now)
					.setExpiration(new Date(now.getTime() + tokenValidTime))
					.setIssuer("sharon.cloud")
					.signWith(SignatureAlgorithm.HS256, jwtkey).compact();
		} 
        catch (Exception e) {
			throw e;
		}
	}
	
	public Jws<Claims> getClaims(String jwt) {
		try {
			return Jwts.parser().setSigningKey(jwtkey).parseClaimsJws(jwt);
		}
        catch (Exception e) {
			return null;
		}
	}
	
	public boolean isEnd(Jws<Claims> claims) {
		return claims.getBody().getExpiration().before(new Date());
	}

	public String getId(Jws<Claims> claims) {
		return claims.getBody().getId();
	}

	public void setJwtkey(String jwtkey) {
		this.jwtkey = jwtkey;
	}

	public String getJwtkey() {
		return jwtkey;
	}

    public Cookie createCookie(String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);   
        cookie.setSecure(false);    
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 1일
        return cookie;
    }
}
