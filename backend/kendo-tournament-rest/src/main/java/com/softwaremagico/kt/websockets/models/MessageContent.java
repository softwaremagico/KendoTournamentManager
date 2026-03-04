package com.softwaremagico.kt.websockets.models;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.softwaremagico.kt.websockets.models.messages.MessageContentType;

public class MessageContent {

    private String topic;
    private String payload;
    private MessageContentType type;
    private String actor;
    private String session;

    //Parameters as json content.
    private Object parameters;

    public MessageContent() {
        super();
    }

    public MessageContent(String topic, String payload) {
        this();
        this.topic = topic;
        this.payload = payload;
    }

    public MessageContent(String topic, String payload, MessageContentType type) {
        this(topic, payload);
        setType(type);
    }

    public MessageContent(String topic, String payload, MessageContentType type, String actor, String session) {
        this(topic, payload);
        setType(type);
        setActor(actor);
        setSession(session);
    }

    public MessageContent(String topic, String payload, MessageContentType type, Object parameters) {
        this(topic, payload, type);
        setParameters(parameters);
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public MessageContentType getType() {
        return type;
    }

    public void setType(MessageContentType type) {
        this.type = type;
    }

    public Object getParameters() {
        return parameters;
    }

    public void setParameters(Object parameters) {
        this.parameters = parameters;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
}
