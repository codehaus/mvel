package org.drools.task;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class I18NText implements Externalizable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long   id;

    private String language;

    @Lob
    private String text;

    public I18NText() {

    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong( id );
        out.writeUTF( language );
        out.writeUTF( text );        
    }
    
    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        id = in.readLong();
        language = in.readUTF();
        text = in.readUTF();        
    }

    public I18NText(String language,
                    String text) {
        this.language = language;
        this.text = text;
    }

    public Long getId() {
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((language == null) ? 0 : language.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof I18NText) ) return false;
        I18NText other = (I18NText) obj;
        if ( language == null ) {
            if ( other.language != null ) return false;
        } else if ( !language.equals( other.language ) ) return false;
        if ( text == null ) {
            if ( other.text != null ) return false;
        } else if ( !text.equals( other.text ) ) return false;
        return true;
    }
    
    public static String getLocalText(List<I18NText> list, String prefferedLanguage, String defaultLanguage) {
        for ( I18NText text : list) {
            if ( text.getLanguage().equals( prefferedLanguage )) {
                return text.getText();
            }
        }
        if (  defaultLanguage == null ) {
            for ( I18NText text : list) {
                if ( text.getLanguage().equals( defaultLanguage )) {
                    return text.getText();
                }
            }    
        }
        return "";
    }


    
}
