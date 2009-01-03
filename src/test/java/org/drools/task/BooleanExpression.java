package org.drools.task;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class BooleanExpression implements Externalizable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long   id;
    private String type;
    
    @Lob
    private String expression;
    
    public BooleanExpression() {
        
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong( id );
        out.writeUTF( type );
        out.writeUTF( expression );        
    }
    
    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        id = in.readLong();
        type = in.readUTF();
        expression = in.readUTF();        
    }
    
    public BooleanExpression(String type, String expression) {
        this.type = type;
        this.expression = expression;        
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof BooleanExpression) ) return false;
        BooleanExpression other = (BooleanExpression) obj;
        if ( expression == null ) {
            if ( other.expression != null ) return false;
        } else if ( !expression.equals( other.expression ) ) return false;
        if ( type == null ) {
            if ( other.type != null ) return false;
        } else if ( !type.equals( other.type ) ) return false;
        return true;
    }

    
}
