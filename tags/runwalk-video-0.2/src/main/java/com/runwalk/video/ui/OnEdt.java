package com.runwalk.video.ui;

import java.awt.EventQueue;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.swing.SwingUtilities;

/**
 * This marker annotation will run the code contained in the annotated method on the Event Dispatching Thread asynchronously.
 * Therefore it will wrap it in a {@link Runnable} and post it on the {@link EventQueue} using {@link SwingUtilities#invokeLater(Runnable)}.
 * 
 * @author Jeroen Peelaerts
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnEdt {

}
