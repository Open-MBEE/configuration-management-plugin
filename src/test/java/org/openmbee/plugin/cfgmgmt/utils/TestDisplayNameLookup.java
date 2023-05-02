package org.openmbee.plugin.cfgmgmt.utils;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TestDisplayNameLookup {
    private DisplayNameLookup displayNameLookup;
    private ApiDomain apiDomain;
    private Element element;
    private String originalName;
    private String key;

    @Before
    public void setup() {
        apiDomain = mock(ApiDomain.class);
        displayNameLookup = spy(new DisplayNameLookup(apiDomain));
        element = mock(Element.class);
        originalName = "originalName";
        key = "s1.p1";

        displayNameLookup.setElement(element);
        displayNameLookup.setOriginalName(originalName);
    }

    @Test
    public void lookup_nullKey() {
        assertEquals(PluginConstant.EMPTY_STRING, displayNameLookup.lookup(null));
    }

    @Test
    public void lookup_noOriginalName() {
        key = PluginConstant.DISPLAY_NAME_STRING_PATTERN;
        displayNameLookup.setOriginalName(null);

        assertEquals(key, displayNameLookup.lookup(key));
    }

    @Test
    public void lookup_fitsNameStringPattern() {
        key = PluginConstant.DISPLAY_NAME_STRING_PATTERN;

        assertEquals(originalName, displayNameLookup.lookup(key));
    }

    @Test
    public void lookup_noElement() {
        displayNameLookup.setElement(null);

        assertEquals(key, displayNameLookup.lookup(key));
    }

    @Test
    public void lookup_hasElementButWrongFormat() {
        key = "s.1.p.1";

        assertEquals(key, displayNameLookup.lookup(key));
    }

    @Test
    public void lookup_hasElementAndSplitFormat() {
        String stereotypeName = "s1";
        String propertyName = "p1";
        String value = "value1";
        List<String> values = new ArrayList<>();
        values.add(value);

        doReturn(values).when(apiDomain).getStereotypePropertyValueAsString(element, stereotypeName, propertyName);

        assertEquals(value, displayNameLookup.lookup(key));
    }
}
