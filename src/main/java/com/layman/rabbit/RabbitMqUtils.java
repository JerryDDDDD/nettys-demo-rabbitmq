package com.layman.rabbit;

import com.layman.entity.CpwMessage;
import com.layman.utils.ContextUtils;
import com.layman.utils.JsonUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @ClassName RabbitMqUtils
 * @Description TODO
 * @Author 叶泽文
 * @Data 2019/10/8 15:31
 * @Version 3.0
 **/
public class RabbitMqUtils {

    private RabbitTemplate rabbitTemplate = ContextUtils.getBean(RabbitTemplate.class);

    public void send2Channel(CpwMessage cpwMessage) {
        rabbitTemplate.convertAndSend("cpw_customer_netty", JsonUtils.objectToJson(cpwMessage));
    }
}
