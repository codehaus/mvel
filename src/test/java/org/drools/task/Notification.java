package org.drools.task;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.drools.task.utils.CollectionUtils;

@Entity
public class Notification implements Externalizable  {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long                             id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "Notification_Documentation_Id", nullable = true)
    private List<I18NText>                   documentation = Collections.emptyList();

    private int                              priority;
    
    @ManyToMany
    @JoinTable(name = "Notification_Recipients", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "entity_id"))    
    private List<OrganizationalEntity>       recipients = Collections.emptyList();;

    @ManyToMany
    @JoinTable(name = "Notification_BusinessAdministrators", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "entity_id"))
    private List<OrganizationalEntity>       businessAdministrators = Collections.emptyList();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "Notification_Names_Id", nullable = true)    
    private List<I18NText> names        = Collections.emptyList();
    
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "Notification_Subjects_Id", nullable = true)    
    private List<I18NText> subjects     = Collections.emptyList();
    
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "Notification_Descriptions_Id", nullable = true)
    @Lob
    private List<I18NText> descriptions = Collections.emptyList();  
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong( id );
        out.writeInt( priority );
        
        CollectionUtils.writeOrganizationalEntityList( recipients, out );
        CollectionUtils.writeOrganizationalEntityList( businessAdministrators, out );                
        
        CollectionUtils.writeI18NTextList( documentation, out );
        CollectionUtils.writeI18NTextList( names, out );
        CollectionUtils.writeI18NTextList( subjects, out );
        CollectionUtils.writeI18NTextList( descriptions, out );
        
    }
    
    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        id = in.readLong();
        priority = in.readInt();
        
        recipients = CollectionUtils.readOrganizationalEntityList( in );
        businessAdministrators = CollectionUtils.readOrganizationalEntityList( in );
                
        documentation = CollectionUtils.readI18NTextList( in );
        names = CollectionUtils.readI18NTextList( in );
        subjects = CollectionUtils.readI18NTextList( in );
        descriptions = CollectionUtils.readI18NTextList( in );        
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public NotificationType getNotificationType() {
        return NotificationType.Default;
    }

    public List<I18NText> getDocumentation() {
        return documentation;
    }

    public void setDocumentation(List<I18NText> documentation) {
        this.documentation = documentation;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<OrganizationalEntity> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<OrganizationalEntity> recipients) {
        this.recipients = recipients;
    }

    public List<OrganizationalEntity> getBusinessAdministrators() {
        return businessAdministrators;
    }

    public void setBusinessAdministrators(List<OrganizationalEntity> businessAdministrators) {
        this.businessAdministrators = businessAdministrators;
    }

    public List<I18NText> getNames() {
        return names;
    }

    public void setNames(List<I18NText> names) {
        this.names = names;
    }

    public List<I18NText> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<I18NText> subjects) {
        this.subjects = subjects;
    }
    
    public List<I18NText> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<I18NText> descriptions) {
        this.descriptions = descriptions;
    }    

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + priority;        
        result = prime * result + CollectionUtils.hashCode( documentation );        
        result = prime * result + CollectionUtils.hashCode( recipients );        
        result = prime * result + CollectionUtils.hashCode( businessAdministrators );
        result = prime * result + CollectionUtils.hashCode( names );       
        result = prime * result + CollectionUtils.hashCode( subjects );        
        result = prime * result + CollectionUtils.hashCode( descriptions );        
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof Notification) ) return false;
        Notification other = (Notification) obj;        
        
        return CollectionUtils.equals( businessAdministrators, other.businessAdministrators ) && CollectionUtils.equals( documentation, other.documentation )
        && CollectionUtils.equals( recipients, other.recipients ) && CollectionUtils.equals( descriptions, other.descriptions ) && CollectionUtils.equals( names, other.names )
        && CollectionUtils.equals( subjects, other.subjects );
    }
                
}
