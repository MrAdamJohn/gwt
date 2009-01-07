/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.user.client.ui.impl;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;

/**
 * Implementation class used by {@link com.google.gwt.user.client.ui.PopupPanel}.
 * This implementation is identical to the implementation provided by
 * {@link com.google.gwt.user.client.ui.impl.PopupImpl} in the case where
 * Mozilla is NOT running on the Mac.
 * <p>
 * A different implemention is provided for the Mac in order to prevent
 * scrollbars underneath the PopupPanel from being rendered on top of the
 * PopupPanel (issue #410). Unfortunately, the solution that fixes this problem
 * for the Mac causes a problem with dragging a
 * {@link com.google.gwt.user.client.ui.DialogBox} on Linux. While dragging the
 * DialogBox (especially diagonally), it jitters significantly.
 * </p>
 * <p>
 * We did not introduce a deferred binding rule for Mozilla on the Mac because
 * this is the first instance in which we have a Mozilla-related bug fix which
 * does not work on all platforms.
 * </p>
 * <p>
 * This implementation can be simplified in the event that the jittering problem
 * on Linux is fixed, or the scrollbar rendering problem on the Mac is fixed.
 * </p>
 */
public class PopupImplMozilla extends PopupImpl {

  /**
   * Cache the value to avoid repeated calls.
   */
  private static boolean isMac = isMac();
  
  private static native boolean isMac() /*-{
    if (navigator.userAgent.indexOf("Macintosh") != -1) {
      return true;
    }  
    return false;
  }-*/;

  @Override
  public Element createElement() {
    final Element outerElem = DOM.createDiv();

    if (isMac) {
      // To solve the scrollbar rendering problem on the Mac, we have to make
      // the PopupPanel a 'heavyweight' element by setting a style of
      // 'overflow:auto' on the outermost div. This ensures that all of the
      // elements that are children of this div will be rendered on top of
      // any underlying scrollbars.

      // Unfortunately, if we add a border to the outer div (which has
      // a style of 'overflow:auto'), the border will not be rendered on top
      // of underlying scrollbars. To get around this problem, we introduce an
      // inner div which acts as the new containing element for the PopupPanel,
      // and this element is the one to which all styling is applied to.
      DOM.setInnerHTML(outerElem, "<div></div>");

      // Mozilla determines the default stacking order for heavyweight elements
      // by their order on the page. If the PopupPanel is declared before
      // another
      // heavyweight element on the page, then the scrollbars of the heavyweight
      // element will still shine through the PopupPanel. By setting
      // 'overflow:auto' after all of the elements on the page have been
      // rendered,
      // the PopupPanel becomes the highest element in the stacking order.
      DeferredCommand.addCommand(new Command() {
        public void execute() {
          DOM.setStyleAttribute(outerElem, "overflow", "auto");
        }
      });
    }

    return outerElem;
  }

  @Override
  public Element getContainerElement(Element outerElem) {
    return isMac ? DOM.getFirstChild(outerElem) : outerElem;
  }

  @Override
  public Element getStyleElement(Element outerElem) {
    return isMac ? outerElem : super.getStyleElement(outerElem);
  }

  @Override
  public void setClip(Element popup, String rect) {
    super.setClip(popup, rect);
    DOM.setStyleAttribute(popup, "display", "none");
    DOM.setStyleAttribute(popup, "display", "");
  }
}
