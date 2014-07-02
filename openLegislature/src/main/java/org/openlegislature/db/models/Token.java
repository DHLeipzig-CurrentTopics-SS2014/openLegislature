package org.openlegislature.db.models;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
@Entity
@Table(name="tokens")
public class Token implements Serializable {
	/**
	 * ID
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique=true, nullable=false)
	private int id;

	/**
	 * token
	 */
	@Column(name="token", unique=true, nullable=false)
	private String token;

	/**
	 * Default Constructor.
	 */
	public Token() {}

	/**
	 * Creates a new token.
	 * @param token token
	 */
	public Token(String token) {
		this.token = token;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return this.token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public boolean equals(Object obj) {
		if ( obj != null && obj instanceof Token )
			if ( this.id == ((Token)obj).getId() || this.token.equals(((Token)obj).getToken()) )
				return true;
		
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 89 * hash + this.id;
		hash = 89 * hash + Objects.hashCode(this.token);
		return hash;
	}

	@Override
	public String toString() {
		return this.token;
	}
}