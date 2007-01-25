package org.mvel.jdk14testproj;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;

import junit.framework.TestCase;

import org.mvel.util.ParseTools;

public class ParseToolsTest extends TestCase {
    
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};
    private Method[] methods;

    protected void setUp() throws Exception {
        super.setUp();
        
        methods = RandomThing.class.getMethods();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGetBestCandidate_NoParams() throws Exception {
        Method method = ParseTools.getBestCanadidate( EMPTY_OBJECT_ARRAY, "methodWithNoParams", methods );
        assertMethod( "methodWithNoParams", method );
    }
    
    public void testGetBestCandidate_OneStringParam() throws Exception {
        Method method = ParseTools.getBestCanadidate( EMPTY_OBJECT_ARRAY, "methodWithOneStringParam", methods );
        assertNull( method );
        
        method = ParseTools.getBestCanadidate( new Object[] { "Bob is awesome" }, "methodWithOneStringParam", methods );
        assertMethod( "methodWithOneStringParam", method, new Class[] { String.class } );
    }
    
    public void testGetBestCandidate_OnePrimitiveIntParam() throws Exception {
        Method method = ParseTools.getBestCanadidate( EMPTY_OBJECT_ARRAY, "methodWithOnePrimitiveIntParam", methods );
        assertNull( method );
        
        method = ParseTools.getBestCanadidate( new Object[] { new Integer(42) }, "methodWithOnePrimitiveIntParam", methods );
        assertMethod( "methodWithOnePrimitiveIntParam", method, new Class[] { Integer.TYPE } );
        
        method = ParseTools.getBestCanadidate( new Object[] { new Short((short) 1) }, "methodWithOnePrimitiveIntParam", methods );
        assertMethod( "methodWithOnePrimitiveIntParam", method, new Class[] { Integer.TYPE } );
        
        method = ParseTools.getBestCanadidate( new Object[] { new Long((long) 42) }, "methodWithOnePrimitiveIntParam", methods );
        assertMethod( "methodWithOnePrimitiveIntParam", method, new Class[] { Integer.TYPE } );
        
        method = ParseTools.getBestCanadidate( new Object[] { new BigInteger("42") }, "methodWithOnePrimitiveIntParam", methods );
        assertMethod( "methodWithOnePrimitiveIntParam", method, new Class[] { Integer.TYPE } );
        
        method = ParseTools.getBestCanadidate( new Object[] { new Float( 42.41 ) }, "methodWithOnePrimitiveIntParam", methods );
        assertMethod( "methodWithOnePrimitiveIntParam", method, new Class[] { Integer.TYPE } );
        
        method = ParseTools.getBestCanadidate( new Object[] { new Double( 42.41 ) }, "methodWithOnePrimitiveIntParam", methods );
        assertMethod( "methodWithOnePrimitiveIntParam", method, new Class[] { Integer.TYPE } );
        
        method = ParseTools.getBestCanadidate( new Object[] { new BigDecimal( "42.41" ) }, "methodWithOnePrimitiveIntParam", methods );
        assertMethod( "methodWithOnePrimitiveIntParam", method, new Class[] { Integer.TYPE } );
        
        method = ParseTools.getBestCanadidate( new Object[] { "42.41" }, "methodWithOnePrimitiveIntParam", methods );
        assertMethod( "methodWithOnePrimitiveIntParam", method, new Class[] { Integer.TYPE } );
        
        // does this seem weird?
        method = ParseTools.getBestCanadidate( new Object[] { "I like tacos" }, "methodWithOnePrimitiveIntParam", methods );
        assertMethod( "methodWithOnePrimitiveIntParam", method, new Class[] { Integer.TYPE } );
    }
    
    public void testGetBestCandidate_OneOverloadedParam() throws Exception {
        Method method = ParseTools.getBestCanadidate( EMPTY_OBJECT_ARRAY, "methodWithOneOverloadedParam", methods );
        assertNull( method );
        
        method = ParseTools.getBestCanadidate( new Object[] { new Integer(42) }, "methodWithOneOverloadedParam", methods );
        assertMethod( "methodWithOneOverloadedParam", method, new Class[] { Integer.TYPE } );
        
        method = ParseTools.getBestCanadidate( new Object[] { "I still like tacos" }, "methodWithOneOverloadedParam", methods );
        assertMethod( "methodWithOneOverloadedParam", method, new Class[] { String.class } );
        
        method = ParseTools.getBestCanadidate( new Object[] { new Short( (short) 1 ) }, "methodWithOneOverloadedParam", methods );
        assertMethod( "methodWithOneOverloadedParam", method, new Class[] { Integer.TYPE } );
        
        method = ParseTools.getBestCanadidate( new Object[] { new Long( 42 ) }, "methodWithOneOverloadedParam", methods );
        assertMethod( "methodWithOneOverloadedParam", method, new Class[] { Integer.TYPE } );
        
        method = ParseTools.getBestCanadidate( new Object[] { new BigInteger( "42" ) }, "methodWithOneOverloadedParam", methods );
        assertMethod( "methodWithOneOverloadedParam", method, new Class[] { Integer.TYPE } );
        
    }
    
    protected void assertMethod(String name, Method method) {
        assertNotNull( method );
        assertTrue( Modifier.isPublic( method.getModifiers() ) );
        assertFalse( Modifier.isStatic( method.getModifiers() ) );
        assertEquals( name, method.getName() );
    }
    
    protected void assertMethod(String name, Method method, Class[] paramTypes) {
        assertMethod( name, method );
        
        Class[] actualParamTypes = method.getParameterTypes();
        
        assertEquals( "not the same number of parameters", paramTypes.length, actualParamTypes.length );
        
        for ( int i = 0 ; i < paramTypes.length ; ++i ) {
            if ( ! paramTypes[i].equals( actualParamTypes[i] ) ) {
                fail( "parameter " + (i+1) + " expected to be " + paramTypes[i].getName() + " but was " + actualParamTypes[i].getName() ); 
            }
        }
    }

}
