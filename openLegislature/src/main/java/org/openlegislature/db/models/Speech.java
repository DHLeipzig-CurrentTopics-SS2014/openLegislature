package org.openlegislature.db.models;

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
@Table(name="speeches")
public class Speech {
	/**
	 * ID
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique=true, nullable=false)
	private int id;

	/**
	 * speech
	 */
	@Column(name="speech", nullable=false)
	private String speech;

	/**
	 * speaker
	 */
	@ManyToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinColumn(name="speaker", nullable=false)
	private Speaker speaker;

	/**
	 * Default Constructor.
	 */
	public Speech() {}

	/**
	 * Creates a new speech.
	 * @param speech speech
	 * @param speaker speaker
	 */
	public Speech(String speech, Speaker speaker) {
		this.speech = speech;
		this.speaker = speaker;
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
	 * @return the speech
	 */
	public String getSpeech() {
		return this.speech;
	}

	/**
	 * @param speech the speech to set
	 */
	public void setSpeech(String speech) {
		this.speech = speech;
	}

	/**
	 * @return the speaker
	 */
	public Speaker getSpeaker() {
		return this.speaker;
	}

	/**
	 * @param speaker the speaker to set
	 */
	public void setSpeaker(Speaker speaker) {
		this.speaker = speaker;
	}
}