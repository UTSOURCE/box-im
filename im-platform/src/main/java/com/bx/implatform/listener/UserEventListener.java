package com.bx.implatform.listener;

import com.bx.imclient.annotation.IMListener;
import com.bx.imclient.listener.EventListener;
import com.bx.imcommon.enums.IMEventType;
import com.bx.imcommon.enums.IMListenerType;
import com.bx.imcommon.model.IMUserEvent;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.implatform.service.FriendService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 用户事件监听
 *
 * @author Blue
 * @version 1.0
 */
@Slf4j
@IMListener(type = IMListenerType.USER_EVENT)
@AllArgsConstructor
public class UserEventListener implements EventListener {

    private final FriendService friendService;


    @Override
    public void process(List<IMUserEvent> events) {
        for (IMUserEvent event : events) {
            if (event.getEventType().equals(IMEventType.ONLINE.code())) {
                // 向好友推送在线状态
                IMUserInfo userInfo = event.getUserInfo();
                friendService.sendOnlineStatus(userInfo.getId(), userInfo.getTerminal());
                log.info("用户上线,id:{},终端：{}", userInfo.getId(), userInfo.getTerminal());
            } else if (event.getEventType().equals(IMEventType.OFFLINE.code())) {
                // 向好友推送在线状态
                IMUserInfo userInfo = event.getUserInfo();
                friendService.sendOnlineStatus(userInfo.getId(), userInfo.getTerminal());
                log.info("用户下线,id:{},终端：{}", userInfo.getId(), userInfo.getTerminal());
            }
        }
    }
}
