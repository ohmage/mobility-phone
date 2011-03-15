package edu.ucla.cens.wifigpslocation;


oneway interface ILocationChangedCallback {
	/**
	 * Is called when the service detects that location has changed.
	 *
	 */
	void locationChanged ();

}
