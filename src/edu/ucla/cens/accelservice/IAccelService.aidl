package edu.ucla.cens.accelservice;

interface IAccelService
{

    /**
     * Returns true if the service is running.
     * 
     * @return                  current state of the service
     */
    boolean isRunning();
    

	/**
	 * Starts the accelerometer service.
     *
     * @param   callerName      String identifying the client
	 */
	 void start(String callerName);
	 
	/**
	 * Stops the accelerometer service to save maximum power.
     *
     * @param   callerName      String identifying the client
	 */
	 void stop(String callerName); 

	/**
	 * Set the rate of accelerometer sampling. This is only a 
	 * suggestion and the service may choose a lower rate to save power. 
	 * Possible values are:
	 * SENSOR_DELAY_FASTEST, SENSOR_DELAY_GAME, 
     * SENSOR_DELAY_NORMA, SENSOR_DELAY_UI
	 * 
	 * @param 	rate	rate of sensor reading
     * @param   callerName      String identifying the client
	 * @return 			the actual rate that was set
	 */
	int suggestRate(String callerName, int rate);
	
	/**
	 * Set the length of the interval that accelerometer is recorded 
	 * before it is turned of (for duty-cycling).
	 *
	 * @param 	length		length of the interval for sensor 
     *                      reading in milliseconds
     * @param   callerName      String identifying the client
	 */
	long setReadingLength(String callerName, long length);

	/**
	 * Set the length of the warm-up interval before the actual
	 * reading interval begins.
	 *
	 * @param 	length		length of the warm-up interval for
	 *                      preparing the accelerometer 
     * @param   callerName      String identifying the client
     *
	 * @return				the new reading length
	 */
	long setWarmupLength(String callerName, long length);

    
	
	/**
	 * Suggest length of the duty-cycling interval. The accelerometer sensor
	 * will be turned off for some time between readings.  This is only a 
	 * suggestion and the service may choose a longer interval to save power
	 * 
	 * @param	interval	suggested length of off interval in milliseconds
     * @param   callerName      String identifying the client
	 * @return				the actual interval in milliseconds
	 */
	long suggestInterval(String callerName, long interval);
	
	/**
	 * Returns the current sleeping interval.
	 * 
	 * @return				current sleep interval used by the 
     *                      service in milliseconds
	 */
	 long getInterval();

	/**
	 * Returns the current rate.
	 * 
	 * @return				current rate
	 */
	 int getRate();

	/**
	 * Returns the current reading length
	 * 
	 * @return				current reading length
	 */
	 long getReadingLength();

	/**
	 * Returns the current length of the warm-up interval
	 * 
	 * @return				current warm-up interval length
	 */
	 long getWarmupLength();

	
	/**
	 * Returns the latest recorded force vector.
	 * 
	 * @return				latest recorded force vector
	 */
	 List getLastForce();
	 
    
	/**
	 * Returns the list of latest recorded X values.
	 * Each element of the list contains an array of values.
	 *
	 * @return				latest recorded values
	 */
	 List getLastXValues();


	/**
	 * Returns the list of latest recorded Y values.
	 * Each element of the list contains an array of values.
	 *
	 * @return				latest recorded values
	 */
	 List getLastYValues();


	/**
	 * Returns the list of latest recorded Z values.
	 * Each element of the list contains an array of values.
	 *
	 * @return				latest recorded values
	 */
	 List getLastZValues();


	/**
	 * Returns the time-stamp of the last recorded value.
	 * This method can be used to verify the freshness of the values.
	 *
	 * @return 			time-stamp of the latest recorded sensor 
     *                    value in milliseconds
	 */
	 long getLastTimeStamp();

}
