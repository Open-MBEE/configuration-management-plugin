package org.openmbee.plugin.cfgmgmt.utils;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.apache.commons.text.lookup.StringLookup;

import java.util.List;

public class DisplayNameLookup implements StringLookup {
    private final ApiDomain apiDomain;
    private Element element;
    private String originalName;

    public DisplayNameLookup(ApiDomain apiDomain) {
        this.apiDomain = apiDomain;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    @Override
    public String lookup(String key) {
        if(key == null) {
            return PluginConstant.EMPTY_STRING;
        }

        if(key.equals(PluginConstant.DISPLAY_NAME_STRING_PATTERN)) {
            return originalName != null ? originalName : key;
        } else if (element != null ) {
            String[] split = key.split(PluginConstant.DISPLAY_NAME_PAIR_SPLIT);
            if(split.length == 2) {
                List<String> value = apiDomain.getStereotypePropertyValueAsString(element, split[0], split[1]);
                return !value.isEmpty() ? value.get(0).trim() : key;
            }
        }
        return key;
    }
}
