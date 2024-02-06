package org.wanji.protocol.codec;

import io.github.yezhihao.protostar.SchemaManager;
import io.github.yezhihao.protostar.util.Explain;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanji.netmc.codec.MessageDecoder;
import org.wanji.netmc.codec.MessageEncoder;
import org.wanji.netmc.session.Session;
import org.wanji.protocol.basic.JTMessage;

/**
 * JTMessageAdapter 同时实现了 MessageEncoder 和 MessageEncoder 两个接口，实现其中的 encoder，decoder方法。
 * 同时将 JTMessageEncoder 和 JTMessageDecoder 作为成员属性，encoder 和 decoder 方法的具体操作调用JTMessageEncoder 和 JTMessageDecoder中的 encode/decoder
 */
public class JTMessageAdapter implements MessageEncoder<JTMessage>, MessageDecoder<JTMessage> {

    protected static final Logger log = LoggerFactory.getLogger(JTMessageAdapter.class);

    private final JTMessageEncoder messageEncoder;

    private final JTMessageDecoder messageDecoder;

    public JTMessageAdapter(String... basePackages) {
        this(new SchemaManager(basePackages));
    }

    public JTMessageAdapter(SchemaManager schemaManager) {
        this(new JTMessageEncoder(schemaManager), new JTMessageDecoder(schemaManager));
    }

    public JTMessageAdapter(JTMessageEncoder messageEncoder, JTMessageDecoder messageDecoder) {
        this.messageEncoder = messageEncoder;
        this.messageDecoder = messageDecoder;
    }

    public ByteBuf encode(JTMessage message, Explain explain) {
        return messageEncoder.encode(message, explain);
    }

    public JTMessage decode(ByteBuf input, Explain explain) {
        return messageDecoder.decode(input, explain);
    }

    @Override
    public JTMessage decode(ByteBuf input, Session session) {
        JTMessage message = messageDecoder.decode(input);
        if (message != null)
            message.setSession(session);
        decodeLog(session, message, input);
        return message;
    }


    @Override
    public ByteBuf encode(JTMessage message, Session session) {
        return null;
    }

    public void encodeLog(Session session, JTMessage message, ByteBuf output) {
        if (log.isInfoEnabled())
            log.info("{}\n>>>>>-{},hex[{}]", session, message, ByteBufUtil.hexDump(output));
    }

    public void decodeLog(Session session, JTMessage message, ByteBuf input) {
        if (log.isInfoEnabled())
            log.info("{}\n<<<<<-{},hex[{}]", session, message, ByteBufUtil.hexDump(input, 0, input.writerIndex()));
    }

}
