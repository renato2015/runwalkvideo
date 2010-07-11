package com.runwalk.video.test.entities;

import org.jdesktop.application.AbstractBean;

public class Recording extends AbstractBean implements Comparable<Recording> {
	
	private static final String DURATION = "duration";

	public static final String VIDEO_CONTAINER_FORMAT = ".avi";
	
	private Long id;
	
	private Analysis analysis;

	private String videoFileName;

	private long duration;

	private Long lastModified;

	private boolean compressable, unCompressed, compressed;
	
	protected Recording() { }

	/**
	 *  1. Vanaf het moment dat je de filename hebt, zou je ook een link moeten hebben naar een Movie object.
	 *  2. statuscode is eigenlijk ook een veld van Movie object..
	 *  3. alle calls gerelateerd naar toestand van het bestand zou je naar de Recording entity moeten sturen (delegeren)
	 *  
	 *  TODO _Alle_ spaties in de bestandsnaam zouden naar een _ moeten geconverteerd worden.
	 *  
	 */
	public Recording(Analysis analysis) {
		Client client = analysis.getClient();
		int analysisCount = client.getAnalysesCount();
		String prefix = analysisCount == 0 ? "" : analysisCount + "_";
		this.videoFileName = prefix + client.getName() + "_" + client.getFirstname() + 
		"_" + analysis.getCreationDate().toString() + Recording.VIDEO_CONTAINER_FORMAT;
		this.analysis = analysis;
	}

	public Long getId() {
		return id;
	}
	
	public Analysis getAnalysis() {
		return analysis;
	}
	
	public void setDuration(long duration) {
		this.firePropertyChange(DURATION, this.duration, this.duration = duration);
	}
	
	
	public String getVideoFileName() {
		return videoFileName;
	}

	public boolean isCompressable() {
		return compressable;
	}
	
	public boolean isUncompressed() {
		return unCompressed;
	}

	public boolean isCompressed() {
		return compressed;
	}
	
	public void setCompressable(boolean compressable) {
		this.compressable = compressable;
	}

	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}

	public boolean isRecorded() {
		return isUncompressed() || isCompressed();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getVideoFileName() == null) ? 0 : getVideoFileName().hashCode());
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass() == obj.getClass()) {
			Recording other = (Recording) obj;
			result = getVideoFileName() != null ? getVideoFileName().equals(other.getVideoFileName()) : other.getVideoFileName() == null;
			result &= getId() != null ? getId().equals(other.getId()) : result;
		}
		return result;
	}

	public int compareTo(Recording o) {
		return this.equals(o) ? 0 : lastModified.compareTo(o.lastModified);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + id + ", videoFileName=" + videoFileName + "]";
	}


}
