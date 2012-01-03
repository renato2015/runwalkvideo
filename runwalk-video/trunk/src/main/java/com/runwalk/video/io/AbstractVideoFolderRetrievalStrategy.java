package com.runwalk.video.io;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * This class is only used for persisting the chosen strategy using JaxB, which does not support mapping interfaces by default.
 * Every marshallable strategy should provide a default no-args constructor. This will be enforced by declaring
 * the default constructor in this superclass. 
 * 
 * Don't forget to add new extended classes to the {@link XmlSeeAlso#value()} method here, so JaxB can register it
 * in its context.
 * 
 * @author Jeroen Peelaerts
 */
@XmlSeeAlso(value={DateVideoFolderRetrievalStrategy.class, DefaultVideoFolderRetrievalStrategy.class})
public abstract class AbstractVideoFolderRetrievalStrategy implements VideoFolderRetrievalStrategy {

	protected AbstractVideoFolderRetrievalStrategy() {}
	
	public static class Adapter extends XmlAdapter<AbstractVideoFolderRetrievalStrategy, VideoFolderRetrievalStrategy> {

		public AbstractVideoFolderRetrievalStrategy marshal(VideoFolderRetrievalStrategy v) throws Exception {
			return (AbstractVideoFolderRetrievalStrategy) v;
		}

		public VideoFolderRetrievalStrategy unmarshal(AbstractVideoFolderRetrievalStrategy v) throws Exception {
			return v;
		}
		
	}
	
}
