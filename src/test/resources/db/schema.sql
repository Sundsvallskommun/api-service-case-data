
    create table appeal (
        version integer,
        appealed_by_id bigint,
        created datetime(6),
        id bigint not null auto_increment,
        judicial_authorisation_id bigint,
        updated datetime(6),
        primary key (id)
    ) engine=InnoDB;

    create table appeal_extra_parameters (
        appeal_id bigint not null,
        extra_parameter_key varchar(255) not null,
        extra_parameter_value varchar(255),
        primary key (appeal_id, extra_parameter_key)
    ) engine=InnoDB;

    create table attachment (
        version integer,
        appeal_id bigint,
        created datetime(6),
        decision_id bigint,
        id bigint not null auto_increment,
        updated datetime(6),
        note varchar(1000),
        errand_number varchar(255),
        extension varchar(255),
        mime_type varchar(255),
        name varchar(255),
        category enum ('ARIT','FAS','FS2','FAP','FAPL','FPSS','FS','FASSIT','FAS2','FASSIT2','FOTOMON','FÄRG','MAST','MUR','MÅTT','PERSPEKTIV','PLA','PLFA','PLFASE','PLFASESI','PLFASI','PLFA2','PFSI2','PLSE2','PSS2','PLSE','PSS','PLASIT','PFS2','PFSS2','TEVS','UPLA','PLAN','PLANK','PLA2','PSI2','REL','REVRIT','RITNING','TJ','RIT','SEK','SEKSIT','SEKT','SEK2','SESI2','SKYL','UPPM','ANV','ANM','ANMÄ','ANS','ANSFÖ','ANSM','ANSR','ANSS','BEGLST','BERBSA','BLST','OMPLÄ','BULL','DEB','DEL','DELK','DELSLU','DELSTA','DOM','ENER','ENEDEK','FAST','FÖLJREVRIT','FÖRG2','GODFÄ','GRAM','INFOSS','INTSLUT','KM','MOTBKR','OVK','PM','PMINN','PROARB','PROTAU','PROTKS','PROTPLU','PROTSBN','PROSS','PROTS','REMISS','REMS','RÄTT','SKP','SBES','STAB','SVAR','SVAR2år','TJÄ','UNDER','ÄRB','ÖVER','ADRESS','ANSUPA','ANNO','ANSF','ANSSL','ANSUP','ANKVU','ARBI','BEHA','AVPLAN','BANK','BEGSTART','BEK','BEKMOTANS','BEMÖ','BESKA','BESLUT','BIL','BRS','BRAB','BRAD','BROS','DPH','DETALJ','DHBHUR','ELD','EPOS','EXRIT','FAKTU','FAKTUS','FOTO','FUM','FSF','FÖLJ','FÖRB','FÖRK','FÖRR','FÖRGARBO','URÖR','GRA','GRAN','GBLAD','HISSINT','HUR','ARK','INTFAK','INTY','KLA','KOMP','KONT','KPLAN','RAPP','KVAL','LUFT','LUTE','MAIL','MAPL','MATINV','MEDDEL','MIRP','MINN','POIT','PRESENTA','PRES','KPV','PROT','PÅMINNTB','RAP','REMUA','RUE','HBB','RIVA','RIVP','SAK','SAKUT','KPR','KP','KR','SIN','SKR','KA','SKY','SLUT','SCB','STIM','SÅF','TEBY','TEKN','TEKRAP','TILL','TILLVR','SBN','SAKNAS','UND','UKP','UKR','UNDUT','UBGARBO','UTBEU','UTSK','UTSKP','UTSKS','BRAU','UKA','ÅTG','GEO','GRUNDP','GRUNDR','KOND','UKON','KONR','STOMR','TAPL','TSR','KART','NYKA','SITU','TOMTPLBE','VAH','VENT','UVEN','VS','VVSH','ANMALAN_LIVSMEDELSANLAGGNING','ANMALAN_ENSKILT_AVLOPP','ANSOKAN_ENSKILT_AVLOPP','ANMALAN_ANDRING_AVLOPPSANLAGGNING','ANMALAN_ANDRING_AVLOPPSANORDNING','ANMALAN_VARMEPUMP','ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW','ANMALAN_HALSOSKYDDSVERKSAMHET','SITUATIONSPLAN','SKRIVELSE','MEDICAL_CONFIRMATION','POLICE_REPORT','PASSPORT_PHOTO','SIGNATURE','POWER_OF_ATTORNEY','OTHER','ERRAND_SCANNED_APPLICATION','SERVICE_RECEIPT','OTHER_ATTACHMENT'),
        file longtext,
        primary key (id)
    ) engine=InnoDB;

    create table attachment_extra_parameters (
        attachment_id bigint not null,
        extra_parameter_key varchar(255) not null,
        extra_parameter_value varchar(255),
        primary key (attachment_id, extra_parameter_key)
    ) engine=InnoDB;

    create table decision (
        version integer,
        appeal_id bigint,
        created datetime(6),
        decided_at datetime(6),
        decided_by_id bigint,
        errand_id bigint,
        id bigint not null auto_increment,
        updated datetime(6),
        valid_from datetime(6),
        valid_to datetime(6),
        description text,
        decision_outcome enum ('APPROVAL','REJECTION','DISMISSAL','CANCELLATION'),
        decision_type enum ('RECOMMENDED','PROPOSED','FINAL'),
        primary key (id)
    ) engine=InnoDB;

    create table decision_extra_parameters (
        decision_id bigint not null,
        extra_parameter_key varchar(255) not null,
        extra_parameter_value varchar(255),
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
        header enum ('IN_REPLY_TO','REFERENCES','MESSAGE_ID'),
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
        updated datetime(6),
        created_by varchar(36),
        updated_by varchar(36),
        case_title_addition varchar(255),
        case_type varchar(255),
        created_by_client varchar(255),
        description varchar(255),
        diary_number varchar(255),
        errand_number varchar(255) not null,
        external_case_id varchar(255),
        municipality_id varchar(255),
        phase varchar(255),
        process_id varchar(255),
        updated_by_client varchar(255),
        channel enum ('ESERVICE','EMAIL','WEB_UI','MOBILE','SYSTEM'),
        priority enum ('HIGH','MEDIUM','LOW'),
        primary key (id)
    ) engine=InnoDB;

    create table errand_extra_parameters (
        errand_id bigint not null,
        extra_parameter_key varchar(255) not null,
        extra_parameter_value varchar(255),
        primary key (errand_id, extra_parameter_key)
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
        house_number varchar(255),
        invoice_marking varchar(255),
        postal_code varchar(255),
        property_designation varchar(255),
        street varchar(255),
        address_category enum ('POSTAL_ADDRESS','INVOICE_ADDRESS','VISITING_ADDRESS'),
        facility_type enum ('ONE_FAMILY_HOUSE','APARTMENT_BLOCK','WEEKEND_COTTAGE','OFFICE_BUILDING','INDUSTRIAL_BUILDING','GARAGE','CARPORT','STOREHOUSE','GREENHOUSE','GUEST_HOUSE','WAREHOUSE','WORKSHOP_BUILDING','RESTAURANT','SCHOOL','PRESCHOOL','PARKING','DEPOT','MARINA','WALL','PALING','RECYCLING_STATION','OTHER','FURNISHING_OF_ADDITIONAL_DWELLING','ANCILLARY_BUILDING','ANCILLARY_HOUSING_BUILDING','DORMER','EXTENSION'),
        primary key (id)
    ) engine=InnoDB;

    create table facility_extra_parameters (
        facility_id bigint not null,
        extra_parameter_key varchar(255) not null,
        extra_parameter_value varchar(255),
        primary key (facility_id, extra_parameter_key)
    ) engine=InnoDB;

    create table message (
        viewed bit not null,
        email varchar(255),
        errand_number varchar(255),
        external_caseid varchar(255),
        familyid varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        messageid varchar(255) not null,
        mobile_number varchar(255),
        sent varchar(255),
        subject varchar(255),
        userid varchar(255),
        username varchar(255),
        classification enum ('INFORMATION','COMPLETION_REQUEST','OBTAIN_OPINION','INTERNAL_COMMUNICATION','OTHER'),
        direction enum ('INBOUND','OUTBOUND'),
        message longtext,
        message_type enum ('SMS','EMAIL'),
        primary key (messageid)
    ) engine=InnoDB;

    create table message_attachment (
        message_attachment_data_id integer not null,
        attachmentid varchar(255) not null,
        content_type varchar(255),
        messageid varchar(255),
        name varchar(255),
        primary key (attachmentid)
    ) engine=InnoDB;

    create table message_attachment_data (
        id integer not null auto_increment,
        file longblob,
        primary key (id)
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
        title varchar(255),
        note_type enum ('INTERNAL','PUBLIC'),
        primary key (id)
    ) engine=InnoDB;

    create table note_extra_parameters (
        note_id bigint not null,
        extra_parameter_key varchar(255) not null,
        extra_parameter_value varchar(255),
        primary key (note_id, extra_parameter_key)
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
        organization_name varchar(255),
        organization_number varchar(255),
        person_id varchar(255),
        type enum ('PERSON','ORGANIZATION'),
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
        address_category enum ('POSTAL_ADDRESS','INVOICE_ADDRESS','VISITING_ADDRESS'),
        primary key (address_order, stakeholder_id)
    ) engine=InnoDB;

    create table stakeholder_contact_information (
        contact_information_order integer not null,
        stakeholder_id bigint not null,
        value varchar(255),
        contact_type enum ('CELLPHONE','PHONE','EMAIL'),
        primary key (contact_information_order, stakeholder_id)
    ) engine=InnoDB;

    create table stakeholder_extra_parameters (
        stakeholder_id bigint not null,
        extra_parameter_key varchar(255) not null,
        extra_parameter_value varchar(255),
        primary key (stakeholder_id, extra_parameter_key)
    ) engine=InnoDB;

    create table stakeholder_roles (
        role_order integer not null,
        stakeholder_id bigint not null,
        roles enum ('CONTROL_OFFICIAL','APPLICANT','PROPERTY_OWNER','PAYMENT_PERSON','INVOICE_RECIPENT','OPERATOR','CONTACT_PERSON','ADMINISTRATOR','FELLOW_APPLICANT','DRIVER','PASSENGER','DOCTOR'),
        primary key (role_order, stakeholder_id)
    ) engine=InnoDB;

    alter table if exists appeal 
       add constraint UK_j1f39p5lklbquuaex6i6alj0w unique (appealed_by_id);

    alter table if exists appeal 
       add constraint UK_6582vskv87a7hta3lhlp6m4tk unique (judicial_authorisation_id);

    create index attachment_errand_number_idx 
       on attachment (errand_number);

    alter table if exists decision 
       add constraint UK_q14peo9ygf2x8pm8lh99h9l0o unique (appeal_id);

    alter table if exists decision 
       add constraint UK_j00sxiyx1fhmcdofxuugumdon unique (decided_by_id);

    alter table if exists errand 
       add constraint UK_errand_errand_number unique (errand_number);

    alter table if exists message_attachment 
       add constraint UK_message_attachment_data_id unique (message_attachment_data_id);

    alter table if exists appeal 
       add constraint FK_appeal_appealed_by_id 
       foreign key (appealed_by_id) 
       references stakeholder (id);

    alter table if exists appeal 
       add constraint FK_appeal_judicial_authorisation_id 
       foreign key (judicial_authorisation_id) 
       references stakeholder (id);

    alter table if exists appeal_extra_parameters 
       add constraint FK_appeal_extra_parameters_appeal_id 
       foreign key (appeal_id) 
       references appeal (id);

    alter table if exists attachment 
       add constraint FK_decision_id 
       foreign key (decision_id) 
       references decision (id);

    alter table if exists attachment 
       add constraint FK_appeal_id 
       foreign key (appeal_id) 
       references appeal (id);

    alter table if exists attachment_extra_parameters 
       add constraint FK_attachment_extra_parameters_attachment_id 
       foreign key (attachment_id) 
       references attachment (id);

    alter table if exists decision 
       add constraint FK_decision_appeal_id 
       foreign key (appeal_id) 
       references appeal (id);

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

    alter table if exists errand_extra_parameters 
       add constraint FK_errand_extra_parameters_errand_id 
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

    alter table if exists note 
       add constraint FK_note_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table if exists note_extra_parameters 
       add constraint FK_note_extra_parameters_note_id 
       foreign key (note_id) 
       references note (id);

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
