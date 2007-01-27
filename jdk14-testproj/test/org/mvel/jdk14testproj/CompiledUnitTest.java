package org.mvel.jdk14testproj;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.mvel.MVEL;

import junit.framework.TestCase;

public class CompiledUnitTest extends TestCase {

    Foo foo = new Foo();
    Map map = new HashMap();
    Base base = new Base();

    public CompiledUnitTest() {
        foo.setBar(new Bar());
        map.put("foo", foo);
        map.put("a", null);
        map.put("b", null);
        map.put("c", "cat");
        map.put("BWAH", "");

        //     map.put("misc", new MiscTestClass());

        map.put("pi", "3.14");
        map.put("hour", "60");
        map.put("zero", new Integer( 0 ) );

        map.put("doubleTen", new Double(10));

        map.put("variable_with_underscore", "HELLO");
    }
    
    public void testComplexExpression() {
        assertEquals("bar", parseDirect("a = 'foo'; b = 'bar'; c = 'jim'; list = {a,b,c}; list[1]"));
    }
    
    public void testShortPathExpression() {
        assertEquals(null, parseDirect("3 > 4 && foo.toUC('test'); foo.register"));
    }
    
    
    public void testAssignment() {
        assertEquals(new Boolean( true ), parseDirect("populate(); blahfoo = 'sarah'; blahfoo == 'sarah'"));
    }

    public void testAssignment2() {
        assertEquals("sarah", parseDirect("populate(); blahfoo = barfoo"));
    }
    
    public void testTernary2() {
        assertEquals("blimpie", parseDirect("zero==1?'foobie':'blimpie'"));
    }

    public void testTernary3() {
        assertEquals("foobiebarbie", parseDirect("zero==1?'foobie':'foobie'+'barbie'"));
    }    
    
    public void testArrayCreation() {
        assertEquals(new Integer( 0 ), parseDirect("arrayTest = {{1, 2, 3}, {2, 1, 0}}; arrayTest[1][2]"));
    }

    public void testMapCreation() {
        assertEquals("sarah", parseDirect("map = ['mike':'sarah','tom':'jacquelin']; map['mike']"));
    }

    public void testMapCreation2() {
        assertEquals("sarah", parseDirect("map = ['mike' :'sarah'  ,'tom'  :'jacquelin'  ]; map['mike']"));
    }

    public void testProjectionSupport() {
        assertEquals(new Boolean( true ), parseDirect("(name in things) contains 'Bob'"));
    }

    public void testProjectionSupport2() {
        assertEquals(new Integer( 3 ), parseDirect("(name in things).size()"));
    }
    
    public void testMethodCallsEtc() {
        parseDirect("title = 1; " +
                "frame = new javax.swing.JFrame; " +
                "label = new javax.swing.JLabel; " +
                "title = title + 1;" +
                "frame.setTitle(title);" +
                "label.setText('MVEL UNIT TEST PACKAGE -- IF YOU SEE THIS, THAT IS GOOD');" +
                "frame.getContentPane().add(label);" +
                "frame.pack();" +
                "frame.setVisible(true);");
    }    
    
    public void testSubListInMap() {
        assertEquals("pear", parseDirect("map = ['test' : 'poo', 'foo' : [c, 'pear']]; map['foo'][1]"));
    }
    
    public void testAssignment3() {
        assertEquals(java.lang.Integer.class, parseDirect("blah = 5").getClass());
    }    
    
    
    public Object parseDirect(String ex) {
        return compiledExecute(ex);
    }

    public Object compiledExecute(String ex) {
        Serializable compiled = MVEL.compileExpression(ex);
        Object first = MVEL.executeExpression(compiled, base, map);
        Object second = MVEL.executeExpression(compiled, base, map);


        if (first != null && !first.getClass().isArray())
            assertEquals(first, second);

        return second;
    }
}
