package org.drools.task;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public abstract class OrganizationalEntity implements Externalizable {
    
    @Id
    private String id;   
    
    public OrganizationalEntity() {
    }
        
    
    public OrganizationalEntity(String id ) {
        this.id = id;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF( id );
        
    } 
    
    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        id = in.readUTF();
    }      
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof OrganizationalEntity) ) return false;
        OrganizationalEntity other = (OrganizationalEntity) obj;
        if ( id == null ) {
            if ( other.id != null ) return false;
        } else if ( !id.equals( other.id ) ) return false;
        return true;
    }     
    
    public String toString() {
        return "[" + getClass().getSimpleName() + ":'" + id + "']";
    }
}
