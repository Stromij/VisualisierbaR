package com.github.bachelorpraktikum.visualisierbar.view;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public interface moveable {
     /**
      * Allows this object to be moveable and change the represented object if set to true
      * @param moveable
      */
     void setMoveable(boolean moveable);
     /**
      * @return whether this thing is moveable or not
      */
     boolean getMoveable();




}
