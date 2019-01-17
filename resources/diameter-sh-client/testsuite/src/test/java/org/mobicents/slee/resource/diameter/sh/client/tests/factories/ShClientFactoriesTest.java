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

package org.mobicents.slee.resource.diameter.sh.client.tests.factories;

import static org.junit.Assert.*;
import net.java.slee.resource.diameter.base.events.avp.AuthSessionStateType;
import net.java.slee.resource.diameter.sh.client.ShClientMessageFactory;
import net.java.slee.resource.diameter.sh.events.ProfileUpdateRequest;
import net.java.slee.resource.diameter.sh.events.PushNotificationAnswer;
import net.java.slee.resource.diameter.sh.events.PushNotificationRequest;
import net.java.slee.resource.diameter.sh.events.SubscribeNotificationsRequest;
import net.java.slee.resource.diameter.sh.events.UserDataRequest;
import net.java.slee.resource.diameter.sh.events.avp.DataReferenceType;
import net.java.slee.resource.diameter.sh.events.avp.DiameterShAvpCodes;
import net.java.slee.resource.diameter.sh.events.avp.SubsReqType;
import net.java.slee.resource.diameter.sh.events.avp.SupportedApplicationsAvp;
import net.java.slee.resource.diameter.sh.events.avp.SupportedFeaturesAvp;
import net.java.slee.resource.diameter.sh.events.avp.UserIdentityAvp;

import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Stack;
import org.jdiameter.api.sh.ClientShSession;
import org.jdiameter.client.impl.app.sh.ShClientSessionImpl;
import org.jdiameter.common.impl.app.sh.ShSessionFactoryImpl;
import org.junit.Assert;
import org.junit.Test;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.mobicents.slee.resource.diameter.base.DiameterAvpFactoryImpl;
import org.mobicents.slee.resource.diameter.base.events.DiameterMessageImpl;
import org.mobicents.slee.resource.diameter.base.tests.factories.BaseFactoriesTest;
import org.mobicents.slee.resource.diameter.base.tests.factories.BaseFactoriesTest.MyConfiguration;
import org.mobicents.slee.resource.diameter.sh.DiameterShAvpFactoryImpl;
import org.mobicents.slee.resource.diameter.sh.client.ShClientMessageFactoryImpl;
import org.mobicents.slee.resource.diameter.sh.client.ShClientSubscriptionActivityImpl;
import org.mobicents.slee.resource.diameter.sh.events.PushNotificationRequestImpl;
import org.mobicents.slee.resource.diameter.sh.events.avp.UserIdentityAvpImpl;

/**
 * 
 * ShClientFactoriesTest.java
 * 
 * <br>
 * Project: mobicents <br>
 * 6:39:33 PM Feb 27, 2009 <br>
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class ShClientFactoriesTest {

	private static ShClientMessageFactoryImpl shClientFactory;
	private static DiameterShAvpFactoryImpl shAvpFactory;
	private static Stack stack;

	private static ShClientSubscriptionActivityImpl clientSubsSession;

	private static final ApplicationId SH_APP_ID = org.jdiameter.api.ApplicationId.createByAuthAppId(ShClientMessageFactory._SH_VENDOR_ID, ShClientMessageFactory._SH_APP_ID);

	static {
		stack = new org.jdiameter.client.impl.StackImpl();
		try {
			stack.init(new MyConfiguration());
			AvpDictionary.INSTANCE.parseDictionary(ShClientFactoriesTest.class.getClassLoader().getResourceAsStream("dictionary.xml"));
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to initialize the stack.");
		}

		ShSessionFactoryImpl sf = null;

		try {
			sf = new ShSessionFactoryImpl(stack.getSessionFactory());
		}
		catch (Exception e) {
			// let's go with null
			e.printStackTrace();
		}
		shAvpFactory = new DiameterShAvpFactoryImpl(new DiameterAvpFactoryImpl());
		shClientFactory = new ShClientMessageFactoryImpl(stack);

		ShClientSessionImpl stackClientSession = (ShClientSessionImpl) sf.getNewSession("321", ClientShSession.class, SH_APP_ID, new Object[0]);
		clientSubsSession = new ShClientSubscriptionActivityImpl(shClientFactory, shAvpFactory, stackClientSession, null, null);
	}

	@Test
	public void isRequestPUR() throws Exception {
		ProfileUpdateRequest pur = shClientFactory.createProfileUpdateRequest();
		assertTrue("Request Flag in Profile-Update-Request is not set.", pur.getHeader().isRequest());
	}

	@Test
	public void isProxiablePUR() throws Exception {
		ProfileUpdateRequest pur = shClientFactory.createProfileUpdateRequest();
		assertTrue("The 'P' bit is not set by default in Profile-Update-Request, it should.", pur.getHeader().isProxiable());
	}

	@Test
	public void testGettersAndSettersPUR() throws Exception {
		ProfileUpdateRequest pur = shClientFactory.createProfileUpdateRequest();

		int nFailures = ShClientAvpAssistant.INSTANCE.testMethods(pur, ProfileUpdateRequest.class);

		assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
	}

	@Test
	public void isAnswerPNA() throws Exception {
		PushNotificationAnswer pna = shClientFactory.createPushNotificationAnswer(createPushNotificationRequest());
		assertFalse("Request Flag in Push-Notification-Answer is set.", pna.getHeader().isRequest());
	}

	@Test
	public void isProxiableCopiedPNA() throws Exception {
		PushNotificationRequest pnr = createPushNotificationRequest();
		PushNotificationAnswer pna = shClientFactory.createPushNotificationAnswer(pnr);
		assertEquals("The 'P' bit is not copied from request in Push-Notification-Answer, it should. [RFC3588/6.2]", pnr.getHeader().isProxiable(), pna.getHeader().isProxiable());

		// Reverse 'P' bit ...
		((DiameterMessageImpl) pnr).getGenericData().setProxiable(!pnr.getHeader().isProxiable());
		assertTrue("The 'P' bit was not modified in Push-Notification-Request, it should.", pnr.getHeader().isProxiable() != pna.getHeader().isProxiable());

		pna = shClientFactory.createPushNotificationAnswer(pnr);
		assertEquals("The 'P' bit is not copied from request in Push-Notification-Answer, it should. [RFC3588/6.2]", pnr.getHeader().isProxiable(), pna.getHeader().isProxiable());
	}

	@Test
	public void hasTFlagSetPNA() throws Exception {
		PushNotificationRequest pnr = createPushNotificationRequest();
		((DiameterMessageImpl) pnr).getGenericData().setReTransmitted(true);

		assertTrue("The 'T' flag should be set in Push-Notification-Request", pnr.getHeader().isPotentiallyRetransmitted());

		PushNotificationAnswer pna = shClientFactory.createPushNotificationAnswer(pnr);
		assertFalse("The 'T' flag should not be set in Push-Notification-Answer", pna.getHeader().isPotentiallyRetransmitted());
	}

	@Test
	public void testGettersAndSettersPNA() throws Exception {
		PushNotificationAnswer pna = shClientFactory.createPushNotificationAnswer(createPushNotificationRequest());

		int nFailures = ShClientAvpAssistant.INSTANCE.testMethods(pna, PushNotificationAnswer.class);

		assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
	}

	@Test
	public void hasDestinationHostPNA() throws Exception {
		PushNotificationAnswer pna = shClientFactory.createPushNotificationAnswer(createPushNotificationRequest());
		assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", pna.getDestinationHost());
	}

	@Test
	public void hasDestinationRealmPNA() throws Exception {
		PushNotificationAnswer pna = shClientFactory.createPushNotificationAnswer(createPushNotificationRequest());
		assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", pna.getDestinationRealm());
	}

	/**
	 * Test for Issue #665 (Diameter Experimental Result AVP is Nested)
	 * http://code.google.com/p/mobicents/issues/detail?id=655
	 * 
	 * @throws Exception
	 */
	@Test
	public void isExperimentalResultCorrectlySetPNA() throws Exception {
		long originalValue = 5001;

		PushNotificationAnswer pna = shClientFactory.createPushNotificationAnswer(createPushNotificationRequest(), originalValue, true);

		long obtainedValue = pna.getExperimentalResult().getExperimentalResultCode();

		assertTrue("Experimental-Result-Code in PNA should be " + originalValue + " and is " + obtainedValue + ".", originalValue == obtainedValue);
	}

	@Test
	public void isRequestSNR() throws Exception {
		SubscribeNotificationsRequest snr = shClientFactory.createSubscribeNotificationsRequest();
		assertTrue("Request Flag in Subscribe-Notifications-Request is not set.", snr.getHeader().isRequest());
	}

	@Test
	public void isProxiableSNR() throws Exception {
		SubscribeNotificationsRequest snr = shClientFactory.createSubscribeNotificationsRequest();
		assertTrue("The 'P' bit is not set by default in Subscribe-Notifications-Request, it should.", snr.getHeader().isProxiable());
	}

	@Test
	public void testGettersAndSettersSNR() throws Exception {
		SubscribeNotificationsRequest snr = shClientFactory.createSubscribeNotificationsRequest();

		int nFailures = ShClientAvpAssistant.INSTANCE.testMethods(snr, SubscribeNotificationsRequest.class);

		assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
	}

	@Test
	public void isRequestUDR() throws Exception {
		UserDataRequest udr = shClientFactory.createUserDataRequest();
		assertTrue("Request Flag in User-Data-Request is not set.", udr.getHeader().isRequest());
	}

	@Test
	public void isProxiableUDR() throws Exception {
		UserDataRequest udr = shClientFactory.createUserDataRequest();
		assertTrue("The 'P' bit is not set by default in User-Data-Request, it should.", udr.getHeader().isProxiable());
	}

	@Test
	public void testGettersAndSettersUDR() throws Exception {
		UserDataRequest udr = shClientFactory.createUserDataRequest();

		int nFailures = ShClientAvpAssistant.INSTANCE.testMethods(udr, UserDataRequest.class);

		assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
	}

	@Test
	public void isUDRPublicIdentityAccessibleTwice() throws Exception {
		String originalValue = "sip:alexandre@diameter.mobicents.org";

		UserIdentityAvpImpl uiAvp = new UserIdentityAvpImpl(DiameterShAvpCodes.USER_IDENTITY, 10415L, 1, 0, new byte[] {});
		uiAvp.setPublicIdentity(originalValue);

		UserDataRequest udr = shClientFactory.createUserDataRequest(uiAvp, DataReferenceType.IMS_PUBLIC_IDENTITY);

		String obtainedValue1 = udr.getUserIdentity().getPublicIdentity();
		String obtainedValue2 = udr.getUserIdentity().getPublicIdentity();

		assertTrue("Obtained value for Public-Identity AVP differs from original.", obtainedValue1.equals(originalValue));
		assertTrue("Obtained #1 value for Public-Identity AVP differs from Obtained #2.", obtainedValue1.equals(obtainedValue2));
	}

	@Test
	public void isPURPublicIdentityAccessibleTwice() throws Exception {
		String originalValue = "sip:alexandre@diameter.mobicents.org";

		UserIdentityAvpImpl uiAvp = new UserIdentityAvpImpl(DiameterShAvpCodes.USER_IDENTITY, 10415L, 1, 0, new byte[] {});
		uiAvp.setPublicIdentity(originalValue);

		ProfileUpdateRequest udr = shClientFactory.createProfileUpdateRequest(uiAvp, DataReferenceType.IMS_PUBLIC_IDENTITY, new byte[1]);

		String obtainedValue1 = udr.getUserIdentity().getPublicIdentity();
		String obtainedValue2 = udr.getUserIdentity().getPublicIdentity();

		assertTrue("Obtained value for Public-Identity AVP differs from original.", obtainedValue1.equals(originalValue));
		assertTrue("Obtained #1 value for Public-Identity AVP differs from Obtained #2.", obtainedValue1.equals(obtainedValue2));
	}

	@Test
	public void isSNRPublicIdentityAccessibleTwice() throws Exception {
		String originalValue = "sip:alexandre@diameter.mobicents.org";

		UserIdentityAvpImpl uiAvp = new UserIdentityAvpImpl(DiameterShAvpCodes.USER_IDENTITY, 10415L, 1, 0, new byte[] {});
		uiAvp.setPublicIdentity(originalValue);

		SubscribeNotificationsRequest udr = shClientFactory.createSubscribeNotificationsRequest(uiAvp, DataReferenceType.IMS_PUBLIC_IDENTITY, SubsReqType.SUBSCRIBE);

		String obtainedValue1 = udr.getUserIdentity().getPublicIdentity();
		String obtainedValue2 = udr.getUserIdentity().getPublicIdentity();

		assertTrue("Obtained value for Public-Identity AVP differs from original.", obtainedValue1.equals(originalValue));
		assertTrue("Obtained #1 value for Public-Identity AVP differs from Obtained #2.", obtainedValue1.equals(obtainedValue2));
	}

	// AVP Factory Testing

	@Test
	public void testAvpFactoryCreateSupportedApplications() throws Exception {
		String avpName = "Supported-Applications";

		// Create AVP with mandatory values
		SupportedApplicationsAvp saAvp1 = shAvpFactory.createSupportedApplications(123L, 456L, shAvpFactory.getBaseFactory().createVendorSpecificApplicationId(999L));

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", saAvp1);

		// Create AVP with default constructor
		SupportedApplicationsAvp saAvp2 = shAvpFactory.createSupportedApplications();

		// Should not contain mandatory values

		// Set mandatory values
		saAvp2.setAuthApplicationId(123L);
		saAvp2.setAcctApplicationId(456L);
		saAvp2.setVendorSpecificApplicationId(shAvpFactory.getBaseFactory().createVendorSpecificApplicationId(999L));

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", saAvp1, saAvp2);

		// Make new copy
		saAvp2 = shAvpFactory.createSupportedApplications();

		// And set all values using setters
		ShClientAvpAssistant.INSTANCE.testSetters(saAvp2);

		// Create empty...
		SupportedApplicationsAvp saAvp3 = shAvpFactory.createSupportedApplications();

		// Verify that no values have been set
		ShClientAvpAssistant.INSTANCE.testHassers(saAvp3, false);

		// Set all previous values
		saAvp3.setExtensionAvps(saAvp2.getExtensionAvps());

		// Verify if values have been set
		ShClientAvpAssistant.INSTANCE.testHassers(saAvp3, true);

		// Verify if values have been correctly set
		ShClientAvpAssistant.INSTANCE.testGetters(saAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setUnitValue should be equal to original.", saAvp2, saAvp3);
	}

	@Test
	public void testAvpFactoryCreateSupportedFeatures() throws Exception {
		String avpName = "Supported-Features";

		// Create AVP with mandatory values
		SupportedFeaturesAvp sfAvp1 = shAvpFactory.createSupportedFeatures(123L, 456L, 789L);

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", sfAvp1);

		// Create AVP with default constructor
		SupportedFeaturesAvp sfAvp2 = shAvpFactory.createSupportedFeatures();

		// Should not contain mandatory values
		Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Vendor-Id AVP.", sfAvp2.hasVendorId());
		Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Feature-List-Id AVP.", sfAvp2.hasFeatureListId());
		Assert.assertFalse("Created " + avpName + " AVP from default constructor should not have Feature-List AVP.", sfAvp2.hasFeatureList());

		// Set mandatory values
		sfAvp2.setVendorId(123L);
		sfAvp2.setFeatureListId(456L);
		sfAvp2.setFeatureList(789L);

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", sfAvp1, sfAvp2);

		// Make new copy
		sfAvp2 = shAvpFactory.createSupportedFeatures();

		// And set all values using setters
		ShClientAvpAssistant.INSTANCE.testSetters(sfAvp2);

		// Create empty...
		SupportedFeaturesAvp sfAvp3 = shAvpFactory.createSupportedFeatures();

		// Verify that no values have been set
		ShClientAvpAssistant.INSTANCE.testHassers(sfAvp3, false);

		// Set all previous values
		sfAvp3.setExtensionAvps(sfAvp2.getExtensionAvps());

		// Verify if values have been set
		ShClientAvpAssistant.INSTANCE.testHassers(sfAvp3, true);

		// Verify if values have been correctly set
		ShClientAvpAssistant.INSTANCE.testGetters(sfAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setExtensionAvps should be equal to original.", sfAvp2, sfAvp3);
	}

	@Test
	public void testAvpFactoryCreateUserIdentity() throws Exception {
		String avpName = "User-Identity";

		// Create AVP with mandatory values
		UserIdentityAvp uiAvp1 = shAvpFactory.createUserIdentity();

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", uiAvp1);

		// Create AVP with default constructor
		UserIdentityAvp uiAvp2 = shAvpFactory.createUserIdentity();

		// Should not contain mandatory values

		// Set mandatory values

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", uiAvp1, uiAvp2);

		// Make new copy
		uiAvp2 = shAvpFactory.createUserIdentity();

		// And set all values using setters
		ShClientAvpAssistant.INSTANCE.testSetters(uiAvp2);

		// Create empty...
		UserIdentityAvp uiAvp3 = shAvpFactory.createUserIdentity();

		// Verify that no values have been set
		ShClientAvpAssistant.INSTANCE.testHassers(uiAvp3, false);

		// Set all previous values
		uiAvp3.setExtensionAvps(uiAvp2.getExtensionAvps());

		// Verify if values have been set
		ShClientAvpAssistant.INSTANCE.testHassers(uiAvp3, true);

		// Verify if values have been correctly set
		ShClientAvpAssistant.INSTANCE.testGetters(uiAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setExtensionAvps should be equal to original.", uiAvp2, uiAvp3);
	}

	@Test
	public void testMessageFactoryApplicationIdChangePUR() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((ShClientMessageFactoryImpl) shClientFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Sh is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		ProfileUpdateRequest originalPUR = shClientFactory.createProfileUpdateRequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalPUR);

		// now we switch..
		originalPUR = null;
		isVendor = !isVendor;
		((ShClientMessageFactoryImpl) shClientFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		ProfileUpdateRequest changedPUR = shClientFactory.createProfileUpdateRequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedPUR);

		// revert back to default
		((ShClientMessageFactoryImpl) shClientFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

	@Test
	public void testMessageFactoryApplicationIdChangePNA() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((ShClientMessageFactoryImpl) shClientFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Sh is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		PushNotificationRequest pnr = createPushNotificationRequest();
		PushNotificationAnswer originalPNA = shClientFactory.createPushNotificationAnswer(pnr);
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalPNA);

		// now we switch..
		originalPNA = null;
		isVendor = !isVendor;
		((ShClientMessageFactoryImpl) shClientFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		PushNotificationAnswer changedPNA = shClientFactory.createPushNotificationAnswer(pnr);
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedPNA);

		// revert back to default
		((ShClientMessageFactoryImpl) shClientFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

	@Test
	public void testClientSessionApplicationIdChangePNA() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((ShClientMessageFactoryImpl) shClientFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Sh is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		PushNotificationRequest pnr = createPushNotificationRequest();
		UserIdentityAvp uiAvp = shAvpFactory.createUserIdentity();
		uiAvp.setPublicIdentity("alexandre@mobicents.org");
		pnr.setUserIdentity(uiAvp);
		pnr.setAuthSessionState(AuthSessionStateType.NO_STATE_MAINTAINED);
		clientSubsSession.fetchSessionData(pnr, true);
		PushNotificationAnswer originalPNA = clientSubsSession.createPushNotificationAnswer();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalPNA);

		// now we switch..
		originalPNA = null;
		isVendor = !isVendor;
		((ShClientMessageFactoryImpl) shClientFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		PushNotificationAnswer changedPNA = clientSubsSession.createPushNotificationAnswer();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedPNA);

		// revert back to default
		((ShClientMessageFactoryImpl) shClientFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

	@Test
	public void testMessageFactoryApplicationIdChangeSNR() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((ShClientMessageFactoryImpl) shClientFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Sh is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		SubscribeNotificationsRequest originalSNR = shClientFactory.createSubscribeNotificationsRequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalSNR);

		// now we switch..
		originalSNR = null;
		isVendor = !isVendor;
		((ShClientMessageFactoryImpl) shClientFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		SubscribeNotificationsRequest changedSNR = shClientFactory.createSubscribeNotificationsRequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedSNR);

		// revert back to default
		((ShClientMessageFactoryImpl) shClientFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

	@Test
	public void testMessageFactoryApplicationIdChangeUDR() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((ShClientMessageFactoryImpl) shClientFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Sh is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		UserDataRequest originalUDR = shClientFactory.createUserDataRequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalUDR);

		// now we switch..
		originalUDR = null;
		isVendor = !isVendor;
		((ShClientMessageFactoryImpl) shClientFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		UserDataRequest changedUDR = shClientFactory.createUserDataRequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedUDR);

		// revert back to default
		((ShClientMessageFactoryImpl) shClientFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

	private PushNotificationRequest createPushNotificationRequest() throws InternalException, IllegalDiameterStateException {
		return new PushNotificationRequestImpl(stack.getSessionFactory().getNewSession().createRequest(PushNotificationRequest.commandCode, SH_APP_ID, "mobicents.org", "hss.mobicents"));
	}
}
