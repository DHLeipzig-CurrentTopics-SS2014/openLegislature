package org.openlegislature.db.models;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
@Entity
@Table(name="speech_cooccurrences")
public class SpeechCooccurrence implements Serializable {
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
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="token", nullable=false)
	private Token token;

	/**
	 * cooccurrence
	 */
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="cooccurrence", nullable=false)
	private Token cooccurrence;

	/**
	 * count
	 */
	@Column(name="count")
	private int count;

	/**
	 * speech
	 */
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="speech", nullable=false)
	private Speech speech;

	/**
	 * Default Constructor.
	 */
	public SpeechCooccurrence() {}

	/**
	 * Creates a new speech cooccurrence. Sets count to 1.
	 * @param token token
	 * @param cooccurrence cooccurrence
	 * @param speech speech
	 */
	public SpeechCooccurrence(Token token, Token cooccurrence, Speech speech) {
		this.token = token;
		this.cooccurrence = cooccurrence;
		this.speech = speech;
		this.count = 1;
	}

	/**
	 * Creates a new speech cooccurrence.
	 * @param token token
	 * @param cooccurrence cooccurrence
	 * @param count count
	 * @param speech speech
	 */
	public SpeechCooccurrence(Token token, Token cooccurrence, int count, Speech speech) {
		this.token = token;
		this.cooccurrence = cooccurrence;
		this.speech = speech;
		this.count = count;
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
	public Token getToken() {
		return this.token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(Token token) {
		this.token = token;
	}

	/**
	 * @return the cooccurrence
	 */
	public Token getCooccurrence() {
		return this.cooccurrence;
	}

	/**
	 * @param cooccurrence the cooccurrence to set
	 */
	public void setCooccurrence(Token cooccurrence) {
		this.cooccurrence = cooccurrence;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return this.count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the speech
	 */
	public Speech getSpeech() {
		return this.speech;
	}

	/**
	 * @param speech the speech to set
	 */
	public void setSpeech(Speech speech) {
		this.speech = speech;
	}
}