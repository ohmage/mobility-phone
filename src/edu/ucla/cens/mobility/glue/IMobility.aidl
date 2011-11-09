package edu.ucla.cens.mobility.glue;

// Declare the interface.
interface IMobility 
{
    // You can pass values in, out, or inout. 
    // Primitive datatypes (such as int, boolean, etc.) can only be passed in.
    void stopMobility();
    void startMobility();
    boolean changeMobilityRate(in int intervalInSeconds);
    boolean isMobilityOn();
    int getMobilityInterval();
    
}