/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.slee.resource.diameter.cca.tests.factories;

import static org.junit.Assert.*;
import net.java.slee.resource.diameter.cca.events.CreditControlAnswer;
import net.java.slee.resource.diameter.cca.events.CreditControlRequest;
import net.java.slee.resource.diameter.cca.events.avp.CcMoneyAvp;
import net.java.slee.resource.diameter.cca.events.avp.CcUnitType;
import net.java.slee.resource.diameter.cca.events.avp.CostInformationAvp;
import net.java.slee.resource.diameter.cca.events.avp.FinalUnitActionType;
import net.java.slee.resource.diameter.cca.events.avp.FinalUnitIndicationAvp;
import net.java.slee.resource.diameter.cca.events.avp.GSUPoolReferenceAvp;
import net.java.slee.resource.diameter.cca.events.avp.GrantedServiceUnitAvp;
import net.java.slee.resource.diameter.cca.events.avp.MultipleServicesCreditControlAvp;
import net.java.slee.resource.diameter.cca.events.avp.RedirectAddressType;
import net.java.slee.resource.diameter.cca.events.avp.RedirectServerAvp;
import net.java.slee.resource.diameter.cca.events.avp.RequestedServiceUnitAvp;
import net.java.slee.resource.diameter.cca.events.avp.ServiceParameterInfoAvp;
import net.java.slee.resource.diameter.cca.events.avp.SubscriptionIdAvp;
import net.java.slee.resource.diameter.cca.events.avp.SubscriptionIdType;
import net.java.slee.resource.diameter.cca.events.avp.UnitValueAvp;
import net.java.slee.resource.diameter.cca.events.avp.UsedServiceUnitAvp;
import net.java.slee.resource.diameter.cca.events.avp.UserEquipmentInfoAvp;
import net.java.slee.resource.diameter.cca.events.avp.UserEquipmentInfoType;

import org.jdiameter.api.Stack;
import org.junit.Assert;
import org.junit.Test;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.mobicents.slee.resource.diameter.base.DiameterAvpFactoryImpl;
import org.mobicents.slee.resource.diameter.base.DiameterMessageFactoryImpl;
import org.mobicents.slee.resource.diameter.base.events.DiameterMessageImpl;
import org.mobicents.slee.resource.diameter.base.tests.factories.BaseFactoriesTest;
import org.mobicents.slee.resource.diameter.base.tests.factories.BaseFactoriesTest.MyConfiguration;
import org.mobicents.slee.resource.diameter.cca.CreditControlAVPFactoryImpl;
import org.mobicents.slee.resource.diameter.cca.CreditControlMessageFactoryImpl;

/**
 * Test class for JAIN SLEE Diameter CCA RA Message and AVP Factories
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class CCAFactoriesTest {

  private static CreditControlMessageFactoryImpl ccaMessageFactory;
  private static CreditControlAVPFactoryImpl ccaAvpFactory;

  static {
    Stack stack = new org.jdiameter.client.impl.StackImpl();
    try {
      stack.init(new MyConfiguration());
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to initialize the stack.");
    }

    DiameterMessageFactoryImpl baseFactory = new DiameterMessageFactoryImpl(stack);
    DiameterAvpFactoryImpl baseAvpFactory = new DiameterAvpFactoryImpl();

    ccaAvpFactory = new CreditControlAVPFactoryImpl(baseAvpFactory);
    try {
      ccaMessageFactory = new CreditControlMessageFactoryImpl(baseFactory, stack.getSessionFactory().getNewSession(), stack, ccaAvpFactory);
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    try {
      AvpDictionary.INSTANCE.parseDictionary(BaseFactoriesTest.class.getClassLoader().getResourceAsStream("dictionary.xml"));
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to parse dictionary file.");
    }
  }

  @Test
  public void isRequestCCR() throws Exception {
    CreditControlRequest ccr = ccaMessageFactory.createCreditControlRequest();
    assertTrue("Request Flag in Credit-Control-Request is not set.", ccr.getHeader().isRequest());
  }

  @Test
  public void isProxiableCCR() throws Exception {
    CreditControlRequest ccr = ccaMessageFactory.createCreditControlRequest();
    assertTrue("The 'P' bit is not set by default in Credit-Control-Request, it should.", ccr.getHeader().isProxiable());
  }

  @Test
  public void testGettersAndSettersCCR() throws Exception {
    CreditControlRequest ccr = ccaMessageFactory.createCreditControlRequest();

    int nFailures = CCAAvpAssistant.INSTANCE.testMethods(ccr, CreditControlRequest.class);

    assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
  }

  @Test
  public void isAnswerCCA() throws Exception {
    CreditControlRequest ccr = ccaMessageFactory.createCreditControlRequest();
    CreditControlAnswer cca = ccaMessageFactory.createCreditControlAnswer(ccr);
    assertFalse("Request Flag in Credit-Control-Answer is set.", cca.getHeader().isRequest());
  }

  @Test
  public void isProxiableCopiedCCA() throws Exception {
    CreditControlRequest ccr = ccaMessageFactory.createCreditControlRequest();
    CreditControlAnswer cca = ccaMessageFactory.createCreditControlAnswer(ccr);
    assertEquals("The 'P' bit is not copied from request in Credit-Control-Answer, it should. [RFC3588/6.2]", ccr.getHeader().isProxiable(), cca.getHeader().isProxiable());

    // Reverse 'P' bit ...
    ((DiameterMessageImpl) ccr).getGenericData().setProxiable(!ccr.getHeader().isProxiable());
    assertTrue("The 'P' bit was not modified in Credit-Control-Request, it should.", ccr.getHeader().isProxiable() != cca.getHeader().isProxiable());

    cca = ccaMessageFactory.createCreditControlAnswer(ccr);
    assertEquals("The 'P' bit is not copied from request in Credit-Control-Answer, it should. [RFC3588/6.2]", ccr.getHeader().isProxiable(), cca.getHeader().isProxiable());
  }

  @Test
  public void hasTFlagSetCCA() throws Exception {
    CreditControlRequest ccr = ccaMessageFactory.createCreditControlRequest();
    ((DiameterMessageImpl) ccr).getGenericData().setReTransmitted(true);

    assertTrue("The 'T' flag should be set in Credit-Control-Request", ccr.getHeader().isPotentiallyRetransmitted());

    CreditControlAnswer cca = ccaMessageFactory.createCreditControlAnswer(ccr);
    assertFalse("The 'T' flag should not be set in Credit-Control-Answer", cca.getHeader().isPotentiallyRetransmitted());
  }

  @Test
  public void testGettersAndSettersCCA() throws Exception {
    CreditControlAnswer cca = ccaMessageFactory.createCreditControlAnswer(ccaMessageFactory.createCreditControlRequest("582364567346578348"));

    int nFailures = CCAAvpAssistant.INSTANCE.testMethods(cca, CreditControlAnswer.class);

    assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
  }

  @Test
  public void hasDestinationHostCCA() throws Exception {
    CreditControlAnswer cca = ccaMessageFactory.createCreditControlAnswer(ccaMessageFactory.createCreditControlRequest("582364567346578348"));
    assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", cca.getDestinationHost());
  }

  @Test
  public void hasDestinationRealmCCA() throws Exception {
    CreditControlAnswer cca = ccaMessageFactory.createCreditControlAnswer(ccaMessageFactory.createCreditControlRequest("582364567346578348"));
    assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", cca.getDestinationRealm());
  }

  @Test
  public void testAvpFactoryCreateCcMoney() throws Exception {
    String avpName = "CC-Money";

    // Create AVP with mandatory values
    CcMoneyAvp ccmAvp1 = ccaAvpFactory.createCcMoney(ccaAvpFactory.createUnitValue(12345L));

    // Make sure it's not null
    Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", ccmAvp1);

    // Create AVP with default constructor
    CcMoneyAvp ccmAvp2 = ccaAvpFactory.createCcMoney();

    // Should not contain mandatory values
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Unit-Value AVP.", ccmAvp2.hasUnitValue());

    // Set mandatory values
    ccmAvp2.setUnitValue(ccaAvpFactory.createUnitValue(12345L));

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", ccmAvp1, ccmAvp2);

    // Make new copy
    ccmAvp2 = ccaAvpFactory.createCcMoney();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(ccmAvp2);

    // Create empty...
    CcMoneyAvp ccmAvp3 = ccaAvpFactory.createCcMoney();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(ccmAvp3, false);

    // Set all previous values
    ccmAvp3.setExtensionAvps(ccmAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(ccmAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(ccmAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", ccmAvp2, ccmAvp3);
  }

  @Test
  public void testAvpFactoryCreateCostInformation() throws Exception {
    String avpName = "Cost-Information";

    // Create AVP with mandatory values
    CostInformationAvp ciAvp1 = ccaAvpFactory.createCostInformation(ccaAvpFactory.createUnitValue(12345L), 3L);

    // Make sure it's not null
    Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", ciAvp1);

    // Create AVP with default constructor
    CostInformationAvp ciAvp2 = ccaAvpFactory.createCostInformation();

    // Should not contain mandatory values
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Unit-Value AVP.", ciAvp2.hasUnitValue());
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Currency-Code AVP.", ciAvp2.hasCurrencyCode());

    // Set mandatory values
    ciAvp2.setUnitValue(ccaAvpFactory.createUnitValue(12345L));
    ciAvp2.setCurrencyCode(3L);

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", ciAvp1, ciAvp2);

    // Make new copy
    ciAvp2 = ccaAvpFactory.createCostInformation();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(ciAvp2);

    // Create empty...
    CostInformationAvp ciAvp3 = ccaAvpFactory.createCostInformation();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(ciAvp3, false);

    // Set all previous values
    ciAvp3.setExtensionAvps(ciAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(ciAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(ciAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", ciAvp2, ciAvp3);
  }

  @Test
  public void testAvpFactoryCreateFinalUnitIndication() throws Exception {
    String avpName = "Final-Unit-Indication";

    // Create AVP with mandatory values
    FinalUnitIndicationAvp fuiAvp1 = ccaAvpFactory.createFinalUnitIndication(FinalUnitActionType.RESTRICT_ACCESS);

    // Make sure it's not null
    Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", fuiAvp1);

    // Create AVP with default constructor
    FinalUnitIndicationAvp fuiAvp2 = ccaAvpFactory.createFinalUnitIndication();

    // Should not contain mandatory values
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Final-Unit-Action AVP.", fuiAvp2.hasFinalUnitAction());

    // Set mandatory values
    fuiAvp2.setFinalUnitAction(FinalUnitActionType.RESTRICT_ACCESS);

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", fuiAvp1, fuiAvp2);

    // Make new copy
    fuiAvp2 = ccaAvpFactory.createFinalUnitIndication();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(fuiAvp2);

    // Create empty...
    FinalUnitIndicationAvp fuiAvp3 = ccaAvpFactory.createFinalUnitIndication();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(fuiAvp3, false);

    // Set all previous values
    fuiAvp3.setExtensionAvps(fuiAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(fuiAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(fuiAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", fuiAvp2, fuiAvp3);
  }

  @Test
  public void testAvpFactoryCreateGSUPoolReference() throws Exception {
    String avpName = "G-S-U-Pool-Reference";

    // Create AVP with mandatory values
    GSUPoolReferenceAvp gsuprAvp1 = ccaAvpFactory.createGSUPoolReference(12345L, CcUnitType.TIME, ccaAvpFactory.createUnitValue(67890L));

    // Make sure it's not null
    Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", gsuprAvp1);

    // Create AVP with default constructor
    GSUPoolReferenceAvp gsuprAvp2 = ccaAvpFactory.createGSUPoolReference();

    // Should not contain mandatory values
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have G-S-U-Pool-Identifier AVP.", gsuprAvp2.hasGSUPoolIdentifier());
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have CC-Unit-Type AVP.", gsuprAvp2.hasCreditControlUnitType());
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Unit-Value AVP.", gsuprAvp2.hasUnitValue());

    // Set mandatory values
    gsuprAvp2.setGSUPoolIdentifier(12345L);
    gsuprAvp2.setCreditControlUnitType(CcUnitType.TIME);
    gsuprAvp2.setUnitValue(ccaAvpFactory.createUnitValue(67890L));

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", gsuprAvp1, gsuprAvp2);

    // Make new copy
    gsuprAvp2 = ccaAvpFactory.createGSUPoolReference();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(gsuprAvp2);

    // Create empty...
    GSUPoolReferenceAvp gsuprAvp3 = ccaAvpFactory.createGSUPoolReference();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(gsuprAvp3, false);

    // Set all previous values
    gsuprAvp3.setExtensionAvps(gsuprAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(gsuprAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(gsuprAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", gsuprAvp2, gsuprAvp3);
  }

  @Test
  public void testAvpFactoryCreateGrantedServiceUnit() throws Exception {
    String avpName = "Granted-Service-Unit";

    // Create AVP with mandatory values
    GrantedServiceUnitAvp gsuAvp1 = ccaAvpFactory.createGrantedServiceUnit();

    // Make sure it's not null
    Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", gsuAvp1);

    // Create AVP with default constructor
    GrantedServiceUnitAvp gsuAvp2 = ccaAvpFactory.createGrantedServiceUnit();

    // Should not contain mandatory values

    // Set mandatory values

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", gsuAvp1, gsuAvp2);

    // Make new copy
    gsuAvp2 = ccaAvpFactory.createGrantedServiceUnit();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(gsuAvp2);

    // Create empty...
    GrantedServiceUnitAvp gsuAvp3 = ccaAvpFactory.createGrantedServiceUnit();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(gsuAvp3, false);

    // Set all previous values
    gsuAvp3.setExtensionAvps(gsuAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(gsuAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(gsuAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", gsuAvp2, gsuAvp3);
  }

  @Test
  public void testAvpFactoryCreateMultipleServicesCreditControl() throws Exception {
    String avpName = "Multiple-Services-Credit-Control";

    // Create AVP with mandatory values
    MultipleServicesCreditControlAvp msccAvp1 = ccaAvpFactory.createMultipleServicesCreditControl();

    // Make sure it's not null
    Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", msccAvp1);

    // Create AVP with default constructor
    MultipleServicesCreditControlAvp msccAvp2 = ccaAvpFactory.createMultipleServicesCreditControl();

    // Should not contain mandatory values

    // Set mandatory values

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", msccAvp1, msccAvp2);

    // Make new copy
    msccAvp2 = ccaAvpFactory.createMultipleServicesCreditControl();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(msccAvp2);

    // Create empty...
    MultipleServicesCreditControlAvp msccAvp3 = ccaAvpFactory.createMultipleServicesCreditControl();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(msccAvp3, false);

    // Set all previous values
    msccAvp3.setExtensionAvps(msccAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(msccAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(msccAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", msccAvp2, msccAvp3);
  }

  @Test
  public void testAvpFactoryCreateRedirectServer() throws Exception {
    String avpName = "Redirect-Server";

    // Create AVP with mandatory values
    RedirectServerAvp rsAvp1 = ccaAvpFactory.createRedirectServer(RedirectAddressType.IPv4_Address, "127.0.0.3");

    // Make sure it's not null
    Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", rsAvp1);

    // Create AVP with default constructor
    RedirectServerAvp rsAvp2 = ccaAvpFactory.createRedirectServer();

    // Should not contain mandatory values
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Redirect-Address-Type AVP.", rsAvp2.hasRedirectAddressType());
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Redirect-Server-Address AVP.", rsAvp2.hasRedirectServerAddress());

    // Set mandatory values
    rsAvp2.setRedirectAddressType(RedirectAddressType.IPv4_Address);
    rsAvp2.setRedirectServerAddress("127.0.0.3");

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", rsAvp1, rsAvp2);

    // Make new copy
    rsAvp2 = ccaAvpFactory.createRedirectServer();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(rsAvp2);

    // Create empty...
    RedirectServerAvp rsAvp3 = ccaAvpFactory.createRedirectServer();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(rsAvp3, false);

    // Set all previous values
    rsAvp3.setExtensionAvps(rsAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(rsAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(rsAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", rsAvp2, rsAvp3);
  }

  @Test
  public void testAvpFactoryCreateRequestedServiceUnit() throws Exception {
    String avpName = "Requested-Service-Unit";

    // Create AVP with mandatory values
    RequestedServiceUnitAvp rsuAvp1 = ccaAvpFactory.createRequestedServiceUnit();

    // Make sure it's not null
    Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", rsuAvp1);

    // Create AVP with default constructor
    RequestedServiceUnitAvp rsuAvp2 = ccaAvpFactory.createRequestedServiceUnit();

    // Should not contain mandatory values

    // Set mandatory values

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", rsuAvp1, rsuAvp2);

    // Make new copy
    rsuAvp2 = ccaAvpFactory.createRequestedServiceUnit();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(rsuAvp2);

    // Create empty...
    RequestedServiceUnitAvp rsuAvp3 = ccaAvpFactory.createRequestedServiceUnit();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(rsuAvp3, false);

    // Set all previous values
    rsuAvp3.setExtensionAvps(rsuAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(rsuAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(rsuAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", rsuAvp2, rsuAvp3);
  }

  @Test
  public void testAvpFactoryCreateServiceParameterInfo() throws Exception {
    String avpName = "Service-Parameter-Info";

    // Create AVP with mandatory values
    ServiceParameterInfoAvp spiAvp1 = ccaAvpFactory.createServiceParameterInfo(55555L, "mobicents diameter".getBytes());

    // Make sure it's not null
    Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", spiAvp1);

    // Create AVP with default constructor
    ServiceParameterInfoAvp spiAvp2 = ccaAvpFactory.createServiceParameterInfo();

    // Should not contain mandatory values
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Service-Parameter-Type AVP.", spiAvp2.hasServiceParameterType());
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Service-Parameter-Value AVP.", spiAvp2.hasServiceParameterValue());

    // Set mandatory values
    spiAvp2.setServiceParameterType(55555L);
    spiAvp2.setServiceParameterValue("mobicents diameter".getBytes());

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", spiAvp1, spiAvp2);

    // Make new copy
    spiAvp2 = ccaAvpFactory.createServiceParameterInfo();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(spiAvp2);

    // Create empty...
    ServiceParameterInfoAvp spiAvp3 = ccaAvpFactory.createServiceParameterInfo();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(spiAvp3, false);

    // Set all previous values
    spiAvp3.setExtensionAvps(spiAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(spiAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(spiAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", spiAvp2, spiAvp3);
  }

  @Test
  public void testAvpFactoryCreateSubscriptionId() throws Exception {
    String avpName = "Subscription-Id";

    // Create AVP with mandatory values
    SubscriptionIdAvp sidAvp1 = ccaAvpFactory.createSubscriptionId(SubscriptionIdType.END_USER_SIP_URI, "sip:alexandre@mobicents.org");

    // Make sure it's not null
    Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", sidAvp1);

    // Create AVP with default constructor
    SubscriptionIdAvp sidAvp2 = ccaAvpFactory.createSubscriptionId();

    // Should not contain mandatory values
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Subscription-Id-Type AVP.", sidAvp2.hasSubscriptionIdType());
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Subscription-Id-Data AVP.", sidAvp2.hasSubscriptionIdData());

    // Set mandatory values
    sidAvp2.setSubscriptionIdType(SubscriptionIdType.END_USER_SIP_URI);
    sidAvp2.setSubscriptionIdData("sip:alexandre@mobicents.org");

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", sidAvp1, sidAvp2);

    // Make new copy
    sidAvp2 = ccaAvpFactory.createSubscriptionId();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(sidAvp2);

    // Create empty...
    SubscriptionIdAvp sidAvp3 = ccaAvpFactory.createSubscriptionId();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(sidAvp3, false);

    // Set all previous values
    sidAvp3.setExtensionAvps(sidAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(sidAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(sidAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", sidAvp2, sidAvp3);
  }

  @Test
  public void testAvpFactoryCreateUnitValue() throws Exception {
    String avpName = "Unit-Value";

    // Create AVP with mandatory values
    UnitValueAvp uvAvp1 = ccaAvpFactory.createUnitValue(19191L);

    // Make sure it's not null
    Assert.assertNotNull("Created Unit-Value AVP from objects should not be null.", uvAvp1);

    // Create AVP with default constructor
    UnitValueAvp uvAvp2 = ccaAvpFactory.createUnitValue();

    // Should not contain mandatory values
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Value-Digits AVP.", uvAvp2.hasValueDigits());

    // Set mandatory values
    uvAvp2.setValueDigits(19191L);

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", uvAvp1, uvAvp2);

    // Make new copy
    uvAvp2 = ccaAvpFactory.createUnitValue();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(uvAvp2);

    // Create empty...
    UnitValueAvp uvAvp3 = ccaAvpFactory.createUnitValue();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(uvAvp3, false);

    // Set all previous values
    uvAvp3.setExtensionAvps(uvAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(uvAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(uvAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", uvAvp2, uvAvp3);
  }

  @Test
  public void testAvpFactoryCreateUsedServiceUnit() throws Exception {
    String avpName = "Used-Service-Unit";

    // Create AVP with mandatory values
    UsedServiceUnitAvp usuAvp1 = ccaAvpFactory.createUsedServiceUnit();

    // Make sure it's not null
    Assert.assertNotNull("Created Unit-Value AVP from objects should not be null.", usuAvp1);

    // Create AVP with default constructor
    UsedServiceUnitAvp usuAvp2 = ccaAvpFactory.createUsedServiceUnit();

    // Should not contain mandatory values

    // Set mandatory values

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", usuAvp1, usuAvp2);

    // Make new copy
    usuAvp2 = ccaAvpFactory.createUsedServiceUnit();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(usuAvp2);

    // Create empty...
    UsedServiceUnitAvp usuAvp3 = ccaAvpFactory.createUsedServiceUnit();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(usuAvp3, false);

    // Set all previous values
    usuAvp3.setExtensionAvps(usuAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(usuAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(usuAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", usuAvp2, usuAvp3);
  }

  @Test
  public void testAvpFactoryCreateUserEquipmentInfo() throws Exception {
    String avpName = "User-Equipment-Info";

    // Create AVP with mandatory values
    UserEquipmentInfoAvp ueiAvp1 = ccaAvpFactory.createUserEquipmentInfo(UserEquipmentInfoType.MAC, "00:11:22:33:44:55".getBytes());

    // Make sure it's not null
    Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", ueiAvp1);

    // Create AVP with default constructor
    UserEquipmentInfoAvp ueiAvp2 = ccaAvpFactory.createUserEquipmentInfo();

    // Should not contain mandatory values
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have User-Equipment-Info-Type AVP.", ueiAvp2.hasUserEquipmentInfoType());
    Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have User-Equipment-Info-Value AVP.", ueiAvp2.hasUserEquipmentInfoValue());

    // Set mandatory values
    ueiAvp2.setUserEquipmentInfoType(UserEquipmentInfoType.MAC);
    ueiAvp2.setUserEquipmentInfoValue("00:11:22:33:44:55".getBytes());

    // Make sure it's equal to the one created with mandatory values constructor
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", ueiAvp1, ueiAvp2);

    // Make new copy
    ueiAvp2 = ccaAvpFactory.createUserEquipmentInfo();

    // And set all values using setters
    CCAAvpAssistant.INSTANCE.testSetters(ueiAvp2);

    // Create empty...
    UserEquipmentInfoAvp ueiAvp3 = ccaAvpFactory.createUserEquipmentInfo();

    // Verify that no values have been set
    CCAAvpAssistant.INSTANCE.testHassers(ueiAvp3, false);

    // Set all previous values
    ueiAvp3.setExtensionAvps(ueiAvp2.getExtensionAvps());

    // Verify if values have been set
    CCAAvpAssistant.INSTANCE.testHassers(ueiAvp3, true);

    // Verify if values have been correctly set
    CCAAvpAssistant.INSTANCE.testGetters(ueiAvp3);

    // Make sure they match!
    Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", ueiAvp2, ueiAvp3);
  }


}
