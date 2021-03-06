//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.interfaces
 * File         : ApplicationListObserver.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.application;

import blackberry.interfaces.Observer;



/**
 * An asynchronous update interface for receiving notifications about
 * ApplicationList information as the ApplicationList is constructed.
 */
public interface ApplicationObserver extends Observer {
    //#ifdef DEBUG
    //#endif
    /**
     * This method is called when information about an ApplicationList which was
     * previously requested using an asynchronous interface becomes available.
     * 
     * @param startedList
     *            the started list
     * @param stoppedList
     *            the stopped list
     */
    /*
     * void onApplicationListChange(final Vector startedListName, final Vector
     * stoppedListName, final Vector startedListMod, final Vector
     * stoppedListMod);
     */

    void onApplicationChange(String startedName, String stoppedName,
            String startedMod, String stoppedMod);
}
