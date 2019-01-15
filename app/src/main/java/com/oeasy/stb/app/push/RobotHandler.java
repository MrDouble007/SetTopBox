package com.oeasy.stb.app.push;

import com.oeasy.stb.utils.OeLog;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.json.JSONException;
import org.json.JSONObject;

public class RobotHandler extends IoHandlerAdapter {
    private static final String TAG = RobotHandler.class.getSimpleName();

    public static final String HEARTBEAT_REQUEST = "0x16";
    public static final String HEARTBEAT_RESPONSE = "0x18";
    private EventListener listener;

    RobotHandler(EventListener listener) {
        this.listener = listener;
    }

    @Override
    public void messageReceived(IoSession iosession, Object message) throws Exception {
        OeLog.i(TAG, "messageReceived : " + message.toString() + " \r\nsession is " + iosession);
        //心跳返回
        if (HEARTBEAT_RESPONSE.equals(message)) {
            return;
        }
        try {
            JSONObject json = new JSONObject(message.toString());
            if (isRequest(json) || isResponse(json)) {
                listener.onMessageReceived(json);
            }
        } catch (JSONException je) {
            OeLog.e(TAG, "messageReceived get exception " + je.getMessage());
            je.printStackTrace();
        }

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        OeLog.e(TAG, "exceptionCaught " + cause.getMessage());
        super.exceptionCaught(session, cause);
        listener.onExceptionCaught(cause);
    }

    @Override
    public void messageSent(IoSession iosession, Object message) throws Exception {
        OeLog.i(TAG, "send message " + message.toString());
        listener.onMessageSent(message);
    }

    public static boolean isRequest(JSONObject json) {
        return json.has("rId") && json.has("command") && json.has("param");
    }

    public static boolean isResponse(JSONObject json) {
        return json.has("rId") && json.has("code");
    }

    @Override
    public void sessionClosed(IoSession iosession) throws Exception {
        OeLog.i(TAG, "sessionClosed : " + iosession);
    }

    @Override
    public void sessionIdle(IoSession iosession, IdleStatus idlestatus) throws Exception {
        OeLog.i(TAG, "sessionIdle : " + iosession);
    }

    @Override
    public void sessionOpened(IoSession iosession) throws Exception {
        OeLog.i(TAG, "sessionOpened : " + iosession);
    }
}
