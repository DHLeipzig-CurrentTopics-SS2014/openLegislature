package org.openlegislature.db.models;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
@Entity
@Table(name="speakers")
public class Speaker implements Serializable {
	/**
	 * ID
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique=true, nullable=false)
	private int id;

	/**
	 * name
	 */
	@Column(name="name", unique=true, nullable=false)
	private String name;

	/**
	 * parties
	 */
	@ManyToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinTable(name="speakers_parties", joinColumns={@JoinColumn(name="speaker", nullable=false, updatable=false)}, inverseJoinColumns={@JoinColumn(name="party", nullable=false, updatable=false)})
	private Set<Party> parties;

	/**
	 * parties
	 */
	@ManyToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JoinTable(name="speakers_public_offices", joinColumns={@JoinColumn(name="speaker", nullable=false, updatable=false)}, inverseJoinColumns={@JoinColumn(name="public_office", nullable=false, updatable=false)})
	private Set<PublicOffice> publicOffices;

	/**
	 * speeches
	 */
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="speaker")
	private Set<Speech> speeches;

	/**
	 * Default Constructor.
	 */
	public Speaker() {
		this.parties = new LinkedHashSet<>();
		this.publicOffices = new LinkedHashSet<>();
		this.speeches = new LinkedHashSet<>();
	}

	/**
	 * Creates a new speaker.
	 * @param name name
	 */
	public Speaker(String name) {
		this.name = name;
		this.parties = new LinkedHashSet<>();
		this.publicOffices = new LinkedHashSet<>();
		this.speeches = new LinkedHashSet<>();
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
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parties
	 */
	public Set<Party> getParties() {
		return this.parties;
	}

	/**
	 * @param parties the parties to set
	 */
	public void setParties(Set<Party> parties) {
		this.parties = parties;
	}

	/**
	 * @return the publicOffices
	 */
	public Set<PublicOffice> getPublicOffices() {
		return this.publicOffices;
	}

	/**
	 * @param publicOffices the publicOffices to set
	 */
	public void setPublicOffices(Set<PublicOffice> publicOffices) {
		this.publicOffices = publicOffices;
	}

	/**
	 * @return the speeches
	 */
	public Set<Speech> getSpeeches() {
		return this.speeches;
	}

	/**
	 * @param speeches the speeches to set
	 */
	public void setSpeeches(Set<Speech> speeches) {
		this.speeches = speeches;
	}

	@Override
	public String toString() {
		return this.name;
	}
}