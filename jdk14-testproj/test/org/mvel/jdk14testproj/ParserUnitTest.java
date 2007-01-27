package org.mvel.jdk14testproj;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.mvel.Interpreter;
import org.mvel.MVEL;
import org.mvel.tests.main.res.Bar;
import org.mvel.tests.main.res.Base;
import org.mvel.tests.main.res.Foo;

public class ParserUnitTest extends TestCase {
    

    Foo foo = new Foo();
    Map map = new HashMap();
    Base base = new Base();

    public ParserUnitTest() {
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
        assertEquals(new Boolean( true ), parseDirect("populate(); blahfoo = barfoo; blahfoo == 'sarah'"));
    }

    public void testAssignment2() {
        assertEquals("sarah", parse("@{populate(); blahfoo = barfoo}"));
    }
    
    public void testTernary2() {
        assertEquals("blimpie", parse("@{zero==1?'foobie':'blimpie'}"));
    }

    public void testTernary3() {
        assertEquals("foobiebarbie", parse("@{zero==1?'foobie':'foobie'+'barbie'}"));
    }    
    
    public void testArrayCreation() {
        assertEquals("foobie", parseDirect("a = {{'foo', 'bar'}, {'foobie', 'barbie'}}; a[1][0]"));
    }
    
    public void testMapCreation() {
        assertEquals("sarah", parse("@{map = ['mike':'sarah','tom':'jacquelin']; map['mike']}"));
    }

    public void testProjectionSupport() {
        assertEquals(new Boolean( true ), parse("@{(name in things) contains 'Bob'}"));
    }

    public void testProjectionSupport2() {
        assertEquals(new Integer( 3 ), parse("@{(name in things).size()}"));
    }
    
    public void testMethodCallsEtc() {
        parseDirect("title = 1; " +
                "frame = new javax.swing.JFrame; " +
                "label = new javax.swing.JLabel; " +
                "title = title + 1;" +
                "frame.setTitle(title);" +
                "label.setText('this is a test of mvel');" +
                "frame.setVisible(true);");
    }
    
    public void testTernary4() {
        assertEquals("no", parse("@{ackbar ? 'yes' : 'no'}"));
    }
    
    public void testArrayCreation2() {
        assertEquals(new Integer( 5 ), parseDirect("a = {1,3,5}; a[2]"));
    }

    public void testTokenMethodAccess() {
        assertEquals(String.class, parse("@{a = 'foo'; a.getClass()}"));
    }
    
    public void testComplexExpression2() {
        assertEquals("foobar", parseDirect("x = 'bar'; y = 'foo'; array = {y,x}; array[0] + array[1]"));
    }
    
    public void testListCreation() {
        assertEquals("foobar", parseDirect("test = ['apple', 'pear', 'foobar']; test[2]"));
    }
    
    public void testInlineVarAssignment() {
        assertTrue(( (Boolean) parseDirect("x = ((a = 100) + (b = 200) + (c = 300)); (a == 100 && b == 200 && c == 300 && x == 600)")).booleanValue());
    }    
    
    public Object parseDirect(String ex) {
        return MVEL.eval(ex, base, map);
    }    
    
    public Object parse(String ex) {
        return Interpreter.parse(ex, base, map);
    }    
}
