package com.layman.receive;

import com.layman.entity.CacheChannel;
import com.layman.entity.CpwMessage;
import com.layman.entity.CustomerUserType;
import com.layman.utils.JsonUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName RabbitMqReceive
 * @Description TODO
 * @Author 叶泽文
 * @Data 2019/10/8 21:19
 * @Version 3.0
 **/
@Component
@RabbitListener(queues = "cpw_customer_netty")
public class RabbitMqReceive {

    @RabbitHandler
    public void showMessage (String message) {
        System.out.println(message);
        CpwMessage cpwMessage = JsonUtils.jsonToPojo(message, CpwMessage.class);
        // CpwMessage 不可以为空
        if (cpwMessage == null) {
            return;
        }
        switch (cpwMessage.getToRole()) {
            case CustomerUserType.admin: {
                sendMessageToChannel(cpwMessage, CacheChannel.adminChannelMap);
            }
            case CustomerUserType.company: {
                sendMessageToChannel(cpwMessage, CacheChannel.companyChannelMap);
            }
            case CustomerUserType.user: {
                sendMessageToChannel(cpwMessage, CacheChannel.userChannelMap);
            }
        }
    }


    // 发送到信道中
    private void sendMessageToChannel(CpwMessage cpwMessage, Map map) {
        String toId = cpwMessage.getToId();
        // 如果toId为空 消息不正正确直接返回, 不发送
        if (!StringUtils.isNotBlank(toId)) {
            return;
        }
        // 群发
        if (toId.equals("0000")) {
            // 迭代信道, 遍历发送消息
            Iterator iterator = map.values().iterator();
            while (iterator.hasNext()) {
                ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) iterator.next();
                channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(cpwMessage)));
            }
        } else {
            // 单发
            ChannelHandlerContext channelHandlerContext = (ChannelHandlerContext) map.get(toId);
            // 判断这个单发的信道是否在自己服务器中
            if (channelHandlerContext != null) {
                System.out.println(channelHandlerContext.channel().isActive());
                channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(cpwMessage)));
            }
        }
    }
}
