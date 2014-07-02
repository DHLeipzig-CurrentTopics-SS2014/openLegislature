package org.openlegislature.db.models;

import java.io.Serializable;
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
@Table(name="public_offices")
public class PublicOffice implements Serializable {
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
	 * Default Constructor.
	 */
	public PublicOffice() {}

	/**
	 * Creates a new public office.
	 * @param name name
	 */
	public PublicOffice(String name) {
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
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

	@Override
	public String toString() {
		return this.name;
	}
}