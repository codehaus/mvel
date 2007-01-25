package org.mvel.jdk14testproj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RandomThing {
    
    public void methodWithNoParams() {
        
    }
    
    public void methodWithOneStringParam(String cheese) {
        
    }
    
    public void methodWithOnePrimitiveIntParam(int cheese) {
        
    }
    
    public void methodWithOneOverloadedParam(String stringParam) {
        
    }
    
    public void methodWithOneOverloadedParam(int intParam) {
        
    }
    
    public List toList(Object object1, Object object2, String object3, int integer, Map map, List inputList) {
        List list = new ArrayList();
        list.add( object1 );
        list.add( object2 );
        list.add( object3 );
        list.add( new Integer( integer ) );
        list.add( map );
        list.add( inputList );
        return list;
    }

}