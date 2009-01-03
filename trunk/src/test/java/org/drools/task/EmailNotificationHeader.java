/**
 * 
 */
package org.drools.task;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class EmailNotificationHeader implements Externalizable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long   id;
    
    private String language;    
    
    @Column( name = "replyToAddress" ) // just rename for consistency
    private String replyTo;
        
    @Column( name = "fromAddress" ) // have to rename as schema's break otherwise
    private String from;
    
    private String subject;
    
    @Lob
    private String body;
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong( id );
        
        if ( language != null ) {
            out.writeBoolean( true );
            out.writeUTF( language );
        } else {
            out.writeBoolean( false );
        }
        
        if ( subject != null ) {
            out.writeBoolean( true );
            out.writeUTF( subject );
        } else {
            out.writeBoolean( false );
        }
        
        if ( replyTo != null ) {
            out.writeBoolean( true );
            out.writeUTF( replyTo );
        } else {
            out.writeBoolean( false );
        }
        
        if ( from != null ) {
            out.writeBoolean( true );
            out.writeUTF( from );
        } else {
            out.writeBoolean( false );
        }        
        
        if ( body != null ) {
            out.writeBoolean( true );
            out.writeUTF( body );
        } else {
            out.writeBoolean( false );
        }          
    }

    
    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        id = in.readLong();
        
        if ( in.readBoolean() ) {
            language = in.readUTF();
        }
        
        if ( in.readBoolean() ) {
            subject = in.readUTF();
        }
     
        if ( in.readBoolean() ) {
            replyTo = in.readUTF();
        }
        
        if ( in.readBoolean() ) {
            from = in.readUTF();
        }
        
        if ( in.readBoolean() ) {
            body = in.readUTF();
        }        
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((language == null) ? 0 : language.hashCode());
        result = prime * result + ((replyTo == null) ? 0 : replyTo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof EmailNotificationHeader) ) return false;
        EmailNotificationHeader other = (EmailNotificationHeader) obj;
        if ( subject == null ) {
            if ( other.subject != null ) return false;
        } else if ( !subject.equals( other.subject ) ) return false;
        if ( body == null ) {
            if ( other.body != null ) return false;
        } else if ( !body.equals( other.body ) ) return false;
        if ( from == null ) {
            if ( other.from != null ) return false;
        } else if ( !from.equals( other.from ) ) return false;
        if ( language == null ) {
            if ( other.language != null ) return false;
        } else if ( !language.equals( other.language ) ) return false;
        if ( replyTo == null ) {
            if ( other.replyTo != null ) return false;
        } else if ( !replyTo.equals( other.replyTo ) ) return false;
        return true;
    }
          
}