
    create table attachment (
        version integer,
        created datetime(6),
        decision_id bigint,
        errand_id bigint,
        id bigint not null auto_increment,
        updated datetime(6),
        note varchar(1000),
        category varchar(255),
        extension varchar(255),
        mime_type varchar(255),
        municipality_id varchar(255),
        name varchar(255),
        namespace varchar(255),
        file longtext,
        primary key (id)
    ) engine=InnoDB;

    create table attachment_extra_parameters (
        attachment_id bigint not null,
        extra_parameter_value varchar(8192),
        extra_parameter_key varchar(255) not null,
        primary key (attachment_id, extra_parameter_key)
    ) engine=InnoDB;

    create table decision (
        version integer,
        created datetime(6),
        decided_at datetime(6),
        decided_by_id bigint,
        errand_id bigint,
        id bigint not null auto_increment,
        updated datetime(6),
        valid_from datetime(6),
        valid_to datetime(6),
        municipality_id varchar(255),
        namespace varchar(255),
        description text,
        decision_outcome enum ('APPROVAL','CANCELLATION','DISMISSAL','REJECTION'),
        decision_type enum ('FINAL','PROPOSED','RECOMMENDED'),
        primary key (id)
    ) engine=InnoDB;

    create table decision_extra_parameters (
        decision_id bigint not null,
        extra_parameter_value varchar(8192),
        extra_parameter_key varchar(255) not null,
        primary key (decision_id, extra_parameter_key)
    ) engine=InnoDB;

    create table decision_laws (
        law_order integer not null,
        decision_id bigint not null,
        article varchar(255),
        chapter varchar(255),
        heading varchar(255),
        sfs varchar(255),
        primary key (law_order, decision_id)
    ) engine=InnoDB;

    create table email_header (
        id bigint not null auto_increment,
        message_id varchar(255),
        header enum ('IN_REPLY_TO','MESSAGE_ID','REFERENCES'),
        primary key (id)
    ) engine=InnoDB;

    create table email_header_values (
        value_index integer not null,
        email_header_id bigint not null,
        value varchar(255),
        primary key (value_index, email_header_id)
    ) engine=InnoDB;

    create table errand (
        end_date date,
        start_date date,
        version integer,
        application_received datetime(6),
        created datetime(6),
        id bigint not null auto_increment,
        status_created datetime(6),
        suspended_from datetime(6),
        suspended_to datetime(6),
        updated datetime(6),
        created_by varchar(36),
        updated_by varchar(36),
        case_title_addition varchar(255),
        case_type varchar(255),
        created_by_client varchar(255),
        diary_number varchar(255),
        errand_number varchar(255) not null,
        external_case_id varchar(255),
        municipality_id varchar(255),
        namespace varchar(255),
        phase varchar(255),
        process_id varchar(255),
        status varchar(255),
        status_description varchar(255),
        updated_by_client varchar(255),
        channel enum ('EMAIL','ESERVICE','MOBILE','SYSTEM','WEB_UI'),
        description longtext,
        priority enum ('HIGH','LOW','MEDIUM'),
        primary key (id)
    ) engine=InnoDB;

    create table errand_extra_parameter_values (
        extra_parameter_id varchar(255) not null,
        value varchar(255)
    ) engine=InnoDB;

    create table errand_extra_parameters (
        errand_id bigint not null,
        display_name varchar(255),
        id varchar(255) not null,
        parameters_key varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table errand_labels (
        value_order integer not null,
        errand_id bigint not null,
        value varchar(255),
        primary key (value_order, errand_id)
    ) engine=InnoDB;

    create table errand_statuses (
        status_order integer not null,
        date_time datetime(6),
        errand_id bigint not null,
        description varchar(255),
        status_type varchar(255),
        primary key (status_order, errand_id)
    ) engine=InnoDB;

    create table facility (
        is_zoning_plan_area bit,
        latitude float(53),
        longitude float(53),
        main_facility bit,
        version integer,
        created datetime(6),
        errand_id bigint,
        id bigint not null auto_increment,
        updated datetime(6),
        apartment_number varchar(255),
        attention varchar(255),
        care_of varchar(255),
        city varchar(255),
        country varchar(255),
        description varchar(255),
        facility_collection_name varchar(255),
        facility_type varchar(255),
        house_number varchar(255),
        invoice_marking varchar(255),
        municipality_id varchar(255),
        namespace varchar(255),
        postal_code varchar(255),
        property_designation varchar(255),
        street varchar(255),
        address_category enum ('INVOICE_ADDRESS','POSTAL_ADDRESS','VISITING_ADDRESS'),
        primary key (id)
    ) engine=InnoDB;

    create table facility_extra_parameters (
        facility_id bigint not null,
        extra_parameter_value varchar(8192),
        extra_parameter_key varchar(255) not null,
        primary key (facility_id, extra_parameter_key)
    ) engine=InnoDB;

    create table message (
        viewed bit not null,
        errand_id bigint,
        email varchar(255),
        external_caseid varchar(255),
        familyid varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        message_type varchar(255),
        messageid varchar(255) not null,
        mobile_number varchar(255),
        municipality_id varchar(255),
        namespace varchar(255),
        sent varchar(255),
        subject varchar(255),
        userid varchar(255),
        username varchar(255),
        classification enum ('COMPLETION_REQUEST','INFORMATION','INTERNAL_COMMUNICATION','OBTAIN_OPINION','OTHER'),
        direction enum ('INBOUND','OUTBOUND'),
        message longtext,
        primary key (messageid)
    ) engine=InnoDB;

    create table message_attachment (
        message_attachment_data_id integer not null,
        attachmentid varchar(255) not null,
        content_type varchar(255),
        messageid varchar(255),
        municipality_id varchar(255),
        name varchar(255),
        namespace varchar(255),
        primary key (attachmentid)
    ) engine=InnoDB;

    create table message_attachment_data (
        id integer not null auto_increment,
        file longblob,
        primary key (id)
    ) engine=InnoDB;

    create table message_recipients (
        message_id varchar(255) not null,
        recipient_email varchar(255)
    ) engine=InnoDB;

    create table note (
        version integer,
        created datetime(6),
        errand_id bigint,
        id bigint not null auto_increment,
        updated datetime(6),
        created_by varchar(36),
        updated_by varchar(36),
        text varchar(10000),
        municipality_id varchar(255),
        namespace varchar(255),
        title varchar(255),
        note_type enum ('INTERNAL','PUBLIC'),
        primary key (id)
    ) engine=InnoDB;

    create table note_extra_parameters (
        note_id bigint not null,
        extra_parameter_value varchar(8192),
        extra_parameter_key varchar(255) not null,
        primary key (note_id, extra_parameter_key)
    ) engine=InnoDB;

    create table notification (
        acknowledged bit,
        global_acknowledged bit,
        created datetime(6),
        errand_id bigint,
        expires datetime(6),
        modified datetime(6),
        content varchar(255),
        created_by varchar(255),
        created_by_full_name varchar(255),
        description varchar(255),
        id varchar(255) not null,
        municipality_id varchar(255) not null,
        namespace varchar(255) not null,
        owner_full_name varchar(255),
        owner_id varchar(255),
        type varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table related_errand (
        errand_id bigint,
        related_errand_id bigint,
        id varchar(255) not null,
        related_errand_number varchar(255),
        relation_reason varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table stakeholder (
        version integer,
        created datetime(6),
        errand_id bigint,
        id bigint not null auto_increment,
        updated datetime(6),
        ad_account varchar(255),
        authorized_signatory varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        municipality_id varchar(255),
        namespace varchar(255),
        organization_name varchar(255),
        organization_number varchar(255),
        person_id varchar(255),
        type enum ('ORGANIZATION','PERSON'),
        primary key (id)
    ) engine=InnoDB;

    create table stakeholder_addresses (
        address_order integer not null,
        is_zoning_plan_area bit,
        latitude float(53),
        longitude float(53),
        stakeholder_id bigint not null,
        apartment_number varchar(255),
        attention varchar(255),
        care_of varchar(255),
        city varchar(255),
        country varchar(255),
        house_number varchar(255),
        invoice_marking varchar(255),
        postal_code varchar(255),
        property_designation varchar(255),
        street varchar(255),
        address_category enum ('INVOICE_ADDRESS','POSTAL_ADDRESS','VISITING_ADDRESS'),
        primary key (address_order, stakeholder_id)
    ) engine=InnoDB;

    create table stakeholder_contact_information (
        contact_information_order integer not null,
        stakeholder_id bigint not null,
        value varchar(255),
        contact_type enum ('CELLPHONE','EMAIL','PHONE'),
        primary key (contact_information_order, stakeholder_id)
    ) engine=InnoDB;

    create table stakeholder_extra_parameters (
        stakeholder_id bigint not null,
        extra_parameter_value varchar(8192),
        extra_parameter_key varchar(255) not null,
        primary key (stakeholder_id, extra_parameter_key)
    ) engine=InnoDB;

    create table stakeholder_roles (
        role_order integer not null,
        stakeholder_id bigint not null,
        roles varchar(255),
        primary key (role_order, stakeholder_id)
    ) engine=InnoDB;

    create index idx_attachment_errand_id 
       on attachment (errand_id);

    create index idx_attachment_municipality_id 
       on attachment (municipality_id);

    create index idx_attachment_namespace 
       on attachment (namespace);

    create index idx_decision_municipality_id 
       on decision (municipality_id);

    create index idx_decision_namespace 
       on decision (namespace);

    alter table if exists decision 
       add constraint UKj00sxiyx1fhmcdofxuugumdon unique (decided_by_id);

    create index idx_errand_municipality_id 
       on errand (municipality_id);

    create index idx_errand_namespace 
       on errand (namespace);

    alter table if exists errand 
       add constraint UK_errand_errand_number unique (errand_number);

    create index idx_facility_municipality_id 
       on facility (municipality_id);

    create index idx_facility_namespace 
       on facility (namespace);

    create index idx_message_municipality_id 
       on message (municipality_id);

    create index idx_message_namespace 
       on message (namespace);

    create index idx_messsage_errand_id 
       on message (errand_id);

    create index idx_message_attachment_municipality_id 
       on message_attachment (municipality_id);

    create index idx_message_attachment_namespace 
       on message_attachment (namespace);

    alter table if exists message_attachment 
       add constraint UK_message_attachment_data_id unique (message_attachment_data_id);

    create index idx_note_municipality_id 
       on note (municipality_id);

    create index idx_note_namespace 
       on note (namespace);

    create index idx_notification_municipality_id 
       on notification (municipality_id);

    create index idx_notification_namespace 
       on notification (namespace);

    create index idx_notification_owner_id 
       on notification (owner_id);

    create index idx_stakeholder_municipality_id 
       on stakeholder (municipality_id);

    create index idx_stakeholder_namespace 
       on stakeholder (namespace);

    alter table if exists attachment 
       add constraint FK_decision_id 
       foreign key (decision_id) 
       references decision (id);

    alter table if exists attachment_extra_parameters 
       add constraint FK_attachment_extra_parameters_attachment_id 
       foreign key (attachment_id) 
       references attachment (id);

    alter table if exists decision 
       add constraint FK_decision_decided_by_id 
       foreign key (decided_by_id) 
       references stakeholder (id);

    alter table if exists decision 
       add constraint FK_decision_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table if exists decision_extra_parameters 
       add constraint FK_decision_extra_parameters_decision_id 
       foreign key (decision_id) 
       references decision (id);

    alter table if exists decision_laws 
       add constraint FK_decision_laws_decision_id 
       foreign key (decision_id) 
       references decision (id);

    alter table if exists email_header 
       add constraint fk_message_header_message_id 
       foreign key (message_id) 
       references message (messageid);

    alter table if exists email_header_values 
       add constraint fk_email_header_values_email_header_id 
       foreign key (email_header_id) 
       references email_header (id);

    alter table if exists errand_extra_parameter_values 
       add constraint fk_errand_extra_parameter_values_parameter_id 
       foreign key (extra_parameter_id) 
       references errand_extra_parameters (id);

    alter table if exists errand_extra_parameters 
       add constraint fk_extra_parameter_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table if exists errand_labels 
       add constraint FK_errand_labels_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table if exists errand_statuses 
       add constraint FK_errand_statuses_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table if exists facility 
       add constraint FK_facility_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table if exists facility_extra_parameters 
       add constraint FK_facility_extra_parameters_facility_id 
       foreign key (facility_id) 
       references facility (id);

    alter table if exists message_attachment 
       add constraint fk_message_attachment_data_message_attachment 
       foreign key (message_attachment_data_id) 
       references message_attachment_data (id);

    alter table if exists message_recipients 
       add constraint fk_message_recipients_message_id 
       foreign key (message_id) 
       references message (messageid);

    alter table if exists note 
       add constraint FK_note_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table if exists note_extra_parameters 
       add constraint FK_note_extra_parameters_note_id 
       foreign key (note_id) 
       references note (id);

    alter table if exists notification 
       add constraint fk_notification_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table if exists related_errand 
       add constraint FK_errand_related_errands_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table if exists stakeholder 
       add constraint FK_stakeholder_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table if exists stakeholder_addresses 
       add constraint FK_stakeholder_addresses_stakeholder_id 
       foreign key (stakeholder_id) 
       references stakeholder (id);

    alter table if exists stakeholder_contact_information 
       add constraint FK_stakeholder_contact_information_stakeholder_id 
       foreign key (stakeholder_id) 
       references stakeholder (id);

    alter table if exists stakeholder_extra_parameters 
       add constraint FK_stakeholder_extra_parameters_stakeholder_id 
       foreign key (stakeholder_id) 
       references stakeholder (id);

    alter table if exists stakeholder_roles 
       add constraint FK_stakeholder_roles_stakeholder_id 
       foreign key (stakeholder_id) 
       references stakeholder (id);
