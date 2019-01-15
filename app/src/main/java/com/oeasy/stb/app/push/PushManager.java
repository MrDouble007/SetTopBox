package com.oeasy.stb.app.push;

import android.content.Context;
import android.text.TextUtils;

import com.oeasy.stb.utils.Constants;
import com.oeasy.stb.utils.OeLog;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.CloseFuture;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.apache.mina.util.ConcurrentHashSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 推着管理对象
 * 1）初始化连接对象
 * 2）连接上服务器
 * 3）断开连接
 * 4) 网络切换时外部可调用断开重连
 * 5）可注册连接事件监听器EventListener，当连接触发事件时调用监听器返回
 * ------------------------------------------
 * a）网络异常时要自动重连
 * b) 主动断不要激活自动重连机制
 * c) 根据不同的配置可实例多个连接对象
 * d) 主动断开的连接断得彻底，内存泄漏问题
 */
public class PushManager implements EventListener {
    private static final String TAG = "PushManager";
    private static final int STATUS_CONNECTING = 1;     //正在连接
    private static final int STATUS_CONNECTED = 2;      //已连接上
    private static final int STATUS_CONNECTFAIL = 3;    //连接失败
    private static final int STATUS_DISCONNECTING = 4;  //正在断开连接
    private static final int STATUS_DISCONNECTED = 5;   //已断开连接
    private static final int STATUS_DISCONNECTFAIL = 6;   //断开连接失败


    //key = config.getHostName() + ":" + config.getPort()
    private static final Map<String, PushManager> pushManagers = new ConcurrentHashMap<>();
    private int rId = 1;
    private boolean autoConnect = false;     //能不能断开重连,disConnect() 的时候会将此值修改autoConnect = false, connect()的时候会修改autoConnect = true
    private int status;          //连接状态

    //已读的pushid ,防止重复读到服务端的推送
    private Set<Integer> readIds = new ConcurrentHashSet<>();
    //监听器集合
    private Set<EventListener> listenerSet = Collections.synchronizedSet(new HashSet<EventListener>());
    //Nio连接器
    private NioSocketConnector connector;
    //处理数据的会话
    private IoSession session;
    private Context context;
    //连接后台用到的配置信息
    private ServerConfig config;
    private LoginChecker loginChecker;  //登录检查器


    //发送数据给服务器时，如果要识别返回的请求，可以根据Rid来
    public synchronized int getrId() {
        return rId++;
    }


    /**
     * 根据配置文件的hostname+port做为key获取 PushManager 对象
     *
     * @param context
     * @param config
     * @return
     */
    public static synchronized PushManager getInstance(Context context, ServerConfig config) {
        OeLog.i(TAG, "getInstance() config = " + config.toString());
        PushManager pushManager = null;
        String key = config.getHost() + ":" + config.getPort();
        pushManager = pushManagers.get(key);
        OeLog.i(TAG, "getInstance pushManager size " + pushManagers.size() + ", key = " + key);
        if (pushManager != null) {
            OeLog.i(TAG, "getInstance " + key + " exists:" + pushManager);
            return pushManager;
        }
        pushManager = new PushManager(config);
        pushManager.context = context.getApplicationContext();
        pushManager.connect();
        pushManagers.put(key, pushManager);
        return pushManager;
    }

    /**
     * 连接后台服务器
     *
     * @return
     */
    public void connect() {
        OeLog.i(TAG, "connect status is " + status + ", pm is " + this);
        if (status == STATUS_CONNECTING) {
            OeLog.e(TAG, "connector is connecting by pushmanager");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                _connect();
            }
        }.start();
    }

    public IoSession getCurSession() {
        return session;
    }

    /**
     * 重连
     * 1. 销毁连接
     * 2. 实例一个新的pushmanager 对象
     * 3. 连接服务
     * 4. 返回新对象
     */
    public void reConnect() {
        if (session != null) {
            boolean connected = session.isConnected();
            OeLog.i(TAG, "reConnect is session connected " + connected);
            CloseFuture closeFuture = session.closeNow();
            session = null;
            OeLog.i(TAG, "reConnect close session isDone:" + closeFuture.isDone() + " isClosed:" + closeFuture.isClosed());
            closeFuture.addListener(new IoFutureListener<IoFuture>() {
                @Override
                public void operationComplete(IoFuture ioFuture) {
                    // status = STATUS_DISCONNECTED;
                    OeLog.i(TAG, "reConnect session.closeNow success host ip = " + config.getHost() + ", port = " + config.getPort());
                }
            });
            if (loginChecker != null) {
                loginChecker = null;
            }
            //因为session没有连上，所以closeNow不会触发断开重连
            if (!connected) {
                connect();
            }
        } else {
            OeLog.i(TAG, "reConnect session is null ,do connect");
            connect();
        }
    }

    /**
     * 登录到后台
     */
    public void login() {
        OeLog.i(TAG, "start login :[" + config.getId() + "," + "" + config.getToken() + "]");
        RequestData request = new RequestData();
        Map<String, Object> param = new HashMap<>();
        try {
            request.setrId(getrId());
            request.setCommand(1);
            param.put("uid", config.getId());
            param.put("token", config.getToken());
            request.setParam(param);
            boolean sendStatus = sendMessage(request);
            OeLog.i(TAG, "login sendmessage return " + sendStatus);
            loginChecker = new LoginChecker();
            loginChecker.requestTime = System.currentTimeMillis();
            loginChecker.status = LoginChecker.STATUS_LOGGING;
            loginChecker.rid = request.getrId();
            checkLogin();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 指定时间内没有登录返回表示已经失去连接了
     */
    private void checkLogin() {
        new Thread() {
            @Override
            public void run() {
                LoginChecker _checker = loginChecker; //防止执行的过程中logingCehcker被修改了
                while (_checker != null && _checker.status == LoginChecker.STATUS_LOGGING) {
                    if (status != STATUS_CONNECTED) {
                        OeLog.e(TAG, "end login check " + _checker.toString());
                        break;
                    }
                    long requestTime = _checker.requestTime;
                    long now = System.currentTimeMillis();
                    if (now - requestTime > config.getLoginTimeout()) {
                        OeLog.e(TAG, "login time out after " + config.getLoginTimeout());
                        _checker.status = LoginChecker.STATUS_FAIL;
                        autoConnect = false;
                        session.closeNow();
                        session = null;
                        connect();
                    } else {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    _checker = loginChecker;
                    OeLog.i(TAG, "login checking  " + _checker.toString());
                }
            }
        }.start();
    }

    /**
     * 私有的构造函数为了控制实例数量
     *
     * @param _config
     */
    private PushManager(ServerConfig _config) {
        this.config = _config;
        OeLog.i(TAG, "construction start ：" + _config.toString());
        connector = new NioSocketConnector();
        connector.setConnectTimeoutMillis(config.getConnectTimeout());
        connector.setHandler(new RobotHandler(this));
        //设置解码器
        TextLineCodecFactory lineCodec = new TextLineCodecFactory(Charset.forName("UTF-8"));
        lineCodec.setDecoderMaxLineLength(1024 * 1024); //1M
        lineCodec.setEncoderMaxLineLength(1024 * 1024);
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(lineCodec));

        //设置心跳
        connector.getFilterChain().addLast("keepalive", new KeepAliveFilter(new KeepAliveMessageFactoryImpl(), IdleStatus.READER_IDLE,
                KeepAliveRequestTimeoutHandler.DEAF_SPEAKER, config.getKeepAliveTimeInterval(), config.getKeepAliveResponseTimeout()));
        connector.getFilterChain().addLast("executor", new ExecutorFilter());
        //设置断开重连
        connector.getFilterChain().addFirst("reconnection", new IoFilterAdapter() {
            @Override
            public void sessionClosed(NextFilter nextFilter, IoSession ioSession) throws Exception {
                OeLog.e(TAG, "session is closed session: " + ioSession);
                //当主动要断开连接时，autoConnect 会变成 false
                if (!autoConnect) {
                    OeLog.i(TAG, "session is closed, do not reConnect to server by autoConnect is false");
                    return;
                }
                status = STATUS_DISCONNECTED;
                _connect();
            }
        });
        OeLog.i(TAG, "construction end : " + this);
    }


    /**
     * 注册监听器
     *
     * @param eventListener
     */
    public void registerListener(EventListener eventListener) {
        if (eventListener instanceof PushManager) {
            OeLog.e(TAG, "can not register MinaClient");
        }
        listenerSet.add(eventListener);
    }

    /**
     * 注销监听器
     *
     * @param eventListener
     */
    public void unRegisterListener(EventListener eventListener) {
        listenerSet.remove(eventListener);
    }

    /**
     * 发消息到后台
     *
     * @param requestData
     * @return
     */
    public boolean sendMessage(RequestData requestData) {
        if (session == null || !session.isConnected()) {
            OeLog.e(TAG, "sendMessage session disconnect");
            return false;
        }

        try {
            WriteFuture wFuture = session.write(requestData.toJson().toString());
            return wFuture.isWritten();
        } catch (Exception e) {
            OeLog.i(TAG, "SESSION error str: " + requestData.toJson().toString());

        }

        return false;
    }

    /**
     * 更新推着状态为已读
     *
     * @param data
     * @return
     */
    public boolean reportToRead(PushData data) {
        return changeStateToServer(data, Constants.STATE_READ);
    }

    /**
     * 更新推着状态为已执行
     *
     * @param data
     * @return
     */
    public boolean reportToExecute(PushData data) {
        return changeStateToServer(data, Constants.STATE_EXECUTED);
    }

    private boolean changeStateToServer(PushData data, int state) {
        try {
            RequestData request = new RequestData();
            request.setCommand(Constants.COMMAND_UPDATE_PUSH_STATE);
            Map<String, Object> param = new HashMap<>();
            param.put("id", data.getId());
            param.put("uid", config.getId());
            param.put("tagsStr", data.getTagsStr());
            param.put("state", state);
            if (state == Constants.STATE_READ && data.getReadTime() != null) {
                param.put("time", data.getReadTime());
            } else if (data.getExecuteTime() != null) {
                param.put("time", data.getExecuteTime());
            } else {
                param.put("time", new Timestamp(System.currentTimeMillis()));
            }
            request.setParam(param);
            return sendMessage(request);
        } catch (Exception ex) {
            //  ex.printStackTrace();
        }
        return false;
    }


    /**
     * 执行连接,这个方法应该在子线程里调用
     * 1. 声明连接监听器
     * 2. 检查网络
     * 3. 检查配置
     * 4. 检查是否已经连接上了
     * 5. 关闭无效的session
     * 6. 连接到后台
     * 7. 连接成功后自动登录后台
     */
    private void _connect() {
        final long delay = 2000;//2秒
        OeLog.i(TAG, "before _connect disposed: " + connector.isDisposed() + " disposing: " + connector.isDisposing() + " active: " + connector.isActive());
        if (!isNetworkConnect() || connector == null) {
            OeLog.e(TAG, "connect again after " + delay + " millisecond, isNetworkConnect:" + isNetworkConnect() + " connector:" + connector);
            delayConnect(delay);
            return;
        }
        final String host = config.getHost();
        final int port = config.getPort();
        if (TextUtils.isEmpty(host) || port < 1) {
            OeLog.e(TAG, "connect again after " + delay + " millisecond, host name empty or port < 1");
            delayConnect(delay);
            return;
        }

        if (isConnected()) {
            OeLog.i(TAG, "session already connected");
            return;
        }

        //正在连接或者正在断开连接
        while (status == STATUS_CONNECTING || status == STATUS_DISCONNECTING || connector.isDisposing()) {
            OeLog.i(TAG, "waiting connect by status is " + status + " connector isposing " + connector.isDisposing());
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized (connector) {
            try {
                if (isConnected()) {
                    OeLog.i(TAG, "session already connected 2");
                    return;
                }
                status = STATUS_CONNECTING;
                autoConnect = true;
                //连接监听器，用于监听连接状态
                IoFutureListener connectListener = new IoFutureListener<ConnectFuture>() {
                    @Override
                    public void operationComplete(ConnectFuture connectFuture) {
                        String host = connector.getDefaultRemoteAddress().getHostName();
                        int port = connector.getDefaultRemoteAddress().getPort();
                        OeLog.i(TAG, host + ":" + port + " future connect complete, isConnected : " + connectFuture.isConnected() + " isDone : " + connectFuture.isDone());
                        if (connectFuture.getException() != null) {
                            OeLog.e(TAG, host + ":" + port + " future connect get exception " + connectFuture.getException());
                        }

                        if (connectFuture.isConnected()) {
                            session = connectFuture.getSession();// 获取会话
                            OeLog.e(TAG, "connect success !! before socketJNI.ope session " + session + " future.getSession() : " + connectFuture.getSession());
                            OeLog.e(TAG, "************************");
                            OeLog.e(TAG, "connect server " + host + ":" + port + " success! " + session);
                            OeLog.e(TAG, "************************");
                            if (session != null && session.isConnected()) {
                                onConnected();
                                status = STATUS_CONNECTED;
                                login();
                            } else {
                                OeLog.e(TAG, host + ":" + port + " future connect fail because session is null or disConnected do connect agent after " + delay + " millisecond,future.getSession():" + connectFuture.getSession());
                                status = STATUS_CONNECTFAIL;
                                delayConnect(delay);
                            }
                        } else {
                            OeLog.e(TAG, host + ":" + port + " future connect fail do connect agent after " + delay + " millisecond");
                            status = STATUS_CONNECTFAIL;
                            delayConnect(delay);
                        }
                    }
                };

                connector.setDefaultRemoteAddress(new InetSocketAddress(host, port));
                OeLog.i(TAG, "start connect to server host ip : " + host + ", port : " + port);
                ConnectFuture future = connector.connect();
                future.addListener(connectListener);
                //future.isConnected()
                OeLog.i(TAG, "future Connected status  " + future.isConnected());
            } catch (Exception e) {
                OeLog.e(TAG, "connect again after " + delay + " millisecond by get exception" + e.getMessage());
                status = STATUS_CONNECTFAIL;
                delayConnect(delay);
            }
        }
        OeLog.i(TAG, "done to connect server host ip : " + host + ", port : " + port);

    }


    /**
     * 断开连接
     */
    public void disConnect() {
        synchronized (connector) {
            OeLog.i(TAG, "start disconnect host ip = " + config.getHost() + ", port = " + config.getPort());
            autoConnect = false;

            if (session != null) {
                status = STATUS_DISCONNECTING;
                OeLog.i(TAG, "is session connected " + session.isConnected());
                CloseFuture closeFuture = session.closeNow();
                OeLog.i(TAG, "disConnect isDone:" + closeFuture.isDone() + " isClosed:" + closeFuture.isClosed());
                closeFuture.addListener(new IoFutureListener<IoFuture>() {
                    @Override
                    public void operationComplete(IoFuture ioFuture) {
                        status = STATUS_DISCONNECTED;
                        OeLog.i(TAG, "disconnect success host ip = " + config.getHost() + ", port = " + config.getPort());
                    }
                });
                session = null;
                if (loginChecker != null) {
                    loginChecker = null;
                }
            } else {
                OeLog.i(TAG, "disconnect do nothing , session is " + session);
            }
            OeLog.i(TAG, "before dispose disposed: " + connector.isDisposed() + " disposing: " + connector.isDisposing() + " active: " + connector.isActive());
            if (!connector.isDisposed() && !connector.isDisposing()) {
                connector.dispose();
            }
            OeLog.i(TAG, "after dispose disposed: " + connector.isDisposed() + " disposing: " + connector.isDisposing() + " active: " + connector.isActive());
            status = STATUS_DISCONNECTED;
        }
        OeLog.i(TAG, "disconnected host ip = " + config.getHost() + ", port = " + config.getPort());
//        pushManagers.remove(config.getHostName());
    }

    /**
     * 销毁连接
     */
    public void destroyConnect() {
        OeLog.i(TAG, "destroyConnect");
        disConnect();
        //connector.dispose();
        String key = config.getHost() + ":" + config.getPort();
        pushManagers.remove(key);
    }

    /**
     * 是否连接着
     *
     * @return
     */
    public boolean isConnected() {
        return (connector.isActive() && session != null && session.isConnected());
    }

    @Override
    public void onConnected() {
        OeLog.i(TAG, "onConnected listenerSet size is" + listenerSet.size());
        for (EventListener listener : listenerSet) {
            listener.onConnected();
        }
    }

    @Override
    public void onExceptionCaught(Throwable cause) {
        OeLog.e(TAG, "onExceptionCaught " + cause.getMessage());
        for (EventListener listener : listenerSet) {
            listener.onExceptionCaught(cause);
        }
    }

    @Override
    public void onMessageSent(Object message) {
        try {
            OeLog.i(TAG, "onMessageSent: " + message);
            for (EventListener listener : listenerSet) {
                listener.onMessageSent(message);
            }
        } catch (Exception ex) {
            OeLog.e(TAG, "onMessageSent error " + ex.getMessage());
        }
    }

    public void onLoginSuccess(Object message) {
        OeLog.e(TAG, "onLoginSuccess " + message);

        //测试获取推送数据
        if (message instanceof JSONObject) {
            JSONObject json = (JSONObject) message;
            ResponseData response = ResponseData.fromJson(json);
            Map<String, Object> data = response.getData();
            if (data == null || data.isEmpty()) {
                return;
            }

            ArrayList<PushData> pushDataList;
            if (data.containsKey("pushList")) {
                try {
                    JSONArray array = (JSONArray) data.get("pushList");
                    pushDataList = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jPush = array.getJSONObject(i);
                        PushData pushData = PushData.fromJson(jPush);
                        OeLog.i(TAG, "pushDate: " + pushData.toString());
                        pushDataList.add(pushData);
                    }

                    // 按照时间排序
                    Collections.sort(pushDataList, new Comparator<PushData>() {
                        @Override
                        public int compare(PushData pd1, PushData pd2) {
                            return pd1.getEffectTime().compareTo(pd2.getEffectTime());
                        }
                    });

                    for (PushData pushData : pushDataList) {
                        for (EventListener listener : listenerSet) {
                            listener.onLoginSuccess(pushData);
                        }
                        changeStateToServer(pushData, PushData.STATE_READED);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onMessageReceived(Object message) {
        OeLog.i(TAG, "onMessageReceived: " + message);
        if (!(message instanceof JSONObject)) {
            OeLog.i(TAG, "onMessageReceived: error data format" + message);
            return;
        }
        JSONObject json = (JSONObject) message;
        if (RobotHandler.isRequest(json)) {
            JSONObject param = json.optJSONObject("param");
            if (param == null) {
                return;
            }
            int pid = param.optInt("id");
            if (readIds.contains(pid)) {
                OeLog.e(TAG, pid + "========already get this message！======");
                return;
            }
            readIds.add(pid);
            PushData data = PushData.fromJson(param);

            for (EventListener listener : listenerSet) {
                listener.onMessageReceived(data);
            }
            changeStateToServer(data, PushData.STATE_READED);
        } else if (RobotHandler.isResponse(json)) {
            int rid = json.optInt("rId");
            if (loginChecker != null && loginChecker.rid == rid) {
                loginChecker.responseTime = System.currentTimeMillis();
                if (json.optInt("code") == 1) {
                    loginChecker.status = LoginChecker.STATUS_SUCCESS;
                    onLoginSuccess(message);
                } else {
                    loginChecker.status = LoginChecker.STATUS_FAIL;
                }
            } else {
                //大于0的是登录操作
                if (rid > 0) {
                    if (loginChecker != null) {
                        OeLog.i(TAG, " :" + loginChecker.toString());
                    } else {
                        OeLog.i(TAG, "loginChecker is null");
                    }
                }
            }
        }

    }

    public void delayConnect(final long delay) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                _connect();
            }
        }.start();
    }

    @Override
    public void onDisConnected() {
        OeLog.i(TAG, "onDisConnected!!!");
        for (EventListener listener : listenerSet) {
            listener.onDisConnected();
        }
    }

    private static class KeepAliveMessageFactoryImpl implements KeepAliveMessageFactory {

        @Override
        public boolean isRequest(IoSession session, Object message) {
            return message.equals(RobotHandler.HEARTBEAT_REQUEST);
        }

        @Override
        public boolean isResponse(IoSession session, Object message) {
            return message.equals(RobotHandler.HEARTBEAT_RESPONSE);
        }

        @Override
        public Object getRequest(IoSession session) {
            /** 返回预设语句 */
            return RobotHandler.HEARTBEAT_REQUEST;
        }

        @Override
        public Object getResponse(IoSession session, Object request) {
            /** 返回预设语句 */
            return RobotHandler.HEARTBEAT_RESPONSE;
        }
    }

    private boolean isNetworkConnect() {
        if (context == null) {
            return false;
        }
/*        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isConnected();
            }
        }
        return false;*/
        return true;
    }

    @Override
    public void finalize() {
        OeLog.i(TAG, "finalize");
        destroyConnect();
        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}