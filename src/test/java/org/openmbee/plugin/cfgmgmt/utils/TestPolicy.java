package org.openmbee.plugin.cfgmgmt.utils;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.spy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;


public class TestPolicy {
    private Policy policy;
    private ApiDomain apiDomain;
    private Class element;

    @Before
    public void setup() {
        element = mock(Class.class);
        apiDomain = mock(ApiDomain.class);
        policy = Mockito.spy(new Policy(apiDomain,element));

        when(policy.getApiDomain()).thenReturn(apiDomain);
    }

    @Test
    public void getRoles(){
        List<String> rolesList = new ArrayList<>();
        String role = "admin";
        rolesList.add(role);

        when(apiDomain.getStereotypePropertyValueAsString(element, PluginConstant.POLICY_STEREOTYPE, PluginConstant.ROLE)).thenReturn(rolesList);
        try{
            assertTrue(policy.getRoles().contains(role));
            verify(apiDomain).getStereotypePropertyValueAsString(element, PluginConstant.POLICY_STEREOTYPE, PluginConstant.ROLE);
        }catch(Exception exception){
            fail("Unexpected exception");
        }
    }

    @Test
    public void getRoles_Null(){
        when(apiDomain.getStereotypePropertyValueAsString(element, PluginConstant.POLICY_STEREOTYPE, PluginConstant.ROLE)).thenReturn(null);
        try{
            assertTrue(policy.getRoles().isEmpty());
        }catch(Exception exception){
            fail("Unexpected exception");
        }
    }
}
