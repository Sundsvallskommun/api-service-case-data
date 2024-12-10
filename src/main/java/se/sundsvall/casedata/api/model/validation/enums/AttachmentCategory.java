package se.sundsvall.casedata.api.model.validation.enums;

import lombok.Getter;

@Getter
public enum AttachmentCategory {

	PLAN_DRAWING("ARIT"), // A-ritningar (A-ritningar)
	FACADE("FAS"), // Fasad
	FACADE_SECTION_DRAWING("FS2"), // Fasad- och sektionsritning
	FACADE_PLAN("FAP"), // Fasad Plan
	FACADE_PLAN_SECTION("FAPL"), // Fasad Plan Sektion
	FACADE_PLAN_SECTION_SITE_PLAN("FPSS"), // Fasad Plan Sektion Situationsplan
	FACADE_SECTION("FS"), // Fasad sektion
	FACADE_SITUATION("FASSIT"), // Fasad Situation
	FACADE_DRAWING("FAS2"), // Fasadritning
	FACADE_DRAWING_SITE_PLAN("FASSIT2"), // Fasadritning + situationsplan
	PHOTO_MONTAGE("FOTOMON"), // Fotomontage
	COLOR_PROPOSAL("FÄRG"), // Färgsättningsförslag
	MAST_DRAWING("MAST"), // Mastritning
	WALL_DRAWING("MUR"), // Mur-ritning
	DIMENSION_DRAWING("MÅTT"), // Måttritning
	PERSPECTIVE_DRAWING("PERSPEKTIV"), // Perspektivritning
	PLAN("PLA"), // Plan
	PLAN_FACADE("PLFA"), // Plan Fasad
	PLAN_FACADE_SECTION("PLFASE"), // Plan Fasad Sektion
	PLAN_FACADE_SECTION_SITE_PLAN("PLFASESI"), // Plan Fasad Sektion Situationsplan
	PLAN_FACADE_SITE_PLAN("PLFASI"), // Plan Fasad Situationsplan
	PLAN_FACADE_DRAWING("PLFA2"), // Plan- och fasadritning
	PLAN_FACADE_DRAWING_SITE_PLAN("PFSI2"), // Plan- och fasadritning + situationsplan
	PLAN_SECTION_DRAWING("PLSE2"), // Plan- och sektionsritning
	PLAN_SECTION_DRAWING_SITE_PLAN("PSS2"), // Plan- och sektionsritning + situationsplan.
	PLAN_SECTION("PLSE"), // Plan Sektion
	PLAN_SECTION_SITE_PLAN("PSS"), // Plan Sektion Situation
	PLAN_SITUATION("PLASIT"), // Plan Situation
	PLAN_FACADE_SECTION_DRAWING("PFS2"), // Plan, fasad- och sektionsritning
	PLAN_FACADE_SECTION_SITE_PLAN_2("PFSS2"), // Plan, fasad, sektion, situation
	DESCRIPTION("TEVS"), // Planbeskrivning
	U_PLANS("UPLA"), // Planer
	PLANS("PLAN"), // Planer
	PLANK_DRAWING("PLANK"), // Plankritning
	PLAN_DRAWING_2("PLA2"), // Planritning
	PLAN_DRAWING_SITE_PLAN("PSI2"), // Planritning + situationsplan
	RELATION_DRAWING("REL"), // Relationsritning
	REVISED_DRAWING("REVRIT"), // Reviderade ritning
	DRAWING("RITNING"), // Ritning
	DRAWINGS("TJ"), // Ritningar
	DRAWINGS_2("RIT"), // Ritningar
	SECTION("SEK"), // Sektion
	SECTION_SITUATION("SEKSIT"), // Sektion Situation
	SECTIONS("SEKT"), // Sektioner
	SECTION_DRAWING("SEK2"), // Sektionsritning
	SECTION_DRAWING_SITE_PLAN("SESI2"), // Sektionsritning + situationsplan
	SIGNBOARD_DRAWING("SKYL"), // Skyltritning
	SURVEY_DRAWING("UPPM"), // Uppmätningsritning
	EXHIBITION_DOCUMENT("ANV"), // Utställningshandling
	APPLICATION("ANM"), // Anmälan
	APPLICATION_CONTROL_OFFICER("ANMÄ"), // Anmälan av kontrollansvarig
	BUILDING_PERMIT_APPLICATION("ANS"), // Ansökan om bygglov
	APPLICATION_PRELIMINARY_INQUIRY("ANSFÖ"), // Ansökan om förhandsbesked
	LAND_USE_APPLICATION("ANSM"), // Ansökan om marklov
	DEMOLITION_PERMIT_APPLICATION("ANSR"), // Ansökan om rivningslov
	SHORELINE_DISPENSATION_APPLICATION("ANSS"), // Ansökan om strandskyddsdispens
	REQUEST_FROM_COUNTY_ADMINISTRATION("BEGLST"), // Begäran från länsstyrelsen
	CALCULATION_BUILDING_SANCTION_FEE("BERBSA"), // Beräkning byggsanktionsavgift
	DECISION_FROM_COUNTY_ADMINISTRATION("BLST"), // Beslut från Länsstyrelsen
	REVIEW_DECISION_FROM_COUNTY_ADMINISTRATION("OMPLÄ"), // Beslut omprövning Länsstyrelsen
	NOISE_SURVEY("BULL"), // Bullerutredning
	BILLING_STATEMENT("DEB"), // Debiteringsblad
	NOTIFICATION("DEL"), // Delgivning
	DELIVERY_RECEIPT("DELK"), // Delgivningskvitto
	PARTIAL_FINAL_DECISION("DELSLU"), // Delslutbesked
	START_DECISION("DELSTA"), // Delstartsbesked
	JUDGMENT("DOM"), // Dom
	ENERGY_BALANCE_CALCULATION("ENER"), // Energibalansberäkning
	ENERGY_DECLARATION("ENEDEK"), // Energideklaration
	APPROVED_CONTROL_PLAN("FAST"), // Fastställd kontrollplan
	COVER_LETTER_REVISED_DRAWING("FÖLJREVRIT"), // Följebrev reviderad ritning
	PRELIMINARY_REVIEW_FORM("FÖRG2"), // Förhandsgranskningsblad
	APPROVAL_FROM_PROPERTY_OWNER("GODFÄ"), // Godkännande från fastighetsägare
	NEIGHBOR_APPROVAL("GRAM"), // Grannmedgivande
	INFO_BEFORE_START_FINAL_DECISION("INFOSS"), // Info inför start- och slutbesked
	INTERIM_FINAL_DECISION("INTSLUT"), // Interimistiskt slutbesked
	CONTROL_NOTICE("KM"), // Kontrollmeddelande
	RECEPTION_CONFIRMATION("MOTBKR"), // Mottagningsbekräftelse
	VENTILATION_INSPECTION_PROTOCOL("OVK"), // OVK-protokoll
	PM("PM"), // PM
	REMINDER("PMINN"), // Påminnelse
	WORKPLACE_VISIT_PROTOCOL("PROARB"), // Protokoll arbetsplatsbesök
	AU_PROTOCOL("PROTAU"), // Protokoll AU
	KS_PROTOCOL("PROTKS"), // Protokoll KS
	PLU_PROTOCOL("PROTPLU"), // Protokoll PLU
	SBN_PROTOCOL("PROTSBN"), // Protokoll SBN
	FINAL_MEETING_PROTOCOL("PROSS"), // Protokoll slutsamråd
	TECHNICAL_MEETING_PROTOCOL("PROTS"), // Protokoll tekniskt samråd
	REFERRAL("REMISS"), // Remiss
	REFERRAL_REPLY("REMS"), // Remissvar
	TIMELINESS_TEST("RÄTT"), // Rättidsprövning
	SIGNED_CONTROL_PLAN("SKP"), // Signerad kontrollplan
	FINAL_MESSAGE("SBES"), // Slutbesked
	START_MESSAGE("STAB"), // Startbesked
	REPLY("SVAR"), // Svar
	REPLY_2_YEARS("SVAR2år"), // Svar 2-årsbrev
	OFFICIAL_MEMO("TJÄ"), // Tjänsteskrivelse
	RESPONSE("UNDER"), // Underrättelsesvar
	CASE_SHEET("ÄRB"), // Ärendeblad
	ADDRESS_SHEET("ADRESS"), // Adressblad
	NOTIFICATION_WITHOUT_PERSONAL_NUMBER("ANSUPA"), // Anmälan utan personnummer
	ADVERTISEMENT("ANNO"), // Annons
	PRELIMINARY_INQUIRY_APPLICATION("ANSF"), // Ansökan om förhandsbesked
	FINAL_DECISION_APPLICATION("ANSSL"), // Ansökan om slutbesked
	APPLICATION_WITHOUT_PERSONAL_NUMBER("ANSUP"), // Ansökan utan personnummer
	ARCHAEOLOGICAL_ASSESSMENT("ANKVU"), // Antikvariskt utlåtande
	EMPLOYEE_CERTIFICATE("ARBI"), // Arbetstagarintyg
	HANDLER_ASSIGNMENT_EMAIL("BEHA"), // Atomutskick handläggare tilldelad
	CLOSURE_PLAN("AVPLAN"), // Avvecklingsplan
	BANK_GUARANTEE("BANK"), // Bankgaranti
	START_REQUEST("BEGSTART"), // Begäran om startbesked
	CONFIRMATION("BEK"), // Bekräftelse
	RECEIVED_APPLICATION_CONFIRMATION("BEKMOTANS"), // Bekräftelse mottagen ansökan
	REBUTTAL("BEMÖ"), // Bemötande
	VISIT_REPORTS_KA("BESKA"), // Besöksrapporter KA
	DECISION("BESLUT"), // Beslut
	APPENDIX("BIL"), // Bilaga
	FIRE_SKETCH("BRS"), // Brandskiss
	FIRE_PROTECTION_DESCRIPTION("BRAB"), // Brandskyddsbeskrivning
	FIRE_PROTECTION_DOCUMENTATION("BRAD"), // Brandskyddsdokumentation
	BROCHURE("BROS"), // Broschyr
	DETAILED_PLAN_MAP("DPH"), // Detaljplanekarta/detaljplanehandling
	DETAIL_DRAWING("DETALJ"), // Detaljritning
	YOU_HAVE_BEEN_GRANTED_BUILDING_PERMIT("DHBHUR"), // Du har fått bygglov/ Hur man överklagar
	BURN_RIGHT("ELD"), // Elda rätt
	EMAIL("EPOS"), // Epost
	EXAMPLE_DRAWING("EXRIT"), // Exempelritning
	INVOICE_DOCUMENT("FAKTU"), // Fakturaunderlag
	SANCTION_FEE_INVOICE_DOCUMENT("FAKTUS"), // Fakturaunderlag sanktionsavgift
	PHOTO("FOTO"), // Foto
	POWER_OF_ATTORNEY_2("FUM"), // Fullmakt
	COMPLETION_INSURANCE("FSF"), // Färdigställandeförsäkring
	COVER_LETTER("FÖLJ"), // Följebrev
	PRELIMINARY_DECISION("FÖRB"), // Förhandsbesked
	PROPOSED_CONTROL_PLAN("FÖRK"), // Förslag till kontrollplan
	PROPOSED_DEMOLITION_PLAN("FÖRR"), // Förslag till rivningsplan
	GAR_BO_INSURANCE_LETTER("FÖRGARBO"), // Försäkringsbrev Gar-Bo
	IMPLEMENTATION_DESCRIPTION("URÖR"), // Genomförandebeskrivning
	NEIGHBOR_HEARING("GRA"), // Grannhörande
	REVIEW_SHEET("GRAN"), // Granskningsblad
	REVIEW_SHEET_2("GBLAD"), // Granskningsblad
	ELEVATOR_CERTIFICATE("HISSINT"), // Hissintyg
	ILLUSTRATION_PERSPECTIVE("ARK"), // Illustration/ perspektiv
	INTERNAL_INVOICE_DOCUMENT("INTFAK"), // Intern fakturaunderlag
	CERTIFICATE("INTY"), // Intyg
	CLASSIFICATION_PLAN("KLA"), // Klassningsplan
	COMPLEMENTARY_ORDER("KOMP"), // Kompletteringsföreläggande
	CONTROLLING_AUTHORITY("KONT"), // Kontrollansvarig
	PBL_CONTROL_PLAN("KPLAN"), // Kontrollplan PBL
	CONTROL_REPORT("RAPP"), // Kontrollrapport
	QUALITY_MANAGER("KVAL"), // Kvalitetsansvarig
	AIRFLOW_PROTOCOL("LUFT"), // Luftflödesprotokoll
	AIR_TIGHTNESS_TEST("LUTE"), // Lufttäthetstest
	MAIL("MAIL"), // Mail
	GROUND_PLANNING_DRAWING("MAPL"), // Markplaneringsritning
	MATERIAL_INVENTORY("MATINV"), // Materialinventering
	MESSAGES("MEDDEL"), // Meddelanden
	ENVIRONMENTAL_INVESTIGATION_DEMOLITION_PLAN("MIRP"), // Miljöinventering/ rivningsplan
	NOTES("MINN"), // Minnesanteckningar
	PO_IT("POIT"), // PoIT
	PRESENTATION("PRESENTA"), // Presentation
	PERFORMANCE_DECLARATION("PRES"), // Prestandadeklaration
	PROGRAM_COUNCIL_DOCUMENT("KPV"), // Programsamrådshandling
	PROTOCOL("PROT"), // Protokoll
	REMINDER_TIME_LIMITED("PÅMINNTB"), // Påminnelse tidsbegränsat lov
	REPORT("RAP"), // Rapport
	REFERRAL_WITHOUT_ADDRESS("REMUA"), // Remiss utan adress
	RESPONSE_WITHOUT_REMARKS("RUE"), // Remissvar utan erinran
	DRAWING_REGISTER("HBB"), // Ritningsförteckning
	DEMOLITION_NOTIFICATION("RIVA"), // Rivningsanmälan
	DEMOLITION_PLAN("RIVP"), // Rivningsplan
	EXPERT_CERTIFICATE("SAK"), // Sakkunnigintyg
	EXPERT_OPINION_FIRE("SAKUT"), // Sakkunnigutlåtande brand
	CONSULTATION_DOCUMENT("KPR"), // Samrådshandling
	CONSULTATION_REPORT_PART_1("KP"), // Samrådsredogörelse del 1
	CONSULTATION_REPORT_PART_2("KR"), // Samrådsredogörelse del 2
	SIGNED_CONTROL_PLAN_2("SIN"), // Signerad kontrollplan
	LETTER("SKR"), // Skrivelse
	LETTERS("KA"), // Skrivelser
	SHELTER_INQUIRY("SKY"), // Skyddsrumsförfrågan
	FINAL_PROOF("SLUT"), // Slutbevis
	STATISTICS_FORM_SCB("SCB"), // Statistikblankett SCB
	STIMULUS_GRANT("STIM"), // Stimulansbidrag
	RESPONSE_TO_REMEDIAL_ORDER("SÅF"), // Svar på åtgärdsföreläggande
	TECHNICAL_DESCRIPTION("TEBY"), // Teknisk beskrivning
	TECHNICAL_DESCRIPTION_BRF("TEKN"), // Teknisk beskrivning brf
	TECHNICAL_REPORT("TEKRAP"), // Teknisk rapport
	INSPECTION("TILL"), // Tillsynsbesiktning
	MANUFACTURING_DRAWING("TILLVR"), // Tillverkningsritning
	SERVICE_BOARD_LETTER("SBN"), // Tjänsteskrivelse till nämnden
	TYPE_MISSING_IN_CONVERSION("SAKNAS"), // Typen saknades vid konverteringen
	SITUATION_UNDERLAY("UND"), // Underlag situationsplan
	CONTROL_PLAN_UNDERLAY("UKP"), // Underlag till kontrollplan
	DEMOLITION_PLAN_UNDERLAY("UKR"), // Underlag till rivningsplan
	NOTIFICATION_2("UNDUT"), // Underrättelse
	GAR_BO_ORDER_CONFIRMATION("UBGARBO"), // Uppdragsbekräftelse
	PAYMENT_DOCUMENT("UTBEU"), // Utbetalningsunderlag
	DISTRIBUTION("UTSK"), // Utskick
	REMINDER_DISTRIBUTION("UTSKP"), // Påminnelseutskick
	REPLY_DISTRIBUTION("UTSKS"), // Svar utskick
	FIRE_CONTROL_EXECUTION("BRAU"), // Utförandekontroll brandskydd
	CONTROL_AUTHORITY_OPINION("UKA"), // Utlåtande KA
	REMEDIAL_ORDER("ÅTG"), // Åtgärdsföreläggande
	GEOTECHNICAL_DOCUMENT("GEO"),
	GROUND_PLAN("GRUNDP"), // Grundplan
	GROUND_DRAWING("GRUNDR"), // Grundritning
	CONSTRUCTION_DOCUMENT("KOND"), // Konstruktionsdokument
	CONSTRUCTION_HANDLING("UKON"), // Konstruktionshandling
	CONSTRUCTION_DRAWING("KONR"), // Konstruktionsritning
	FRAME_DRAWINGS("STOMR"), // Stomritningar
	ROOF_PLAN("TAPL"), // Takplan
	ROOF_TRUSS_DRAWING("TSR"), // Takstolsritning
	MAP("KART"), // Karta
	NEW_BUILDING_MAP("NYKA"), // Nybyggnadskarta
	SITE_PLAN("SITU"), // Situationsplan
	PLOT_DETERMINATION("TOMTPLBE"), // Tomtplatsbestämning
	WATER_AND_SEWER_HANDLING("VAH"), // VA-handling
	VENTILATION_HANDLING("VENT"), // Ventilationshandling
	VENTILATION_DRAWING("UVEN"), // Ventilationsritning
	PLUMBING_HANDLING("VS"), // VS-handling
	HVAC_HANDLING("VVSH"), // VVS-handling

	///////////////////////////////////
	// Ecos
	///////////////////////////////////
	ANMALAN_LIVSMEDELSANLAGGNING("ANMALAN_LIVSMEDELSANLAGGNING"),
	ANMALAN_ENSKILT_AVLOPP("ANMALAN_ENSKILT_AVLOPP"),
	ANSOKAN_ENSKILT_AVLOPP("ANSOKAN_ENSKILT_AVLOPP"),
	ANMALAN_ANDRING_AVLOPPSANLAGGNING("ANMALAN_ANDRING_AVLOPPSANLAGGNING"),
	ANMALAN_ANDRING_AVLOPPSANORDNING("ANMALAN_ANDRING_AVLOPPSANORDNING"),
	ANMALAN_VARMEPUMP("ANMALAN_VARMEPUMP"),
	ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW("ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW"),
	ANMALAN_HALSOSKYDDSVERKSAMHET("ANMALAN_HALSOSKYDDSVERKSAMHET"),
	SITUATIONSPLAN("SITUATIONSPLAN"),
	SKRIVELSE("SKRIVELSE"),

	///////////////////////////////////
	// PARKING PERMIT
	///////////////////////////////////
	MEDICAL_CONFIRMATION("MEDICAL_CONFIRMATION"),
	POLICE_REPORT("POLICE_REPORT"),
	PASSPORT_PHOTO("PASSPORT_PHOTO"),
	SIGNATURE("SIGNATURE"),
	POWER_OF_ATTORNEY("POWER_OF_ATTORNEY"),
	OTHER("OTHER"),

	///////////////////////////////////
	ERRAND_SCANNED_APPLICATION("ERRAND_SCANNED_APPLICATION"),
	SERVICE_RECEIPT("SERVICE_RECEIPT"),
	OTHER_ATTACHMENT("OTHER_ATTACHMENT"),

	///////////////////////////////////
	// MEX
	///////////////////////////////////
	LEASE_REQUEST("LEASE_REQUEST"),
	RECEIVED_MAP("RECEIVED_MAP"),
	RECEIVED_CONTRACT("RECEIVED_CONTRACT"),
	LAND_PURCHASE_REQUEST("LAND_PURCHASE_REQUEST"),
	INQUIRY_LAND_SALE("INQUIRY_LAND_SALE"),
	APPLICATION_SQUARE_PLACE("APPLICATION_SQUARE_PLACE"),
	CORPORATE_TAX_CARD("CORPORATE_TAX_CARD"),
	TERMINATION_OF_HUNTING_RIGHTS("TERMINATION_OF_HUNTING_RIGHTS"),
	REQUEST_TO_BUY_SMALL_HOUSE_PLOT("REQUEST_TO_BUY_SMALL_HOUSE_PLOT"),
	CONTRACT_DRAFT("CONTRACT_DRAFT"),

	OEP_APPLICATION("OEP_APPLICATION"), // Ansökan
	ROAD_ALLOWANCE_APPROVAL("ROAD_ALLOWANCE_APPROVAL"),// Godkännande för vägbidrag
	MEX_PROTOCOL("PROTOCOL"), // "Protokoll"
	PREVIOUS_AGREEMENT("PREVIOUS_AGREEMENT"); // Tidigare avtal

	private final String code;

	AttachmentCategory(final String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return code;
	}
}
