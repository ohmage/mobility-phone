/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/falaki/phd/projects/systemlog/2.0/src/edu/ucla/cens/systemlog/ISystemLog.aidl
 */
package edu.ucla.cens.systemlog;
public interface ISystemLog extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements edu.ucla.cens.systemlog.ISystemLog
{
private static final java.lang.String DESCRIPTOR = "edu.ucla.cens.systemlog.ISystemLog";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an edu.ucla.cens.systemlog.ISystemLog interface,
 * generating a proxy if needed.
 */
public static edu.ucla.cens.systemlog.ISystemLog asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof edu.ucla.cens.systemlog.ISystemLog))) {
return ((edu.ucla.cens.systemlog.ISystemLog)iin);
}
return new edu.ucla.cens.systemlog.ISystemLog.Stub.Proxy(obj);
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
case TRANSACTION_registerLogger:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _result = this.registerLogger(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isRegistered:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.isRegistered(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_verbose:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _result = this.verbose(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_info:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _result = this.info(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_debug:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _result = this.debug(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_warning:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _result = this.warning(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_error:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _result = this.error(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements edu.ucla.cens.systemlog.ISystemLog
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
     * Registers the given tag with the application name.
     * All logs with the given tag will be recorded with the given
     * application name.
     * 
     *
     * @param       tag         tag that will be used for logging
     * @param       appName     Application name 
     * @return                  registration result. True if succeeds.
     */
public boolean registerLogger(java.lang.String tag, java.lang.String dbTable) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(tag);
_data.writeString(dbTable);
mRemote.transact(Stub.TRANSACTION_registerLogger, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Returns true of the given tag has been registered.
     *
     * @param       tag         tag to check for registeration status
     * @return                  true if the tag has been registered
     */
public boolean isRegistered(java.lang.String tag) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(tag);
mRemote.transact(Stub.TRANSACTION_isRegistered, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Sends the given verbose-level log message to be logged with 
	 * the given tag.
	 *
	 * @param		tag			tag associated with the log message
	 * @param		message		log message
	 */
public boolean verbose(java.lang.String tag, java.lang.String message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(tag);
_data.writeString(message);
mRemote.transact(Stub.TRANSACTION_verbose, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Sends the given info-level log message to be logged with 
	 * the given tag.
	 *
	 * @param		tag			tag associated with the log message
	 * @param		message		log message
	 */
public boolean info(java.lang.String tag, java.lang.String message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(tag);
_data.writeString(message);
mRemote.transact(Stub.TRANSACTION_info, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Sends the given debug-level log message to be logged with 
	 * the given tag.
	 *
	 * @param		tag			tag associated with the log message
	 * @param		message		log message
	 */
public boolean debug(java.lang.String tag, java.lang.String message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(tag);
_data.writeString(message);
mRemote.transact(Stub.TRANSACTION_debug, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Sends the given warning-level log message to be logged with 
	 * the given tag.
	 *
	 * @param		tag			tag associated with the log message
	 * @param		message		log message
	 */
public boolean warning(java.lang.String tag, java.lang.String message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(tag);
_data.writeString(message);
mRemote.transact(Stub.TRANSACTION_warning, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Sends the given error-level log message to be logged with 
	 * the given tag.
	 *
	 * @param		tag			tag associated with the log message
	 * @param		message		log message
	 */
public boolean error(java.lang.String tag, java.lang.String message) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(tag);
_data.writeString(message);
mRemote.transact(Stub.TRANSACTION_error, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_registerLogger = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_isRegistered = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_verbose = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_info = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_debug = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_warning = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_error = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
}
/**
     * Registers the given tag with the application name.
     * All logs with the given tag will be recorded with the given
     * application name.
     * 
     *
     * @param       tag         tag that will be used for logging
     * @param       appName     Application name 
     * @return                  registration result. True if succeeds.
     */
public boolean registerLogger(java.lang.String tag, java.lang.String dbTable) throws android.os.RemoteException;
/**
     * Returns true of the given tag has been registered.
     *
     * @param       tag         tag to check for registeration status
     * @return                  true if the tag has been registered
     */
public boolean isRegistered(java.lang.String tag) throws android.os.RemoteException;
/**
	 * Sends the given verbose-level log message to be logged with 
	 * the given tag.
	 *
	 * @param		tag			tag associated with the log message
	 * @param		message		log message
	 */
public boolean verbose(java.lang.String tag, java.lang.String message) throws android.os.RemoteException;
/**
	 * Sends the given info-level log message to be logged with 
	 * the given tag.
	 *
	 * @param		tag			tag associated with the log message
	 * @param		message		log message
	 */
public boolean info(java.lang.String tag, java.lang.String message) throws android.os.RemoteException;
/**
	 * Sends the given debug-level log message to be logged with 
	 * the given tag.
	 *
	 * @param		tag			tag associated with the log message
	 * @param		message		log message
	 */
public boolean debug(java.lang.String tag, java.lang.String message) throws android.os.RemoteException;
/**
	 * Sends the given warning-level log message to be logged with 
	 * the given tag.
	 *
	 * @param		tag			tag associated with the log message
	 * @param		message		log message
	 */
public boolean warning(java.lang.String tag, java.lang.String message) throws android.os.RemoteException;
/**
	 * Sends the given error-level log message to be logged with 
	 * the given tag.
	 *
	 * @param		tag			tag associated with the log message
	 * @param		message		log message
	 */
public boolean error(java.lang.String tag, java.lang.String message) throws android.os.RemoteException;
}
