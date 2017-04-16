/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * @(#)TransportBean.java
 *
 * Copyright:	Copyright (c) 2016
 * Company:		Oathouse.com Ltd
 */
package io.aistac.common.api.sockets.transport;

import io.aistac.common.canonical.data.ObjectBean;
import io.aistac.common.canonical.data.ObjectEnum;
import io.aistac.common.api.sockets.valueholder.CommandBits;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.jdom2.Element;

/**
 * The {@code TransportBean} Class
 *
 * @author Darryl Oatridge
 * @version 1.00 25-Mar-2016
 */
public class TransportBean extends ObjectBean {

    private static final long serialVersionUID = 2900319025411956636L;

    private volatile int command;
    private volatile String data;

    public TransportBean() {
        super();
        this.command = ObjectEnum.INITIALISATION.value();
        this.data = "";
    }

    public TransportBean(int identifier, int key, int command, String data, String owner) {
        super(identifier, key, owner);
        this.command = command;
        this.data = data == null ? "" : data;
    }

    public int getId() {
        return super.getIdentifier();
    }

    public int getKey() {
        return super.getGroupKey();
    }

    public int getConnectionId() {
        return super.getGroupKey();
    }

    public int getCommand() {
        return command;
    }

    public String getData() {
        return data;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.command;
        hash = 89 * hash + Objects.hashCode(this.data);
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
        final TransportBean other = (TransportBean) obj;
        if(this.command != other.command) {
            return false;
        }
        if(!Objects.equals(this.data, other.data)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * crates all the elements that represent this bean at this level.
     * @return List of elements in order
     */
    @Override
    public List<Element> getXMLElement() {
        List<Element> rtnList = new LinkedList<>();
        // create and add the content Element
        super.getXMLElement().stream().forEach((e) -> {
            rtnList.add(e);
        });
        Element bean = new Element("TransportBean");
        rtnList.add(bean);
        // set the data
         bean.setAttribute("command", Integer.toString(command));
         bean.setAttribute("data", data);

        bean.setAttribute("serialVersionUID", Long.toString(serialVersionUID));
        return(rtnList);
    }

    /**
     * sets all the values in the bean from the XML. Remember to
     * put default values in getAttribute() and check the content
     * of getText() if you are parsing to a value.
     *
     * @param root element of the DOM
     */
    @Override
    public void setXMLDOM(Element root) {
        // extract the super meta data
        super.setXMLDOM(root);
        // extract the bean data
        Element bean = root.getChild("TransportBean");
        // set up the data
         command = Integer.parseInt(bean.getAttributeValue("command", "-1"));
         data = bean.getAttributeValue("data", "");
    }

    @Override
    public synchronized String toXML(XmlFormat... formatArgs) {
        StringBuilder xmlString = new StringBuilder(super.toXML(formatArgs));
        // change the bit values
        if(XmlFormat.PRINTED.isIn(formatArgs)) {
            int start = xmlString.indexOf("<TransportBean command=\"");
            int end = xmlString.indexOf("\" data=\"");
            xmlString.replace(start, end,"<TransportBean command=\"" + CommandBits.getStringFromBits(command).toString());
        }
        return (xmlString.toString());
    }

}
