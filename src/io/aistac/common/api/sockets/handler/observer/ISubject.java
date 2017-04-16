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
public interface ISubject {
    //methods to register and unregister observers
    public void register(IObserver obj);
    public void unregister(IObserver obj);

    //method to notify observers of change
    public void notifyObservers();

    //method to get updates from subject
    public Object getUpdate(IObserver obj);

}
