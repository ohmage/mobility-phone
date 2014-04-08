/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/falaki/phd/projects/systemsens/3.0/src/edu/ucla/cens/systemsens/IPowerMonitor.aidl
 */
package edu.ucla.cens.systemsens;
public interface IPowerMonitor extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements edu.ucla.cens.systemsens.IPowerMonitor
{
private static final java.lang.String DESCRIPTOR = "edu.ucla.cens.systemsens.IPowerMonitor";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an edu.ucla.cens.systemsens.IPowerMonitor interface,
 * generating a proxy if needed.
 */
public static edu.ucla.cens.systemsens.IPowerMonitor asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof edu.ucla.cens.systemsens.IPowerMonitor))) {
return ((edu.ucla.cens.systemsens.IPowerMonitor)iin);
}
return new edu.ucla.cens.systemsens.IPowerMonitor.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_register:
{
data.enforceInterface(DESCRIPTOR);
edu.ucla.cens.systemsens.IAdaptiveApplication _arg0;
_arg0 = edu.ucla.cens.systemsens.IAdaptiveApplication.Stub.asInterface(data.readStrongBinder());
int _arg1;
_arg1 = data.readInt();
this.register(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_unregister:
{
data.enforceInterface(DESCRIPTOR);
edu.ucla.cens.systemsens.IAdaptiveApplication _arg0;
_arg0 = edu.ucla.cens.systemsens.IAdaptiveApplication.Stub.asInterface(data.readStrongBinder());
this.unregister(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_setDeadline:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.setDeadline(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements edu.ucla.cens.systemsens.IPowerMonitor
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Register the application with power monitor
     *
     * @param   app     An implementation of IAdaptiveApplication
     */
public void register(edu.ucla.cens.systemsens.IAdaptiveApplication app, int horizon) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((app!=null))?(app.asBinder()):(null)));
_data.writeInt(horizon);
mRemote.transact(Stub.TRANSACTION_register, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Unregister the application with power monitor
     *
     * @param   app     An implementation of IAdaptiveApplication
     */
public void unregister(edu.ucla.cens.systemsens.IAdaptiveApplication app) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((app!=null))?(app.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregister, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Set the battery deadline for the phone.
     * 
     * @param   deadline    deadline in minutes from now
     */
public void setDeadline(int deadline) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(deadline);
mRemote.transact(Stub.TRANSACTION_setDeadline, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_register = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_unregister = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_setDeadline = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
/**
     * Register the application with power monitor
     *
     * @param   app     An implementation of IAdaptiveApplication
     */
public void register(edu.ucla.cens.systemsens.IAdaptiveApplication app, int horizon) throws android.os.RemoteException;
/**
     * Unregister the application with power monitor
     *
     * @param   app     An implementation of IAdaptiveApplication
     */
public void unregister(edu.ucla.cens.systemsens.IAdaptiveApplication app) throws android.os.RemoteException;
/**
     * Set the battery deadline for the phone.
     * 
     * @param   deadline    deadline in minutes from now
     */
public void setDeadline(int deadline) throws android.os.RemoteException;
}
