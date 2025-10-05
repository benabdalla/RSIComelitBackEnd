package com.rsi.comelit.dto;

import com.rsi.comelit.enumeration.NotificationType;
import lombok.Data;

import java.util.Date;

@Data
public class NotificationDto {
    private Long id;
    private NotificationType type;
    private UserSimpleDto receiver;
    private UserSimpleDto sender;
    private Boolean isSeen;
    private Boolean isRead;
    private Date dateCreated;
    private Date dateUpdated;
    private Date dateLastModified;

}
