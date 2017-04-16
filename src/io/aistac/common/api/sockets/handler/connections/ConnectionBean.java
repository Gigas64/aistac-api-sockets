 /*
 * @(#)ConnectionBean.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.handler.connections;

import io.aistac.common.canonical.data.ObjectBean;
import io.aistac.common.canonical.data.ObjectEnum;
import static io.aistac.common.api.sockets.handler.connections.ConnectionTypeEnum.UNDEFINED;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jdom2.Element;

/**
 * The {@code ConnectionBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 28-Mar-2016
 */
public class ConnectionBean extends ObjectBean {

    private static final long serialVersionUID = 5407933807120039835L;

    private volatile ConnectionTypeEnum type;
    private volatile String host;
    private volatile int port;
    private volatile int taskId;

    public ConnectionBean() {
        super();
        this.type = UNDEFINED;
        this.host = "";
        this.port = ObjectEnum.INITIALISATION.value();
        this.taskId = ObjectEnum.INITIALISATION.value();
    }

    public ConnectionBean(int identifier, ConnectionTypeEnum type, String host, int port, int taskId, String owner) {
        super(identifier, owner);
        this.type = type;
        this.host = host == null ? "localhost" : host;
        this.port = port;
        this.taskId = taskId;
    }

    public int getId() {
        return super.getIdentifier();
    }

    public int getKey() {
        return super.getGroupKey();
    }

    public ConnectionTypeEnum getType() {
        return type;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getTaskId() {
        return taskId;
    }

    protected void setTaskId(int taskId, String owner) {
        super.setOwner(owner);
        this.taskId = taskId;
    }

    public String getQueueIn() {
        return "connection." + String.format("%05d", getIdentifier()) + "." + getOwner() + ".queue.in";
    }

    public String getQueueOut() {
        return "connection." + String.format("%05d", getIdentifier()) + "." + getOwner() + ".queue.out";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.type);
        hash = 29 * hash + Objects.hashCode(this.host);
        hash = 29 * hash + this.port;
        hash = 29 * hash + this.taskId;
        return hash + super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final ConnectionBean other = (ConnectionBean) obj;
        if(this.port != other.port) {
            return false;
        }
        if(this.taskId != other.taskId) {
            return false;
        }
        if(!Objects.equals(this.host, other.host)) {
            return false;
        }
        if(this.type != other.type) {
            return false;
        }
        return super.equals(obj);
    }


    /**
     * crates all the elements that represent this bean at this level.
     *
     * @return List of elements in order
     */
    @Override
    public List<Element> getXMLElement() {
        List<Element> rtnList = new LinkedList<>();
        // create and add the content Element
        super.getXMLElement().stream().forEach((e) -> {
            rtnList.add(e);
        });
        Element bean = new Element("ConnectionBean");
        rtnList.add(bean);
        // set the data
        bean.setAttribute("type",type.toString());
        bean.setAttribute("host", host);
        bean.setAttribute("port", Integer.toString(port));
        bean.setAttribute("taskId", Integer.toString(taskId));

        bean.setAttribute("serialVersionUID", Long.toString(serialVersionUID));
        return (rtnList);
    }

    /**
     * sets all the values in the bean from the XML. Remember to put default values in getAttribute() and check the content of getText() if
     * you are parsing to a value.
     *
     * @param root element of the DOM
     */
    @Override
    public void setXMLDOM(Element root) {
        // extract the super meta data
        super.setXMLDOM(root);
        // extract the bean data
        Element bean = root.getChild("ConnectionBean");
        // set up the data
        type = ConnectionTypeEnum.valueOf(bean.getAttributeValue("type", "NO_VALUE"));
        host = bean.getAttributeValue("host", "");
        port = Integer.parseInt(bean.getAttributeValue("port", "-1"));
        taskId = Integer.parseInt(bean.getAttributeValue("taskId", "-1"));

    }

}
