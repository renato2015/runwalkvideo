package com.runwalk.video.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@SuppressWarnings("serial")
@Table(schema="testdb", name="keyframes")
public class Keyframe extends SerializableEntity<Keyframe> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@SuppressWarnings("unused")
	@ManyToOne
	@JoinColumn(name="movie_id", nullable=false)
	private Recording movie; 

	@Column(name="position")
	private int position;

	protected Keyframe() { }

	public Keyframe(Recording movie, int position) {
		super();
		this.movie = movie;
		this.position = position;
	}

	public Long getId() {
		return id;
	}

	public Integer getPosition() {
		return position;
	}

	public int compareTo(Keyframe frame) {
		return getPosition().compareTo(frame.getPosition());
	}

}
