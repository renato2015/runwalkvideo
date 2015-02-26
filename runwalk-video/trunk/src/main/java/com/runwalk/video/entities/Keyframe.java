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
@Table(name="keyframes")
public class Keyframe extends SerializableEntity<Keyframe> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name="recording_id", nullable=false)
	private Recording recording; 

	@Column(name="position")
	private int position;

	protected Keyframe() { }

	public Keyframe(Recording movie, int position) {
		this.recording = movie;
		this.position = position;
	}

	public Long getId() {
		return id;
	}

	public Integer getPosition() {
		return position;
	}
	
	public Recording getRecording() {
		return recording;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getRecording() == null) ? 0 : getRecording().hashCode());
		result = prime * result + getPosition();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass() == obj.getClass()) {
			Keyframe other = (Keyframe) obj;
			result = getPosition().equals(other.getPosition());
			result &= getRecording() != null ? getRecording().equals(other.getRecording()) : other.getRecording() == null;
		}
		return result;
	}
	
	public int compareTo(Keyframe frame) {
		return getPosition().compareTo(frame.getPosition());
	}

}
