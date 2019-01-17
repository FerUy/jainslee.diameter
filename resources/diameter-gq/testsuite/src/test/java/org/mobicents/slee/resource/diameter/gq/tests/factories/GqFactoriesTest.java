/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.mobicents.slee.resource.diameter.gq.tests.factories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.java.slee.resource.diameter.base.events.avp.DiameterIdentity;
import net.java.slee.resource.diameter.base.events.avp.IPFilterRule;
import net.java.slee.resource.diameter.gq.GqAvpFactory;
import net.java.slee.resource.diameter.gq.GqMessageFactory;
import net.java.slee.resource.diameter.gq.GqServerSessionActivity;
import net.java.slee.resource.diameter.gq.events.GqAAAnswer;
import net.java.slee.resource.diameter.gq.events.GqAARequest;
import net.java.slee.resource.diameter.gq.events.GqAbortSessionAnswer;
import net.java.slee.resource.diameter.gq.events.GqAbortSessionRequest;
import net.java.slee.resource.diameter.gq.events.GqReAuthAnswer;
import net.java.slee.resource.diameter.gq.events.GqReAuthRequest;
import net.java.slee.resource.diameter.gq.events.GqSessionTerminationAnswer;
import net.java.slee.resource.diameter.gq.events.GqSessionTerminationRequest;
import net.java.slee.resource.diameter.gq.events.avp.BindingInformation;
import net.java.slee.resource.diameter.gq.events.avp.BindingInputList;
import net.java.slee.resource.diameter.gq.events.avp.BindingOutputList;
import net.java.slee.resource.diameter.gq.events.avp.FlowGrouping;
import net.java.slee.resource.diameter.gq.events.avp.FlowStatus;
import net.java.slee.resource.diameter.gq.events.avp.FlowUsage;
import net.java.slee.resource.diameter.gq.events.avp.Flows;
import net.java.slee.resource.diameter.gq.events.avp.GloballyUniqueAddress;
import net.java.slee.resource.diameter.gq.events.avp.MediaComponentDescription;
import net.java.slee.resource.diameter.gq.events.avp.MediaSubComponent;
import net.java.slee.resource.diameter.gq.events.avp.ReservationPriority;
import net.java.slee.resource.diameter.gq.events.avp.V4TransportAddress;
import net.java.slee.resource.diameter.gq.events.avp.V6TransportAddress;

import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.Request;
import org.jdiameter.api.Stack;
import org.jdiameter.api.auth.events.ReAuthAnswer;
import org.jdiameter.api.auth.events.ReAuthRequest;
import org.jdiameter.api.gq.GqServerSession;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.common.impl.app.gq.GqSessionFactoryImpl;
import org.jdiameter.server.impl.app.auth.ServerAuthSessionDataLocalImpl;
import org.jdiameter.server.impl.app.gq.GqServerSessionImpl;
import org.junit.Assert;
import org.junit.Test;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.mobicents.slee.resource.diameter.base.DiameterAvpFactoryImpl;
import org.mobicents.slee.resource.diameter.base.DiameterMessageFactoryImpl;
import org.mobicents.slee.resource.diameter.base.events.DiameterMessageImpl;
import org.mobicents.slee.resource.diameter.base.tests.factories.BaseFactoriesTest;
import org.mobicents.slee.resource.diameter.base.tests.factories.BaseFactoriesTest.MyConfiguration;
import org.mobicents.slee.resource.diameter.gq.GqAvpFactoryImpl;
import org.mobicents.slee.resource.diameter.gq.GqMessageFactoryImpl;
import org.mobicents.slee.resource.diameter.gq.GqServerSessionActivityImpl;

/**
 * Test class for JAIN SLEE Diameter Gq' RA Message and AVP Factories
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public class GqFactoriesTest {

	private static GqMessageFactory gqMessageFactory;
	private static GqAvpFactory gqAvpFactory;

	private static Stack stack;

	private static GqServerSession serverSession;
	// private static GqClientSession clientSession;

	static {
		stack = new org.jdiameter.client.impl.StackImpl();
		try {
			stack.init(new MyConfiguration());
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to initialize the stack.");
		}

		DiameterMessageFactoryImpl baseFactory = new DiameterMessageFactoryImpl(stack);
		DiameterAvpFactoryImpl baseAvpFactory = new DiameterAvpFactoryImpl();

		gqAvpFactory = new GqAvpFactoryImpl(baseAvpFactory);
		try {
			gqMessageFactory = new GqMessageFactoryImpl(baseFactory, stack.getSessionFactory().getNewSession().getSessionId(), stack);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		try {
			AvpDictionary.INSTANCE.parseDictionary(GqFactoriesTest.class.getClassLoader().getResourceAsStream("dictionary.xml"));
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to parse dictionary file.");
		}
	}

	private GqServerSessionActivity gqServerSession = null;

	// private GqClientSessionActivity gqClientSession = null;

	public GqFactoriesTest() {
		try {
			GqSessionFactoryImpl gqSessionFactory = new GqSessionFactoryImpl(stack.getSessionFactory());
			serverSession = new GqServerSessionImpl(new ServerAuthSessionDataLocalImpl(), (ISessionFactory) stack.getSessionFactory(), gqSessionFactory, gqSessionFactory,
					gqSessionFactory, gqSessionFactory, 30000L, true);
			// clientSession = new GqClientSessionImpl(new ClientAuthSessionDataLocalImpl(), (ISessionFactory)
			// stack.getSessionFactory(), gqSessionFactory, gqSessionFactory, gqSessionFactory, gqSessionFactory, true);
			gqServerSession = new GqServerSessionActivityImpl(gqMessageFactory.getBaseMessageFactory(), gqAvpFactory.getBaseFactory(), serverSession, new DiameterIdentity("127.0.0.2"),
					new DiameterIdentity("mobicents.org"), stack);
			// gqClientSession = new GqClientSessionActivityImpl(gqMessageFactory.getBaseMessageFactory(),
			// gqAvpFactory.getBaseFactory(), clientSession, new DiameterIdentity("127.0.0.2"), new
			// DiameterIdentity("mobicents.org"), stack);
			// ((GqServerSessionActivityImpl)roServerSession).fetchCurrentState(roMessageFactory.createGqAARequest());
		}
		catch (IllegalDiameterStateException e) {
			throw new RuntimeException("Failed to parse dictionary file.");
		}
	}

	// AA-Request

	@Test
	public void isRequestAAR() throws Exception {
		GqAARequest aar = gqMessageFactory.createGqAARequest();
		assertTrue("Request Flag in AA-Request is not set.", aar.getHeader().isRequest());
	}

	@Test
	public void isProxiableAAR() throws Exception {
		GqAARequest rar = gqMessageFactory.createGqAARequest();
		assertTrue("The 'P' bit is not set by default in Gq' AA-Request, it should.", rar.getHeader().isProxiable());
	}

	@Test
	public void testGettersAndSettersAAR() throws Exception {
		GqAARequest aar = gqMessageFactory.createGqAARequest();

		int nFailures = GqAvpAssistant.INSTANCE.testMethods(aar, GqAARequest.class);

		assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
	}

	@Test
	public void hasGqApplicationIdAAR() throws Exception {
		GqAARequest aar = gqMessageFactory.createGqAARequest();
		assertTrue("Auth-Application-Id AVP in Gq AAR must be " + GqMessageFactory._GQ_AUTH_APP_ID + ", it is " + aar.getAuthApplicationId(),
				aar.getAuthApplicationId() == GqMessageFactory._GQ_AUTH_APP_ID);
	}

	// AA-Answer

	@Test
	public void isAnswerAAA() throws Exception {
		GqAAAnswer aaa = gqServerSession.createGqAAAnswer(gqMessageFactory.createGqAARequest());
		assertFalse("Request Flag in AA-Answer is set.", aaa.getHeader().isRequest());
	}

	@Test
	public void isProxiableCopiedAAA() throws Exception {
		GqAARequest aar = gqMessageFactory.createGqAARequest();
		GqAAAnswer aaa = gqMessageFactory.createGqAAAnswer(aar);
		assertEquals("The 'P' bit is not copied from request in Gq' AA-Answer, it should. [RFC3588/6.2]", aar.getHeader().isProxiable(), aaa.getHeader().isProxiable());

		// Reverse 'P' bit ...
		((DiameterMessageImpl) aar).getGenericData().setProxiable(!aar.getHeader().isProxiable());
		assertTrue("The 'P' bit was not modified in Gq' AA-Request, it should.", aar.getHeader().isProxiable() != aaa.getHeader().isProxiable());

		aaa = gqMessageFactory.createGqAAAnswer(aar);
		assertEquals("The 'P' bit is not copied from request in Gq' AA-Answer, it should. [RFC3588/6.2]", aar.getHeader().isProxiable(), aaa.getHeader().isProxiable());
	}

	@Test
	public void hasTFlagSetAAA() throws Exception {
		GqAARequest aar = gqMessageFactory.createGqAARequest();
		((DiameterMessageImpl) aar).getGenericData().setReTransmitted(true);

		assertTrue("The 'T' flag should be set in AA-Request", aar.getHeader().isPotentiallyRetransmitted());

		GqAAAnswer aaa = gqMessageFactory.createGqAAAnswer(aar);
		assertFalse("The 'T' flag should not be set in AA-Answer", aaa.getHeader().isPotentiallyRetransmitted());
	}

	@Test
	public void testGettersAndSettersAAA() throws Exception {
		GqAAAnswer aaa = gqServerSession.createGqAAAnswer(gqMessageFactory.createGqAARequest());

		int nFailures = GqAvpAssistant.INSTANCE.testMethods(aaa, GqAAAnswer.class);

		assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
	}

	@Test
	public void hasGqApplicationIdAAA() throws Exception {
		GqAAAnswer aaa = gqServerSession.createGqAAAnswer(gqMessageFactory.createGqAARequest());
		assertTrue("Auth-Application-Id AVP in Gq AAA must be " + GqMessageFactory._GQ_AUTH_APP_ID + ", it is " + aaa.getAuthApplicationId(),
				aaa.getAuthApplicationId() == GqMessageFactory._GQ_AUTH_APP_ID);
	}

	@Test
	public void hasDestinationHostAAA() throws Exception {
		GqAAAnswer aaa = gqServerSession.createGqAAAnswer(gqMessageFactory.createGqAARequest());
		assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", aaa.getDestinationHost());
	}

	@Test
	public void hasDestinationRealmAAA() throws Exception {
		GqAAAnswer aaa = gqServerSession.createGqAAAnswer(gqMessageFactory.createGqAARequest());
		assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", aaa.getDestinationRealm());
	}

	// Abort-Session-Request

	@Test
	public void isRequestASR() throws Exception {
		GqAbortSessionRequest asr = gqMessageFactory.createGqAbortSessionRequest();
		assertTrue("Request Flag in Abort-Session-Request is not set.", asr.getHeader().isRequest());
	}

	@Test
	public void isProxiableASR() throws Exception {
		GqAbortSessionRequest rar = gqMessageFactory.createGqAbortSessionRequest();
		assertTrue("The 'P' bit is not set by default in Gq' Abort-Session-Request, it should.", rar.getHeader().isProxiable());
	}

	@Test
	public void testGettersAndSettersASR() throws Exception {
		GqAbortSessionRequest asr = gqMessageFactory.createGqAbortSessionRequest();

		int nFailures = GqAvpAssistant.INSTANCE.testMethods(asr, GqAbortSessionRequest.class);

		assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
	}

	@Test
	public void hasGqApplicationIdASR() throws Exception {
		GqAbortSessionRequest asr = gqMessageFactory.createGqAbortSessionRequest();
		assertTrue("Auth-Application-Id AVP in Gq ASR must be " + GqMessageFactory._GQ_AUTH_APP_ID + ", it is " + asr.getAuthApplicationId(),
				asr.getAuthApplicationId() == GqMessageFactory._GQ_AUTH_APP_ID);
	}

	// Abort-Session-Answer

	@Test
	public void isAnswerASA() throws Exception {
		GqAbortSessionAnswer asa = gqMessageFactory.createGqAbortSessionAnswer(gqMessageFactory.createGqAbortSessionRequest());
		assertFalse("Request Flag in Abort-Session-Answer is set.", asa.getHeader().isRequest());
	}

	@Test
	public void isProxiableCopiedASA() throws Exception {
		GqAbortSessionRequest asr = gqMessageFactory.createGqAbortSessionRequest();
		GqAbortSessionAnswer asa = gqMessageFactory.createGqAbortSessionAnswer(asr);
		assertEquals("The 'P' bit is not copied from request in Gq' Abort-Session-Answer, it should. [RFC3588/6.2]", asr.getHeader().isProxiable(), asa.getHeader().isProxiable());

		// Reverse 'P' bit ...
		((DiameterMessageImpl) asr).getGenericData().setProxiable(!asr.getHeader().isProxiable());
		assertTrue("The 'P' bit was not modified in Gq' Abort-Session-Request, it should.", asr.getHeader().isProxiable() != asa.getHeader().isProxiable());

		asa = gqMessageFactory.createGqAbortSessionAnswer(asr);
		assertEquals("The 'P' bit is not copied from request in Gq' Abort-Session-Answer, it should. [RFC3588/6.2]", asr.getHeader().isProxiable(), asa.getHeader().isProxiable());
	}

	@Test
	public void hasTFlagSetASA() throws Exception {
		GqAbortSessionRequest asr = gqMessageFactory.createGqAbortSessionRequest();
		((DiameterMessageImpl) asr).getGenericData().setReTransmitted(true);

		assertTrue("The 'T' flag should be set in Abort-Session-Request", asr.getHeader().isPotentiallyRetransmitted());

		GqAbortSessionAnswer asa = gqMessageFactory.createGqAbortSessionAnswer(asr);
		assertFalse("The 'T' flag should not be set in Abort-Session-Answer", asa.getHeader().isPotentiallyRetransmitted());
	}

	@Test
	public void testGettersAndSettersASA() throws Exception {
		GqAbortSessionAnswer asa = gqMessageFactory.createGqAbortSessionAnswer(gqMessageFactory.createGqAbortSessionRequest());

		int nFailures = GqAvpAssistant.INSTANCE.testMethods(asa, GqAbortSessionAnswer.class);

		assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
	}

	@Test
	public void hasDestinationHostASA() throws Exception {
		GqAbortSessionAnswer asa = gqMessageFactory.createGqAbortSessionAnswer(gqMessageFactory.createGqAbortSessionRequest());
		assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", asa.getDestinationHost());
	}

	@Test
	public void hasDestinationRealmASA() throws Exception {
		GqAbortSessionAnswer asa = gqMessageFactory.createGqAbortSessionAnswer(gqMessageFactory.createGqAbortSessionRequest());
		assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", asa.getDestinationRealm());
	}

	// Re-Auth-Request

	@Test
	public void isRequestRAR() throws Exception {
		GqReAuthRequest rar = gqMessageFactory.createGqReAuthRequest();
		assertTrue("Request Flag in Re-Auth-Request is not set.", rar.getHeader().isRequest());
	}

	@Test
	public void isProxiableRAR() throws Exception {
		GqReAuthRequest rar = gqMessageFactory.createGqReAuthRequest();
		assertTrue("The 'P' bit is not set by default in Gq' Re-Auth-Request, it should.", rar.getHeader().isProxiable());
	}

	@Test
	public void testGettersAndSettersRAR() throws Exception {
		GqReAuthRequest rar = gqMessageFactory.createGqReAuthRequest();

		int nFailures = GqAvpAssistant.INSTANCE.testMethods(rar, GqReAuthRequest.class);

		assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
	}

	@Test
	public void hasGqApplicationIdRAR() throws Exception {
		GqReAuthRequest rar = gqMessageFactory.createGqReAuthRequest();
		assertTrue("Auth-Application-Id AVP in Gq RAR must be " + GqMessageFactory._GQ_AUTH_APP_ID + ", it is " + rar.getAuthApplicationId(),
				rar.getAuthApplicationId() == GqMessageFactory._GQ_AUTH_APP_ID);
	}

	// Re-Auth-Answer

	@Test
	public void isAnswerRAA() throws Exception {
		GqReAuthAnswer raa = gqMessageFactory.createGqReAuthAnswer(gqMessageFactory.createGqReAuthRequest());
		assertFalse("Request Flag in Re-Auth-Answer is set.", raa.getHeader().isRequest());
	}

	@Test
	public void isProxiableCopiedRAA() throws Exception {
		GqReAuthRequest rar = gqMessageFactory.createGqReAuthRequest();
		GqReAuthAnswer raa = gqMessageFactory.createGqReAuthAnswer(rar);
		assertEquals("The 'P' bit is not copied from request in Gq' Re-Auth-Answer, it should. [RFC3588/6.2]", rar.getHeader().isProxiable(), raa.getHeader().isProxiable());

		// Reverse 'P' bit ...
		((DiameterMessageImpl) rar).getGenericData().setProxiable(!rar.getHeader().isProxiable());
		assertTrue("The 'P' bit was not modified in Gq' Re-Auth-Request, it should.", rar.getHeader().isProxiable() != raa.getHeader().isProxiable());

		raa = gqMessageFactory.createGqReAuthAnswer(rar);
		assertEquals("The 'P' bit is not copied from request in Gq' Re-Auth-Answer, it should. [RFC3588/6.2]", rar.getHeader().isProxiable(), raa.getHeader().isProxiable());
	}

	@Test
	public void hasTFlagSetRAA() throws Exception {
		GqReAuthRequest rar = gqMessageFactory.createGqReAuthRequest();
		((DiameterMessageImpl) rar).getGenericData().setReTransmitted(true);

		assertTrue("The 'T' flag should be set in Re-Auth-Request", rar.getHeader().isPotentiallyRetransmitted());

		GqReAuthAnswer raa = gqMessageFactory.createGqReAuthAnswer(rar);
		assertFalse("The 'T' flag should not be set in Re-Auth-Answer", raa.getHeader().isPotentiallyRetransmitted());
	}

	@Test
	public void testGettersAndSettersRAA() throws Exception {
		GqReAuthAnswer raa = gqMessageFactory.createGqReAuthAnswer(gqMessageFactory.createGqReAuthRequest());

		int nFailures = GqAvpAssistant.INSTANCE.testMethods(raa, GqReAuthAnswer.class);

		assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
	}

	@Test
	public void hasDestinationHostRAA() throws Exception {
		GqReAuthAnswer raa = gqMessageFactory.createGqReAuthAnswer(gqMessageFactory.createGqReAuthRequest());
		assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", raa.getDestinationHost());
	}

	@Test
	public void hasDestinationRealmRAA() throws Exception {
		GqReAuthAnswer raa = gqMessageFactory.createGqReAuthAnswer(gqMessageFactory.createGqReAuthRequest());
		assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", raa.getDestinationRealm());
	}

	// Session-Termination-Request

	@Test
	public void isRequestSTR() throws Exception {
		GqSessionTerminationRequest str = gqMessageFactory.createGqSessionTerminationRequest();
		assertTrue("Request Flag in Session-Termination-Request is not set.", str.getHeader().isRequest());
	}

	@Test
	public void isProxiableSTR() throws Exception {
		GqSessionTerminationRequest str = gqMessageFactory.createGqSessionTerminationRequest();
		assertTrue("The 'P' bit is not set by default in Session-Termination-Request, it should.", str.getHeader().isProxiable());
	}

	@Test
	public void testGettersAndSettersSTR() throws Exception {
		GqSessionTerminationRequest str = gqMessageFactory.createGqSessionTerminationRequest();

		int nFailures = GqAvpAssistant.INSTANCE.testMethods(str, GqSessionTerminationRequest.class);

		assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
	}

	@Test
	public void hasGqApplicationIdSTR() throws Exception {
		GqSessionTerminationRequest str = gqMessageFactory.createGqSessionTerminationRequest();
		assertTrue("Auth-Application-Id AVP in Gq STR must be " + GqMessageFactory._GQ_AUTH_APP_ID + ", it is " + str.getAuthApplicationId(),
				str.getAuthApplicationId() == GqMessageFactory._GQ_AUTH_APP_ID);
	}

	// Session-Termination-Answer

	@Test
	public void isAnswerSTA() throws Exception {
		GqSessionTerminationAnswer sta = gqMessageFactory.createGqSessionTerminationAnswer(gqMessageFactory.createGqSessionTerminationRequest());
		assertFalse("Request Flag in Re-Auth-Answer is set.", sta.getHeader().isRequest());
	}

	@Test
	public void isProxiableCopiedSTA() throws Exception {
		GqSessionTerminationRequest str = gqMessageFactory.createGqSessionTerminationRequest();
		GqSessionTerminationAnswer sta = gqMessageFactory.createGqSessionTerminationAnswer(str);
		assertEquals("The 'P' bit is not copied from request in Session-Termination-Answer, it should. [RFC3588/6.2]", str.getHeader().isProxiable(), sta.getHeader().isProxiable());

		// Reverse 'P' bit ...
		((DiameterMessageImpl) str).getGenericData().setProxiable(!str.getHeader().isProxiable());
		assertTrue("The 'P' bit was not modified in Session-Termination-Request, it should.", str.getHeader().isProxiable() != sta.getHeader().isProxiable());

		sta = gqMessageFactory.createGqSessionTerminationAnswer(str);
		assertEquals("The 'P' bit is not copied from request in Session-Termination-Answer, it should. [RFC3588/6.2]", str.getHeader().isProxiable(), sta.getHeader().isProxiable());
	}

	@Test
	public void hasTFlagSetSTA() throws Exception {
		GqSessionTerminationRequest str = gqMessageFactory.createGqSessionTerminationRequest();
		((DiameterMessageImpl) str).getGenericData().setReTransmitted(true);

		assertTrue("The 'T' flag should be set in Session-Termination-Request", str.getHeader().isPotentiallyRetransmitted());

		GqSessionTerminationAnswer sta = gqMessageFactory.createGqSessionTerminationAnswer(str);
		assertFalse("The 'T' flag should not be set in Session-Termination-Answer", sta.getHeader().isPotentiallyRetransmitted());
	}

	@Test
	public void testGettersAndSettersSTA() throws Exception {
		GqSessionTerminationAnswer sta = gqMessageFactory.createGqSessionTerminationAnswer(gqMessageFactory.createGqSessionTerminationRequest());

		int nFailures = GqAvpAssistant.INSTANCE.testMethods(sta, GqSessionTerminationAnswer.class);

		assertEquals("Some methods have failed. See logs for more details.", 0, nFailures);
	}

	@Test
	public void hasDestinationHostSTA() throws Exception {
		GqSessionTerminationAnswer sta = gqMessageFactory.createGqSessionTerminationAnswer(gqMessageFactory.createGqSessionTerminationRequest());
		assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", sta.getDestinationHost());
	}

	@Test
	public void hasDestinationRealmSTA() throws Exception {
		GqSessionTerminationAnswer sta = gqMessageFactory.createGqSessionTerminationAnswer(gqMessageFactory.createGqSessionTerminationRequest());
		assertNull("The Destination-Host and Destination-Realm AVPs MUST NOT be present in the answer message. [RFC3588/6.2]", sta.getDestinationRealm());
	}

	// Gq AVP Factory Tests

	private static BindingInformation BI_AVP_DEFAULT = gqAvpFactory.createBindingInformation();
	private static BindingInputList BIL_AVP_DEFAULT = gqAvpFactory.createBindingInputList();
	private static BindingOutputList BOL_AVP_DEFAULT = gqAvpFactory.createBindingOutputList();
	private static FlowGrouping FG_AVP_DEFAULT = gqAvpFactory.createFlowGrouping();
	private static Flows F_AVP_DEFAULT = gqAvpFactory.createFlows();
	private static GloballyUniqueAddress GUA_AVP_DEFAULT = gqAvpFactory.createGloballyUniqueAddress();
	private static MediaComponentDescription MCD_AVP_DEFAULT = gqAvpFactory.createMediaComponentDescription();
	private static MediaSubComponent MSC_AVP_DEFAULT = gqAvpFactory.createMediaSubComponent();
	private static V4TransportAddress V4TA_AVP_DEFAULT = gqAvpFactory.createV4TransportAddress();
	private static V6TransportAddress V6TA_AVP_DEFAULT = gqAvpFactory.createV6TransportAddress();

	static {
		V4TA_AVP_DEFAULT.setFramedIPAddress("255.255.255.254".getBytes());
		V4TA_AVP_DEFAULT.setPortNumber(13579);

		V6TA_AVP_DEFAULT.setFramedIPV6Prefix("A123:B456:C789:DE80::/57".getBytes());
		V6TA_AVP_DEFAULT.setPortNumber(24680);

		BIL_AVP_DEFAULT.setV4TransportAddress(V4TA_AVP_DEFAULT);
		BIL_AVP_DEFAULT.setV6TransportAddress(V6TA_AVP_DEFAULT);

		BI_AVP_DEFAULT.setBindingInputList(BIL_AVP_DEFAULT);
		BI_AVP_DEFAULT.setBindingOutputList(BOL_AVP_DEFAULT);

		F_AVP_DEFAULT.setFlowNumber(1);
		F_AVP_DEFAULT.setMediaComponentNumber(2);

		FG_AVP_DEFAULT.setFlow(F_AVP_DEFAULT);

		GUA_AVP_DEFAULT.setAddressRealm("mobicents.org".getBytes());
		GUA_AVP_DEFAULT.setFramedIPAddress("255.255.255.254".getBytes());
		GUA_AVP_DEFAULT.setFramedIPV6Prefix("A123:B456:C789:DE80::/57".getBytes());

		MSC_AVP_DEFAULT.setFlowDescription(new IPFilterRule("deny in ip from 1.2.3.4/24 to any"));
		MSC_AVP_DEFAULT.setFlowNumber(7);
		MSC_AVP_DEFAULT.setFlowStatus(FlowStatus.ENABLED);
		MSC_AVP_DEFAULT.setFlowUsage(FlowUsage.RTCP);
		MSC_AVP_DEFAULT.setMaxRequestedBandwidthDL(555);
		MSC_AVP_DEFAULT.setMaxRequestedBandwidthUL(222);

		MCD_AVP_DEFAULT.setAFApplicationIdentifier("AFApplicationIdentifier".getBytes());
		MCD_AVP_DEFAULT.setCodecData("codecData".getBytes());
		MCD_AVP_DEFAULT.setFlowStatus(FlowStatus.DISABLED);
		MCD_AVP_DEFAULT.setMaxRequestedBandwidthDL(999);
		MCD_AVP_DEFAULT.setMaxRequestedBandwidthUL(111);
		MCD_AVP_DEFAULT.setMediaAuthorizationContextId("mediaAuthorizationContextId");
		MCD_AVP_DEFAULT.setMediaComponentNumber(1);
		MCD_AVP_DEFAULT.setMediaSubComponent(MSC_AVP_DEFAULT);
		MCD_AVP_DEFAULT.setReservationClass(4);
		MCD_AVP_DEFAULT.setReservationPriority(ReservationPriority.PRIORITYSEVEN);
		MCD_AVP_DEFAULT.setRRBandwidth(65);
		MCD_AVP_DEFAULT.setRSBandwidth(56);
		MCD_AVP_DEFAULT.setTransportClass(76576);
	}

	@Test
	public void testAvpFactoryCreateBindingInformation() throws Exception {

		String avpName = "Binding-Information";

		// Create AVP with mandatory values
		BindingInformation biAvp1 = gqAvpFactory.createBindingInformation();

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", biAvp1);

		// Create AVP with default constructor
		BindingInformation biAvp2 = gqAvpFactory.createBindingInformation();

		// Should not contain mandatory values

		// Set mandatory values

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", biAvp1, biAvp2);

		// Make new copy
		biAvp2 = gqAvpFactory.createBindingInformation();

		// And set all values using setters
		GqAvpAssistant.INSTANCE.testSetters(biAvp2);

		// Create empty...
		BindingInformation biAvp3 = gqAvpFactory.createBindingInformation();

		// Verify that no values have been set
		GqAvpAssistant.INSTANCE.testHassers(biAvp3, false);

		// Set all previous values
		biAvp3.setExtensionAvps(biAvp2.getExtensionAvps());

		// Verify if values have been set
		GqAvpAssistant.INSTANCE.testHassers(biAvp3, true);

		// Verify if values have been correctly set
		GqAvpAssistant.INSTANCE.testGetters(biAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setExtensionAvps should be equal to original.", biAvp2, biAvp3);
	}

	@Test
	public void testAvpFactoryCreateBindingInputList() throws Exception {

		String avpName = "Binding-Input-List";

		// Create AVP with mandatory values
		BindingInputList bilAvp1 = gqAvpFactory.createBindingInputList();

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", bilAvp1);

		// Create AVP with default constructor
		BindingInputList bilAvp2 = gqAvpFactory.createBindingInputList();

		// Should not contain mandatory values

		// Set mandatory values

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", bilAvp1, bilAvp2);

		// Make new copy
		bilAvp2 = gqAvpFactory.createBindingInputList();

		// And set all values using setters
		GqAvpAssistant.INSTANCE.testSetters(bilAvp2);

		// Create empty...
		BindingInputList bilAvp3 = gqAvpFactory.createBindingInputList();

		// Verify that no values have been set
		GqAvpAssistant.INSTANCE.testHassers(bilAvp3, false);

		// Set all previous values
		bilAvp3.setExtensionAvps(bilAvp2.getExtensionAvps());

		// Verify if values have been set
		GqAvpAssistant.INSTANCE.testHassers(bilAvp3, true);

		// Verify if values have been correctly set
		GqAvpAssistant.INSTANCE.testGetters(bilAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setExtensionAvps should be equal to original.", bilAvp2, bilAvp3);
	}

	@Test
	public void testAvpFactoryCreateBindingOutputList() throws Exception {

		String avpName = "Binding-Output-List";

		// Create AVP with mandatory values
		BindingOutputList bolAvp1 = gqAvpFactory.createBindingOutputList();

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", bolAvp1);

		// Create AVP with default constructor
		BindingOutputList bolAvp2 = gqAvpFactory.createBindingOutputList();

		// Should not contain mandatory values

		// Set mandatory values

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", bolAvp1, bolAvp2);

		// Make new copy
		bolAvp2 = gqAvpFactory.createBindingOutputList();

		// And set all values using setters
		GqAvpAssistant.INSTANCE.testSetters(bolAvp2);

		// Create empty...
		BindingOutputList bolAvp3 = gqAvpFactory.createBindingOutputList();

		// Verify that no values have been set
		GqAvpAssistant.INSTANCE.testHassers(bolAvp3, false);

		// Set all previous values
		bolAvp3.setExtensionAvps(bolAvp2.getExtensionAvps());

		// Verify if values have been set
		GqAvpAssistant.INSTANCE.testHassers(bolAvp3, true);

		// Verify if values have been correctly set
		GqAvpAssistant.INSTANCE.testGetters(bolAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setExtensionAvps should be equal to original.", bolAvp2, bolAvp3);
	}

	@Test
	public void testAvpFactoryCreateFlowGrouping() throws Exception {

		String avpName = "Flow-Grouping";

		// Create AVP with mandatory values
		FlowGrouping fgAvp1 = gqAvpFactory.createFlowGrouping();

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", fgAvp1);

		// Create AVP with default constructor
		FlowGrouping fgAvp2 = gqAvpFactory.createFlowGrouping();

		// Should not contain mandatory values

		// Set mandatory values

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", fgAvp1, fgAvp2);

		// Make new copy
		fgAvp2 = gqAvpFactory.createFlowGrouping();

		// And set all values using setters
		GqAvpAssistant.INSTANCE.testSetters(fgAvp2);

		// Create empty...
		FlowGrouping fgAvp3 = gqAvpFactory.createFlowGrouping();

		// Verify that no values have been set
		GqAvpAssistant.INSTANCE.testHassers(fgAvp3, false);

		// Set all previous values
		fgAvp3.setExtensionAvps(fgAvp2.getExtensionAvps());

		// Verify if values have been set
		GqAvpAssistant.INSTANCE.testHassers(fgAvp3, true);

		// Verify if values have been correctly set
		GqAvpAssistant.INSTANCE.testGetters(fgAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setExtensionAvps should be equal to original.", fgAvp2, fgAvp3);
	}

	@Test
	public void testAvpFactoryCreateFlows() throws Exception {

		String avpName = "Flows";

		// Create AVP with mandatory values
		Flows fAvp1 = gqAvpFactory.createFlows();

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", fAvp1);

		// Create AVP with default constructor
		Flows fAvp2 = gqAvpFactory.createFlows();

		// Should not contain mandatory values

		// Set mandatory values

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", fAvp1, fAvp2);

		// Make new copy
		fAvp2 = gqAvpFactory.createFlows();

		// And set all values using setters
		GqAvpAssistant.INSTANCE.testSetters(fAvp2);

		// Create empty...
		Flows fAvp3 = gqAvpFactory.createFlows();

		// Verify that no values have been set
		GqAvpAssistant.INSTANCE.testHassers(fAvp3, false);

		// Set all previous values
		fAvp3.setExtensionAvps(fAvp2.getExtensionAvps());

		// Verify if values have been set
		GqAvpAssistant.INSTANCE.testHassers(fAvp3, true);

		// Verify if values have been correctly set
		GqAvpAssistant.INSTANCE.testGetters(fAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setExtensionAvps should be equal to original.", fAvp2, fAvp3);
	}

	@Test
	public void testAvpFactoryCreateGloballyUniqueAddress() throws Exception {

		String avpName = "Globally-Unique-Address";

		// Create AVP with mandatory values
		GloballyUniqueAddress guaAvp1 = gqAvpFactory.createGloballyUniqueAddress();

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", guaAvp1);

		// Create AVP with default constructor
		GloballyUniqueAddress guaAvp2 = gqAvpFactory.createGloballyUniqueAddress();

		// Should not contain mandatory values

		// Set mandatory values

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", guaAvp1, guaAvp2);

		// Make new copy
		guaAvp2 = gqAvpFactory.createGloballyUniqueAddress();

		// And set all values using setters
		GqAvpAssistant.INSTANCE.testSetters(guaAvp2);

		// Create empty...
		GloballyUniqueAddress guaAvp3 = gqAvpFactory.createGloballyUniqueAddress();

		// Verify that no values have been set
		GqAvpAssistant.INSTANCE.testHassers(guaAvp3, false);

		// Set all previous values
		guaAvp3.setExtensionAvps(guaAvp2.getExtensionAvps());

		// Verify if values have been set
		GqAvpAssistant.INSTANCE.testHassers(guaAvp3, true);

		// Verify if values have been correctly set
		GqAvpAssistant.INSTANCE.testGetters(guaAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setExtensionAvps should be equal to original.", guaAvp2, guaAvp3);
	}

	@Test
	public void testAvpFactoryCreateMediaComponentDescription() throws Exception {

		String avpName = "Media-Component-Description";

		// Create AVP with mandatory values
		MediaComponentDescription mcdAvp1 = gqAvpFactory.createMediaComponentDescription();

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", mcdAvp1);

		// Create AVP with default constructor
		MediaComponentDescription mcdAvp2 = gqAvpFactory.createMediaComponentDescription();

		// Should not contain mandatory values

		// Set mandatory values

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", mcdAvp1, mcdAvp2);

		// Make new copy
		mcdAvp2 = gqAvpFactory.createMediaComponentDescription();

		// And set all values using setters
		GqAvpAssistant.INSTANCE.testSetters(mcdAvp2);

		// Create empty...
		MediaComponentDescription mcdAvp3 = gqAvpFactory.createMediaComponentDescription();

		// Verify that no values have been set
		GqAvpAssistant.INSTANCE.testHassers(mcdAvp3, false);

		// Set all previous values
		mcdAvp3.setExtensionAvps(mcdAvp2.getExtensionAvps());

		// Verify if values have been set
		GqAvpAssistant.INSTANCE.testHassers(mcdAvp3, true);

		// Verify if values have been correctly set
		GqAvpAssistant.INSTANCE.testGetters(mcdAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setExtensionAvps should be equal to original.", mcdAvp2, mcdAvp3);
	}

	@Test
	public void testAvpFactoryCreateMediaSubComponent() throws Exception {

		String avpName = "Media-Sub-Component";

		// Create AVP with mandatory values
		MediaSubComponent mscAvp1 = gqAvpFactory.createMediaSubComponent();

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", mscAvp1);

		// Create AVP with default constructor
		MediaSubComponent mscAvp2 = gqAvpFactory.createMediaSubComponent();

		// Should not contain mandatory values

		// Set mandatory values

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", mscAvp1, mscAvp2);

		// Make new copy
		mscAvp2 = gqAvpFactory.createMediaSubComponent();

		// And set all values using setters
		GqAvpAssistant.INSTANCE.testSetters(mscAvp2);

		// Create empty...
		MediaSubComponent mscAvp3 = gqAvpFactory.createMediaSubComponent();

		// Verify that no values have been set
		GqAvpAssistant.INSTANCE.testHassers(mscAvp3, false);

		// Set all previous values
		mscAvp3.setExtensionAvps(mscAvp2.getExtensionAvps());

		// Verify if values have been set
		GqAvpAssistant.INSTANCE.testHassers(mscAvp3, true);

		// Verify if values have been correctly set
		GqAvpAssistant.INSTANCE.testGetters(mscAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setExtensionAvps should be equal to original.", mscAvp2, mscAvp3);
	}

	@Test
	public void testAvpFactoryCreateV4TransportAddress() throws Exception {

		String avpName = "V4-Transport-Address";

		// Create AVP with mandatory values
		V4TransportAddress v4taAvp1 = gqAvpFactory.createV4TransportAddress();

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", v4taAvp1);

		// Create AVP with default constructor
		V4TransportAddress v4taAvp2 = gqAvpFactory.createV4TransportAddress();

		// Should not contain mandatory values

		// Set mandatory values

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", v4taAvp1, v4taAvp2);

		// Make new copy
		v4taAvp2 = gqAvpFactory.createV4TransportAddress();

		// And set all values using setters
		GqAvpAssistant.INSTANCE.testSetters(v4taAvp2);

		// Create empty...
		V4TransportAddress v4taAvp3 = gqAvpFactory.createV4TransportAddress();

		// Verify that no values have been set
		GqAvpAssistant.INSTANCE.testHassers(v4taAvp3, false);

		// Set all previous values
		v4taAvp3.setExtensionAvps(v4taAvp2.getExtensionAvps());

		// Verify if values have been set
		GqAvpAssistant.INSTANCE.testHassers(v4taAvp3, true);

		// Verify if values have been correctly set
		GqAvpAssistant.INSTANCE.testGetters(v4taAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setExtensionAvps should be equal to original.", v4taAvp2, v4taAvp3);
	}

	@Test
	public void testAvpFactoryCreateV6TransportAddress() throws Exception {

		String avpName = "V6-Transport-Address";

		// Create AVP with mandatory values
		V6TransportAddress v6taAvp1 = gqAvpFactory.createV6TransportAddress();

		// Make sure it's not null
		Assert.assertNotNull("Created " + avpName + " AVP from objects should not be null.", v6taAvp1);

		// Create AVP with default constructor
		V6TransportAddress v6taAvp2 = gqAvpFactory.createV6TransportAddress();

		// Should not contain mandatory values

		// Set mandatory values

		// Make sure it's equal to the one created with mandatory values constructor
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + set<Mandatory-AVPs> should be equal to original.", v6taAvp1, v6taAvp2);

		// Make new copy
		v6taAvp2 = gqAvpFactory.createV6TransportAddress();

		// And set all values using setters
		GqAvpAssistant.INSTANCE.testSetters(v6taAvp2);

		// Create empty...
		V6TransportAddress v6taAvp3 = gqAvpFactory.createV6TransportAddress();

		// Verify that no values have been set
		GqAvpAssistant.INSTANCE.testHassers(v6taAvp3, false);

		// Set all previous values
		v6taAvp3.setExtensionAvps(v6taAvp2.getExtensionAvps());

		// Verify if values have been set
		GqAvpAssistant.INSTANCE.testHassers(v6taAvp3, true);

		// Verify if values have been correctly set
		GqAvpAssistant.INSTANCE.testGetters(v6taAvp3);

		// Make sure they match!
		Assert.assertEquals("Created " + avpName + " AVP from default constructor + setExtensionAvps should be equal to original.", v6taAvp2, v6taAvp3);
	}

	public ReAuthAnswer createReAuthAnswer(Answer answer) {
		return null;
	}

	public ReAuthRequest createReAuthRequest(Request req) {
		return null;
	}

	public long[] getApplicationIds() {
		return new long[] { GqMessageFactory._GQ_AUTH_APP_ID };
	}

	@Test
	public void testMessageFactoryApplicationIdChangeAAR() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((GqMessageFactoryImpl) gqMessageFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Gq is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		GqAARequest originalAAR = gqMessageFactory.createGqAARequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalAAR);

		// now we switch..
		originalAAR = null;
		isVendor = !isVendor;
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		GqAARequest changedAAR = gqMessageFactory.createGqAARequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedAAR);

		// revert back to default
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

	@Test
	public void testMessageFactoryApplicationIdChangeAAA() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((GqMessageFactoryImpl) gqMessageFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Gq is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		GqAAAnswer originalAAA = gqMessageFactory.createGqAAAnswer(gqMessageFactory.createGqAARequest());
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalAAA);

		// now we switch..
		originalAAA = null;
		isVendor = !isVendor;
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		GqAAAnswer changedAAA = gqMessageFactory.createGqAAAnswer(gqMessageFactory.createGqAARequest());
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedAAA);

		// revert back to default
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

	@Test
	public void testMessageFactoryApplicationIdChangeASR() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((GqMessageFactoryImpl) gqMessageFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Gq is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		GqAbortSessionRequest originalASR = gqMessageFactory.createGqAbortSessionRequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalASR);

		// now we switch..
		originalASR = null;
		isVendor = !isVendor;
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		GqAbortSessionRequest changedASR = gqMessageFactory.createGqAbortSessionRequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedASR);

		// revert back to default
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

	@Test
	public void testMessageFactoryApplicationIdChangeASA() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((GqMessageFactoryImpl) gqMessageFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Gq is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		GqAbortSessionAnswer originalASA = gqMessageFactory.createGqAbortSessionAnswer(gqMessageFactory.createGqAbortSessionRequest());
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalASA);

		// now we switch..
		originalASA = null;
		isVendor = !isVendor;
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		GqAbortSessionAnswer changedASA = gqMessageFactory.createGqAbortSessionAnswer(gqMessageFactory.createGqAbortSessionRequest());
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedASA);

		// revert back to default
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

	@Test
	public void testMessageFactoryApplicationIdChangeRAR() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((GqMessageFactoryImpl) gqMessageFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Gq is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		GqReAuthRequest originalRAR = gqMessageFactory.createGqReAuthRequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalRAR);

		// now we switch..
		originalRAR = null;
		isVendor = !isVendor;
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		GqReAuthRequest changedRAR = gqMessageFactory.createGqReAuthRequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedRAR);

		// revert back to default
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

	@Test
	public void testMessageFactoryApplicationIdChangeRAA() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((GqMessageFactoryImpl) gqMessageFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Gq is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		GqReAuthAnswer originalRAA = gqMessageFactory.createGqReAuthAnswer(gqMessageFactory.createGqReAuthRequest());
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalRAA);

		// now we switch..
		originalRAA = null;
		isVendor = !isVendor;
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		GqReAuthAnswer changedRAA = gqMessageFactory.createGqReAuthAnswer(gqMessageFactory.createGqReAuthRequest());
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedRAA);

		// revert back to default
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

	@Test
	public void testMessageFactoryApplicationIdChangeSTR() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((GqMessageFactoryImpl) gqMessageFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Gq is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		GqSessionTerminationRequest originalSTR = gqMessageFactory.createGqSessionTerminationRequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalSTR);

		// now we switch..
		originalSTR = null;
		isVendor = !isVendor;
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		GqSessionTerminationRequest changedSTR = gqMessageFactory.createGqSessionTerminationRequest();
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedSTR);

		// revert back to default
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

	@Test
	public void testMessageFactoryApplicationIdChangeSTA() throws Exception {
		long vendor = 10415L;
		ApplicationId originalAppId = ((GqMessageFactoryImpl) gqMessageFactory).getApplicationId();

		boolean isAuth = originalAppId.getAuthAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;
		boolean isAcct = originalAppId.getAcctAppId() != org.jdiameter.api.ApplicationId.UNDEFINED_VALUE;

		boolean isVendor = originalAppId.getVendorId() != 0L;

		assertTrue("Invalid Application-Id (" + originalAppId + "). Should only, and at least, contain either Auth or Acct value.", (isAuth && !isAcct) || (!isAuth && isAcct));

		System.out.println("Default VENDOR-ID for Gq is " + originalAppId.getVendorId());
		// let's create a message and see how it comes...
		GqSessionTerminationAnswer originalSTA = gqMessageFactory.createGqSessionTerminationAnswer(gqMessageFactory.createGqSessionTerminationRequest());
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, originalSTA);

		// now we switch..
		originalSTA = null;
		isVendor = !isVendor;
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(isVendor ? vendor : 0L, isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());

		// create a new message and see how it comes...
		GqSessionTerminationAnswer changedSTA = gqMessageFactory.createGqSessionTerminationAnswer(gqMessageFactory.createGqSessionTerminationRequest());
		BaseFactoriesTest.checkCorrectApplicationIdAVPs(isVendor, isAuth, isAcct, changedSTA);

		// revert back to default
		((GqMessageFactoryImpl) gqMessageFactory).setApplicationId(originalAppId.getVendorId(), isAuth ? originalAppId.getAuthAppId() : originalAppId.getAcctAppId());
	}

}
