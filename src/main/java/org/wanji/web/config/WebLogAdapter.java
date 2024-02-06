package org.wanji.web.config;

import io.github.yezhihao.protostar.SchemaManager;
import org.wanji.protocol.codec.JTMessageDecoder;
import org.wanji.protocol.codec.JTMessageEncoder;
import org.wanji.protocol.basic.JTMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.wanji.protocol.codec.JTMessageAdapter;
import reactor.core.publisher.FluxSink;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.wanji.netmc.session.Session;

/**
 * 继承JTMessageAdapter
 */
public class WebLogAdapter extends JTMessageAdapter {

    protected static final Logger log = LoggerFactory.getLogger(WebLogAdapter.class);
    public static final HashMap<Integer, Set<FluxSink<Object>>> platformIds = new HashMap<>();
    public static final HashSet<Integer> ignoreMsgs = new HashSet<>();

/*
    static {
        ignoreMsgs.add(JT808.定位数据批量上传);
    }
*/

    public WebLogAdapter(SchemaManager schemaManager) {
        super(schemaManager);
    }

    public WebLogAdapter(JTMessageEncoder messageEncoder, JTMessageDecoder messageDecoder) {
        super(messageEncoder, messageDecoder);
    }

    @Override
    public void encodeLog(Session session, JTMessage message, ByteBuf output) {
        Set<FluxSink<Object>> emitters = platformIds.get(message.getMsgGnssCenterId());
        if (emitters != null) {
            ServerSentEvent<Object> event = ServerSentEvent.builder().event(Integer.toString(message.getMsgGnssCenterId()))
                    .data(message + "hex:" + ByteBufUtil.hexDump(output, 0, output.writerIndex())).build();
            for (FluxSink<Object> emitter : emitters) {
                emitter.next(event);
            }
        }
        if ((!ignoreMsgs.contains(message.getMsgGnssCenterId())) && (emitters != null || platformIds.isEmpty()))
            super.encodeLog(session, message, output);
    }

    @Override
    public void decodeLog(Session session, JTMessage message, ByteBuf input) {
        if (message != null) {
            Set<FluxSink<Object>> emitters = platformIds.get(message.getMsgGnssCenterId());
            if (emitters != null) {
                ServerSentEvent<Object> event = ServerSentEvent.builder().event(Integer.toString(message.getMsgGnssCenterId()))
                        .data(message + "hex:" + ByteBufUtil.hexDump(input, 0, input.writerIndex())).build();
                for (FluxSink<Object> emitter : emitters) {
                    emitter.next(event);
                }
            }
            if (!ignoreMsgs.contains(message.getMsgGnssCenterId()) && (emitters != null || platformIds.isEmpty()))
                super.decodeLog(session, message, input);

            if (!message.isVerified())
                log.error("<<<<<校验码错误session={},payload={}", session, ByteBufUtil.hexDump(input, 0, input.writerIndex()));
        }
    }

    public static void clearMessage() {
        synchronized (ignoreMsgs) {
            ignoreMsgs.clear();
        }
    }

    public static void addMessage(int messageId) {
        if (!ignoreMsgs.contains(messageId)) {
            synchronized (ignoreMsgs) {
                ignoreMsgs.add(messageId);
            }
        }
    }

    public static void removeMessage(int messageId) {
        if (ignoreMsgs.contains(messageId)) {
            synchronized (ignoreMsgs) {
                ignoreMsgs.remove(messageId);
            }
        }
    }

    public static void clearClient() {
        synchronized (platformIds) {
            platformIds.clear();
        }
    }

    public static void addClient(int platformId, FluxSink<Object> emitter) {
        synchronized (platformIds) {
            platformIds.computeIfAbsent(platformId, k -> new HashSet<>()).add(emitter);
        }
    }

    public static void removeClient(int platformId, FluxSink<Object> emitter) {
        synchronized (platformIds) {
            Set<FluxSink<Object>> emitters = platformIds.get(platformId);
            if (emitters != null) {
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    platformIds.remove(platformId);
                }
            }
        }
    }
}