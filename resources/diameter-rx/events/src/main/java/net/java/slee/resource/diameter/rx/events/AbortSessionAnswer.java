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

package net.java.slee.resource.diameter.rx.events;

import net.java.slee.resource.diameter.base.events.DiameterMessage;
import net.java.slee.resource.diameter.base.events.avp.DiameterIdentity;
import net.java.slee.resource.diameter.base.events.avp.DiameterURI;
import net.java.slee.resource.diameter.base.events.avp.FailedAvp;
import net.java.slee.resource.diameter.base.events.avp.ProxyInfoAvp;
import net.java.slee.resource.diameter.base.events.avp.RedirectHostUsageType;

/**
 * 
 * Defines an interface representing the Abort-Session-Answer command.
 * 
 * From the Diameter Base Protocol (rfc3588.txt) specification:
 * 
 * <pre>
 * 8.5.2.  Abort-Session-Answer
 * 
 *    The Abort-Session-Answer (ASA), indicated by the Command-Code set to
 *    274 and the message flags' 'R' bit clear, is sent in response to the
 *    ASR.  The Result-Code AVP MUST be present, and indicates the
 *    disposition of the request.
 * 
 *    If the session identified by Session-Id in the ASR was successfully
 *    terminated, Result-Code is set to DIAMETER_SUCCESS.  If the session
 *    is not currently active, Result-Code is set to
 *    DIAMETER_UNKNOWN_SESSION_ID.  If the access device does not stop the
 *    session for any other reason, Result-Code is set to
 *    DIAMETER_UNABLE_TO_COMPLY.
 * 
 * < AS-Answer> ::=  < Diameter Header: 274, PXY >
 *                   < Session-Id >
 *                   { Origin-Host }
 *                   { Origin-Realm }
 *                   [ Result-Code ]     
 *                   [ Origin-State-Id ]
 *                   [ Error-Message ]
 *                   [ Error-Reporting-Host ]
 *                  *[ Failed-AVP ]
 *                  *[ Redirected-Host ]
 *                   [ Redirected-Host-Usage ]
 *                   [ Redirected-Max-Cache-Time ]
 *                  *[ Proxy-Info ]
 *                  *[ AVP ]
 * </pre>
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:richard.good@smilecoms.com"> Richard Good </a>
 */
public interface AbortSessionAnswer extends DiameterMessage {

  static final int commandCode = 274;

  /**
   * Returns true if the Result-Code AVP is present in the message.
   */
  boolean hasResultCode();

  /**
   * Returns the value of the Result-Code AVP, of type Unsigned32. Use
   * {@link #hasResultCode()} to check the existence of this AVP.
   * 
   * @return the value of the Result-Code AVP
   * @throws IllegalStateException
   *             if the Result-Code AVP has not been set on this message
   */
  long getResultCode();

  /**
   * Sets the value of the Result-Code AVP, of type Unsigned32.
   * 
   * @throws IllegalStateException
   *             if setResultCode has already been called
   */
  void setResultCode(long resultCode);

  /**
   * Returns true if the Origin-State-Id AVP is present in the message.
   */
  boolean hasOriginStateId();

  /**
   * Returns the value of the Origin-State-Id AVP, of type Unsigned32. Use
   * {@link #hasOriginStateId()} to check the existence of this AVP.
   * 
   * @return the value of the Origin-State-Id AVP
   * @throws IllegalStateException
   *             if the Origin-State-Id AVP has not been set on this message
   */
  long getOriginStateId();

  /**
   * Sets the value of the Origin-State-Id AVP, of type Unsigned32.
   * 
   * @throws IllegalStateException
   *             if setOriginStateId has already been called
   */
  void setOriginStateId(long originStateId);

  /**
   * Returns true if the Error-Message AVP is present in the message.
   */
  boolean hasErrorMessage();

  /**
   * Returns the value of the Error-Message AVP, of type UTF8String.
   * 
   * @return the value of the Error-Message AVP or null if it has not been set
   *         on this message
   */
  String getErrorMessage();

  /**
   * Sets the value of the Error-Message AVP, of type UTF8String.
   * 
   * @throws IllegalStateException
   *             if setErrorMessage has already been called
   */
  void setErrorMessage(String errorMessage);

  /**
   * Returns true if the Error-Reporting-Host AVP is present in the message.
   */
  boolean hasErrorReportingHost();

  /**
   * Returns the value of the Error-Reporting-Host AVP, of type
   * DiameterIdentity.
   * 
   * @return the value of the Error-Reporting-Host AVP or null if it has not
   *         been set on this message
   */
  DiameterIdentity getErrorReportingHost();

  /**
   * Sets the value of the Error-Reporting-Host AVP, of type DiameterIdentity.
   * 
   * @throws IllegalStateException
   *             if setErrorReportingHost has already been called
   */
  void setErrorReportingHost(DiameterIdentity errorReportingHost);

  public boolean hasFailedAvp();

  /**
   * Returns the set of Failed-AVP AVPs. The returned array contains the AVPs
   * in the order they appear in the message. A return value of null implies
   * that no Failed-AVP AVPs have been set. The elements in the given array
   * are FailedAvp objects.
   */
  FailedAvp[] getFailedAvps();

  /**
   * Sets a single Failed-AVP AVP in the message, of type Grouped.
   * 
   * @throws IllegalStateException
   *             if setFailedAvp or setFailedAvps has already been called
   */
  void setFailedAvp(FailedAvp failedAvp);

  /**
   * Sets the set of Failed-AVP AVPs, with all the values in the given array.
   * The AVPs will be added to message in the order in which they appear in
   * the array.
   * 
   * Note: the array must not be altered by the caller following this call,
   * and getFailedAvps() is not guaranteed to return the same array instance,
   * e.g. an "==" check would fail.
   * 
   * @throws IllegalStateException
   *             if setFailedAvp or setFailedAvps has already been called
   */
  void setFailedAvps(FailedAvp[] failedAvps);

  public boolean hasRediredectHosts();

  /**
   * Returns the set of Redirect-Host AVPs. The returned array contains the
   * AVPs in the order they appear in the message. A return value of null
   * implies that no Redirect-Host AVPs have been set. The elements in the
   * given array are DiameterURI objects.
   */
  DiameterURI[] getRedirectHosts();

  /**
   * Sets a single Redirect-Host AVP in the message, of type DiameterURI.
   * 
   * @throws IllegalStateException
   *             if setRedirectHost or setRedirectHosts has already been
   *             called
   */
  void setRedirectHost(DiameterURI redirectHost);

  /**
   * Sets the set of Redirect-Host AVPs, with all the values in the given
   * array. The AVPs will be added to message in the order in which they
   * appear in the array.
   * 
   * Note: the array must not be altered by the caller following this call,
   * and getRedirectHosts() is not guaranteed to return the same array
   * instance, e.g. an "==" check would fail.
   * 
   * @throws IllegalStateException
   *             if setRedirectHost or setRedirectHosts has already been
   *             called
   */
  void setRedirectHosts(DiameterURI[] redirectHosts);

  /**
   * Returns true if the Redirect-Host-Usage AVP is present in the message.
   */
  boolean hasRedirectHostUsage();

  /**
   * Returns the value of the Redirect-Host-Usage AVP, of type Enumerated.
   * 
   * @return the value of the Redirect-Host-Usage AVP or null if it has not
   *         been set on this message
   */
  RedirectHostUsageType getRedirectHostUsage();

  /**
   * Sets the value of the Redirect-Host-Usage AVP, of type Enumerated.
   * 
   * @throws IllegalStateException
   *             if setRedirectHostUsage has already been called
   */
  void setRedirectHostUsage(RedirectHostUsageType redirectHostUsage);

  /**
   * Returns true if the Redirect-Max-Cache-Time AVP is present in the
   * message.
   */
  boolean hasRedirectMaxCacheTime();

  /**
   * Returns the value of the Redirect-Max-Cache-Time AVP, of type Unsigned32.
   * Use {@link #hasRedirectMaxCacheTime()} to check the existence of this
   * AVP.
   * 
   * @return the value of the Redirect-Max-Cache-Time AVP
   * @throws IllegalStateException
   *             if the Redirect-Max-Cache-Time AVP has not been set on this
   *             message
   */
  long getRedirectMaxCacheTime();

  /**
   * Sets the value of the Redirect-Max-Cache-Time AVP, of type Unsigned32.
   * 
   * @throws IllegalStateException
   *             if setRedirectMaxCacheTime has already been called
   */
  void setRedirectMaxCacheTime(long redirectMaxCacheTime);

  public boolean hasProxyInfo();

  /**
   * Returns the set of Proxy-Info AVPs. The returned array contains the AVPs
   * in the order they appear in the message. A return value of null implies
   * that no Proxy-Info AVPs have been set. The elements in the given array
   * are ProxyInfo objects.
   */
  ProxyInfoAvp[] getProxyInfos();

  /**
   * Sets a single Proxy-Info AVP in the message, of type Grouped.
   * 
   * @throws IllegalStateException
   *             if setProxyInfo or setProxyInfos has already been called
   */
  void setProxyInfo(ProxyInfoAvp proxyInfo);

  /**
   * Sets the set of Proxy-Info AVPs, with all the values in the given array.
   * The AVPs will be added to message in the order in which they appear in
   * the array.
   * 
   * Note: the array must not be altered by the caller following this call,
   * and getProxyInfos() is not guaranteed to return the same array instance,
   * e.g. an "==" check would fail.
   * 
   * @throws IllegalStateException
   *             if setProxyInfo or setProxyInfos has already been called
   */
  void setProxyInfos(ProxyInfoAvp[] proxyInfos);

}
