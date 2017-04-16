/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.aistac.common.api.sockets.handler.observer;

/**
 * http://www.journaldev.com/1739/observer-design-pattern-in-java-example-tutorial
 * @author Darryl Oatridge
 */
public interface IObserver {
    //method to update the observer, used by subject
    public void update();

    //attach with subject to observe
    public void setSubject(ISubject sub);

}
