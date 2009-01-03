package org.drools.task;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.drools.task.utils.CollectionUtils;

@Embeddable
public class PeopleAssignments
    implements
    Externalizable {
    @ManyToOne()
    private User                       taskInitiator;

    @ManyToMany
    @JoinTable(name = "PeopleAssignments_PotentialOwners", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "entity_id"))
    private List<OrganizationalEntity> potentialOwners        = Collections.emptyList();

    @ManyToMany
    @JoinTable(name = "PeopleAssignments_ExcludedOwners", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "entity_id"))
    private List<OrganizationalEntity> excludedOwners         = Collections.emptyList();

    @ManyToMany
    @JoinTable(name = "PeopleAssignments_TaskStakeholders", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "entity_id"))
    private List<OrganizationalEntity> taskStakeholders       = Collections.emptyList();

    @ManyToMany
    @JoinTable(name = "PeopleAssignments_BusinessAdministrators", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "entity_id"))
    private List<OrganizationalEntity> businessAdministrators = Collections.emptyList();

    @ManyToMany
    @JoinTable(name = "PeopleAssignments_Recipients", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "entity_id"))
    private List<OrganizationalEntity> recipients             = Collections.emptyList();

    public PeopleAssignments() {

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        if ( taskInitiator != null ) {
            out.writeBoolean( true );
            taskInitiator.writeExternal( out );
        } else {
            out.writeBoolean( false );
        }
        CollectionUtils.writeOrganizationalEntityList( potentialOwners,
                                                       out );
        CollectionUtils.writeOrganizationalEntityList( excludedOwners,
                                                       out );
        CollectionUtils.writeOrganizationalEntityList( taskStakeholders,
                                                       out );
        CollectionUtils.writeOrganizationalEntityList( businessAdministrators,
                                                       out );
        CollectionUtils.writeOrganizationalEntityList( recipients,
                                                       out );
    }

    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        if ( in.readBoolean() ) {
            taskInitiator = new User();
            taskInitiator.readExternal( in );
        }
        potentialOwners = CollectionUtils.readOrganizationalEntityList( in );
        excludedOwners = CollectionUtils.readOrganizationalEntityList( in );
        taskStakeholders = CollectionUtils.readOrganizationalEntityList( in );
        businessAdministrators = CollectionUtils.readOrganizationalEntityList( in );
        recipients = CollectionUtils.readOrganizationalEntityList( in );
    }

    public User getTaskInitiator() {
        return taskInitiator;
    }

    public void setTaskInitiator(User taskInitiator) {
        this.taskInitiator = taskInitiator;
    }

    public List<OrganizationalEntity> getPotentialOwners() {
        return potentialOwners;
    }

    public void setPotentialOwners(List<OrganizationalEntity> potentialOwners) {
        this.potentialOwners = potentialOwners;
    }

    public List<OrganizationalEntity> getExcludedOwners() {
        return excludedOwners;
    }

    public void setExcludedOwners(List<OrganizationalEntity> excludedOwners) {
        this.excludedOwners = excludedOwners;
    }

    public List<OrganizationalEntity> getTaskStakeholders() {
        return taskStakeholders;
    }

    public void setTaskStakeholders(List<OrganizationalEntity> taskStakeholders) {
        this.taskStakeholders = taskStakeholders;
    }

    public List<OrganizationalEntity> getBusinessAdministrators() {
        return businessAdministrators;
    }

    public void setBusinessAdministrators(List<OrganizationalEntity> businessAdministrators) {
        this.businessAdministrators = businessAdministrators;
    }

    public List<OrganizationalEntity> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<OrganizationalEntity> recipients) {
        this.recipients = recipients;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + CollectionUtils.hashCode( businessAdministrators );
        result = prime * result + CollectionUtils.hashCode( excludedOwners );
        result = prime * result + CollectionUtils.hashCode( potentialOwners );
        result = prime * result + CollectionUtils.hashCode( recipients );
        result = prime * result + ((taskInitiator == null) ? 0 : taskInitiator.hashCode());
        result = prime * result + CollectionUtils.hashCode( taskStakeholders );
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( !(obj instanceof PeopleAssignments) ) return false;
        PeopleAssignments other = (PeopleAssignments) obj;

        if ( taskInitiator == null ) {
            if ( other.taskInitiator != null ) return false;
        } else if ( !taskInitiator.equals( other.taskInitiator ) ) return false;

        return CollectionUtils.equals( businessAdministrators,
                                       other.businessAdministrators ) && CollectionUtils.equals( excludedOwners,
                                                                                                 other.excludedOwners ) && CollectionUtils.equals( potentialOwners,
                                                                                                                                                   other.potentialOwners ) && CollectionUtils.equals( recipients,
                                                                                                                                                                                                      other.recipients )
               && CollectionUtils.equals( taskStakeholders,
                                          other.taskStakeholders );
    }

}
