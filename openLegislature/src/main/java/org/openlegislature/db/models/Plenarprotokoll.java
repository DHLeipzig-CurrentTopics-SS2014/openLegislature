package org.openlegislature.db.models;


import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author jnphilipp
 * @version 0.0.1
 */
@Entity
@Table(name="plenarprotokolls", uniqueConstraints=@UniqueConstraint(columnNames={"periode", "proceeding"}))
public class Plenarprotokoll implements Serializable {
	/**
	 * ID
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id", unique=true, nullable=false)
	private int id;

	/**
	 * election periode
	 */
	@Column(name="periode")
	private int periode;

	/**
	 * session number
	 */
	@Column(name="proceeding")
	private int proceeding;

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
	 * @return the periode
	 */
	public int getPeriode() {
		return this.periode;
	}

	/**
	 * @param periode the periode to set
	 */
	public void setPeriode(int periode) {
		this.periode = periode;
	}

	/**
	 * @return the proceeding
	 */
	public int getProceeding() {
		return this.proceeding;
	}

	/**
	 * @param proceeding the proceeding to set
	 */
	public void setProceeding(int proceeding) {
		this.proceeding = proceeding;
	}
}