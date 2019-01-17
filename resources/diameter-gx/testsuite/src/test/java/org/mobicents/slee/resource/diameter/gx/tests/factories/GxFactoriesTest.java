/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and individual contributors
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

package org.mobicents.slee.resource.diameter.gx.tests.factories;

import static org.junit.Assert.*;
import net.java.slee.resource.diameter.base.events.avp.DiameterIdentity;
import net.java.slee.resource.diameter.cca.events.avp.CcRequestType;
import net.java.slee.resource.diameter.gx.GxAvpFactory;
import net.java.slee.resource.diameter.gx.GxClientSessionActivity;
import net.java.slee.resource.diameter.gx.GxMessageFactory;
import net.java.slee.resource.diameter.gx.GxServerSessionActivity;
import net.java.slee.resource.diameter.gx.events.GxCreditControlAnswer;
import net.java.slee.resource.diameter.gx.events.GxCreditControlRequest;
import net.java.slee.resource.diameter.gx.events.GxReAuthAnswer;
import net.java.slee.resource.diameter.gx.events.GxReAuthRequest;

import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.Request;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.Stack;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.gx.ClientGxSession;
import org.jdiameter.api.gx.ClientGxSessionListener;
import org.jdiameter.api.gx.ServerGxSession;
import org.jdiameter.api.gx.ServerGxSessionListener;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.client.impl.app.gx.ClientGxSessionDataLocalImpl;
import org.jdiameter.client.impl.app.gx.ClientGxSessionImpl;
import org.jdiameter.common.api.app.gx.IGxMessageFactory;
import org.jdiameter.server.impl.app.gx.ServerGxSessionDataLocalImpl;
import org.jdiameter.server.impl.app.gx.ServerGxSessionImpl;
import org.junit.Test;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.mobicents.slee.resource.diameter.base.DiameterAvpFactoryImpl;
import org.mobicents.slee.resource.diameter.base.DiameterMessageFactoryImpl;
import org.mobicents.slee.resource.diameter.base.events.DiameterMessageImpl;
import org.mobicents.slee.resource.diameter.base.tests.factories.BaseFactoriesTest;
import org.mobicents.slee.resource.diameter.base.tests.factories.BaseFactoriesTest.MyConfiguration;
import org.mobicents.slee.resource.diameter.gx.GxAvpFactoryImpl;
import org.mobicents.slee.resource.diameter.gx.GxClientSessionActivityImpl;
import org.mobicents.slee.resource.diameter.gx.GxMessageFactoryImpl;
import org.mobicents.slee.resource.diameter.gx.GxServerSessionActivityImpl;

/**
 * Test class for JAIN SLEE Diameter Gx RA Message and AVP Factories
 *
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class GxFactoriesTest implements IGxMessageFactory, ServerGxSessionListener, ClientGxSessionListener {

  private static GxMessageFactory gxMessageFactory;
  private static GxAvpFactory gxAvpFactory;

  private static Stack stack;

  private static ServerGxSession serverSession; 
  private static ClientGxSession clientSession; 

  static
  {
    stack = new org.jdiameter.client.impl.StackImpl();
    try {
      stack.init(new MyConfiguration());
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to initialize the stack.");
    }

    DiameterMessageFactoryImpl baseFactory = new DiameterMessageFactoryImpl(stack);
    DiameterAvpFactoryImpl baseAvpFactory = new DiameterAvpFactoryImpl();

    gxAvpFactory = new GxAvpFactoryImpl(baseAvpFactory);
    try {
      gxMessageFactory = new GxMessageFactoryImpl(baseFactory, stack.getSessionFactory().getNewSession().getSessionId(), stack);
      // Gx: Vendor-Specific-Application-Id is not permitted, only Auth-Application-Id;
      ((GxMessageFactoryImpl)gxMessageFactory).setApplicationId(0L, 16777224L);
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    try {
      AvpDictionary.INSTANCE.parseDictionary(GxFactoriesTest.class.getClassLoader().getResourceAsStream("dictionary.xml"));
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to parse dictionary file.");
    }
  }

  private GxServerSessionActivity gxServerSession = null;
  private GxClientSessionActivity gxClientSession = null;

  public GxFactoriesTest() {
    try {
      serverSession = new ServerGxSessionImpl(new ServerGxSessionDataLocalImpl(), this, (ISessionFactory) stack.getSessionFactory(), this, null, null);
      clientSession = new ClientGxSessionImpl(new ClientGxSessionDataLocalImpl(), this, (ISessionFactory) stack.getSessionFactory(), this, null, null);
      gxServerSession = new GxServerSessionActivityImpl(gxMessageFactory, gxAvpFactory, serverSession, new DiameterIdentity("127.0.0.2"), new DiameterIdentity("mobicents.org"), stack);
      gxClientSession = new GxClientSessionActivityImpl(gxMessageFactory, gxAvpFactory, clientSession, new DiameterIdentity("127.0.0.2"), new DiameterIdentity("mobicents.org"), stack);
      ((GxServerSessionActivityImpl)gxServerSession).fetchCurrentState(gxMessageFactory.createGxCreditControlRequest());
    }
    catch (IllegalDiameterStateException e) {
      throw new RuntimeException("Failed to parse dictionary file.");
    }
  }

  @Test
  public void isRequestCCR() throws Exception {
    GxCreditControlRequest ccr = gxMessageFactory.createGxCreditControlRequest();
    assertTrue("Request Flag in Credit-Control-Request is not set.", ccr.getHeader().isRequest());
  }

  @Test
  public void testGettersAndSettersCCR() throws Exception {
    GxCreditControlRequest ccr = gxMessageFactory.createGxCreditControlRequest();

    int nFailures = GxAvpAssistant.INSTANCE.testMethods(ccr, GxCreditControlRequest.class);

    assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
  }  

  @Test
  public void isProxiableCCR() throws Exception {
    GxCreditControlRequest ccr = gxMessageFactory.createGxCreditControlRequest();
    assertTrue("The 'P' bit is not set by default in Gx Credit-Control-Request, it should.", ccr.getHeader().isProxiable());
  }

  @Test
  public void  GxApplicationIdCCR() throws Exception {
    GxCreditControlRequest ccr = gxMessageFactory.createGxCreditControlRequest();
    assertTrue("Auth-Application-Id AVP in Gx CCR must be 16777224, it is " + ccr.getAuthApplicationId(), ccr.getAuthApplicationId() == 16777224);
  }

  @Test
  public void isAnswerCCA() throws Exception {
    GxCreditControlAnswer cca = gxServerSession.createGxCreditControlAnswer();
    assertFalse("Request Flag in Credit-Control-Answer is set.", cca.getHeader().isRequest());
  }

  @Test
  public void isProxiableCopiedCCA() throws Exception {
    GxCreditControlRequest ccr = gxMessageFactory.createGxCreditControlRequest();
    ((GxServerSessionActivityImpl)gxServerSession).fetchCurrentState(ccr);
    GxCreditControlAnswer cca = gxServerSession.createGxCreditControlAnswer();
    assertEquals("The 'P' bit is not copied from request in Credit-Control-Answer, it should. [RFC3588/6.2]", ccr.getHeader().isProxiable(), cca.getHeader().isProxiable());

    // Reverse 'P' bit ...
    ((DiameterMessageImpl) ccr).getGenericData().setProxiable(!ccr.getHeader().isProxiable());
    assertTrue("The 'P' bit was not modified in Credit-Control-Request, it should.", ccr.getHeader().isProxiable() != cca.getHeader().isProxiable());
    ((GxServerSessionActivityImpl)gxServerSession).fetchCurrentState(ccr);

    cca = gxServerSession.createGxCreditControlAnswer();
    assertEquals("The 'P' bit is not copied from request in Credit-Control-Answer, it should. [RFC3588/6.2]", ccr.getHeader().isProxiable(), cca.getHeader().isProxiable());
  }

  @Test
  public void hasTFlagSetCCA() throws Exception {
    GxCreditControlRequest ccr = gxMessageFactory.createGxCreditControlRequest();
    ((DiameterMessageImpl) ccr).getGenericData().setReTransmitted(true);

    assertTrue("The 'T' flag should be set in Credit-Control-Request", ccr.getHeader().isPotentiallyRetransmitted());

    ((GxServerSessionActivityImpl)gxServerSession).fetchCurrentState(ccr);
    GxCreditControlAnswer cca = gxServerSession.createGxCreditControlAnswer();
    assertFalse("The 'T' flag should not be set in Credit-Control-Answer", cca.getHeader().isPotentiallyRetransmitted());
  }

  @Test
  public void testGettersAndSettersCCA() throws Exception {
    GxCreditControlAnswer cca = gxServerSession.createGxCreditControlAnswer();

    int nFailures = GxAvpAssistant.INSTANCE.testMethods(cca, GxCreditControlAnswer.class);

    assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
  }  

  @Test
  public void hasGxApplicationIdCCA() throws Exception {
    GxCreditControlAnswer cca = gxServerSession.createGxCreditControlAnswer();
    assertTrue("Auth-Application-Id AVP in Gx CCA must be 16777224, it is " + cca.getAuthApplicationId(), cca.getAuthApplicationId() == 16777224);
  }

  @Test
  public void hasDestinationHostCCA() throws Exception {
    GxCreditControlAnswer cca = gxServerSession.createGxCreditControlAnswer();
    assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", cca.getDestinationHost());    
  }

  @Test
  public void hasDestinationRealmCCA() throws Exception {
    GxCreditControlAnswer cca = gxServerSession.createGxCreditControlAnswer();
    assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", cca.getDestinationRealm());    
  }


  @Test
  public void isRequestRAR() throws Exception {
    GxReAuthRequest rar = gxMessageFactory.createGxReAuthRequest();
    assertTrue("Request Flag in Gx Re-Auth-Request is not set.", rar.getHeader().isRequest());
  }

  @Test
  public void isProxiableRAR() throws Exception {
    GxReAuthRequest rar = gxMessageFactory.createGxReAuthRequest();
    assertTrue("The 'P' bit is not set by default in Re-Auth-Request, it should.", rar.getHeader().isProxiable());
  }

  @Test
  public void testGettersAndSettersRAR() throws Exception {
    GxReAuthRequest rar = gxMessageFactory.createGxReAuthRequest();

    int nFailures = GxAvpAssistant.INSTANCE.testMethods(rar, GxReAuthRequest.class);

    assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
  }  

  @Test
  public void hasGxApplicationIdRAR() throws Exception {
    GxReAuthRequest rar = gxMessageFactory.createGxReAuthRequest();
    assertTrue("Auth-Application-Id AVP in Gx RAR must be 16777224, it is " + rar.getAuthApplicationId(), rar.getAuthApplicationId() == 16777224);
  }

  @Test
  public void isAnswerRAA() throws Exception {
    GxReAuthAnswer raa = gxClientSession.createGxReAuthAnswer(gxMessageFactory.createGxReAuthRequest());
    assertFalse("Request Flag in Gx Re-Auth-Answer is set.", raa.getHeader().isRequest());
  }

  @Test
  public void isProxiableCopiedRAA() throws Exception {
    GxReAuthRequest rar = gxMessageFactory.createGxReAuthRequest();
    GxReAuthAnswer raa = gxClientSession.createGxReAuthAnswer(rar);
    assertEquals("The 'P' bit is not copied from request in Re-Auth-Answer, it should. [RFC3588/6.2]", rar.getHeader().isProxiable(), raa.getHeader().isProxiable());

    // Reverse 'P' bit ...
    ((DiameterMessageImpl) rar).getGenericData().setProxiable(!rar.getHeader().isProxiable());
    assertTrue("The 'P' bit was not modified in Re-Auth-Request, it should.", rar.getHeader().isProxiable() != raa.getHeader().isProxiable());

    raa = gxClientSession.createGxReAuthAnswer(rar);
    assertEquals("The 'P' bit is not copied from request in Re-Auth-Answer, it should. [RFC3588/6.2]", rar.getHeader().isProxiable(), raa.getHeader().isProxiable());
  }

  @Test
  public void hasTFlagSetRAA() throws Exception {
    GxReAuthRequest rar = gxMessageFactory.createGxReAuthRequest();
    ((DiameterMessageImpl) rar).getGenericData().setReTransmitted(true);

    assertTrue("The 'T' flag should be set in Re-Auth-Request", rar.getHeader().isPotentiallyRetransmitted());

    GxReAuthAnswer raa = gxClientSession.createGxReAuthAnswer(rar);
    assertFalse("The 'T' flag should not be set in Re-Auth-Answer", raa.getHeader().isPotentiallyRetransmitted());
  }

  @Test
  public void testGettersAndSettersRAA() throws Exception {
    GxReAuthAnswer raa = gxClientSession.createGxReAuthAnswer(gxMessageFactory.createGxReAuthRequest());

    int nFailures = GxAvpAssistant.INSTANCE.testMethods(raa, GxReAuthAnswer.class);

    assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
  }  

  @Test
  public void hasGxApplicationIdRAA() throws Exception {
    GxReAuthAnswer raa = gxClientSession.createGxReAuthAnswer(gxMessageFactory.createGxReAuthRequest());
    assertTrue("Auth-Application-Id AVP in Gx CCA must be 16777224, it is " + raa.getAuthApplicationId(), raa.getAuthApplicationId() == 16777224);
  }

  @Test
  public void hasDestinationHostRAA() throws Exception {
    GxReAuthAnswer raa = gxClientSession.createGxReAuthAnswer(gxMessageFactory.createGxReAuthRequest());
    assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", raa.getDestinationHost());    
  }

  @Test
  public void hasDestinationRealmRAA() throws Exception {
    GxReAuthAnswer raa = gxClientSession.createGxReAuthAnswer(gxMessageFactory.createGxReAuthRequest());
    assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", raa.getDestinationRealm());    
  }

  @Test
  public void testMessageFactoryApplicationIdChangeCCR() throws Exception {
    long vendor = 10415L; 
    ApplicationId originalAppId = ((GxMessageFactoryImpl)gxMessageFactory).getApplicationId();

    boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
    boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

    boolean isVendor = originalAppId.getVendorId() != 0L;

    assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

    System.out.println("Default VENDOR-ID for Gx is " + originalAppId.getVendorId());
    // let's create a message and see how it comes...
    GxCreditControlRequest originalCCR = gxMessageFactory.createGxCreditControlRequest();
    BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalCCR);
    
    // now we switch..
    originalCCR = null;
    isVendor = !isVendor;
    ((GxMessageFactoryImpl)gxMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
    
    // create a new message and see how it comes...
    GxCreditControlRequest changedCCR = gxMessageFactory.createGxCreditControlRequest();
    BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedCCR);

    // revert back to default
    ((GxMessageFactoryImpl)gxMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
  }

  @Test
  public void testMessageFactoryApplicationIdChangeRAR() throws Exception {
    long vendor = 10415L; 
    ApplicationId originalAppId = ((GxMessageFactoryImpl)gxMessageFactory).getApplicationId();

    boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
    boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

    boolean isVendor = originalAppId.getVendorId() != 0L;

    assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

    System.out.println("Default VENDOR-ID for Gx is " + originalAppId.getVendorId());
    // let's create a message and see how it comes...
    GxReAuthRequest originalRAR = gxMessageFactory.createGxReAuthRequest();
    BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalRAR);
    
    // now we switch..
    originalRAR = null;
    isVendor = !isVendor;
    ((GxMessageFactoryImpl)gxMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
    
    // create a new message and see how it comes...
    GxCreditControlRequest changedRAR = gxMessageFactory.createGxCreditControlRequest();
    BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedRAR);

    // revert back to default
    ((GxMessageFactoryImpl)gxMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
  }

  @Test
  public void testMessageFactoryApplicationIdChangeInClientSessionCCR() throws Exception {
    long vendor = 10415L; 
    ApplicationId originalAppId = ((GxMessageFactoryImpl)gxMessageFactory).getApplicationId();

    boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
    boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

    boolean isVendor = originalAppId.getVendorId() != 0L;

    assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

    System.out.println("Default VENDOR-ID for Gx is " + originalAppId.getVendorId());

    // let's create a message and see how it comes...
    GxCreditControlRequest originalCCR = gxClientSession.createGxCreditControlRequest(CcRequestType.EVENT_REQUEST);
    BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalCCR);

    // now we switch..
    originalCCR = null;
    isVendor = !isVendor;
    ((GxMessageFactoryImpl)gxMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

    // create a new message and see how it comes...
    GxCreditControlRequest changedCCR = gxClientSession.createGxCreditControlRequest(CcRequestType.EVENT_REQUEST);
    BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedCCR);

    // revert back to default
    ((GxMessageFactoryImpl)gxMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
  }

  @Test
  public void testMessageFactoryApplicationIdChangeInClientSessionRAA() throws Exception {
    long vendor = 10415L; 
    ApplicationId originalAppId = ((GxMessageFactoryImpl)gxMessageFactory).getApplicationId();

    boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
    boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

    boolean isVendor = originalAppId.getVendorId() != 0L;

    assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

    System.out.println("Default VENDOR-ID for Gx is " + originalAppId.getVendorId());

    // let's create a message and see how it comes...
    GxReAuthAnswer originalRAA = gxClientSession.createGxReAuthAnswer(gxMessageFactory.createGxReAuthRequest());
    BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalRAA);

    // now we switch..
    originalRAA = null;
    isVendor = !isVendor;
    ((GxMessageFactoryImpl)gxMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

    // create a new message and see how it comes...
    GxReAuthAnswer changedRAA = gxClientSession.createGxReAuthAnswer(gxMessageFactory.createGxReAuthRequest());
    BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedRAA);

    // revert back to default
    ((GxMessageFactoryImpl)gxMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
  }

  @Test
  public void testMessageFactoryApplicationIdChangeInServerSessionCCA() throws Exception {
    long vendor = 10415L; 
    ApplicationId originalAppId = ((GxMessageFactoryImpl)gxMessageFactory).getApplicationId();

    boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
    boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

    boolean isVendor = originalAppId.getVendorId() != 0L;

    assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

    System.out.println("Default VENDOR-ID for Gx is " + originalAppId.getVendorId());

    // let's create a message and see how it comes...
    GxCreditControlAnswer originalCCA = gxServerSession.createGxCreditControlAnswer();
    BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalCCA);

    // now we switch..
    originalCCA = null;
    isVendor = !isVendor;
    ((GxMessageFactoryImpl)gxMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

    // create a new message and see how it comes...
    GxCreditControlAnswer changedCCA = gxServerSession.createGxCreditControlAnswer();
    BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedCCA);

    // revert back to default
    ((GxMessageFactoryImpl)gxMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
  }

  public org.jdiameter.api.gx.events.GxReAuthAnswer createGxReAuthAnswer(Answer answer) {
    return null;
  }

  public org.jdiameter.api.gx.events.GxReAuthRequest createGxReAuthRequest(Request req) {
    return null;
  }

  public long[] getApplicationIds() {
    return new long[]{GxMessageFactory._GX_AUTH_APP_ID};
  }

  public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    // NO-OP
  }

  public void doGxReAuthAnswer(ServerGxSession session, org.jdiameter.api.gx.events.GxReAuthRequest request, org.jdiameter.api.gx.events.GxReAuthAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    // NO-OP
  }

  public void doGxReAuthRequest(ClientGxSession session, org.jdiameter.api.gx.events.GxReAuthRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    // NO-OP
  }

  public void doCreditControlRequest(ServerGxSession session, org.jdiameter.api.gx.events.GxCreditControlRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    // NO-OP
  }

  public void doCreditControlAnswer(ClientGxSession session, org.jdiameter.api.gx.events.GxCreditControlRequest request, org.jdiameter.api.gx.events.GxCreditControlAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    // NO-OP
  }

  public org.jdiameter.api.gx.events.GxCreditControlRequest createCreditControlRequest(Request request) {
    return null;
  }

  public org.jdiameter.api.gx.events.GxCreditControlAnswer createCreditControlAnswer(Answer answer) {
    return null;
  }

  public int getDefaultDDFHValue() {
    return 0;
  }

  public int getDefaultCCFHValue() {
    return 0;
  }

}
