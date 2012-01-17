package com.runwalk.video.ui;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Property;

import com.google.common.base.Preconditions;
import com.runwalk.video.core.PropertyChangeSupport;

/**
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/
 * or send a letter to Creative Commons, 171 Second Street,
 * Suite 300, San Francisco, California, 94105, USA.
 *
 * @author Peter Levart
 */
@SuppressWarnings("serial")
public class EnumButtonGroup<E extends Enum<E>> extends ButtonGroup implements PropertyChangeSupport
{
    public static final String SELECTED_ENUM = "selectedEnum";

	public static final Property<EnumButtonGroup<? extends Enum<?>>, ? extends Enum<?>> SELECTED_ENUM_PROPERTY = BeanProperty.create(SELECTED_ENUM);
	
    private Map<E, AbstractButton> enum2button;
    private Map<ButtonModel, E> buttonModel2enum = new HashMap<ButtonModel, E>();
    private Class<E> enumType;

    public EnumButtonGroup(Class<E> enumType)
    {
        this.enumType = Preconditions.checkNotNull(enumType);
        enum2button = new EnumMap<E, AbstractButton>(enumType);
    }

    public void add(E e, AbstractButton b)
    {
        Preconditions.checkNotNull(e);
        Preconditions.checkNotNull(b);

        if (enum2button.containsKey(e))
            throw new IllegalStateException("Enum value: " + e + " is already associated with this button group");

        if (buttonModel2enum.containsKey(b.getModel()))
            throw new IllegalStateException("Given button is already part of this button group");

        enum2button.put(e, b);
        buttonModel2enum.put(b.getModel(), e);

        super.add(b);
    }

    public AbstractButton remove(E e)
    {
        Preconditions.checkNotNull(e);

        AbstractButton b = enum2button.get(e);
        if (b == null)
            return null;

        super.remove(b);

        enum2button.remove(e);
        buttonModel2enum.remove(b.getModel());

        return b;
    }

    public void assertButtonGroupCoversAllEnumConstants()
    {
        Set<E> fullSet = EnumSet.allOf(enumType);
        if (fullSet.size() != enum2button.size())
            throw new AssertionError("ButtonGroup only has the following enum constants associated: " + enum2button.keySet() + " but it was asserted that it covers all of them: " + fullSet);
    }

    @Deprecated
    @Override
    public void add(AbstractButton b)
    {
        throw new UnsupportedOperationException("use add(E, AbstractButton) instead");
    }

    @Deprecated
    @Override
    public void remove(AbstractButton b)
    {
        throw new UnsupportedOperationException("use remove(E) instead");
    }

    @Override
    public void setSelected(ButtonModel m, boolean b)
    {
        ButtonModel oldSelection = getSelection();
        super.setSelected(m, b);
        fireSelectedEnumPropertyChange(oldSelection, getSelection());
    }

    @Override
    public void clearSelection()
    {
        ButtonModel oldSelection = getSelection();
        super.clearSelection();
        fireSelectedEnumPropertyChange(oldSelection, getSelection());
    }

    private void fireSelectedEnumPropertyChange(ButtonModel oldSelection, ButtonModel newSelection)
    {
        firePropertyChange(SELECTED_ENUM, buttonModel2enum.get(oldSelection), buttonModel2enum.get(newSelection));
    }
    
    public E getSelectedEnum()
    {
        return buttonModel2enum.get(getSelection());
    }

    public void setSelectedEnum(E e)
    {
        AbstractButton b = enum2button.get(e);
        if (b == null)
            clearSelection();
        else
            setSelected(b.getModel(), true);
    }
}
